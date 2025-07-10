package com.manfredi.flightcatcher.security.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
        @Value("\${app.custom.schema}") private val customSchema: String,
        private val jwtService: JwtService
) {

    @GetMapping("/token")
    fun emitirJwt(
            @AuthenticationPrincipal principal: OAuth2User?,
            response: HttpServletResponse
    ) {
        if (principal == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authenticated user")
            return
        }
        val email = principal.getAttribute<String>("email") ?: "unknown"
        val id = principal.getAttribute<String>("sub") ?: principal.name
        val jwt = jwtService.generateToken(id, email)

        // Redirigí al esquema personalizado de la app iOS
        val url = "${customSchema}://oauth-callback?token=$jwt"
        response.sendRedirect(url)
    }
}
