package com.manfredi.flightcatcher.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .authorizeHttpRequests {
                    it
                            .requestMatchers("/", "/login/**", "/error").permitAll()
                            .anyRequest().authenticated()
                }
                .oauth2Login(Customizer.withDefaults()) // <- importante para habilitar login con Google
        return http.build()
    }
}
