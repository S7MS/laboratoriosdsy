package com.example.labx.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit configurado como Singleton para manejo de peticiones HTTP
 *
 * Esta clase proporciona una instancia única de Retrofit para toda la aplicación,
 * optimizando recursos y evitando múltiples configuraciones innecesarias.
 *
 * @author Sting Parra Silva
 * @version 1.0
 */
object RetrofitClient {

    /**
     * URL base de la API REST
     *
     * Opciones disponibles:
     * - FakeStoreAPI: "https://fakestoreapi.com/"
     * - DummyJSON: "https://dummyjson.com/"
     * - JSON Server local (emulador): "http://10.0.2.2:3000/"
     * - JSON Server local (dispositivo): "http://[IP_LOCAL]:3000/"
     */
    private const val URL_BASE = "https://fakestoreapi.com/"

    /**
     * Interceptor para logging de peticiones y respuestas HTTP
     *
     * Niveles disponibles:
     * - NONE: Sin logs
     * - BASIC: Request method y URL, response code
     * - HEADERS: Request y response headers
     * - BODY: Request y response body completo
     */
    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP configurado con timeouts e interceptores
     *
     * Timeouts configurados:
     * - connectTimeout: Tiempo máximo para establecer conexión
     * - readTimeout: Tiempo máximo para leer respuesta
     * - writeTimeout: Tiempo máximo para enviar datos
     */
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(interceptorLog)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instancia de Retrofit inicializada de forma perezosa
     *
     * La palabra clave 'lazy' garantiza que:
     * - Se crea solo cuando se usa por primera vez
     * - Se crea una sola vez (thread-safe)
     * - Mejora el rendimiento al evitar inicialización innecesaria
     */
    val instancia: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Crea una instancia del servicio API especificado
     *
     * @param T Tipo del servicio API (interface)
     * @param servicioClase Clase del servicio a crear
     * @return Implementación del servicio API lista para usar
     *
     * Ejemplo de uso:
     * ```
     * val apiService = RetrofitClient.crearServicio(ProductoApiService::class.java)
     * ```
     */
    fun <T> crearServicio(servicioClase: Class<T>): T {
        return instancia.create(servicioClase)
    }
}
