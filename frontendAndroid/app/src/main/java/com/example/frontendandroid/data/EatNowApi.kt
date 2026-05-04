package com.example.frontendandroid.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class ApiException(message: String) : Exception(message)

class EatNowApi(
    private val baseUrlProvider: () -> String,
    private val tokenProvider: () -> String?
) {
    suspend fun get(path: String, query: Map<String, Any?> = emptyMap(), auth: Boolean = false): JSONObject =
        request(path = path, method = "GET", query = query, body = null, auth = auth)

    suspend fun post(path: String, body: JSONObject = JSONObject(), auth: Boolean = false): JSONObject =
        request(path = path, method = "POST", body = body, auth = auth)

    suspend fun put(path: String, body: JSONObject = JSONObject(), auth: Boolean = false): JSONObject =
        request(path = path, method = "PUT", body = body, auth = auth)

    suspend fun patch(path: String, body: JSONObject = JSONObject(), auth: Boolean = false): JSONObject =
        request(path = path, method = "PATCH", body = body, auth = auth)

    suspend fun delete(path: String, auth: Boolean = false): JSONObject =
        request(path = path, method = "DELETE", body = null, auth = auth)

    private suspend fun request(
        path: String,
        method: String,
        query: Map<String, Any?> = emptyMap(),
        body: JSONObject? = null,
        auth: Boolean = false
    ): JSONObject = withContext(Dispatchers.IO) {
        val url = URL(buildUrl(path, query))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            if (auth) {
                val token = tokenProvider().orEmpty()
                if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        if (body != null) {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val raw = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
        if (raw.isBlank()) {
            if (code in 200..299) return@withContext JSONObject("""{"code":200,"message":"success","data":null}""")
            throw ApiException("HTTP $code")
        }

        val json = JSONObject(raw)
        val apiCode = json.optInt("code", code)
        if (code !in 200..299 || apiCode !in 200..299) {
            throw ApiException(json.optString("message", "请求失败"))
        }
        json
    }

    private fun buildUrl(path: String, query: Map<String, Any?>): String {
        val cleanBase = baseUrlProvider().trim().trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        val pairs = query
            .filterValues { it != null && it.toString().isNotBlank() }
            .map { (key, value) ->
                "${key.encode()}=${value.toString().encode()}"
            }
        return if (pairs.isEmpty()) "$cleanBase$cleanPath" else "$cleanBase$cleanPath?${pairs.joinToString("&")}"
    }

    private fun String.encode(): String = URLEncoder.encode(this, "UTF-8")
}
