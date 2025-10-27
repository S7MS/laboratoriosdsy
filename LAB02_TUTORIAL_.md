# Laboratorio 02: Navegación Multi-Pantalla con Navigation Compose

## Información General

**Tema:** Navegación entre pantallas con Navigation Component  
**Tiempo estimado:** 90 minutos  
**Nivel:** Intermedio

---

## Objetivos de Aprendizaje

En este laboratorio aprenderás a:
- Implementar Navigation Component para Compose
- Crear rutas de navegación con sealed classes
- Pasar argumentos entre pantallas
- Construir interfaces con múltiples vistas
- Navegar programáticamente entre destinos

---

## Configuración Inicial

### Paso 1: Agregar dependencia de Navigation

Abre el archivo `build.gradle.kts (Module: app)` y añade la siguiente dependencia en el bloque `dependencies`:

```kotlin
dependencies {
    // ... dependencias existentes ...
    
    implementation("androidx.navigation:navigation-compose:2.8.3")
}
```

Sincroniza el proyecto presionando **Sync Now** en la barra superior.

### Verificación de la instalación

Para confirmar que la dependencia se instaló correctamente:
1. Compila el proyecto: **Build** → **Make Project**
2. Verifica que no haya errores en el panel de Gradle

---

## Arquitectura del Proyecto

Crearás una aplicación con tres pantallas principales:

```
HomeView          ProfileView       SettingsView
┌─────────┐      ┌─────────┐      ┌─────────┐
│  Home   │  →   │ Profile │      │Settings │
│         │      │ Usuario │      │Ajustes  │
└─────────┘      └─────────┘      └─────────┘
     ↑                                  ↓
     └──────────────────────────────────┘
```

**Estructura de archivos:**
```
lab_02/app/src/main/java/com/example/lab02/
├── navigation/
│   └── AppRoutes.kt          (Definición de rutas)
├── views/
│   ├── HomeView.kt           (Pantalla principal)
│   ├── ProfileView.kt        (Perfil de usuario)
│   └── SettingsView.kt       (Ajustes)
└── MainActivity.kt            (Host de navegación)
```

---

## Parte 1: Definir Rutas de Navegación (15 minutos)

### Paso 1.1: Crear paquete `navigation`

1. Clic derecho en `com.example.lab02`
2. **New** → **Package**
3. Nombre: `navigation`

### Paso 1.2: Crear archivo `AppRoutes.kt`

Crea el archivo y añade el siguiente código:

```kotlin
package com.example.lab02.navigation

sealed class AppRoutes(val route: String) {
    data object Home : AppRoutes("home")
    data object Profile : AppRoutes("profile/{userName}") {
        fun createRoute(userName: String) = "profile/$userName"
    }
    data object Settings : AppRoutes("settings")
}
```

### Explicación del código

**Sealed Class para rutas:**
```kotlin
sealed class AppRoutes(val route: String)
```
- Las sealed classes permiten un conjunto cerrado de subclases
- Perfectas para representar destinos de navegación
- Type-safe: El compilador conoce todos los posibles destinos

**Rutas con argumentos:**
```kotlin
data object Profile : AppRoutes("profile/{userName}")
```
- `{userName}` es un placeholder para argumentos dinámicos
- La función `createRoute()` construye la ruta con valores reales
- Ejemplo: `Profile.createRoute("Juan")` → `"profile/Juan"`

**Data objects:**
- Representan instancias únicas (singleton)
- No necesitan ser instanciados
- Uso: `AppRoutes.Home` en lugar de `AppRoutes.Home()`

---

## Parte 2: Crear las Pantallas (30 minutos)

### Paso 2.1: Crear paquete `views`

1. Clic derecho en `com.example.lab02`
2. **New** → **Package**
3. Nombre: `views`

### Paso 2.2: Crear HomeView.kt

```kotlin
package com.example.lab02.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Pantalla Principal",
                style = MaterialTheme.typography.headlineMedium
            )
            
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { 
                    if (userName.isNotBlank()) {
                        onNavigateToProfile(userName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = userName.isNotBlank()
            ) {
                Text("Ir a Perfil")
            }
            
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a Ajustes")
            }
        }
    }
}
```

**Puntos clave:**

1. **Callbacks de navegación:** Los composables no navegan directamente, reciben lambdas
2. **TextField con estado:** `userName` se almacena con `remember`
3. **Validación:** El botón solo se activa si hay texto (`enabled = userName.isNotBlank()`)

### Paso 2.3: Crear ProfileView.kt

```kotlin
package com.example.lab02.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    userName: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = userName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información del Usuario", style = MaterialTheme.typography.titleMedium)
                    Divider()
                    Text("Usuario: $userName")
                    Text("Rol: Estudiante")
                    Text("Estado: Activo")
                }
            }
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Home")
            }
        }
    }
}
```

**Características destacadas:**

- **Recepción de argumentos:** `userName: String` viene de la navegación
- **Botón de retroceso:** IconButton con `Icons.Default.ArrowBack`
- **Card para información:** Componente Material 3 para agrupar datos

### Paso 2.4: Crear SettingsView.kt

```kotlin
package com.example.lab02.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificaciones")
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo Oscuro")
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Home")
            }
        }
    }
}
```

**Elementos de configuración:**

- **Switch components:** Para activar/desactivar opciones
- **Estado local:** Las preferencias se mantienen mientras la pantalla está activa
- **Spacer con weight:** Empuja el botón hacia abajo

---

## Parte 3: Configurar el NavHost (20 minutos)

### Paso 3.1: Modificar MainActivity.kt

Reemplaza todo el contenido con:

```kotlin
package com.example.lab02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab02.navigation.AppRoutes
import com.example.lab02.ui.theme.Lab02Theme
import com.example.lab02.views.HomeView
import com.example.lab02.views.ProfileView
import com.example.lab02.views.SettingsView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab02Theme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppRoutes.Home.route) {
                            HomeView(
                                onNavigateToProfile = { userName ->
                                    navController.navigate(
                                        AppRoutes.Profile.createRoute(userName)
                                    )
                                },
                                onNavigateToSettings = {
                                    navController.navigate(AppRoutes.Settings.route)
                                }
                            )
                        }
                        
                        composable(AppRoutes.Profile.route) { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: ""
                            ProfileView(
                                userName = userName,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(AppRoutes.Settings.route) {
                            SettingsView(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Explicación del NavHost

**1. NavController:**
```kotlin
val navController = rememberNavController()
```
- Controla la navegación entre destinos
- Mantiene el back stack (pila de navegación)
- Se crea una vez y se reutiliza

**2. NavHost:**
```kotlin
NavHost(
    navController = navController,
    startDestination = AppRoutes.Home.route
)
```
- Contenedor que muestra el destino actual
- `startDestination`: Primera pantalla al iniciar

**3. Navegación hacia adelante:**
```kotlin
navController.navigate(AppRoutes.Profile.createRoute(userName))
```
- Añade destino al back stack
- Pasa argumentos en la ruta

**4. Navegación hacia atrás:**
```kotlin
navController.popBackStack()
```
- Retira el destino actual del back stack
- Vuelve al destino anterior

**5. Extracción de argumentos:**
```kotlin
val userName = backStackEntry.arguments?.getString("userName") ?: ""
```
- `backStackEntry`: Entrada actual en el back stack
- `arguments`: Bundle con los argumentos de la ruta
- `?: ""`: Valor por defecto si no existe

---

## Parte 4: Probar la Aplicación (10 minutos)

### Lista de verificación

Ejecuta la app y prueba lo siguiente:

| Acción | Resultado Esperado |
|--------|-------------------|
| Abrir app | Muestra HomeView |
| Escribir nombre y presionar "Ir a Perfil" | Navega a ProfileView con el nombre |
| Presionar botón atrás del sistema | Vuelve a HomeView |
| Desde Home, ir a Ajustes | Muestra SettingsView |
| Activar/desactivar switches | Estado cambia visualmente |
| Presionar "Volver al Home" | Regresa a HomeView |
| Rotar dispositivo | Mantiene posición en la navegación |

### Flujo de navegación esperado

```
[Home] → (nombre: "Juan") → [Profile Juan] → (atrás) → [Home]
   ↓
[Settings] → (atrás) → [Home]
```

---

## Conceptos Avanzados

### 1. Back Stack

El back stack es una pila que mantiene el historial de navegación:

```
Inicial:           Después de navegar:      Después de pop:
┌────────┐        ┌────────┐              ┌────────┐
│  Home  │   →    │ Profile│      →       │  Home  │
└────────┘        ├────────┤              └────────┘
                  │  Home  │
                  └────────┘
```

### 2. Navegación con argumentos opcionales

Para argumentos opcionales, modifica la ruta:

```kotlin
data object Profile : AppRoutes("profile?userName={userName}") {
    fun createRoute(userName: String = "Invitado") = "profile?userName=$userName"
}
```

### 3. Deep Links

Puedes configurar deep links para abrir pantallas específicas desde fuera de la app:

```kotlin
composable(
    route = AppRoutes.Profile.route,
    deepLinks = listOf(navDeepLink { uriPattern = "myapp://profile/{userName}" })
) { ... }
```

---

## Ejercicios Adicionales

### Ejercicio 1: Pantalla de Detalle
Añade una cuarta pantalla "DetailView" accesible desde Settings.

### Ejercicio 2: Navegación condicional
Desde ProfileView, navega a Settings solo si el userName es "admin".

### Ejercicio 3: Argumentos múltiples
Modifica ProfileView para recibir `userName` y `userAge`.

**Pista:**
```kotlin
data object Profile : AppRoutes("profile/{userName}/{userAge}") {
    fun createRoute(userName: String, userAge: Int) = 
        "profile/$userName/$userAge"
}
```

---

## Solución de Problemas

### Error: "NavController should be passed to composable in order to navigate"

**Causa:** Intentas navegar sin tener acceso al NavController.

**Solución:** Pasa callbacks desde MainActivity donde tienes acceso al NavController.

### La app se cierra al presionar el botón atrás

**Causa:** El back stack está vacío.

**Solución:** Verifica que `startDestination` esté correctamente configurado.

### Los argumentos llegan como `null`

**Causa:** El nombre del parámetro en la ruta no coincide con el getString().

**Solución:** Asegúrate de que coincidan:
```kotlin
route = "profile/{userName}"  // Definición
arguments?.getString("userName")  // Extracción
```

---

## Recursos Adicionales

- [Navigation Compose Documentation](https://developer.android.com/jetpack/compose/navigation)
- [Passing data between destinations](https://developer.android.com/guide/navigation/navigation-pass-data)
- [Testing Navigation](https://developer.android.com/guide/navigation/navigation-testing)

---

## Siguiente Paso

Continúa con el **Laboratorio 03: Arquitectura MVVM y Validación de Formularios**, donde aprenderás:
- Patrón MVVM completo
- ViewModel con StateFlow
- Validaciones en tiempo real
- Gestión de estado complejo
