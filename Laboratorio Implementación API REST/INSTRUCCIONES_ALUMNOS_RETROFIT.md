# Laboratorio: Implementacion de API REST con Retrofit

## Arquitectura MVVM + Retrofit
#### PROF. STING PARRA SILVA

---

## Introduccion

En este laboratorio vas a aprender a consumir una API REST utilizando Retrofit en una aplicacion Android con arquitectura MVVM. Actualmente la aplicacion funciona con datos locales guardados en Room, y tu tarea sera integrar una fuente de datos remota manteniendo la misma arquitectura.

---

## Objetivos de Aprendizaje

Al finalizar este laboratorio seras capaz de:

1. Configurar Retrofit en un proyecto Android
2. Crear interfaces de servicio API con anotaciones HTTP
3. Mapear respuestas JSON a objetos Kotlin (DTOs)
4. Integrar Retrofit con el patron Repository
5. Manejar errores de red y respuestas HTTP
6. Implementar estrategias de fallback (API + cache local)

---

## Requisitos Previos

- Android Studio instalado
- Conocimientos de Kotlin basico
- Comprension del patron MVVM
- Conocimientos basicos de coroutines
- Conexion a internet

---

## Parte 1: Configuracion Inicial 

### Paso 1.1: Agregar Dependencias

Abre el archivo `app/build.gradle.kts` y agrega las siguientes dependencias en el bloque `dependencies`:

```kotlin
// Retrofit para consumo de API REST
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp para logging
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson para parseo JSON
implementation("com.google.code.gson:gson:2.10.1")
```

Sincroniza el proyecto (Sync Now).

### Paso 1.2: Verificar Permisos

Abre `AndroidManifest.xml` y verifica que exista el permiso de internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Ya deberia estar presente en el proyecto.

---

## Parte 2: Crear Estructura de Carpetas 

Dentro de `app/src/main/java/com/example/labx/data/`, crea las siguientes carpetas:

```
data/
└── remote/
    ├── api/
    └── dto/
```

Pasos:
1. Click derecho en `data` > New > Package
2. Nombrar: `remote`
3. Repetir para crear `api` y `dto` dentro de `remote`

---

## Parte 3: Implementar Cliente Retrofit 

### Paso 3.1: Crear RetrofitClient

Dentro de `data/remote/`, crea un archivo Kotlin llamado `RetrofitClient.kt`:

Click derecho en `remote` > New > Kotlin Class/File > Object

```kotlin
package com.example.labx.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL base de la API (FakeStoreAPI)
    private const val URL_BASE = "https://fakestoreapi.com/"

    // Interceptor para ver las peticiones en Logcat
    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con configuracion de timeouts
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(interceptorLog)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia de Retrofit (se crea solo una vez)
    val instancia: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Funcion para crear servicios de API
    fun <T> crearServicio(servicioClase: Class<T>): T {
        return instancia.create(servicioClase)
    }
}
```

**Conceptos clave:**
- `object` = Patron Singleton (una sola instancia en toda la app)
- `by lazy` = La instancia se crea solo cuando se usa por primera vez
- `HttpLoggingInterceptor` = Permite ver las peticiones HTTP en Logcat para debugging

---

## Parte 4: Crear DTO (Data Transfer Object) 

### Paso 4.1: Entender la Respuesta de la API

Primero, veamos como se ve un producto en la API de FakeStoreAPI:

```json
{
  "id": 1,
  "title": "Fjallraven Backpack",
  "price": 109.95,
  "description": "Your perfect pack...",
  "category": "men's clothing",
  "image": "https://fakestoreapi.com/img/..."
}
```

Observa que:
- Los nombres estan en ingles ("title", "price")
- Nuestra app usa nombres en español ("titulo", "precio")
- Necesitamos mapear entre estos nombres

### Paso 4.2: Crear ProductoDto.kt

Dentro de `data/remote/dto/`, crea `ProductoDto.kt`:

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
```

**Pregunta de reflexion:**
- ¿Por que usamos `@SerializedName`?
- ¿Que pasaria si el JSON tiene "title" pero nosotros ponemos "titulo" sin la anotacion?

### Paso 4.3: Crear Funcion de Mapeo

Agrega al final del archivo `ProductoDto.kt`:

```kotlin
// Convierte ProductoDto (API) a Producto (dominio)
fun ProductoDto.aModelo(): Producto {
    return Producto(
        id = this.identificador,
        nombre = this.titulo,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.urlImagen,
        categoria = this.categoria,
        stock = 10  // Valor por defecto ya que la API no lo provee
    )
}

// Convierte Producto (dominio) a ProductoDto (API)
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

**Concepto importante:**
Estas son "extension functions" que permiten convertir entre el modelo de la API (DTO) y el modelo de dominio (Producto). Esto mantiene separadas las capas de tu arquitectura.

---

## Parte 5: Crear Interface de Servicio API 

Dentro de `data/remote/api/`, crea `ProductoApiService.kt`:

```kotlin
package com.example.labx.data.remote.api

import com.example.labx.data.remote.dto.ProductoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductoApiService {

    @GET("products")
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoDto>>

    @GET("products/{id}")
    suspend fun obtenerProductoPorId(
        @Path("id") identificador: Int
    ): Response<ProductoDto>
}
```

**Analisis del codigo:**

1. `@GET("products")` = HTTP GET a https://fakestoreapi.com/products
2. `suspend` = Funcion de coroutine (no bloquea la UI)
3. `Response<T>` = Envuelve la respuesta con codigo HTTP (200, 404, 500, etc)
4. `@Path("id")` = Reemplaza {id} en la URL con el valor del parametro

**Ejercicio:**
Agrega tu mismo el metodo para obtener productos por categoria:

```kotlin
@GET("products/category/{categoria}")
suspend fun obtenerProductosPorCategoria(
    @Path("categoria") nombreCategoria: String
): Response<List<ProductoDto>>
```

---

## Parte 6: Modificar el Repositorio 

### Paso 6.1: Actualizar el Constructor

Abre `data/repository/ProductoRepositoryImpl.kt` y modifica el constructor:

**Antes:**
```kotlin
class ProductoRepositoryImpl(
    private val productoDao: ProductoDao
) : RepositorioProductos {
```

**Despues:**
```kotlin
class ProductoRepositoryImpl(
    private val productoDao: ProductoDao,
    private val apiService: ProductoApiService
) : RepositorioProductos {
```

### Paso 6.2: Modificar obtenerProductos()

Reemplaza el metodo `obtenerProductos()`:

```kotlin
private val TAG = "ProductoRepository"

override fun obtenerProductos(): Flow<List<Producto>> = flow {
    try {
        Log.d(TAG, "Intentando obtener productos desde API...")

        // 1. Hacer peticion a la API
        val respuesta = apiService.obtenerTodosLosProductos()

        // 2. Verificar si la respuesta fue exitosa (codigo 200-299)
        if (respuesta.isSuccessful) {
            val cuerpoRespuesta = respuesta.body()

            if (cuerpoRespuesta != null) {
                // 3. Convertir DTOs a modelos de dominio
                val listaProductos = cuerpoRespuesta.map { dto ->
                    dto.aModelo()
                }

                Log.d(TAG, "Productos obtenidos de API: ${listaProductos.size}")
                emit(listaProductos)

            } else {
                Log.w(TAG, "Respuesta exitosa pero cuerpo vacio")
                usarDatosLocales(this)
            }

        } else {
            // 4. Si el servidor responde con error, usar datos locales
            Log.w(TAG, "Error HTTP ${respuesta.code()}, usando datos locales")
            usarDatosLocales(this)
        }

    } catch (excepcion: IOException) {
        // 5. Si hay error de red (sin internet), usar datos locales
        Log.e(TAG, "Error de red: ${excepcion.message}")
        usarDatosLocales(this)

    } catch (excepcion: Exception) {
        // 6. Cualquier otro error inesperado
        Log.e(TAG, "Error inesperado: ${excepcion.message}")
        usarDatosLocales(this)
    }
}

// Funcion auxiliar para cargar datos de Room
private suspend fun usarDatosLocales(
    flowCollector: kotlinx.coroutines.flow.FlowCollector<List<Producto>>
) {
    productoDao.obtenerProductos().collect { entidades ->
        val productosLocales = entidades.map { it.toDomainModel() }
        flowCollector.emit(productosLocales)
    }
}
```

**Estrategia implementada:**
1. Primero intenta obtener de la API
2. Si falla por cualquier razon, usa los datos locales de Room
3. Siempre tiene un plan B (fallback)

**Preguntas de comprension:**
- ¿Que pasa si no hay internet?
- ¿Que pasa si el servidor responde con error 500?
- ¿Por que es importante tener el fallback a datos locales?

---

## Parte 7: Actualizar MainActivity

Abre `MainActivity.kt` y modifica el metodo `inicializarRepositorios()`:

**Antes:**
```kotlin
private fun inicializarRepositorios() {
    repositorioProductos = ProductoRepositoryImpl(
        productoDao = baseDatos.productoDao()
    )

    repositorioCarrito = CarritoRepository(
        carritoDao = baseDatos.carritoDao(),
        productoDao = baseDatos.productoDao()
    )
}
```

**Despues:**
```kotlin
private fun inicializarRepositorios() {
    // Crear instancia del servicio API
    val apiService = RetrofitClient.crearServicio(ProductoApiService::class.java)

    // Repositorio de productos con API y DAO
    repositorioProductos = ProductoRepositoryImpl(
        productoDao = baseDatos.productoDao(),
        apiService = apiService
    )

    // Repositorio de carrito (solo local)
    repositorioCarrito = CarritoRepository(
        carritoDao = baseDatos.carritoDao(),
        productoDao = baseDatos.productoDao()
    )

    Log.d(TAG, "Repositorios inicializados con Retrofit")
}
```

Agrega el import:
```kotlin
import com.example.labx.data.remote.RetrofitClient
import com.example.labx.data.remote.api.ProductoApiService
```

---

## Parte 8: Probar la Implementacion 

### Paso 8.1: Ejecutar la App

1. Conecta un dispositivo fisico o inicia el emulador
2. Ejecuta la app
3. Navega a la pantalla de productos

### Paso 8.2: Revisar Logcat

Abre Logcat en Android Studio y filtra por:
```
ProductoRepository
```

Deberias ver mensajes como:
```
D/ProductoRepository: Intentando obtener productos desde API...
D/OkHttp: --> GET https://fakestoreapi.com/products
D/OkHttp: <-- 200 OK (245ms)
D/ProductoRepository: Productos obtenidos de API: 20
```

### Paso 8.3: Verificar Comportamiento

**Prueba 1: Con internet**
- La app deberia mostrar 20 productos de FakeStoreAPI
- Los nombres estan en ingles (Backpack, T-shirt, etc)

**Prueba 2: Sin internet**
1. Activa modo avion
2. Vuelve a abrir la app
3. Deberia mostrar los 8 productos locales precargados
4. Deberia aparecer mensaje en Logcat: "Error de red, usando datos locales"

**Prueba 3: Recuperacion**
1. Desactiva modo avion
2. Cierra y vuelve a abrir la app
3. Deberia volver a mostrar productos de la API

---

## Parte 9: Mejorar la UI - Manejo de Estados

### Paso 9.1: Actualizar ProductoViewModel

Abre `ProductoViewModel.kt` y verifica que tenga la sealed class:

```kotlin
sealed class EstadoProductos {
    object Inicial : EstadoProductos()
    object Cargando : EstadoProductos()
    data class Exito(val productos: List<Producto>) : EstadoProductos()
    data class Error(val mensaje: String) : EstadoProductos()
}
```

Agrega el metodo para reintentar:

```kotlin
fun reintentarCarga() {
    Log.d(TAG, "Reintentando carga de productos...")
    cargarProductos()
}
```

### Paso 9.2: Actualizar HomeScreen

Abre `HomeScreen.kt` y busca donde se usa `LazyColumn` para mostrar productos.

Envuelve todo el contenido actual con este codigo:

```kotlin
val estadoProductos by productoViewModel.estadoProductos.collectAsState()

when (estadoProductos) {
    is ProductoViewModel.EstadoProductos.Cargando -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando productos...")
            }
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

        // AQUI VA TU CODIGO EXISTENTE DE LazyColumn
    }

    else -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Iniciando...")
        }
    }
}
```

Agrega los imports necesarios:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
```

---

## Parte 10: Desafios Opcionales (Avanzado)

### Desafio 1: Implementar POST (Crear Producto)

Agrega en `ProductoApiService.kt`:

```kotlin
@POST("products")
suspend fun agregarProducto(
    @Body producto: ProductoDto
): Response<ProductoDto>
```

Modifica `insertarProducto()` en el repositorio para usar este endpoint.

### Desafio 2: Implementar Cache Inteligente

Modifica el repositorio para:
1. Guardar productos de la API en Room al obtenerlos
2. Mostrar primero cache (rapido)
3. Actualizar desde API en segundo plano
4. Refrescar solo si han pasado mas de 5 minutos

### Desafio 3: Agregar Paginacion

FakeStoreAPI soporta el parametro `limit`:

```kotlin
@GET("products")
suspend fun obtenerProductosConLimite(
    @Query("limit") cantidad: Int
): Response<List<ProductoDto>>
```

Implementa carga paginada en la lista de productos.

### Desafio 4: Filtrar por Categoria desde API

Modifica HomeScreen para que al seleccionar una categoria, use el endpoint:

```
GET /products/category/electronics
```

En lugar de filtrar localmente.

---

## Parte 11: Preguntas de Autoevaluacion

Responde estas preguntas para verificar tu comprension:

1. ¿Que es un DTO y por que lo usamos?

2. ¿Cual es la diferencia entre usar `Response<T>` vs `T` directamente?

3. ¿Por que todas las funciones de la API tienen la palabra `suspend`?

4. ¿Que ventaja tiene usar `object` para RetrofitClient?

5. ¿Por que es importante manejar excepciones en las llamadas a la API?

6. Explica el flujo completo: Usuario abre pantalla → Se muestran productos

7. ¿Que pasa si el servidor devuelve un JSON diferente al esperado?

8. ¿Por que mantenemos Room si ahora usamos Retrofit?

---

## Parte 12: Errores Comunes y Soluciones

### Error 1: Unable to resolve host

**Causa:** No hay internet o URL incorrecta

**Solucion:**
- Verifica conexion a internet
- Revisa la URL_BASE en RetrofitClient
- Confirma que el permiso INTERNET esta en el manifest

### Error 2: Expected BEGIN_OBJECT but was BEGIN_ARRAY

**Causa:** El DTO no coincide con la estructura del JSON

**Solucion:**
- Revisa el JSON real en Logcat
- Ajusta el DTO para que coincida
- Verifica que `@SerializedName` este correcto

### Error 3: NetworkOnMainThreadException

**Causa:** Llamada HTTP en el hilo principal

**Solucion:**
- Asegurate de usar `suspend` en la funcion
- Llamar desde viewModelScope.launch { }

### Error 4: La app crashea al abrir

**Causa:** Probablemente error en el mapeo DTO → Model

**Solucion:**
- Revisa Logcat para ver el error exacto
- Verifica que todos los campos requeridos existan
- Usa valores por defecto para campos opcionales


---

### Documentacion Oficial
- Retrofit: https://square.github.io/retrofit/
- FakeStoreAPI: https://fakestoreapi.com/docs


### Herramientas Utiles
- Postman: Para probar APIs antes de codificar
- JSON Viewer: Para visualizar respuestas JSON



Si tienes problemas:

1. Revisa Logcat primero
2. Verifica que seguiste todos los pasos
3. Consulta la seccion de errores comunes
4. Pregunta al profesor con el mensaje de error especifico

---

## Conclusion

- Consumir APIs REST en Android
- Integrar Retrofit con MVVM
- Manejar errores de red




**Proximo paso:** Aprende sobre autenticacion con tokens.}

**AUTOR:  PROF. STING PARRA SILVA**
