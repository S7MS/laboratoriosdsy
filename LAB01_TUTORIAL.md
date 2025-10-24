# Laboratorio 01: Aplicación Contador con Jetpack Compose

## Información General

**Tema:** Gestión de estado básico con `remember` y `mutableStateOf`  
**Tiempo estimado:** 40 minutos  
**Nivel:** Básico - Introducción a Compose

---

## Objetivos de Aprendizaje

Al completar este laboratorio, serás capaz de:
- Crear tu primera interfaz con Jetpack Compose
- Entender el concepto de estado en Compose
- Usar `remember` para mantener valores entre recomposiciones
- Implementar eventos de usuario con botones
- Aplicar el patrón de elevación de estado

---

## Requisitos Previos

- Android Studio instalado
- Proyecto base Lab 01 (Empty Activity)
- Conocimientos básicos de Kotlin

---

## Estructura del Proyecto

El proyecto tendrá la siguiente estructura de archivos:

```
lab_01/app/src/main/java/com/example/lab01/
├── views/
│   └── ContadorView.kt      (Nueva - Vista del contador)
└── MainActivity.kt           (Modificar - Integración)
```

---

## Parte 1: Crear la Vista del Contador (20 minutos)

### Paso 1.1: Crear el paquete `views`

1. En Android Studio, navega a `app/src/main/java/com/example/lab01/`
2. Clic derecho en `lab01` → **New** → **Package**
3. Nombre: `views`
4. Presiona Enter

### Paso 1.2: Crear archivo `ContadorView.kt`

1. Clic derecho en el paquete `views`
2. Selecciona **New** → **Kotlin Class/File**
3. Nombre: `ContadorView`
4. Tipo: **File**

### Paso 1.3: Implementar el composable ContadorView

Copia el siguiente código en `ContadorView.kt`:

```kotlin
package com.example.lab01.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContadorView(modifier: Modifier = Modifier) {
    // Estado: Valor del contador que persiste entre recomposiciones
    var contador by remember { mutableIntStateOf(0) }
    
    // Contenedor principal centrado
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Contador Simple",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Valor del contador
        Text(
            text = contador.toString(),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Botones de control
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón Decrementar
            Button(
                onClick = { contador-- },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("- Decrementar")
            }
            
            // Botón Incrementar
            Button(
                onClick = { contador++ }
            ) {
                Text("+ Incrementar")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón Reiniciar
        OutlinedButton(
            onClick = { contador = 0 }
        ) {
            Text("Reiniciar")
        }
    }
}
```

### Explicación del Código

**Estado con `remember` y `mutableIntStateOf`:**
```kotlin
var contador by remember { mutableIntStateOf(0) }
```
- `mutableIntStateOf(0)`: Crea un estado observable inicializado en 0
- `remember`: Mantiene el valor entre recomposiciones
- `by`: Delegación que permite acceder directamente al valor (sin `.value`)

**Eventos de Usuario:**
```kotlin
onClick = { contador++ }
```
- Cuando el usuario presiona el botón, se modifica el estado
- Al cambiar el estado, Compose recompone automáticamente la UI
- El nuevo valor se muestra sin necesidad de `notifyDataSetChanged` o similar

**Layout con Column y Row:**
- `Column`: Organiza elementos verticalmente
- `Row`: Organiza elementos horizontalmente
- `Arrangement.spacedBy`: Define espaciado entre elementos

---

## Parte 2: Integrar en MainActivity (15 minutos)

### Paso 2.1: Modificar MainActivity.kt

Reemplaza el contenido de `MainActivity.kt` con:

```kotlin
package com.example.lab01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.lab01.ui.theme.Lab01Theme
import com.example.lab01.views.ContadorView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContadorView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

### Explicación de la Integración

**Estructura básica:**
1. `Lab01Theme`: Aplica el tema Material 3 de la app
2. `Scaffold`: Proporciona estructura base con padding para system bars
3. `ContadorView`: Nuestro composable personalizado

**Edge-to-Edge:**
- `enableEdgeToEdge()`: Permite que la UI llegue hasta los bordes de la pantalla
- `innerPadding`: Maneja automáticamente los insets (status bar, navigation bar)

---

## Parte 3: Ejecutar y Probar (5 minutos)

### Paso 3.1: Compilar el proyecto

1. En el menú superior, selecciona **Build** → **Make Project**
2. Verifica que no haya errores en la consola

### Paso 3.2: Ejecutar en emulador o dispositivo

1. Click en el botón **Run** (triángulo verde) o presiona `Shift + F10`
2. Selecciona tu emulador o dispositivo físico
3. Espera a que la app se instale y lance

### Paso 3.3: Verificar funcionalidad

Prueba las siguientes acciones:

| Acción | Resultado Esperado |
|--------|-------------------|
| Presionar "Incrementar" | El contador aumenta en 1 |
| Presionar "Decrementar" | El contador disminuye en 1 |
| Presionar "Reiniciar" | El contador vuelve a 0 |
| Rotar el dispositivo | **El contador se reinicia** (comportamiento esperado por ahora) |

**Nota sobre rotación:** El estado se pierde al rotar porque no estamos usando `rememberSaveable`. Esto se verá en laboratorios posteriores.

---

## Conceptos Clave Aprendidos

### 1. Composables
Los composables son funciones que definen UI en Compose:
- Anotados con `@Composable`
- Se pueden componer entre sí (como bloques de construcción)
- Retornan Unit (no devuelven nada, solo declaran UI)

### 2. Estado y Recomposición
**Recomposición:** Proceso donde Compose vuelve a ejecutar composables cuando el estado cambia.

```kotlin
var contador by remember { mutableIntStateOf(0) }
```
Cuando `contador` cambia:
1. Compose detecta el cambio
2. Recompone solo los composables que leen `contador`
3. La UI se actualiza eficientemente

### 3. Delegación `by`
Permite acceder directamente al valor del estado:

```kotlin
// Sin delegación
var contador = remember { mutableIntStateOf(0) }
contador.value++  // Necesitas .value

// Con delegación (by)
var contador by remember { mutableIntStateOf(0) }
contador++  // Acceso directo
```

### 4. Modifiers
Los modificadores alteran el comportamiento y apariencia de composables:
- `fillMaxSize()`: Ocupa todo el espacio disponible
- `padding(16.dp)`: Añade espaciado interno
- Se encadenan de izquierda a derecha

---

## Ejercicios Adicionales

### Ejercicio 1: Paso configurable
Modifica el contador para que incremente de 2 en 2 en lugar de 1 en 1.

**Pista:**
```kotlin
onClick = { contador += 2 }
```

### Ejercicio 2: Límites
Añade límites al contador (por ejemplo, entre 0 y 10).

**Pista:**
```kotlin
onClick = { 
    if (contador < 10) contador++ 
}
```

### Ejercicio 3: Mensaje condicional
Muestra un mensaje cuando el contador llegue a 10.

**Pista:**
```kotlin
if (contador == 10) {
    Text("¡Llegaste a 10!", color = MaterialTheme.colorScheme.error)
}
```

---

## Solución de Problemas Comunes

### Error: "Unresolved reference: ContadorView"

**Causa:** Import faltante o archivo en ubicación incorrecta.

**Solución:**
1. Verifica que `ContadorView.kt` esté en `com.example.lab01.views`
2. Añade el import en MainActivity:
```kotlin
import com.example.lab01.views.ContadorView
```

### Error: "Cannot inline bytecode built with JVM target 1.8..."

**Causa:** Incompatibilidad de versión de Kotlin.

**Solución:**
Verifica en `build.gradle.kts (Module: app)`:
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
kotlinOptions {
    jvmTarget = "1.8"
}
```

### La app se cierra al iniciar

**Causa:** Error de runtime no detectado en compilación.

**Solución:**
1. Abre Logcat en Android Studio
2. Filtra por "Error" o "Exception"
3. Lee el stack trace para identificar el problema

---

## Recursos Adicionales

**Documentación oficial:**
- [Thinking in Compose](https://developer.android.com/jetpack/compose/mental-model)
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [Compose Layout Basics](https://developer.android.com/jetpack/compose/layouts/basics)

**Tutoriales recomendados:**
- Compose Basics Codelab
- State Management in Compose

---

## Siguiente Paso

Una vez completado este laboratorio, estarás listo para el **Laboratorio 02: Navegación con Navigation Compose**, donde aprenderás a crear aplicaciones con múltiples pantallas.

**Conceptos del Lab 02:**
- Navigation Component
- Rutas con sealed classes
- Paso de argumentos entre pantallas
- Bottom Navigation Bar
