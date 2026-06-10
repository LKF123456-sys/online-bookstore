package com.bookstore.common.config;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.DbType;  // 导入数据库类型枚举
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;  // 导入元数据对象处理器接口
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;  // 导入MyBatis-Plus拦截器
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;  // 导入分页插件拦截器
import org.apache.ibatis.reflection.MetaObject;  // 导入MyBatis元数据对象
import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Spring的@Configuration注解

import java.time.LocalDateTime;  // 导入Java8日期时间类

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

    /**
     * 配置自动填充处理器
     * 在插入和更新数据时自动填充createTime/createdAt和updateTime/updatedAt字段
     * 支持两种命名风格，覆盖项目中所有实体的时间字段
     * @return MetaObjectHandler实例
     */
    @Bean  // Spring注解，将方法返回值注册为Spring Bean
    public MetaObjectHandler metaObjectHandler() {  // 创建自动填充处理器方法
        return new MetaObjectHandler() {  // 匿名内部类实现MetaObjectHandler接口
            @Override  // 重写插入填充方法
            public void insertFill(MetaObject metaObject) {  // 插入数据时自动填充
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());  // 填充createTime为当前时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());  // 填充createdAt为当前时间
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());  // 填充updateTime为当前时间
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());  // 填充updatedAt为当前时间
            }

            @Override  // 重写更新填充方法
            public void updateFill(MetaObject metaObject) {  // 更新数据时自动填充
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());  // 填充updateTime为当前时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());  // 填充updatedAt为当前时间
            }
        };
    }
}
