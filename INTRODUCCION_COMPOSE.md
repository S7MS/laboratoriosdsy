# 🎨 GUÍA 04: Introducción a Jetpack Compose

## 🎯 Objetivos de Aprendizaje

Al finalizar esta guía, los estudiantes sabrán:
- ✅ Qué es Jetpack Compose y por qué se usa
- ✅ Diferencias con el sistema de Views tradicional
- ✅ Crear Composables básicos
- ✅ Entender el concepto de recomposición
- ✅ Usar Modifiers para personalizar UI

---

## 📖 ¿Qué es Jetpack Compose?

**Jetpack Compose** es el toolkit moderno de Android para construir interfaces de usuario de forma **declarativa**.

### **Paradigma Declarativo vs Imperativo**

```kotlin
// ❌ IMPERATIVO (Views tradicionales con XML)
// MainActivity.kt
val textView = findViewById<TextView>(R.id.myText)
textView.text = "Hola Mundo"
textView.setTextColor(Color.BLUE)
textView.textSize = 20f

// ✅ DECLARATIVO (Jetpack Compose)
@Composable
fun MiTexto() {
    Text(
        text = "Hola Mundo",
        color = Color.Blue,
        fontSize = 20.sp
    )
}
```

**Diferencias clave:**

| Aspecto | Views (XML) | Compose |
|---------|-------------|---------|
| Definición | XML + Código | 100% Kotlin |
| Mutabilidad | Imperativa (`setText()`) | Declarativa (recomposición) |
| Preview | Requiere ejecutar | Vista previa en tiempo real |
| Boilerplate | Alto (`findViewById`) | Bajo |
| Mantenimiento | Fragmentado (XML + Kotlin) | Todo en un lugar |
| Animaciones | Complejas | APIs simples |

---

## 🏗️ Anatomía de un Composable

```kotlin
@Composable  // ← 1. Anotación obligatoria
fun Saludo(   // ← 2. Nombre con CamelCase (como componente)
    nombre: String,  // ← 3. Parámetros (props)
    modifier: Modifier = Modifier  // ← 4. Modifier (siempre opcional)
) {
    // ← 5. Cuerpo: llama a otros Composables
    Text(
        text = "Hola, $nombre!",
        modifier = modifier
    )
}
```

### **Uso:**
```kotlin
@Composable
fun PantallaInicio() {
    Saludo(nombre = "María")
}
```

---

## 🧱 Composables Fundamentales

### **1. Text - Mostrar Texto**

```kotlin
@Composable
fun EjemplosTexto() {
    Column {
        // Texto simple
        Text("Hola Mundo")
        
        // Con estilo
        Text(
            text = "Título Grande",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
        
        // Con estilo de Material3
        Text(
            text = "Usando MaterialTheme",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
```

---

### **2. Button - Botones Interactivos**

```kotlin
@Composable
fun EjemplosBotones() {
    Column {
        // Botón filled (por defecto)
        Button(onClick = { /* acción */ }) {
            Text("Botón Normal")
        }
        
        // Outlined button
        OutlinedButton(onClick = { }) {
            Text("Botón Outlined")
        }
        
        // Text button
        TextButton(onClick = { }) {
            Text("Botón de Texto")
        }
        
        // Con ícono
        Button(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar"
            )
            Spacer(Modifier.width(8.dp))
            Text("Agregar")
        }
    }
}
```

---

### **3. Image - Mostrar Imágenes**

```kotlin
@Composable
fun EjemplosImagenes() {
    Column {
        // Desde recursos (drawable)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de la app",
            modifier = Modifier.size(100.dp)
        )
        
        // Desde vector (iconos)
        Image(
            imageVector = Icons.Default.Star,
            contentDescription = "Estrella",
            colorFilter = ColorFilter.tint(Color.Yellow)
        )
        
        // Con Coil (desde URL)
        AsyncImage(
            model = "https://example.com/foto.jpg",
            contentDescription = "Foto remota",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
        )
    }
}
```

---

### **4. TextField - Entrada de Texto**

```kotlin
@Composable
fun EjemploTextField() {
    var texto by remember { mutableStateOf("") }
    
    Column {
        // TextField básico
        TextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Nombre") }
        )
        
        // OutlinedTextField (más común)
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Email") },
            placeholder = { Text("usuario@ejemplo.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )
        
        Text("Escribiste: $texto")
    }
}
```

---

## 📐 Layouts: Organizando la UI

### **Column - Vertical**

```kotlin
@Composable
fun EjemploColumn() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,  // distribución
        horizontalAlignment = Alignment.CenterHorizontally  // alineación
    ) {
        Text("Arriba")
        Text("Centro")
        Text("Abajo")
    }
}
```

**Valores de `Arrangement.Vertical`:**
- `Top`, `Center`, `Bottom`
- `SpaceBetween`, `SpaceAround`, `SpaceEvenly`
- `spacedBy(16.dp)` - separación fija

---

### **Row - Horizontal**

```kotlin
@Composable
fun EjemploRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Home, contentDescription = null)
        Text("Inicio")
        Icon(Icons.Default.Settings, contentDescription = null)
    }
}
```

---

### **Box - Apilado (Z-index)**

```kotlin
@Composable
fun EjemploBox() {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fondo
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        
        // Texto encima
        Text(
            text = "Superpuesto",
            color = Color.White,
            fontSize = 24.sp
        )
        
        // Badge en esquina superior derecha
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Red, CircleShape)
                .padding(4.dp)
        ) {
            Text("3", color = Color.White, fontSize = 12.sp)
        }
    }
}
```

---

### **LazyColumn - Listas Eficientes**

```kotlin
@Composable
fun EjemploLista() {
    val items = (1..100).map { "Item #$it" }
    
    LazyColumn {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
```

**¿Por qué LazyColumn y no Column con scroll?**
- ✅ Solo renderiza items visibles (eficiente con miles de items)
- ✅ Recicla vistas automáticamente
- ✅ Mejor performance

---

## 🎨 Modifiers: El Sistema de Estilos

Los **Modifiers** son cadenas de transformaciones aplicadas a Composables.

### **Orden Importa**

```kotlin
@Composable
fun OrdenModifiers() {
    Column {
        // ❌ Orden incorrecto
        Box(
            Modifier
                .background(Color.Red)
                .padding(16.dp)  // padding DESPUÉS del fondo
                .size(100.dp)
        )
        // Resultado: fondo rojo de 100x100, pero sin padding visible
        
        // ✅ Orden correcto
        Box(
            Modifier
                .size(100.dp)
                .padding(16.dp)  // padding ANTES del fondo
                .background(Color.Blue)
        )
        // Resultado: fondo azul de 68x68 (100 - 16*2)
    }
}
```

### **Modifiers Comunes**

```kotlin
@Composable
fun ModifiersComunes() {
    Text(
        text = "Ejemplo",
        modifier = Modifier
            // Tamaño
            .size(200.dp)            // ancho y alto fijos
            .width(150.dp)           // solo ancho
            .fillMaxWidth()          // ancho completo
            .fillMaxSize()           // ancho y alto completos
            
            // Espaciado
            .padding(16.dp)          // padding en todos los lados
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(start = 8.dp)   // solo inicio (respeta RTL)
            
            // Fondo y bordes
            .background(Color.Blue)
            .background(Color.Blue, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.Red, RoundedCornerShape(4.dp))
            
            // Forma
            .clip(CircleShape)       // recorta en círculo
            .clip(RoundedCornerShape(12.dp))
            
            // Interacción
            .clickable { /* acción */ }
            .clickable(
                onClick = { },
                indication = null,    // sin efecto ripple
                interactionSource = remember { MutableInteractionSource() }
            )
            
            // Peso (en Row/Column)
            .weight(1f)              // toma espacio disponible
            
            // Alineación (en Box)
            .align(Alignment.Center)
    )
}
```

---

## 🔄 Estado y Recomposición

### **¿Qué es Recomposición?**

Compose observa el estado y **recompone** (redibuja) automáticamente cuando cambia.

```kotlin
@Composable
fun Contador() {
    // ❌ ESTO NO FUNCIONA (se reinicia a 0 cada recomposición)
    var contador = 0
    
    Button(onClick = { contador++ }) {
        Text("Clicks: $contador")  // Siempre muestra 0
    }
}
```

```kotlin
@Composable
fun ContadorFuncional() {
    // ✅ ESTO SÍ FUNCIONA (remember guarda el estado)
    var contador by remember { mutableStateOf(0) }
    
    Column {
        Text("Clicks: $contador")
        Button(onClick = { contador++ }) {
            Text("Incrementar")
        }
    }
}
```

### **remember vs rememberSaveable**

```kotlin
@Composable
fun DiferenciaRemember() {
    // Se pierde al rotar pantalla
    var temp by remember { mutableStateOf(0) }
    
    // Sobrevive a rotaciones
    var persistente by rememberSaveable { mutableStateOf(0) }
    
    Column {
        Text("Temporal: $temp")
        Text("Persistente: $persistente")
        
        Button(onClick = { 
            temp++
            persistente++
        }) {
            Text("Incrementar ambos")
        }
        
        Text("Rota el dispositivo y observa la diferencia")
    }
}
```

---

## 🎨 Material3 - Sistema de Diseño

### **Scaffold - Estructura Base**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConScaffold() {
    var textoSnackbar by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi App") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("¡Botón presionado!")
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Contenido de la pantalla")
        }
    }
}
```

---

### **Card - Contenedor con Elevación**

```kotlin
@Composable
fun EjemploCards() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card básica
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Card simple",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // ElevatedCard
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Card con elevación", fontWeight = FontWeight.Bold)
                Text("Más contenido aquí")
            }
        }
        
        // OutlinedCard
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* acción */ }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Card clickeable con borde")
            }
        }
    }
}
```

---

## 🎓 Ejercicios Prácticos

### **Ejercicio 1: Perfil de Usuario (Básico)**

Crear una pantalla con:
- Imagen de perfil (circular)
- Nombre y descripción
- Botón "Editar Perfil"

**Solución:**
```kotlin
@Composable
fun PerfilUsuario() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de perfil
        Image(
            painter = painterResource(R.drawable.avatar),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Nombre
        Text(
            text = "Juan Pérez",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Desarrollador Android",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(onClick = { }) {
            Text("Editar Perfil")
        }
    }
}
```

---

### **Ejercicio 2: Lista de Contactos (Intermedio)**

```kotlin
data class Contacto(val nombre: String, val telefono: String)

@Composable
fun ListaContactos() {
    val contactos = remember {
        listOf(
            Contacto("Ana García", "+34 612 345 678"),
            Contacto("Carlos Ruiz", "+34 623 456 789"),
            Contacto("María López", "+34 634 567 890")
        )
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(contactos) { contacto ->
            ItemContacto(contacto)
        }
    }
}

@Composable
fun ItemContacto(contacto: Contacto) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6200EE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contacto.nombre.first().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Info
            Column {
                Text(
                    text = contacto.nombre,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contacto.telefono,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Botón llamar
            IconButton(onClick = { }) {
                Icon(Icons.Default.Call, contentDescription = "Llamar")
            }
        }
    }
}
```

---

## ❓ Preguntas Frecuentes

### **P1: ¿Por qué no puedo usar findViewById?**
**R:** Compose no usa XML, no hay IDs. Usas variables directamente.

### **P2: ¿Cómo cambio un texto después de crearlo?**
**R:** No "cambias" el texto, cambias el **estado** que lo controla:
```kotlin
var texto by remember { mutableStateOf("Hola") }
Text(texto)  // Se actualiza automáticamente cuando cambie 'texto'
```

### **P3: ¿Cuándo usar Column vs LazyColumn?**
**R:**
- **Column**: Pocos elementos fijos (< 20)
- **LazyColumn**: Muchos elementos o dinámicos (> 20)

### **P4: ¿Por qué mi Composable no se actualiza?**
**R:** Olvidaste `remember`:
```kotlin
// ❌ No se actualiza
var count = 0  

// ✅ Se actualiza
var count by remember { mutableStateOf(0) }
```

---

## 🚀 Próximos Pasos

1. Practica los ejercicios anteriores
2. Lee [Guía 05: Arquitectura MVVM](./05_ARQUITECTURA_MVVM.md)
3. Experimenta combinando layouts (Row dentro de Column, etc.)
4. Usa Android Studio Preview para ver cambios en tiempo real

---

**Recursos:**
- [Compose Pathway (Google)](https://developer.android.com/courses/pathways/compose)
- [Compose Samples](https://github.com/android/compose-samples)

**Última actualización:** Octubre 2025
