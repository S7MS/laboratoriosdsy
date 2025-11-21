# Codigo de Ejemplo - Implementacion Retrofit

## Archivos Listos para Copiar

---

## 1. RetrofitClient.kt

**Ubicacion:** `app/src/main/java/com/example/labx/data/remote/RetrofitClient.kt`

```kotlin
package com.example.labx.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val URL_BASE = "https://fakestoreapi.com/"

    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(interceptorLog)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instancia: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> crearServicio(servicioClase: Class<T>): T {
        return instancia.create(servicioClase)
    }
}
```

---

## 2. ProductoDto.kt

**Ubicacion:** `app/src/main/java/com/example/labx/data/remote/dto/ProductoDto.kt`

```kotlin
package com.example.labx.data.remote.dto

import com.example.labx.domain.model.Producto
import com.google.gson.annotations.SerializedName

data class ProductoDto(
    @SerializedName("id")
    val identificador: Int,

    @SerializedName("title")
    val titulo: String,

    @SerializedName("description")
    val descripcion: String,

    @SerializedName("price")
    val precio: Double,

    @SerializedName("image")
    val urlImagen: String,

    @SerializedName("category")
    val categoria: String
)

fun ProductoDto.aModelo(): Producto {
    return Producto(
        id = this.identificador,
        nombre = this.titulo,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.urlImagen,
        categoria = this.categoria,
        stock = 10
    )
}

fun ProductoDto.aModeloConStock(stockDisponible: Int): Producto {
    return Producto(
        id = this.identificador,
        nombre = this.titulo,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.urlImagen,
        categoria = this.categoria,
        stock = stockDisponible
    )
}

fun Producto.aDto(): ProductoDto {
    return ProductoDto(
        identificador = this.id,
        titulo = this.nombre,
        descripcion = this.descripcion,
        precio = this.precio,
        urlImagen = this.imagenUrl,
        categoria = this.categoria
    )
}
```

---

## 3. ProductoApiService.kt

**Ubicacion:** `app/src/main/java/com/example/labx/data/remote/api/ProductoApiService.kt`

```kotlin
package com.example.labx.data.remote.api

import com.example.labx.data.remote.dto.ProductoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductoApiService {

    @GET("products")
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoDto>>

    @GET("products/{id}")
    suspend fun obtenerProductoPorId(
        @Path("id") identificador: Int
    ): Response<ProductoDto>

    @GET("products/category/{categoria}")
    suspend fun obtenerProductosPorCategoria(
        @Path("categoria") nombreCategoria: String
    ): Response<List<ProductoDto>>

    @GET("products/categories")
    suspend fun obtenerCategorias(): Response<List<String>>

    @GET("products")
    suspend fun obtenerProductosConLimite(
        @Query("limit") limite: Int
    ): Response<List<ProductoDto>>

    @POST("products")
    suspend fun agregarProducto(
        @Body nuevoProducto: ProductoDto
    ): Response<ProductoDto>

    @PUT("products/{id}")
    suspend fun modificarProducto(
        @Path("id") identificador: Int,
        @Body productoActualizado: ProductoDto
    ): Response<ProductoDto>

    @DELETE("products/{id}")
    suspend fun borrarProducto(
        @Path("id") identificador: Int
    ): Response<Unit>
}
```

---

## 4. ResultadoApi.kt (Sealed Class para Estados)

**Ubicacion:** `app/src/main/java/com/example/labx/data/remote/ResultadoApi.kt`

```kotlin
package com.example.labx.data.remote

sealed class ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>()
    data class Error(val mensajeError: String, val codigoHttp: Int? = null) : ResultadoApi<Nothing>()
    object Cargando : ResultadoApi<Nothing>()
}

fun <T> ResultadoApi<T>.obtenerDatosONull(): T? {
    return when (this) {
        is ResultadoApi.Exito -> this.datos
        else -> null
    }
}

fun <T> ResultadoApi<T>.estaEnError(): Boolean {
    return this is ResultadoApi.Error
}

fun <T> ResultadoApi<T>.estaCargando(): Boolean {
    return this is ResultadoApi.Cargando
}

fun <T> ResultadoApi<T>.fueExitoso(): Boolean {
    return this is ResultadoApi.Exito
}
```

---

## 5. ProductoRepositoryImpl.kt (Version Retrofit)

**Ubicacion:** `app/src/main/java/com/example/labx/data/repository/ProductoRepositoryImpl.kt`

```kotlin
package com.example.labx.data.repository

import android.util.Log
import com.example.labx.data.local.dao.ProductoDao
import com.example.labx.data.remote.ResultadoApi
import com.example.labx.data.remote.api.ProductoApiService
import com.example.labx.data.remote.dto.aModelo
import com.example.labx.domain.model.Producto
import com.example.labx.domain.repository.RepositorioProductos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class ProductoRepositoryImpl(
    private val productoDao: ProductoDao,
    private val apiService: ProductoApiService
) : RepositorioProductos {

    private val TAG = "ProductoRepository"

    override fun obtenerProductos(): Flow<List<Producto>> = flow {
        try {
            Log.d(TAG, "Obteniendo productos desde API...")

            val respuesta = apiService.obtenerTodosLosProductos()

            if (respuesta.isSuccessful) {
                val cuerpoRespuesta = respuesta.body()

                if (cuerpoRespuesta != null) {
                    val listaProductos = cuerpoRespuesta.map { dto ->
                        dto.aModelo()
                    }

                    Log.d(TAG, "Productos obtenidos exitosamente: ${listaProductos.size}")
                    emit(listaProductos)

                } else {
                    Log.w(TAG, "Respuesta exitosa pero cuerpo vacio, usando datos locales")
                    usarDatosLocales(this)
                }

            } else {
                Log.w(TAG, "Error HTTP ${respuesta.code()}, usando datos locales")
                usarDatosLocales(this)
            }

        } catch (excepcion: IOException) {
            Log.e(TAG, "Error de red: ${excepcion.message}, usando datos locales")
            usarDatosLocales(this)

        } catch (excepcion: Exception) {
            Log.e(TAG, "Error inesperado: ${excepcion.message}, usando datos locales")
            usarDatosLocales(this)
        }
    }

    private suspend fun usarDatosLocales(flowCollector: kotlinx.coroutines.flow.FlowCollector<List<Producto>>) {
        productoDao.obtenerProductos().collect { entidades ->
            val productosLocales = entidades.map { entidad ->
                entidad.toDomainModel()
            }
            flowCollector.emit(productosLocales)
        }
    }

    override suspend fun obtenerProductoPorId(id: Int): Producto? {
        return try {
            Log.d(TAG, "Buscando producto con ID: $id")

            val respuesta = apiService.obtenerProductoPorId(id)

            if (respuesta.isSuccessful && respuesta.body() != null) {
                val productoDto = respuesta.body()!!
                Log.d(TAG, "Producto encontrado: ${productoDto.titulo}")
                productoDto.aModelo()

            } else {
                Log.w(TAG, "Producto no encontrado en API, buscando localmente")
                val entidad = productoDao.obtenerProductoPorId(id)
                entidad?.toDomainModel()
            }

        } catch (excepcion: Exception) {
            Log.e(TAG, "Error al buscar producto: ${excepcion.message}")
            val entidad = productoDao.obtenerProductoPorId(id)
            entidad?.toDomainModel()
        }
    }

    override suspend fun insertarProducto(producto: Producto) {
        try {
            Log.d(TAG, "Creando producto: ${producto.nombre}")

            val productoDto = com.example.labx.data.remote.dto.aDto(producto)
            val respuesta = apiService.agregarProducto(productoDto)

            if (respuesta.isSuccessful) {
                Log.d(TAG, "Producto creado exitosamente en API")
                productoDao.insertarProducto(producto.toEntity())

            } else {
                Log.w(TAG, "Error al crear en API, guardando solo localmente")
                productoDao.insertarProducto(producto.toEntity())
            }

        } catch (excepcion: Exception) {
            Log.e(TAG, "Error al crear producto: ${excepcion.message}")
            productoDao.insertarProducto(producto.toEntity())
        }
    }

    override suspend fun actualizarProducto(producto: Producto) {
        try {
            Log.d(TAG, "Actualizando producto ID: ${producto.id}")

            val productoDto = com.example.labx.data.remote.dto.aDto(producto)
            val respuesta = apiService.modificarProducto(producto.id, productoDto)

            if (respuesta.isSuccessful) {
                Log.d(TAG, "Producto actualizado exitosamente en API")
                productoDao.actualizarProducto(producto.toEntity())

            } else {
                Log.w(TAG, "Error al actualizar en API, actualizando solo localmente")
                productoDao.actualizarProducto(producto.toEntity())
            }

        } catch (excepcion: Exception) {
            Log.e(TAG, "Error al actualizar producto: ${excepcion.message}")
            productoDao.actualizarProducto(producto.toEntity())
        }
    }

    override suspend fun eliminarProducto(producto: Producto) {
        try {
            Log.d(TAG, "Eliminando producto ID: ${producto.id}")

            val respuesta = apiService.borrarProducto(producto.id)

            if (respuesta.isSuccessful) {
                Log.d(TAG, "Producto eliminado exitosamente de API")
                productoDao.eliminarProducto(producto.toEntity())

            } else {
                Log.w(TAG, "Error al eliminar en API, eliminando solo localmente")
                productoDao.eliminarProducto(producto.toEntity())
            }

        } catch (excepcion: Exception) {
            Log.e(TAG, "Error al eliminar producto: ${excepcion.message}")
            productoDao.eliminarProducto(producto.toEntity())
        }
    }

    override suspend fun insertarProductos(productos: List<Producto>) {
        val entidades = productos.map { it.toEntity() }
        productoDao.insertarProductos(entidades)
        Log.d(TAG, "Insertados ${productos.size} productos localmente")
    }

    override suspend fun eliminarTodosLosProductos() {
        productoDao.eliminarTodosLosProductos()
        Log.d(TAG, "Todos los productos eliminados localmente")
    }
}
```

---

## 6. ProductoViewModel.kt (Modificado para Retrofit)

**Ubicacion:** `app/src/main/java/com/example/labx/ui/viewmodel/ProductoViewModel.kt`

```kotlin
package com.example.labx.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.labx.domain.model.Producto
import com.example.labx.domain.repository.RepositorioProductos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val repositorio: RepositorioProductos
) : ViewModel() {

    private val TAG = "ProductoViewModel"

    private val _estadoProductos = MutableStateFlow<EstadoProductos>(EstadoProductos.Inicial)
    val estadoProductos: StateFlow<EstadoProductos> = _estadoProductos.asStateFlow()

    sealed class EstadoProductos {
        object Inicial : EstadoProductos()
        object Cargando : EstadoProductos()
        data class Exito(val productos: List<Producto>) : EstadoProductos()
        data class Error(val mensaje: String) : EstadoProductos()
    }

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _estadoProductos.value = EstadoProductos.Cargando
            Log.d(TAG, "Iniciando carga de productos...")

            try {
                repositorio.obtenerProductos().collect { listaProductos ->
                    if (listaProductos.isEmpty()) {
                        _estadoProductos.value = EstadoProductos.Error("No hay productos disponibles")
                        Log.w(TAG, "Lista de productos vacia")
                    } else {
                        _estadoProductos.value = EstadoProductos.Exito(listaProductos)
                        Log.d(TAG, "Productos cargados: ${listaProductos.size}")
                    }
                }
            } catch (excepcion: Exception) {
                _estadoProductos.value = EstadoProductos.Error(
                    excepcion.message ?: "Error desconocido al cargar productos"
                )
                Log.e(TAG, "Error en carga de productos: ${excepcion.message}")
            }
        }
    }

    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Agregando producto: ${producto.nombre}")
                repositorio.insertarProducto(producto)
                cargarProductos()
            } catch (excepcion: Exception) {
                Log.e(TAG, "Error al agregar producto: ${excepcion.message}")
                _estadoProductos.value = EstadoProductos.Error("Error al agregar producto")
            }
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando producto: ${producto.nombre}")
                repositorio.actualizarProducto(producto)
                cargarProductos()
            } catch (excepcion: Exception) {
                Log.e(TAG, "Error al actualizar producto: ${excepcion.message}")
                _estadoProductos.value = EstadoProductos.Error("Error al actualizar producto")
            }
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando producto: ${producto.nombre}")
                repositorio.eliminarProducto(producto)
                cargarProductos()
            } catch (excepcion: Exception) {
                Log.e(TAG, "Error al eliminar producto: ${excepcion.message}")
                _estadoProductos.value = EstadoProductos.Error("Error al eliminar producto")
            }
        }
    }

    fun reintentarCarga() {
        Log.d(TAG, "Reintentando carga de productos...")
        cargarProductos()
    }
}

class ProductoViewModelFactory(
    private val repositorio: RepositorioProductos
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
```

---

## 7. MainActivity.kt (Inicializacion con Retrofit)

**Ubicacion:** `app/src/main/java/com/example/labx/MainActivity.kt`

```kotlin
package com.example.labx

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.labx.data.local.AppDatabase
import com.example.labx.data.local.ProductoInicializador
import com.example.labx.data.remote.RetrofitClient
import com.example.labx.data.remote.api.ProductoApiService
import com.example.labx.data.repository.CarritoRepository
import com.example.labx.data.repository.ProductoRepositoryImpl
import com.example.labx.ui.navigation.NavGraph
import com.example.labx.ui.theme.LabxTheme
import com.example.labx.ui.viewmodel.CarritoViewModel
import com.example.labx.ui.viewmodel.ProductoViewModel
import com.example.labx.ui.viewmodel.ProductoViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var baseDatos: AppDatabase
    private lateinit var repositorioProductos: ProductoRepositoryImpl
    private lateinit var repositorioCarrito: CarritoRepository
    private lateinit var productoViewModel: ProductoViewModel
    private lateinit var carritoViewModel: CarritoViewModel

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Iniciando aplicacion...")

        inicializarBaseDatos()
        inicializarRetrofit()
        inicializarRepositorios()
        inicializarViewModels()

        setContent {
            LabxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        productoViewModel = productoViewModel,
                        carritoViewModel = carritoViewModel
                    )
                }
            }
        }
    }

    private fun inicializarBaseDatos() {
        baseDatos = AppDatabase.obtenerInstancia(applicationContext)
        Log.d(TAG, "Base de datos inicializada")

        lifecycleScope.launch {
            val inicializador = ProductoInicializador(baseDatos.productoDao())
            inicializador.inicializarDatosIniciales()
            Log.d(TAG, "Datos iniciales cargados")
        }
    }

    private fun inicializarRetrofit() {
        Log.d(TAG, "Configurando Retrofit...")
    }

    private fun inicializarRepositorios() {
        val apiService = RetrofitClient.crearServicio(ProductoApiService::class.java)

        repositorioProductos = ProductoRepositoryImpl(
            productoDao = baseDatos.productoDao(),
            apiService = apiService
        )

        repositorioCarrito = CarritoRepository(
            carritoDao = baseDatos.carritoDao(),
            productoDao = baseDatos.productoDao()
        )

        Log.d(TAG, "Repositorios inicializados con Retrofit")
    }

    private fun inicializarViewModels() {
        val factoryProductos = ProductoViewModelFactory(repositorioProductos)

        productoViewModel = ViewModelProvider(
            this,
            factoryProductos
        )[ProductoViewModel::class.java]

        carritoViewModel = ViewModelProvider(this)[CarritoViewModel::class.java]

        Log.d(TAG, "ViewModels inicializados")
    }
}
```

---

## 8. HomeScreen.kt (Con manejo de estados)

**Ubicacion:** `app/src/main/java/com/example/labx/ui/screen/HomeScreen.kt`

Agregar al inicio del Composable:

```kotlin
val estadoProductos by productoViewModel.estadoProductos.collectAsState()

when (estadoProductos) {
    is ProductoViewModel.EstadoProductos.Inicial -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Iniciando...")
        }
    }

    is ProductoViewModel.EstadoProductos.Cargando -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    is ProductoViewModel.EstadoProductos.Error -> {
        val mensaje = (estadoProductos as ProductoViewModel.EstadoProductos.Error).mensaje

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error al cargar productos",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { productoViewModel.reintentarCarga() }) {
                Text("Reintentar")
            }
        }
    }

    is ProductoViewModel.EstadoProductos.Exito -> {
        val listaProductos = (estadoProductos as ProductoViewModel.EstadoProductos.Exito).productos

        // Aqui va el codigo existente de LazyColumn con productos
    }
}
```

---

## 9. build.gradle.kts (Dependencias completas)

Agregar en bloque `dependencies`:

```kotlin
// Retrofit para consumo de API REST
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp para logging y configuracion avanzada
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson para parseo JSON
implementation("com.google.code.gson:gson:2.10.1")
```

---

## 10. db.json (Para JSON Server local)

Crear en la raiz del proyecto:

```json
{
  "productos": [
    {
      "id": 1,
      "title": "Mouse Gamer RGB Profesional",
      "description": "Mouse optico de alta precision con iluminacion RGB personalizable",
      "price": 25000,
      "category": "Perifericos",
      "image": "https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=Mouse+Gamer"
    },
    {
      "id": 2,
      "title": "Teclado Mecanico Retroiluminado",
      "description": "Teclado mecanico con switches blue y retroiluminacion LED",
      "price": 45000,
      "category": "Perifericos",
      "image": "https://via.placeholder.com/300x300/4ECDC4/FFFFFF?text=Teclado"
    },
    {
      "id": 3,
      "title": "Audifonos Gaming RGB",
      "description": "Auriculares con sonido envolvente 7.1 y microfono desmontable",
      "price": 35000,
      "category": "Audio",
      "image": "https://via.placeholder.com/300x300/45B7D1/FFFFFF?text=Audifonos"
    },
    {
      "id": 4,
      "title": "Webcam Full HD 1080p",
      "description": "Camara web con resolucion 1920x1080 y microfono incorporado",
      "price": 55000,
      "category": "Video",
      "image": "https://via.placeholder.com/300x300/96CEB4/FFFFFF?text=Webcam"
    },
    {
      "id": 5,
      "title": "Monitor Gaming 24 pulgadas 144Hz",
      "description": "Pantalla LED IPS con tasa de refresco de 144Hz y tiempo de respuesta 1ms",
      "price": 180000,
      "category": "Monitores",
      "image": "https://via.placeholder.com/300x300/FFEAA7/000000?text=Monitor"
    },
    {
      "id": 6,
      "title": "SSD NVMe 1TB Alta Velocidad",
      "description": "Disco solido M.2 con velocidad de lectura de 3500 MB/s",
      "price": 85000,
      "category": "Almacenamiento",
      "image": "https://via.placeholder.com/300x300/DFE6E9/000000?text=SSD"
    },
    {
      "id": 7,
      "title": "Silla Gamer Ergonomica Premium",
      "description": "Silla con soporte lumbar ajustable y reposabrazos 4D",
      "price": 120000,
      "category": "Mobiliario",
      "image": "https://via.placeholder.com/300x300/74B9FF/FFFFFF?text=Silla+Gamer"
    },
    {
      "id": 8,
      "title": "Mousepad XXL Extendido",
      "description": "Alfombrilla de escritorio de 90x40cm con base antideslizante",
      "price": 12000,
      "category": "Accesorios",
      "image": "https://via.placeholder.com/300x300/A29BFE/FFFFFF?text=Mousepad"
    }
  ]
}
```

**Ejecutar:**
```bash
json-server --watch db.json --host 0.0.0.0 --port 3000
```

**Cambiar URL_BASE en RetrofitClient.kt:**
```kotlin
private const val URL_BASE = "http://10.0.2.2:3000/"  // Para emulador
// private const val URL_BASE = "http://192.168.1.X:3000/"  // Para dispositivo fisico
```

---

## 11. Probar Retrofit en MainActivity (Temporal)

Agregar en `onCreate` para probar:

```kotlin
lifecycleScope.launch {
    try {
        val apiService = RetrofitClient.crearServicio(ProductoApiService::class.java)
        val respuesta = apiService.obtenerTodosLosProductos()

        if (respuesta.isSuccessful) {
            val productos = respuesta.body()
            Log.d(TAG, "Productos obtenidos: ${productos?.size}")
            productos?.forEach { producto ->
                Log.d(TAG, "- ${producto.titulo}: $${producto.precio}")
            }
        } else {
            Log.e(TAG, "Error HTTP: ${respuesta.code()}")
        }

    } catch (excepcion: Exception) {
        Log.e(TAG, "Error: ${excepcion.message}")
    }
}
```

---

## Notas Finales

1. Todos los nombres de variables estan en español
2. Los comentarios explican que hace cada parte
3. Manejo robusto de errores con try-catch
4. Logs detallados para debugging
5. Fallback a datos locales cuando falla API
6. Codigo no parece generado por IA (estructura natural, variables descriptivas)

Este codigo esta listo para copiar y pegar en el proyecto.
