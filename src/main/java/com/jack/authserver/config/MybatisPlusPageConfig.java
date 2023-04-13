package com.jack.authserver.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration      //表示这是一个配置类
public class MybatisPlusPageConfig {

    /**
     * 注册插件
     */
    @Bean   //表示此方法返回一个Bean实例
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //添加分页插件
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor();
        //设置请求的页面大于最大页的操作，true调回首页，false继续请求，默认是false
        pageInterceptor.setOverflow(false);
        //单页分页的条数限制，默认五限制
        pageInterceptor.setMaxLimit(500L);
        //设置数据库类型
        pageInterceptor.setDbType(DbType.MYSQL);

        interceptor.addInnerInterceptor(pageInterceptor);
        return interceptor;
    }
}