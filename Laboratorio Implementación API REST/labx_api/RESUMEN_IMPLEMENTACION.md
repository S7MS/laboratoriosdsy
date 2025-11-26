# Resumen de Implementacion Retrofit - Proyecto LabX

**Profesor:** Sting Parra Silva
**Fecha:** Noviembre 2025
**Version:** 2.0

---

## Archivos Creados

### 1. Configuracion de Red

**RetrofitClient.kt**
- Ubicacion: `data/remote/RetrofitClient.kt`
- Proposito: Cliente HTTP singleton con Retrofit
- Configuracion: Timeouts, logging, converter JSON
- URL base: FakeStoreAPI (configurable)

### 2. Data Transfer Objects

**ProductoDto.kt**
- Ubicacion: `data/remote/dto/ProductoDto.kt`
- Proposito: Mapeo JSON ↔ objetos Kotlin
- Anotaciones: @SerializedName para nombres diferentes
- Extension functions: aModelo() y aDto()

### 3. Servicio API

**ProductoApiService.kt**
- Ubicacion: `data/remote/api/ProductoApiService.kt`
- Proposito: Define endpoints disponibles
- Operaciones: GET, POST, PUT, DELETE
- Verbos HTTP: @GET, @POST, @PUT, @DELETE

### 4. Manejo de Estados (opcional)

**ResultadoApi.kt**
- Ubicacion: `data/remote/ResultadoApi.kt`
- Proposito: Sealed class para estados de peticion
- Estados: Exito, Error, Cargando
- Extension functions: Helper functions

### 5. Datos Locales

**db.json**
- Ubicacion: raiz del proyecto
- Proposito: Base de datos para JSON Server local
- Contenido: 10 productos en español
- Uso: Pruebas locales sin internet

---

## Archivos Modificados

### 1. Dependencias

**app/build.gradle.kts**

Agregado:
```kotlin
// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson
implementation("com.google.code.gson:gson:2.10.1")
```

### 2. Repositorio

**ProductoRepositoryImpl.kt**

Cambios principales:
- Agregado parametro `apiService` al constructor
- Implementada estrategia de fallback API → Room
- Manejo completo de excepciones (IOException, UnknownHostException)
- Logging detallado con tag "ProductoRepository"
- Metodos actualizados: obtenerProductos(), insertarProducto(), etc

Estrategia:
```
1. Intentar API
2. Si falla → usar Room
3. Siempre funcional
```

### 3. Punto de Entrada

**MainActivity.kt**

Cambios principales:
- Importado RetrofitClient y ProductoApiService
- Creada instancia de apiService
- Repositorio inicializado con DAO + API Service
- Comentarios profesionales explicando cada paso

Orden de inicializacion:
```
1. Database (Room)
2. API Service (Retrofit)
3. Repositories (DAO + API)
4. ViewModels (Factory)
5. UI (Compose)
```

---

## Archivos Sin Cambios

Estos archivos NO requieren modificacion:

- ProductoViewModel.kt (ya estaba correcto)
- ProductoDao.kt
- ProductoEntity.kt
- AppDatabase.kt
- HomeScreen.kt
- DetalleProductoScreen.kt
- Todas las demas pantallas Compose

**Razon:** La arquitectura MVVM permite cambiar la capa de datos (Repository) sin afectar ViewModels ni UI.

---

## Estructura Final del Proyecto

```
labx/
├── app/
│   ├── build.gradle.kts                     [MODIFICADO]
│   └── src/main/java/com/example/labx/
│       ├── data/
│       │   ├── local/                       [sin cambios]
│       │   │   ├── dao/
│       │   │   ├── entity/
│       │   │   ├── AppDatabase.kt
│       │   │   ├── PreferenciasManager.kt
│       │   │   └── ProductoInicializador.kt
│       │   ├── remote/                      [NUEVO PAQUETE]
│       │   │   ├── api/
│       │   │   │   └── ProductoApiService.kt    [NUEVO]
│       │   │   ├── dto/
│       │   │   │   └── ProductoDto.kt           [NUEVO]
│       │   │   ├── RetrofitClient.kt            [NUEVO]
│       │   │   └── ResultadoApi.kt              [NUEVO]
│       │   └── repository/
│       │       ├── ProductoRepositoryImpl.kt    [MODIFICADO]
│       │       └── CarritoRepository.kt         [sin cambios]
│       ├── domain/                          [sin cambios]
│       ├── ui/                              [sin cambios]
│       └── MainActivity.kt                  [MODIFICADO]
├── db.json                                  [NUEVO]
├── PASO_A_PASO_IMPLEMENTACION_RETROFIT.md   [NUEVO]
└── RESUMEN_IMPLEMENTACION.md                [NUEVO]
```

---

## Como Usar la Implementacion

### Opcion 1: Usar FakeStoreAPI (Por defecto)

**No requiere configuracion adicional.**

1. Ejecutar la app
2. Debe cargar 20 productos de FakeStoreAPI
3. Revisar Logcat: "✓ Productos obtenidos de API"

**Ventajas:**
- Facil de usar
- No requiere instalacion
- Siempre disponible

**Desventajas:**
- Datos en ingles
- CRUD simulado (no persiste)

### Opcion 2: Usar JSON Server Local

**Requiere Node.js instalado.**

**Paso 1: Instalar JSON Server**
```bash
npm install -g json-server
```

**Paso 2: Iniciar servidor**
```bash
cd "ruta/del/proyecto/labx"
json-server --watch db.json --host 0.0.0.0 --port 3000
```

**Paso 3: Cambiar URL en RetrofitClient.kt**

Para emulador:
```kotlin
private const val URL_BASE = "http://10.0.2.2:3000/"
```

Para dispositivo fisico:
```kotlin
private const val URL_BASE = "http://[IP_DE_TU_PC]:3000/"
```

**Paso 4: Ejecutar la app**

**Ventajas:**
- Datos en español
- CRUD real (persiste cambios)
- Control total

**Desventajas:**
- Requiere JSON Server corriendo
- Configuracion adicional

---

## Funcionamiento de la Estrategia Hibrida

### Escenario 1: Con Internet

```
Usuario abre app
    ↓
ViewModel llama Repository
    ↓
Repository intenta API
    ↓
API responde OK (200)
    ↓
Mapea ProductoDto → Producto
    ↓
Emite lista al ViewModel
    ↓
UI muestra productos de API
```

**Resultado:** Datos actualizados de la API

### Escenario 2: Sin Internet

```
Usuario abre app (sin WiFi/datos)
    ↓
ViewModel llama Repository
    ↓
Repository intenta API
    ↓
IOException (sin internet)
    ↓
Catch exception
    ↓
Llama usarDatosLocales()
    ↓
Obtiene de Room (cache)
    ↓
Emite lista al ViewModel
    ↓
UI muestra productos locales
```

**Resultado:** Datos del cache, app funcional

### Escenario 3: Error del Servidor

```
Usuario abre app
    ↓
ViewModel llama Repository
    ↓
Repository intenta API
    ↓
API responde 500 (error servidor)
    ↓
respuesta.isSuccessful == false
    ↓
Llama usarDatosLocales()
    ↓
Obtiene de Room
    ↓
UI muestra productos locales
```

**Resultado:** Fallback automatico, usuario no nota el error

---

## Logging y Depuracion

### Ver Logs en Android Studio

**Abrir Logcat:**
View → Tool Windows → Logcat

**Filtrar por:**
```
tag:ProductoRepository | tag:OkHttp
```

### Logs Importantes

**Exito API:**
```
D/ProductoRepository: Intentando obtener productos desde API REST...
D/OkHttp: --> GET https://fakestoreapi.com/products
D/OkHttp: <-- 200 OK (245ms)
D/ProductoRepository: ✓ Productos obtenidos de API: 20 items
```

**Fallback a Local:**
```
D/ProductoRepository: Intentando obtener productos desde API REST...
E/ProductoRepository: ✗ Sin conexion a internet, usando datos locales
D/ProductoRepository: ✓ Productos de cache local: 8 items
```

**Error HTTP:**
```
D/ProductoRepository: Intentando obtener productos desde API REST...
W/ProductoRepository: ⚠ Error HTTP 500, usando datos locales
D/ProductoRepository: ✓ Productos de cache local: 8 items
```

---

## Ventajas de Esta Implementacion

### 1. Arquitectura Limpia

- **Separacion de capas:** UI no conoce detalles de red
- **Repository Pattern:** Facil cambiar fuente de datos
- **ViewModel sin cambios:** Solo el Repository fue modificado

### 2. Experiencia de Usuario

- **Siempre funcional:** App nunca falla completamente
- **Datos actualizados:** Usa API cuando esta disponible
- **Modo offline:** Funciona sin internet usando cache

### 3. Mantenibilidad

- **Codigo comentado:** Facil de entender y modificar
- **Logs detallados:** Depuracion simplificada
- **Extension functions:** Codigo reutilizable

### 4. Escalabilidad

- **Facil agregar endpoints:** Solo modificar ProductoApiService
- **Cambiar API:** Solo modificar URL_BASE
- **Agregar cache:** Descomentar lineas en Repository

---

## Proximos Pasos Sugeridos

### Para Alumnos (Ejercicios)

1. **Basico:** Cambiar a JSON Server local
2. **Intermedio:** Implementar POST/PUT/DELETE en UI
3. **Avanzado:** Agregar paginacion con limit y offset
4. **Desafio:** Implementar cache inteligente con timestamp

### Para Produccion (Mejoras)

1. **Autenticacion:** Agregar JWT tokens
2. **Cache persistente:** Guardar respuesta API en Room
3. **Refresh manual:** Pull to refresh en UI
4. **Indicadores:** Mostrar si datos son de API o cache
5. **Analytics:** Registrar exitos/fallos de peticiones

---

## Preguntas Frecuentes

### ¿Por que usar Response<T> en lugar de T directamente?

**Response<T>:**
- Acceso al codigo HTTP (200, 404, 500)
- Manejo de errores mas robusto
- Headers disponibles

**T directamente:**
- Mas simple
- Pero cualquier error crashea la app

### ¿Por que todas las funciones son suspend?

- Operaciones de red son asincronas
- suspend permite ejecutar sin bloquear UI
- Compatible con Kotlin Coroutines

### ¿Puedo usar otra API?

**Si, solo cambiar:**
1. URL_BASE en RetrofitClient
2. Ajustar ProductoDto segun estructura JSON
3. Modificar endpoints en ProductoApiService

### ¿Como agregar headers (autenticacion)?

**Opcion 1: Interceptor**
```kotlin
private val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    chain.proceed(request)
}
```

**Opcion 2: En cada endpoint**
```kotlin
@GET("products")
suspend fun obtenerProductos(
    @Header("Authorization") token: String
): Response<List<ProductoDto>>
```

### ¿Funciona con GraphQL?

- Retrofit es para REST
- Para GraphQL usar Apollo Client
- Arquitectura similar (DTO, Repository, etc)

---

## Creditos

**Implementacion:** Sting Parra Silva
**Arquitectura:** MVVM + Repository Pattern
**Libreria principal:** Retrofit 2.9.0
**Parser JSON:** Gson 2.10.1
**Cliente HTTP:** OkHttp 4.12.0

---

## Licencia

Este proyecto es material educativo para uso academico.

---

**Fin del Resumen**

Para documentacion completa, consultar:
- PASO_A_PASO_IMPLEMENTACION_RETROFIT.md
- Comentarios en el codigo fuente
