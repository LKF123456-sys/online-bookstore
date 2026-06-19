package com.bookstore.common.config;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.DbType;  // 导入数据库类型枚举
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;  // 导入MyBatis-Plus拦截器
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;  // 导入分页插件拦截器
import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Spring的@Configuration注解

/**
 * MyBatis-Plus配置类
 * 配置分页插件和自动填充功能
 * 分页插件支持MySQL数据库的分页查询
 * 自动填充支持多种命名风格的时间字段（createTime/createdAt 和 updateTime/updatedAt）
 */
@Configuration  // Spring注解，标记该类为配置类
public class MybatisPlusConfig {  // MyBatis-Plus配置类

    /**
     * 配置MyBatis-Plus分页拦截器
     * @return MybatisPlusInterceptor实例
     */
    @Bean  // Spring注解，将方法返回值注册为Spring Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {  // 创建分页拦截器方法
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();  // 创建MyBatis-Plus拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));  // 添加MySQL分页插件
        return interceptor;  // 返回配置好的拦截器
    }
}
