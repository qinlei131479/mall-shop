package com.tigshop.common.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tigshop.common.core.TranslatePackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module translateModule(TranslatePackage translationService) {
        SimpleModule module = new SimpleModule();
        // 注册序列化器并关联 TranslationService
        module.addSerializer(new TranslateSerializer(translationService));
        return module;
    }
}