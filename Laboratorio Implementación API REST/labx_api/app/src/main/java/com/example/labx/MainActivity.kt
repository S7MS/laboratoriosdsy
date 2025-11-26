package com.example.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.labx.data.local.AppDatabase
import com.example.labx.data.local.PreferenciasManager
import com.example.labx.data.local.ProductoInicializador
import com.example.labx.data.remote.RetrofitClient
import com.example.labx.data.remote.api.ProductoApiService
import com.example.labx.data.repository.CarritoRepository
import com.example.labx.data.repository.ProductoRepositoryImpl
import com.example.labx.ui.navigation.NavGraph
import com.example.labx.ui.viewmodel.ProductoViewModel
import com.example.labx.ui.viewmodel.ProductoViewModelFactory

/**
 * MainActivity: Punto de entrada de la aplicación
 *
 * Responsabilidades:
 * - Crear la base de datos local (Room)
 * - Configurar cliente HTTP (Retrofit)
 * - Crear los repositorios con acceso a API y cache
 * - Inicializar ViewModels con inyección de dependencias
 * - Configurar el sistema de navegación entre pantallas
 *
 * Arquitectura implementada: MVVM + Repository Pattern + Retrofit
 *
 * @author Sting Parra Silva
 * @version 2.0
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PASO 1: Inicializar base de datos local (Room)
        // La base de datos funciona como cache offline
        val database = AppDatabase.getDatabase(applicationContext)

        // PASO 2: Inicializar productos de ejemplo en cache local (solo primera vez)
        // Esto garantiza que la app funcione sin internet la primera vez
        ProductoInicializador.inicializarProductos(applicationContext)

        // PASO 3: Configurar cliente Retrofit para consumo de API REST
        // Crea una instancia del servicio API usando el RetrofitClient singleton
        val apiService: ProductoApiService = RetrofitClient.crearServicio(ProductoApiService::class.java)

        // PASO 4: Crear repositorios con acceso a ambas fuentes de datos
        // ProductoRepository: usa API (fuente primaria) + Room (fallback)
        val productoRepository = ProductoRepositoryImpl(
            productoDao = database.productoDao(),
            apiService = apiService
        )

        // CarritoRepository: usa solo Room (datos locales, no necesita API)
        val carritoRepository = CarritoRepository(database.carritoDao())

        // PASO 5: Crear PreferenciasManager para sesión de admin
        val preferenciasManager = PreferenciasManager(applicationContext)

        setContent {
            MaterialTheme {
                Surface {
                    // PASO 6: Crear NavController para gestionar navegación entre pantallas
                    val navController = rememberNavController()

                    // PASO 7: Crear ViewModel con Factory (inyección de dependencias)
                    // El Factory permite pasar el repositorio al ViewModel
                    val productoViewModel: ProductoViewModel = viewModel(
                        factory = ProductoViewModelFactory(productoRepository)
                    )

                    // PASO 8: Iniciar el grafo de navegación
                    // Define todas las pantallas de la app y cómo navegar entre ellas
                    NavGraph(
                        navController = navController,
                        productoRepository = productoRepository,
                        carritoRepository = carritoRepository,
                        preferenciasManager = preferenciasManager,
                        productoViewModel = productoViewModel
                    )
                }
            }
        }
    }
}

/**
 * NOTAS DE IMPLEMENTACIÓN:
 *
 * 1. ORDEN DE INICIALIZACIÓN:
 *    - Primero: Base de datos (necesaria para fallback)
 *    - Segundo: API Service (Retrofit)
 *    - Tercero: Repositorios (conectan API + DB)
 *    - Cuarto: ViewModels (usan repositorios)
 *
 * 2. ESTRATEGIA DE DATOS:
 *    - ProductoRepository: Híbrido (API + Room)
 *    - CarritoRepository: Solo local (Room)
 *
 * 3. FUNCIONAMIENTO:
 *    - Con internet: Muestra productos de la API
 *    - Sin internet: Muestra productos del cache local
 *    - Siempre funcional: Nunca deja al usuario sin datos
 *
 * 4. PARA DEPURAR:
 *    - Abrir Logcat en Android Studio
 *    - Filtrar por: "ProductoRepository"
 *    - Ver logs de: API exitosa, fallback a local, errores
 *
 * 5. CAMBIAR URL DE API:
 *    - Editar: RetrofitClient.kt
 *    - Modificar: URL_BASE
 *    - Opciones: FakeStoreAPI, DummyJSON, JSON Server local
 */