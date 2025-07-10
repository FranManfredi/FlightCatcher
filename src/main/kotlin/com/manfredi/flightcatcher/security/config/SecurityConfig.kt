package com.manfredi.flightcatcher.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun oauth2LoginCustomizer(): Customizer<OAuth2LoginConfigurer<HttpSecurity>> {
        return Customizer { oauth2 ->
            oauth2.defaultSuccessUrl("/auth/token", true)
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .authorizeHttpRequests {
                    it
                            .requestMatchers("/", "/login/**", "/error").permitAll()
                            .anyRequest().authenticated()
                }
                .oauth2Login(oauth2LoginCustomizer()) // <- importante para habilitar login con Google
        return http.build()
    }
}
