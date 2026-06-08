package com.bookstore.common.config;  // 声明当前类所属的包路径

import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Spring的@Configuration注解
import org.springframework.data.redis.connection.RedisConnectionFactory;  // 导入Redis连接工厂
import org.springframework.data.redis.core.RedisTemplate;  // 导入RedisTemplate模板类
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;  // 导入JSON序列化器
import org.springframework.data.redis.serializer.StringRedisSerializer;  // 导入字符串序列化器

/**
 * Redis配置类
 * 配置RedisTemplate的序列化方式
 * 使用JSON格式存储value，字符串格式存储key
 */
@Configuration  // Spring注解，标记该类为配置类
public class RedisConfig {  // Redis配置类

    /**
     * 配置RedisTemplate Bean
     * @param factory Redis连接工厂，由Spring自动注入
     * @return 配置好的RedisTemplate实例
     */
    @Bean  // Spring注解，将方法返回值注册为Spring Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {  // 创建RedisTemplate方法
        RedisTemplate<String, Object> template = new RedisTemplate<>();  // 创建RedisTemplate实例
        template.setConnectionFactory(factory);  // 设置Redis连接工厂
        template.setKeySerializer(new StringRedisSerializer());  // 设置key的序列化方式为字符串
        template.setHashKeySerializer(new StringRedisSerializer());  // 设置hash key的序列化方式为字符串
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());  // 设置value的序列化方式为JSON
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());  // 设置hash value的序列化方式为JSON
        template.afterPropertiesSet();  // 初始化模板，应用所有配置
        return template;  // 返回配置好的RedisTemplate
    }
}
