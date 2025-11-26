# Paso a Paso: Implementacion Completa de Retrofit en Android MVVM

**Profesor:** Sting Parra Silva
**Arquitectura:** MVVM + Repository Pattern + Retrofit
**Objetivo:** Consumir API REST manteniendo funcionamiento offline

---

## Tabla de Contenidos

1. [Introduccion](#1-introduccion)
2. [Configuracion de Dependencias](#2-configuracion-de-dependencias)
3. [Estructura de Carpetas](#3-estructura-de-carpetas)
4. [Implementacion de RetrofitClient](#4-implementacion-de-retrofitclient)
5. [Creacion de DTOs](#5-creacion-de-dtos)
6. [Definicion del Servicio API](#6-definicion-del-servicio-api)
7. [Modificacion del Repositorio](#7-modificacion-del-repositorio)
8. [Actualizacion de MainActivity](#8-actualizacion-de-mainactivity)
9. [Pruebas y Verificacion](#9-pruebas-y-verificacion)
10. [Configuracion de JSON Server Local](#10-configuracion-de-json-server-local)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Introduccion

### Que vamos a lograr

Al finalizar esta implementacion, tu aplicacion Android:

- Consumira datos desde una API REST usando Retrofit
- Funcionara sin internet usando cache local (Room)
- Implementara estrategia de fallback automatico
- Mantendra la arquitectura MVVM limpia y separada
- Tendra logging detallado para depuracion

### Arquitectura Hibrida

```
┌─────────────────┐
│   UI (Compose)  │
└────────┬────────┘
         │
┌────────▼────────┐
│   ViewModel     │
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │◄──── AQUI implementamos Retrofit
└────┬───────┬────┘
     │       │
┌────▼───┐ ┌▼────────┐
│  Room  │ │ Retrofit│
│ (Local)│ │ (API)   │
└────────┘ └─────────┘
```

**Estrategia:**
- Fuente primaria: API REST (datos actualizados)
- Fuente secundaria: Room (cache offline)
- Si falla API → usa Room automaticamente

---

## 2. Configuracion de Dependencias

### Paso 2.1: Abrir build.gradle.kts

Navega a: `app/build.gradle.kts`

### Paso 2.2: Agregar Dependencias

Busca el bloque `dependencies` y agrega al final (antes de `testImplementation`):

```kotlin
// Retrofit para consumo de API REST
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp para logging de peticiones HTTP
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson para parseo de JSON
implementation("com.google.code.gson:gson:2.10.1")
```

**Que hace cada dependencia:**

- `retrofit`: Libreria principal para peticiones HTTP
- `converter-gson`: Convierte JSON ↔ objetos Kotlin
- `okhttp`: Cliente HTTP usado por Retrofit
- `logging-interceptor`: Muestra peticiones en Logcat (debugging)
- `gson`: Parser JSON usado por el converter

### Paso 2.3: Sincronizar Proyecto

Click en "Sync Now" que aparece en la parte superior.

**Tiempo estimado:** 1-2 minutos

Si aparecen errores de sincronizacion:
- Verifica tu conexion a internet
- File → Invalidate Caches / Restart
- Build → Clean Project, luego Build → Rebuild Project

---

## 3. Estructura de Carpetas

### Paso 3.1: Navegar a la carpeta data

Ubicacion: `app/src/main/java/com/example/labx/data/`

### Paso 3.2: Crear estructura remote

Necesitamos crear esta estructura:

```
data/
├── local/          (ya existe - Room)
├── remote/         (NUEVO)
│   ├── api/        (NUEVO)
│   ├── dto/        (NUEVO)
│   └── RetrofitClient.kt
└── repository/     (ya existe)
```

**Como crear las carpetas:**

1. Click derecho en `data` → New → Package
2. Escribir: `remote` → Enter
3. Click derecho en `remote` → New → Package
4. Escribir: `api` → Enter
5. Click derecho en `remote` → New → Package
6. Escribir: `dto` → Enter

**Resultado esperado:**
- `com.example.labx.data.remote`
- `com.example.labx.data.remote.api`
- `com.example.labx.data.remote.dto`

---

## 4. Implementacion de RetrofitClient

### Paso 4.1: Crear archivo RetrofitClient.kt

**Ubicacion:** `data/remote/RetrofitClient.kt`

**Como crear:**
1. Click derecho en `remote` → New → Kotlin Class/File
2. Seleccionar: Object
3. Nombre: `RetrofitClient`

### Paso 4.2: Copiar el codigo completo

```kotlin
package com.example.labx.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit configurado como Singleton
 * @author Sting Parra Silva
 */
object RetrofitClient {

    // URL base de la API
    private const val URL_BASE = "https://fakestoreapi.com/"

    // Interceptor para ver peticiones en Logcat
    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con timeouts
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(interceptorLog)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia de Retrofit (se crea solo una vez)
    val instancia: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Funcion para crear servicios
    fun <T> crearServicio(servicioClase: Class<T>): T {
        return instancia.create(servicioClase)
    }
}
```

### Paso 4.3: Entender el codigo

**Patron Singleton (object):**
- Solo existe una instancia en toda la app
- Ahorra recursos (conexiones HTTP)
- Reutilizable desde cualquier lugar

**URL_BASE:**
- Define el servidor API
- Opciones: `https://fakestoreapi.com/`, `http://10.0.2.2:3000/` (local)
- Cambiar aqui cambia para toda la app

**Timeouts:**
- `connectTimeout`: 30s para conectar
- `readTimeout`: 30s para recibir respuesta
- `writeTimeout`: 30s para enviar datos
- Evita esperas infinitas

**Logging:**
- `BODY`: Muestra todo (peticion completa y respuesta)
- Ver en Logcat filtrando por "OkHttp"
- Deshabilitar en produccion (privacidad)

### Paso 4.4: Verificar imports

Android Studio deberia resolver automaticamente los imports. Si no:

```kotlin
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
```

---

## 5. Creacion de DTOs

### Paso 5.1: Entender que es un DTO

**DTO = Data Transfer Object**

- Representa la estructura del JSON de la API
- Diferente del modelo de dominio interno
- Permite mapear nombres JSON → nombres Kotlin

**Ejemplo:**

API envia:
```json
{
  "id": 1,
  "title": "Product Name",
  "price": 99.99
}
```

Nosotros usamos internamente:
```kotlin
Producto(
    identificador = 1,
    nombre = "Product Name",
    precio = 99.99
)
```

### Paso 5.2: Crear ProductoDto.kt

**Ubicacion:** `data/remote/dto/ProductoDto.kt`

**Como crear:**
1. Click derecho en `dto` → New → Kotlin Class/File
2. Seleccionar: Class
3. Nombre: `ProductoDto`

### Paso 5.3: Implementar el DTO

```kotlin
package com.example.labx.data.remote.dto

import com.example.labx.domain.model.Producto
import com.google.gson.annotations.SerializedName

/**
 * DTO para mapear JSON de la API a objetos Kotlin
 * @author Sting Parra Silva
 */
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

// Extension function: DTO → Modelo de dominio
fun ProductoDto.aModelo(): Producto {
    return Producto(
        id = this.identificador,
        nombre = this.titulo,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.urlImagen,
        categoria = this.categoria,
        stock = 10  // Valor por defecto (API no lo provee)
    )
}

// Extension function: Modelo de dominio → DTO
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

### Paso 5.4: Explicacion de @SerializedName

**Problema:**
- API usa nombres en ingles: "title", "price"
- Nuestra app usa nombres en español: "titulo", "precio"

**Solucion:**
```kotlin
@SerializedName("title")  // Nombre en JSON
val titulo: String         // Nombre en Kotlin
```

Gson automaticamente mapea `"title"` del JSON a `titulo` en Kotlin.

**Sin @SerializedName:**
- Los nombres deben ser EXACTAMENTE iguales
- `val title: String` funcionaria
- `val titulo: String` fallaria

### Paso 5.5: Extension Functions

Las funciones `aModelo()` y `aDto()` son extension functions:

```kotlin
// Uso:
val dto = ProductoDto(...)
val modelo = dto.aModelo()  // Convierte DTO → Producto

val producto = Producto(...)
val dtoNuevo = producto.aDto()  // Convierte Producto → DTO
```

**Ventajas:**
- Sintaxis limpia y legible
- Separacion de responsabilidades
- Reutilizable en toda la app

---

## 6. Definicion del Servicio API

### Paso 6.1: Crear ProductoApiService.kt

**Ubicacion:** `data/remote/api/ProductoApiService.kt`

**Como crear:**
1. Click derecho en `api` → New → Kotlin Class/File
2. Seleccionar: Interface
3. Nombre: `ProductoApiService`

### Paso 6.2: Implementar la interface

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

/**
 * Interface del servicio API - Define endpoints disponibles
 * @author Sting Parra Silva
 */
interface ProductoApiService {

    // GET: Obtener todos los productos
    @GET("products")
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoDto>>

    // GET: Obtener producto por ID
    @GET("products/{id}")
    suspend fun obtenerProductoPorId(
        @Path("id") identificador: Int
    ): Response<ProductoDto>

    // GET: Obtener productos por categoria
    @GET("products/category/{categoria}")
    suspend fun obtenerProductosPorCategoria(
        @Path("categoria") nombreCategoria: String
    ): Response<List<ProductoDto>>

    // POST: Crear nuevo producto
    @POST("products")
    suspend fun agregarProducto(
        @Body nuevoProducto: ProductoDto
    ): Response<ProductoDto>

    // PUT: Actualizar producto existente
    @PUT("products/{id}")
    suspend fun modificarProducto(
        @Path("id") identificador: Int,
        @Body productoActualizado: ProductoDto
    ): Response<ProductoDto>

    // DELETE: Eliminar producto
    @DELETE("products/{id}")
    suspend fun borrarProducto(
        @Path("id") identificador: Int
    ): Response<Unit>
}
```

### Paso 6.3: Entender las anotaciones HTTP

**@GET("products")**
- Verbo HTTP: GET
- Endpoint: `/products`
- URL completa: `https://fakestoreapi.com/products`
- Proposito: Listar recursos

**@GET("products/{id}")**
- `{id}` es un placeholder
- Se reemplaza con el valor del parametro `@Path("id")`
- Ejemplo: `identificador = 5` → `/products/5`

**@POST("products")**
- Verbo HTTP: POST
- Crea un nuevo recurso
- Envia datos en el body usando `@Body`

**@PUT("products/{id}")**
- Verbo HTTP: PUT
- Actualiza un recurso existente
- Reemplaza TODOS los campos

**@DELETE("products/{id}")**
- Verbo HTTP: DELETE
- Elimina un recurso
- Response<Unit> porque no retorna datos

### Paso 6.4: Entender suspend y Response

**suspend:**
- Palabra clave de Kotlin Coroutines
- Permite ejecutar codigo asincrono sin bloquear UI
- DEBE llamarse desde otra funcion suspend o desde viewModelScope

**Response<T>:**
- Envuelve la respuesta HTTP completa
- Propiedades importantes:
  - `isSuccessful`: true si codigo 200-299
  - `body()`: datos de la respuesta (puede ser null)
  - `code()`: codigo HTTP (200, 404, 500, etc)
  - `message()`: mensaje de estado HTTP

**Uso:**
```kotlin
val respuesta = apiService.obtenerTodosLosProductos()

if (respuesta.isSuccessful) {
    val productos = respuesta.body()  // List<ProductoDto>?
    // Usar productos
} else {
    val codigoError = respuesta.code()  // 404, 500, etc
    // Manejar error
}
```

---

## 7. Modificacion del Repositorio

### Paso 7.1: Localizar ProductoRepositoryImpl.kt

**Ubicacion:** `data/repository/ProductoRepositoryImpl.kt`

### Paso 7.2: Actualizar constructor

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
    private val apiService: ProductoApiService  // NUEVO
) : RepositorioProductos {
```

### Paso 7.3: Agregar imports necesarios

```kotlin
import android.util.Log
import com.example.labx.data.remote.api.ProductoApiService
import com.example.labx.data.remote.dto.aDto
import com.example.labx.data.remote.dto.aModelo
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.UnknownHostException
```

### Paso 7.4: Agregar companion object para logging

Al inicio de la clase (despues del constructor):

```kotlin
companion object {
    private const val TAG = "ProductoRepository"
}
```

### Paso 7.5: Reemplazar obtenerProductos()

**Codigo completo con estrategia de fallback:**

```kotlin
override fun obtenerProductos(): Flow<List<Producto>> = flow {
    try {
        Log.d(TAG, "Intentando obtener productos desde API REST...")

        // Hacer peticion a la API
        val respuesta = apiService.obtenerTodosLosProductos()

        // Verificar si fue exitosa
        if (respuesta.isSuccessful) {
            val cuerpoRespuesta = respuesta.body()

            if (cuerpoRespuesta != null) {
                // Mapear DTOs a modelos de dominio
                val listaProductos = cuerpoRespuesta.map { it.aModelo() }

                Log.d(TAG, "✓ Productos obtenidos de API: ${listaProductos.size} items")
                emit(listaProductos)

            } else {
                // Respuesta exitosa pero sin datos
                Log.w(TAG, "⚠ Respuesta vacía, usando datos locales")
                usarDatosLocales(this)
            }

        } else {
            // Error HTTP (4xx, 5xx)
            Log.w(TAG, "⚠ Error HTTP ${respuesta.code()}, usando datos locales")
            usarDatosLocales(this)
        }

    } catch (excepcion: UnknownHostException) {
        // Sin internet o host invalido
        Log.e(TAG, "✗ Sin conexion a internet, usando datos locales")
        usarDatosLocales(this)

    } catch (excepcion: IOException) {
        // Error de red (timeout, etc)
        Log.e(TAG, "✗ Error de red, usando datos locales")
        usarDatosLocales(this)

    } catch (excepcion: Exception) {
        // Error inesperado
        Log.e(TAG, "✗ Error inesperado: ${excepcion.message}")
        usarDatosLocales(this)
    }
}
```

### Paso 7.6: Agregar funcion auxiliar usarDatosLocales()

Despues de `obtenerProductos()`:

```kotlin
private suspend fun usarDatosLocales(
    flowCollector: kotlinx.coroutines.flow.FlowCollector<List<Producto>>
) {
    productoDao.obtenerTodosLosProductos().collect { listaEntidades ->
        val productosLocales = listaEntidades.map { it.toProducto() }

        if (productosLocales.isEmpty()) {
            Log.w(TAG, "Base de datos local está vacía")
        } else {
            Log.d(TAG, "✓ Productos de cache local: ${productosLocales.size} items")
        }

        flowCollector.emit(productosLocales)
    }
}
```

### Paso 7.7: Actualizar insertarProducto()

**Codigo completo:**

```kotlin
override suspend fun insertarProducto(producto: Producto): Long {
    return try {
        Log.d(TAG, "Creando producto: ${producto.nombre} en API...")

        val productoDto = producto.aDto()
        val respuesta = apiService.agregarProducto(productoDto)

        if (respuesta.isSuccessful) {
            Log.d(TAG, "✓ Producto creado en API")
            val idLocal = productoDao.insertarProducto(producto.toEntity())
            Log.d(TAG, "✓ Producto guardado localmente con ID: $idLocal")
            idLocal
        } else {
            Log.w(TAG, "⚠ Error en API, guardando solo localmente")
            productoDao.insertarProducto(producto.toEntity())
        }

    } catch (excepcion: Exception) {
        Log.e(TAG, "✗ Error de red, guardando solo localmente")
        productoDao.insertarProducto(producto.toEntity())
    }
}
```

### Paso 7.8: Entender la estrategia de fallback

```
┌─────────────────────────────────┐
│ 1. Intentar peticion a la API   │
└─────────────┬───────────────────┘
              │
              ▼
      ┌───────────────┐
      │ ¿API exitosa? │
      └───┬───────┬───┘
          │       │
       SI │       │ NO
          │       │
          ▼       ▼
    ┌─────────┐ ┌────────────────┐
    │Usar API │ │Usar Room(cache)│
    └─────────┘ └────────────────┘
```

**Casos manejados:**
1. API exitosa → Usa datos de API
2. Sin internet → Usa cache Room
3. Error 404/500 → Usa cache Room
4. Timeout → Usa cache Room
5. JSON invalido → Usa cache Room

**Resultado:** La app SIEMPRE tiene datos, nunca pantalla en blanco.

---

## 8. Actualizacion de MainActivity

### Paso 8.1: Abrir MainActivity.kt

**Ubicacion:** `MainActivity.kt` (raiz del paquete)

### Paso 8.2: Agregar imports

```kotlin
import com.example.labx.data.remote.RetrofitClient
import com.example.labx.data.remote.api.ProductoApiService
```

### Paso 8.3: Modificar onCreate()

**Buscar esta linea:**
```kotlin
val productoRepository = ProductoRepositoryImpl(database.productoDao())
```

**Reemplazar con:**
```kotlin
// Crear instancia del servicio API
val apiService: ProductoApiService = RetrofitClient.crearServicio(ProductoApiService::class.java)

// Crear repositorio con API y DAO
val productoRepository = ProductoRepositoryImpl(
    productoDao = database.productoDao(),
    apiService = apiService
)
```

### Paso 8.4: Verificar el orden

El orden correcto en `onCreate()` debe ser:

```kotlin
// 1. Base de datos
val database = AppDatabase.getDatabase(applicationContext)

// 2. Inicializar datos locales
ProductoInicializador.inicializarProductos(applicationContext)

// 3. Servicio API
val apiService = RetrofitClient.crearServicio(ProductoApiService::class.java)

// 4. Repositorios
val productoRepository = ProductoRepositoryImpl(database.productoDao(), apiService)
val carritoRepository = CarritoRepository(database.carritoDao())

// 5. Preferencias
val preferenciasManager = PreferenciasManager(applicationContext)

// 6. UI
setContent { ... }
```

### Paso 8.5: Explicacion del flujo

**Por que este orden:**

1. **Base de datos primero:** Necesaria para fallback
2. **Datos locales:** Garantiza funcionamiento sin API
3. **API Service:** Configuracion de Retrofit
4. **Repositorios:** Conectan API + DB
5. **UI:** Usa repositorios a traves de ViewModels

**Inyeccion de dependencias:**
- MainActivity crea las dependencias
- Las pasa a los repositorios
- Repositorios las pasan a ViewModels via Factory
- ViewModels las usan para obtener datos

---

## 9. Pruebas y Verificacion

### Paso 9.1: Compilar el proyecto

Build → Clean Project
Build → Rebuild Project

**Verificar:**
- Sin errores de compilacion
- Todas las dependencias resueltas

### Paso 9.2: Ejecutar en emulador/dispositivo

Run → Run 'app' (o Shift + F10)

### Paso 9.3: Abrir Logcat

View → Tool Windows → Logcat

**Configurar filtro:**
```
tag:ProductoRepository | tag:OkHttp
```

### Paso 9.4: Logs esperados CON INTERNET

```
D/ProductoRepository: Intentando obtener productos desde API REST...
D/OkHttp: --> GET https://fakestoreapi.com/products
D/OkHttp: <-- 200 OK (245ms, 5.2kB)
D/ProductoRepository: ✓ Productos obtenidos de API: 20 items
```

### Paso 9.5: Logs esperados SIN INTERNET

```
D/ProductoRepository: Intentando obtener productos desde API REST...
E/ProductoRepository: ✗ Sin conexion a internet, usando datos locales
D/ProductoRepository: ✓ Productos de cache local: 8 items
```

### Paso 9.6: Prueba funcional completa

**Test 1: Con internet**
1. Abrir app
2. Ver productos (deben ser de FakeStoreAPI)
3. Revisar Logcat: debe decir "✓ Productos obtenidos de API"

**Test 2: Sin internet**
1. Activar modo avion
2. Cerrar app (swipe en recientes)
3. Abrir app nuevamente
4. Ver productos (deben ser los 8 locales)
5. Revisar Logcat: debe decir "✗ Sin conexion" y "✓ cache local"

**Test 3: Recuperacion**
1. Desactivar modo avion
2. Cerrar app
3. Abrir app
4. Productos de API deben volver

### Paso 9.7: Verificar respuesta HTTP en Logcat

Buscar en Logcat:

```json
{
  "id": 1,
  "title": "Fjallraven - Foldsack No. 1 Backpack...",
  "price": 109.95,
  "description": "Your perfect pack...",
  "category": "men's clothing",
  "image": "https://fakestoreapi.com/img/..."
}
```

Esto confirma que Retrofit esta recibiendo y parseando JSON correctamente.

---

## 10. Configuracion de JSON Server Local

### Paso 10.1: Instalar Node.js

1. Descargar desde: https://nodejs.org/
2. Instalar version LTS
3. Verificar instalacion:
   ```bash
   node --version
   npm --version
   ```

### Paso 10.2: Instalar JSON Server

```bash
npm install -g json-server
```

Verificar:
```bash
json-server --version
```

### Paso 10.3: Usar archivo db.json incluido

El archivo `db.json` ya esta creado en la raiz del proyecto con 10 productos.

### Paso 10.4: Iniciar JSON Server

**Desde la carpeta del proyecto:**
```bash
cd "c:\Users\Sting\Documents\DRUMS\MVVM\GUIAS_PROFESOR\labx (3)\labx"
json-server --watch db.json --host 0.0.0.0 --port 3000
```

**Salida esperada:**
```
\{^_^}/ hi!

Loading db.json
Done

Resources
http://localhost:3000/products
http://localhost:3000/comentarios
http://localhost:3000/categorias

Home
http://localhost:3000
```

### Paso 10.5: Probar JSON Server

Abrir en navegador:
- http://localhost:3000/products
- http://localhost:3000/products/1

Deberia mostrar JSON de productos.

### Paso 10.6: Cambiar URL en RetrofitClient

**Para emulador Android:**
```kotlin
private const val URL_BASE = "http://10.0.2.2:3000/"
```

**Para dispositivo fisico:**
1. Obtener IP de tu PC:
   - Windows: `ipconfig` → IPv4 Address
   - Ejemplo: `192.168.1.100`
2. Usar en RetrofitClient:
   ```kotlin
   private const val URL_BASE = "http://192.168.1.100:3000/"
   ```

**Importante:**
- PC y dispositivo deben estar en la misma red WiFi
- Firewall puede bloquear: agregar excepcion para puerto 3000
- JSON Server debe estar corriendo mientras pruebas

### Paso 10.7: Ventajas de JSON Server vs FakeStoreAPI

**JSON Server (Local):**
- Datos personalizados (en español)
- CRUD real (cambios persisten)
- Sin limite de peticiones
- Funciona sin internet externo
- Total control

**FakeStoreAPI:**
- Mas facil de usar (no requiere instalar nada)
- Siempre disponible
- CRUD simulado (no persiste)
- Datos en ingles

**Recomendacion:** Empezar con FakeStoreAPI, luego migrar a JSON Server.

---

## 11. Troubleshooting

### Error 1: Unable to resolve host

**Sintoma:**
```
E/ProductoRepository: ✗ Sin conexion a internet
```

**Causas posibles:**
1. Sin internet real
2. URL_BASE incorrecta
3. Permiso INTERNET falta en AndroidManifest.xml

**Solucion:**
- Verificar conexion a internet
- Revisar URL_BASE en RetrofitClient.kt
- Verificar `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```

### Error 2: Expected BEGIN_OBJECT but was BEGIN_ARRAY

**Sintoma:**
```
com.google.gson.JsonSyntaxException: Expected BEGIN_OBJECT but was BEGIN_ARRAY
```

**Causa:** El DTO no coincide con la estructura JSON

**Solucion:**
- Revisar el JSON real en Logcat (buscar OkHttp)
- Ajustar ProductoDto para que coincida
- Ejemplo:
  - API retorna lista: `Response<List<ProductoDto>>`
  - API retorna objeto: `Response<ProductoDto>`

### Error 3: NetworkOnMainThreadException

**Sintoma:**
```
android.os.NetworkOnMainThreadException
```

**Causa:** Llamada HTTP en hilo principal (sin coroutine)

**Solucion:**
- Asegurate que la funcion sea `suspend`
- Llamar desde `viewModelScope.launch { }`
- NUNCA llamar API fuera de coroutines

### Error 4: 10.0.2.2 no funciona en dispositivo fisico

**Sintoma:** App no carga datos cuando usas JSON Server local

**Causa:** 10.0.2.2 es IP especial solo para EMULADOR

**Solucion dispositivo fisico:**
1. Obtener IP real de tu PC: `ipconfig`
2. Cambiar URL_BASE:
   ```kotlin
   private const val URL_BASE = "http://[TU_IP]:3000/"
   ```
3. Asegurar PC y dispositivo en misma red WiFi

### Error 5: La app crashea al abrir

**Sintoma:** App se cierra inmediatamente

**Pasos de depuracion:**
1. Revisar Logcat completo
2. Buscar "FATAL EXCEPTION"
3. Leer el stacktrace (pila de llamadas)

**Errores comunes:**
- `ClassCastException`: Error en mapeo DTO → Model
- `NullPointerException`: Campo null no esperado
- `UninitializedPropertyAccessException`: Variable no inicializada

**Solucion general:**
- Leer el error completo
- Ir a la linea indicada
- Agregar validacion de null si es necesario

### Error 6: Productos se duplican

**Sintoma:** Al volver a la pantalla, los productos aparecen 2 veces

**Causa:** El Flow emite multiples veces

**Solucion:**
- Usar `.collectAsState()` en Compose (ya deberia estar asi)
- No llamar `cargarProductos()` multiples veces
- Revisar que el ViewModel se cree con `viewModel()` (no manualmente)

### Error 7: Retrofit no se encuentra (import rojo)

**Sintoma:**
```kotlin
import retrofit2.Retrofit  // Rojo/error
```

**Causa:** Dependencias no sincronizadas

**Solucion:**
1. File → Sync Project with Gradle Files
2. Si persiste: File → Invalidate Caches / Restart
3. Verificar `build.gradle.kts` tiene las dependencias correctas

### Error 8: Timeout al hacer peticion

**Sintoma:**
```
E/ProductoRepository: ✗ Error de red: timeout
```

**Causa:** Servidor muy lento o no responde

**Solucion:**
- Aumentar timeouts en RetrofitClient:
  ```kotlin
  .connectTimeout(60, TimeUnit.SECONDS)
  .readTimeout(60, TimeUnit.SECONDS)
  ```
- Verificar que el servidor esta corriendo (JSON Server)
- Probar la URL en navegador primero

---

## Resumen de Archivos Modificados/Creados

### Archivos CREADOS:
1. `data/remote/RetrofitClient.kt`
2. `data/remote/dto/ProductoDto.kt`
3. `data/remote/api/ProductoApiService.kt`
4. `data/remote/ResultadoApi.kt` (opcional, no usado todavia)
5. `db.json` (raiz del proyecto)

### Archivos MODIFICADOS:
1. `app/build.gradle.kts` (dependencias)
2. `data/repository/ProductoRepositoryImpl.kt` (constructor + logica)
3. `MainActivity.kt` (inicializacion)

### Archivos SIN CAMBIOS:
- ProductoViewModel.kt (ya estaba bien)
- ProductoDao.kt
- ProductoEntity.kt
- AppDatabase.kt
- Todas las pantallas Compose

---

## Proximos Pasos

### Mejoras Opcionales:

1. **Implementar cache inteligente:**
   - Guardar respuesta API en Room
   - Mostrar cache primero (rapido)
   - Actualizar desde API en background

2. **Agregar pantalla de error:**
   - Mostrar mensaje cuando falla API
   - Boton "Reintentar"
   - Indicador de modo offline

3. **Implementar paginacion:**
   - Cargar productos de 10 en 10
   - Scroll infinito
   - Mejor rendimiento

4. **Agregar autenticacion:**
   - Login con JWT tokens
   - Headers de autorizacion
   - Refresh tokens

5. **Optimizar imagenes:**
   - Coil ya esta configurado
   - Placeholder mientras carga
   - Manejo de errores de imagen

---

## Recursos Adicionales

### Documentacion Oficial:
- Retrofit: https://square.github.io/retrofit/
- Gson: https://github.com/google/gson
- Coroutines: https://developer.android.com/kotlin/coroutines

### APIs de Prueba:
- FakeStoreAPI: https://fakestoreapi.com/
- DummyJSON: https://dummyjson.com/
- JSONPlaceholder: https://jsonplaceholder.typicode.com/

### Herramientas:
- Postman: Probar APIs antes de codificar
- JSON Formatter: Visualizar JSON
- Logcat: Depuracion en Android Studio

---

**Fin del documento**

Para dudas o consultas:
Profesor: Sting Parra Silva
