package com.manfredi.flightcatcher.security.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
        @Value("\${app.jwt.secret}") private val secret: String,
        @Value("\${app.jwt.expiration}") private val expirationSeconds: Long
) {

    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: String, email: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationSeconds * 1000)

        return Jwts.builder()
                .setSubject(userId)
                .setIssuer("flightcatcher")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("email", email)
                .signWith(key)
                .compact()
    }
}