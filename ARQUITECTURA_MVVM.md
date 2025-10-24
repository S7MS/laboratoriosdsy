# 🏛️ GUÍA 05: Arquitectura MVVM Explicada

## 🎯 Objetivos

- Entender qué es MVVM y por qué se usa
- Diferenciar las capas: Model, View, ViewModel
- Implementar el patrón desde cero
- Conectar ViewModel con Compose usando StateFlow
- Aplicar mejores prácticas

---

## 📖 ¿Qué es MVVM?

**MVVM** (Model-View-ViewModel) es un patrón arquitectónico que separa la lógica de negocio de la interfaz de usuario.

```
┌──────────────────────────────────────────────────┐
│                     VIEW                         │
│  (Composables - UI declarativa)                  │
│  - Option1View.kt, WelcomeView.kt               │
│  - Solo muestra datos                            │
│  - Emite eventos del usuario                     │
└────────────┬─────────────────────────────────────┘
             │
             │ observa (collectAsState)
             │ emite eventos (onClick, onValueChange)
             ↓
┌──────────────────────────────────────────────────┐
│                  VIEWMODEL                       │
│  (Lógica de presentación)                       │
│  - Option1ViewModel.kt, WelcomeViewModel.kt     │
│  - Maneja StateFlow/LiveData                     │
│  - Valida datos                                  │
│  - Sobrevive a cambios de configuración         │
└────────────┬─────────────────────────────────────┘
             │
             │ llama métodos
             │ obtiene/guarda datos
             ↓
┌──────────────────────────────────────────────────┐
│                    MODEL                         │
│  (Datos y lógica de negocio)                    │
│  - PrefsDataStore.kt, UserRepository.kt         │
│  - Room Database, Retrofit API                   │
│  - Reglas de negocio                            │
└──────────────────────────────────────────────────┘
```

---

## 🤔 ¿Por Qué Usar MVVM?

### **Sin MVVM (Todo en una Activity)**

```kotlin
// ❌ ANTI-PATRÓN: Todo mezclado
class MalaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var usuarios by remember { mutableStateOf(listOf<User>()) }
            var loading by remember { mutableStateOf(false) }
            
            // ❌ Lógica de red en la UI
            LaunchedEffect(Unit) {
                loading = true
                try {
                    val response = apiService.getUsers()
                    usuarios = response
                } catch (e: Exception) {
                    // ❌ Manejo de errores mezclado con UI
                    Toast.makeText(this@MalaActivity, "Error", Toast.LENGTH_SHORT).show()
                }
                loading = false
            }
            
            // ❌ Validación en la UI
            Button(onClick = {
                if (usuarios.isEmpty()) {
                    // Más lógica aquí...
                }
            }) {
                Text("Procesar")
            }
        }
    }
}
```

**Problemas:**
- ❌ Se pierde todo al rotar pantalla
- ❌ No se puede testear
- ❌ Código difícil de mantener
- ❌ No se puede reutilizar lógica

---

### **Con MVVM (Separado y organizado)**

```kotlin
// ✅ VIEW - Solo UI
@Composable
fun UsuariosView(vm: UsuariosViewModel = viewModel()) {
    val state = vm.uiState.collectAsState().value
    
    when {
        state.loading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}")
        else -> LazyColumn {
            items(state.usuarios) { usuario ->
                Text(usuario.nombre)
            }
        }
    }
}

// ✅ VIEWMODEL - Lógica de presentación
class UsuariosViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        cargarUsuarios()
    }
    
    private fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val usuarios = repository.obtenerUsuarios()
                _uiState.value = UiState(usuarios = usuarios)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

// ✅ MODEL - Fuente de datos
class UserRepository {
    suspend fun obtenerUsuarios(): List<User> {
        return apiService.getUsers()
    }
}

data class UiState(
    val usuarios: List<User> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
```

**Beneficios:**
- ✅ Separación de responsabilidades
- ✅ Sobrevive a rotaciones
- ✅ Fácil de testear
- ✅ Código reutilizable

---

## 🔨 Implementación Paso a Paso

### **PASO 1: Crear el Model (Data Layer)**

```kotlin
// data/User.kt
data class User(
    val id: Int,
    val nombre: String,
    val email: String
)

// data/UserRepository.kt
class UserRepository {
    // Simulamos datos (en producción vendría de API/BD)
    private val usuarios = listOf(
        User(1, "Ana García", "ana@example.com"),
        User(2, "Carlos Ruiz", "carlos@example.com"),
        User(3, "María López", "maria@example.com")
    )
    
    // Simulamos delay de red
    suspend fun obtenerUsuarios(): List<User> {
        delay(1000)
        return usuarios
    }
    
    suspend fun buscarUsuario(id: Int): User? {
        delay(500)
        return usuarios.find { it.id == id }
    }
}
```

---

### **PASO 2: Crear el ViewModel**

```kotlin
// viewmodels/UserViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado de la UI
data class UserUiState(
    val usuarios: List<User> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    
    // Estado privado (solo el ViewModel puede modificarlo)
    private val _uiState = MutableStateFlow(UserUiState())
    
    // Estado público (la View solo puede observarlo)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    init {
        cargarUsuarios()
    }
    
    // Método público que la View puede llamar
    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            
            try {
                val usuarios = repository.obtenerUsuarios()
                _uiState.update { it.copy(
                    usuarios = usuarios,
                    loading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    loading = false,
                    error = e.message ?: "Error desconocido"
                )}
            }
        }
    }
    
    fun buscar(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    // Computed property basado en el estado
    val usuariosFiltrados: StateFlow<List<User>> = uiState
        .map { state ->
            if (state.searchQuery.isEmpty()) {
                state.usuarios
            } else {
                state.usuarios.filter { user ->
                    user.nombre.contains(state.searchQuery, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

---

### **PASO 3: Crear la View (UI Layer)**

```kotlin
// views/UserListView.kt
@Composable
fun UserListView(
    viewModel: UserViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState().value
    val usuariosFiltrados = viewModel.usuariosFiltrados.collectAsState().value
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Usuarios") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.buscar(it) },
                label = { Text("Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Estados
            when {
                state.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarUsuarios() }) {
                            Text("Reintentar")
                        }
                    }
                }
                
                usuariosFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay usuarios")
                    }
                }
                
                else -> {
                    LazyColumn {
                        items(usuariosFiltrados) { usuario ->
                            UserItem(usuario)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFF6200EE),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.nombre.first().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Info
            Column {
                Text(
                    text = user.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
```

---

## 🔄 StateFlow vs LiveData

| Característica | StateFlow | LiveData |
|----------------|-----------|----------|
| Librería | Kotlin Coroutines | Android Lifecycle |
| Tipo | Flow (Kotlin) | Android-específico |
| Lifecycle aware | No (manual) | Sí (automático) |
| Valor inicial | Obligatorio | Opcional |
| Testing | Más fácil | Requiere LiveData testing |
| Compose | Nativo | Requiere `.observeAsState()` |
| Recomendado para Compose | ✅ Sí | ❌ No |

**Ejemplo comparativo:**

```kotlin
// Con LiveData (antigua)
class OldViewModel : ViewModel() {
    private val _usuarios = MutableLiveData<List<User>>()
    val usuarios: LiveData<List<User>> = _usuarios
}

@Composable
fun OldView(vm: OldViewModel) {
    val usuarios = vm.usuarios.observeAsState().value
    // ...
}

// Con StateFlow (moderno, recomendado)
class NewViewModel : ViewModel() {
    private val _usuarios = MutableStateFlow<List<User>>(emptyList())
    val usuarios: StateFlow<List<User>> = _usuarios.asStateFlow()
}

@Composable
fun NewView(vm: NewViewModel) {
    val usuarios = vm.usuarios.collectAsState().value
    // ...
}
```

---

## 🎓 Ejemplo Completo: Contador con MVVM

### **Model**
```kotlin
// data/CounterRepository.kt
class CounterRepository {
    private var count = 0
    
    fun increment(): Int {
        count++
        return count
    }
    
    fun decrement(): Int {
        count--
        return count
    }
    
    fun reset(): Int {
        count = 0
        return count
    }
    
    fun getCount(): Int = count
}
```

### **ViewModel**
```kotlin
// viewmodels/CounterViewModel.kt
data class CounterUiState(
    val count: Int = 0,
    val message: String = "Haz clic para contar"
)

class CounterViewModel(
    private val repository: CounterRepository = CounterRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()
    
    init {
        _uiState.value = CounterUiState(count = repository.getCount())
    }
    
    fun increment() {
        val newCount = repository.increment()
        _uiState.value = CounterUiState(
            count = newCount,
            message = when {
                newCount == 10 -> "¡Has llegado a 10!"
                newCount > 0 -> "Contador positivo"
                else -> "Haz clic para contar"
            }
        )
    }
    
    fun decrement() {
        val newCount = repository.decrement()
        _uiState.value = CounterUiState(
            count = newCount,
            message = when {
                newCount < 0 -> "Contador negativo"
                else -> "Haz clic para contar"
            }
        )
    }
    
    fun reset() {
        val newCount = repository.reset()
        _uiState.value = CounterUiState(
            count = newCount,
            message = "Contador reiniciado"
        )
    }
}
```

### **View**
```kotlin
// views/CounterView.kt
@Composable
fun CounterView(vm: CounterViewModel = viewModel()) {
    val state = vm.uiState.collectAsState().value
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.message,
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = state.count.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            ),
            color = when {
                state.count > 0 -> Color.Green
                state.count < 0 -> Color.Red
                else -> Color.Gray
            }
        )
        
        Spacer(Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { vm.decrement() }) {
                Text("-")
            }
            
            Button(onClick = { vm.reset() }) {
                Text("Reset")
            }
            
            Button(onClick = { vm.increment() }) {
                Text("+")
            }
        }
    }
}
```

---

## ✅ Mejores Prácticas

### **1. Un ViewModel por pantalla compleja**
```kotlin
// ✅ Bueno
class LoginViewModel : ViewModel()  // Para LoginScreen
class HomeViewModel : ViewModel()   // Para HomeScreen

// ❌ Malo
class AppViewModel : ViewModel()  // Para toda la app
```

### **2. Estado inmutable**
```kotlin
// ✅ Bueno - Estado inmutable con data class
data class UiState(val users: List<User> = emptyList())

// ❌ Malo - Estado mutable
class UiState {
    var users: List<User> = mutableListOf()
}
```

### **3. Exponer StateFlow, no MutableStateFlow**
```kotlin
// ✅ Bueno
private val _state = MutableStateFlow(UiState())
val state: StateFlow<UiState> = _state.asStateFlow()

// ❌ Malo
val state = MutableStateFlow(UiState())  // La View puede modificarlo
```

### **4. Usar viewModelScope para coroutines**
```kotlin
// ✅ Bueno
fun loadData() {
    viewModelScope.launch {
        // Se cancela automáticamente si el ViewModel se destruye
    }
}

// ❌ Malo
fun loadData() {
    GlobalScope.launch {  // Puede causar leaks
        // ...
    }
}
```

---

## ❓ Preguntas Frecuentes

### **P1: ¿Puedo tener múltiples ViewModels en una pantalla?**
**R:** Sí, pero generalmente indica que tu pantalla es demasiado compleja. Considera dividirla en sub-pantallas.

```kotlin
@Composable
fun PantallaTerrible(
    vm1: ViewModel1 = viewModel(),
    vm2: ViewModel2 = viewModel(),
    vm3: ViewModel3 = viewModel()  // ❌ Demasiados
) { }

// ✅ Mejor: combinar en uno o dividir la pantalla
```

### **P2: ¿Cómo paso datos entre ViewModels?**
**R:** No lo hagas directamente. Usa un Repository compartido o eventos.

```kotlin
// ✅ Bueno - Repository compartido
class SharedRepository {
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser = _selectedUser.asStateFlow()
    
    fun selectUser(user: User) {
        _selectedUser.value = user
    }
}

class ViewModel1(val repo: SharedRepository) : ViewModel()
class ViewModel2(val repo: SharedRepository) : ViewModel()
```

### **P3: ¿Cuándo crear el UiState?**
**R:** Cuando tienes más de 2-3 propiedades de estado.

```kotlin
// Simple - No necesita UiState
class SimpleViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
}

// Complejo - SÍ necesita UiState
data class ComplexUiState(
    val users: List<User>,
    val loading: Boolean,
    val error: String?,
    val searchQuery: String,
    val selectedTab: Int
)
```

---

## 🧪 Testing del ViewModel

```kotlin
class CounterViewModelTest {
    
    private lateinit var viewModel: CounterViewModel
    private lateinit var repository: CounterRepository
    
    @Before
    fun setup() {
        repository = CounterRepository()
        viewModel = CounterViewModel(repository)
    }
    
    @Test
    fun `increment increases count`() = runTest {
        // Given
        val initialState = viewModel.uiState.value
        
        // When
        viewModel.increment()
        
        // Then
        val newState = viewModel.uiState.value
        assertEquals(initialState.count + 1, newState.count)
    }
    
    @Test
    fun `reset sets count to zero`() = runTest {
        // Given
        viewModel.increment()
        viewModel.increment()
        
        // When
        viewModel.reset()
        
        // Then
        assertEquals(0, viewModel.uiState.value.count)
    }
}
```

---

## 🚀 Próximos Pasos

1. Implementa el contador siguiendo el ejemplo
2. Convierte una pantalla existente a MVVM
3. Lee [Guía 06: Componentes Básicos](./06_COMPONENTES_BASICOS.md)
4. Practica con el proyecto de la carcasa

---

**Recursos adicionales:**
- [Guide to app architecture (Android)](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)

**Última actualización:** Octubre 2025
