package com.jack.authserver.annotation;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.jack.utils.web.RRException;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 阿里云短信服务
 */
@Slf4j
public class AliSmsProvider implements SmsProvider {
    @Value("${ALIBABA_CLOUD_ACCESS_KEY_ID:}")
    private String accessKeyId;
    @Value("${ALIBABA_CLOUD_ACCESS_KEY_SECRET:}")
    private String accessKeySecret;

    /**
     * 在阿里云sms注册的短信签名
     */
    @Value("${jack.sms.login.sign-name:}")
    private String signName;

    /**
     * 在阿里云sms注册的短信模板
     */
    @Value("${jack.sms.login.template-code:}")
    private String templateCode;

    /**
     * 短信模板参数名称
     */
    @Value("${jack.sms.login.template-param:code}")
    private String templateParam;

    @Override
    public void sendLoginCode(String phone, String code) {
        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build());
        try(AsyncClient client = AsyncClient.builder()
                .region("cn-hangzhou") // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                )
                .build()) {
            // Parameter settings for API request
            SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                    .phoneNumbers(phone)
                    .signName(signName)
                    .templateCode(templateCode)
                    .templateParam("{\"" + templateParam + "\":\"" + code + "\"}")
                    .build();

            // Asynchronously get the return value of the API request
            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            // Synchronously get the return value of the API request
            SendSmsResponse resp = response.get();
            if (!"OK".equalsIgnoreCase(resp.getBody().getCode())) {
                log.error("Fail to send sms: {}", resp);
                throw new RRException(resp.getBody().getMessage());
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RRException("短信下发异常");
        }
    }
}
