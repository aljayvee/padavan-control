package com.example.padavancontrol.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(
    private val usernameProvider: () -> String,
    private val passwordProvider: () -> String,
    private val targetHostProvider: () -> String
) : Interceptor {

    @Volatile
    private var cachedUsername: String? = null

    @Volatile
    private var cachedPassword: String? = null

    @Volatile
    private var cachedCredential: String? = null

    private fun credentialHeader(username: String, password: String): String {
        val cached = cachedCredential
        if (cached != null && username == cachedUsername && password == cachedPassword) {
            return cached
        }

        val credential = Credentials.basic(username, password, java.nio.charset.StandardCharsets.UTF_8)
        cachedUsername = username
        cachedPassword = password
        cachedCredential = credential
        return credential
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val targetHost = targetHostProvider()
        val requestHost = originalRequest.url.host

        val username = usernameProvider()
        val password = passwordProvider()

        if (username.isEmpty() && password.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Only add authorization header if requestHost matches targetHost
        if (targetHost.isNotEmpty() && requestHost.equals(targetHost, ignoreCase = true)) {
            val credential = credentialHeader(username, password)
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", credential)
                .build()
            
            val response = chain.proceed(authenticatedRequest)
            if (response.code == 401) {
                RetrofitClient.notifyUnauthorized()
            }
            return response
        }

        return chain.proceed(originalRequest)
    }
}
