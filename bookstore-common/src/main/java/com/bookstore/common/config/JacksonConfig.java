package com.bookstore.common.config;  // 声明当前类所属的包路径

import com.fasterxml.jackson.databind.ObjectMapper;  // 导入Jackson的ObjectMapper，用于JSON序列化/反序列化
import com.fasterxml.jackson.databind.SerializationFeature;  // 导入序列化特性配置
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;  // 导入Java8日期时间模块
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;  // 导入LocalDateTime反序列化器
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;  // 导入LocalDateTime序列化器
import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Spring的@Configuration注解

import java.time.LocalDateTime;  // 导入Java8日期时间类
import java.time.format.DateTimeFormatter;  // 导入日期时间格式化器

/**
 * Jackson序列化配置类
 * 配置JSON序列化时LocalDateTime的格式
 * 默认格式为"yyyy-MM-dd HH:mm:ss"
 */
@Configuration  // Spring注解，标记该类为配置类
public class JacksonConfig {  // Jackson配置类

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";  // 定义日期时间格式模式

    /**
     * 配置ObjectMapper Bean
     * 设置LocalDateTime的序列化和反序列化格式
     * @return 配置好的ObjectMapper实例
     */
    @Bean  // Spring注解，将方法返回值注册为Spring Bean
    public ObjectMapper objectMapper() {  // 创建ObjectMapper方法
        ObjectMapper mapper = new ObjectMapper();  // 创建ObjectMapper实例
        JavaTimeModule module = new JavaTimeModule();  // 创建Java8日期时间模块
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);  // 创建日期时间格式化器
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));  // 注册LocalDateTime序列化器
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));  // 注册LocalDateTime反序列化器
        mapper.registerModule(module);  // 将JavaTimeModule注册到ObjectMapper
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 禁用将日期序列化为时间戳的特性，使用字符串格式
        return mapper;  // 返回配置好的ObjectMapper
    }
}
