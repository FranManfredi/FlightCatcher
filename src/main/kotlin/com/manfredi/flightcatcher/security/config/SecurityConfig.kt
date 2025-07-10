package com.manfredi.flightcatcher.security.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
class SecurityConfig {

    @Bean
    fun oauth2LoginCustomizer(): Customizer<OAuth2LoginConfigurer<HttpSecurity>> {
        return Customizer { oauth2 ->
            oauth2.defaultSuccessUrl("/auth/token", true)
        }
    }

    @Bean
    fun jwtAuthenticationFilter(jwtService: JwtService): OncePerRequestFilter {
        return object : OncePerRequestFilter() {
            override fun doFilterInternal(
                    request: HttpServletRequest,
                    response: HttpServletResponse,
                    filterChain: FilterChain
            ) {
                val header = request.getHeader("Authorization") ?: return filterChain.doFilter(request, response)
                if (!header.startsWith("Bearer ")) return filterChain.doFilter(request, response)

                val token = header.removePrefix("Bearer ").trim()
                val claims = jwtService.parseToken(token)

                if (claims == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT")
                    return
                }

                val email = claims["email"] as? String
                val auth = UsernamePasswordAuthenticationToken(email, null, listOf(SimpleGrantedAuthority("USER")))
                SecurityContextHolder.getContext().authentication = auth

                filterChain.doFilter(request, response)
            }

            override fun shouldNotFilter(request: HttpServletRequest): Boolean {
                return !request.servletPath.startsWith("/api/")
            }
        }
    }


    @Bean
    @Order(1)
    fun apiSecurityChain(http: HttpSecurity, jwtAuthenticationFilter: OncePerRequestFilter): SecurityFilterChain {
        http
                .securityMatcher("/api/**")
                .csrf { it.disable() }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { it.anyRequest().authenticated() }
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    @Order(2)
    fun defaultSecurityChain(http: HttpSecurity): SecurityFilterChain {
        http
                .authorizeHttpRequests {
                    it
                            .requestMatchers("/login/**", "/auth/token", "/oauth2/**").permitAll()
                            .anyRequest().authenticated()
                }
                .oauth2Login(oauth2LoginCustomizer())
        return http.build()
    }
}
