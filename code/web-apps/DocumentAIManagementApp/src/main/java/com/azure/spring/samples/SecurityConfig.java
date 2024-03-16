package com.azure.spring.samples;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadWebApplicationHttpSecurityConfigurer.aadWebApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain defaultAadWebApplicationFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.with(aadWebApplication(), (customizer) -> {})
            .authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/login/**").permitAll()
                         .anyRequest().authenticated());
        return http.build();
    }
}