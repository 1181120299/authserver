这是一个基于Spring Authorization Server 1.0.1实现的认证授权应用。可以实现OAuth2.0用户授权访问，以及作为统一认证中心，实现各个OAuth client应用之间的跳转访问控制。

# 一、环境要求

- JDK17
- Mysql8.0.11

# 二、开始使用

## 2.1 登录auth server

默认端口号9000，auth server默认会生成一个管理员账号：**jack**，密码：**123456**。

访问链接示例：http://localhost:9000

![image-20230414221956793](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230414221956793.png)

## 2.2 添加OAuth client

需要在auth server管理页面注册应用，用于后续请求用户授权访问。

列表页面如下所示：

![image-20230414222231779](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230414222231779.png)

点击”添加应用“，进入新增页面：

![image-20230414222315923](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230414222315923.png)

## 2.3 项目配置OAuth client信息

创建一个spring boot应用。

### （1）添加maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### （2）application.yml添加OAuth client信息

client信息是之前在auth server管理页面上新增应用信息，可以在[列表页](#2.2 添加OAuth client)查看。

```yaml
authserver: http://localhost:9000     # 授权服务的地址，例如：http://ip:port/contextPath
client-id: bookstore-client           # 在授权服务器上注册的OAuth client的id（应用名称）
client-secret: secret                 # 在授权服务器上注册的OAuth client的秘钥
client-ip: 192.168.1.101              # 当前应用部署的服务器ip，需要和授权服务器上注册的OAuth client回调地址中的ip保持一致
```

### （3）配置SecurityFilterChain

```java
package com.jack.bookstore.config;

import com.jack.bookstore.utils.ClientRegistrationConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/webjars/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().authenticated()
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login.loginPage("/oauth2/authorization/" + ClientRegistrationConstant.CLIENT_OIDC)
                )
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // Set the location that the End-User`s User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/index");

        return oidcLogoutSuccessHandler;
    }
}
```

至此，完成client应用配置。

## 2.4 测试

访问client应用页面，将会跳转到auth server配置的登录页面。登录成功后，跳转会配置的应用回调地址。回调地址接口处理完成，重定向到应用主页(/)。

# 三、client应用间接口调用

假设client A要访问client B的接口/message。可以通过client credentials的方式，或者authorization code的方式要求用户授权访问。

下面示例通过spring-webflux实现client应用间接口授权访问：

### 3.1 添加maven依赖

```xml
<!--webflux-->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webflux</artifactId>
</dependency>

<dependency>
    <groupId>io.projectreactor.netty</groupId>
    <artifactId>reactor-netty</artifactId>
</dependency>
```

### 3.2 配置webclient

```java
package com.jack.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .build();
        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
```

### 3.3 调用接口

配置好webclient后，就可以通过webclient进行应用之间的接口访问了。

```java
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class DemoController{
    @Autowired
    private WebClient webClient;

    @GetMapping(value = "/authorize", params = "grant_type=authorization_code")
    public String authorizationCodeGrant(Model model,
                                         @RegisteredOAuth2AuthorizedClient(ClientRegistrationConstant.CLIENT_AUTHORIZATION_CODE) OAuth2AuthorizedClient authorizedClient) {

        String[] messages = this.webClient
            .get()
            .uri(this.messageBaseUri)
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String[].class)
            .block();
        model.addAttribute("messages", messages);

        return "index";
    }

    @GetMapping(value = "/authorize", params = "grant_type=client_credentials")
    public String clientCredentialsGrant(Model model) {
        String[] messages = this.webClient
            .get()
            .uri(this.messageBaseUri)
            .attributes(clientRegistrationId(ClientRegistrationConstant.CLIENT_CLIENT_CREDENTIALS))
            .retrieve()
            .bodyToMono(String[].class)
            .block();
        model.addAttribute("messages", messages);

        return "index";
    }
}
```

# 四、client之间跳转

从client A应用跳转到client B应用，可以在client A的页面中加超链接，点击后直接通过window.open()的方法，打开client B的页面。

如果用户已经授权登录client A应用，则跳转到client B应用会保持登录状态。这是auth server通过client的ip地址实现的统一登录认证功能。同一个ip地址，访问在auth server中注册的应用时，登录了其中一个应用，其他应用也会授权登录访问。

为了方便应用间跳转地址的修改管理，可以在client A点击超链接时，访问自身的接口，然后由接口进行重定向到client B的页面。如下所示：

```java
@Value("${apple.homepage}")
private String appleHomepage;

@GetMapping("/toAppleMobile")
public String toAppleMobile() {
    System.out.println("appleHomepage = " + appleHomepage);
    return "redirect:" + appleHomepage;
}
```

# 五、自定义用户信息

spring authorization server提供的用户表（users）只有三个字段：username、password、enabled。

真实的业务系统，用户的信息往往会有很多字段。这时候就需要扩展用户数据。整体思路如下：

1. 新建一个用户表，比如叫`t_user`，主键username可以和users表的主键（username）建立foreign key。然后随意添加用户表的其他字段。
2. 自定义token令牌格式（推荐使用JWT令牌），登录成功后，根据username查询`t_user`表中的用户信息。将用户信息编码进token令牌。
3. client端会解析token令牌，生成Authentication实现类的对象，然后只需要取对应的字段，填充用户信息对象即可。

代码示例如下：

**第一步：auth server需要自定义token令牌。提供一个`OAuth2TokenCustomizer`的实现类，在`customize()`方法中将用户信息编码入token令牌。**

```java
@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
    return new FederatedIdentityIdTokenCustomizer();
}

public class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    @Override
    public void customize(JwtEncodingContext context) {
        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            // If user from local, load userinfo from database, set userinfo property to claim
            if (context.getPrincipal() instanceof UsernamePasswordAuthenticationToken) {				①
                OidcUserInfo userInfo = userInfoService.loadUser(context.getPrincipal().getName());		②
                if (userInfo != null) {
                    Map<String, Object> userClaimMap = new HashMap<>(userInfo.getClaims());
                    context.getClaims().claims(existingClaims -> {										③
                        existingClaims.keySet().forEach(userClaimMap::remove);
                        ID_TOKEN_CLAIMS.forEach(userClaimMap::remove);
                    });

                    context.getClaims().claims(claims -> claims.putAll(userClaimMap));					④
                }
            }
        }
    }
}
```

① 当本地账号登录（即授权的提供者为我们的auth server）时，才查询用户信息，编码进token令牌。如果是第三方授权（比如github），`principal`为`OAuth2AuthenticatedPrincipal`。

② 根据username查询用户信息（t_user），返回`OidcUserInfo`对象。需要将用户信息字段写入`OidcUserInfo`对象的Map字段`claims`中。

③ 因为用户信息会放在token令牌的`User Attributes`数组中，假如用户信息的某个字段和spring security规定的字段字段重名，这时候只能将用户信息的字段移除，避免异常。

④ 将用户信息编码进token令牌，即放入令牌的`User Attributes`数组中。

**第二步：client提取用户信息**

```java
@GetMapping("/index")
public String index(Authentication authentication) {
    log.info("welcome to homepage: {}", authentication);

    SecurityContext context = SecurityContextHolder.getContext();
    log.info("get authentication from context: {}", context.getAuthentication());

    User user = extractUserFromAuthentication(authentication);
    log.info("===>user = {}", user);

    return "index";
}

private User extractUserFromAuthentication(Authentication authentication) {
    Assert.notNull(authentication, "authentication can not be null");
    if (authentication.getPrincipal() instanceof DefaultOidcUser) {
        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        return JSON.parseObject(JSON.toJSONString(oidcUser.getClaims()), User.class);
    }

    return null;
}
```

**注意事项：**新增用户，需要操作三张表：

- 自定义的用户信息表`t_user`
- spring authorization server提供的用户表`users`
- 用户权限表`authorities`