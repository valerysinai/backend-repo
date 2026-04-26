# 🚀 Backend — Taller Fullstack

API REST construida con **Spring Boot** y **Java 17**, conectada a **PostgreSQL**.  
Gestiona las entidades: **Usuario**, **Producto** y **Pedido**.

---

## 📁 Estructura del Proyecto

```
backend-repo/
├── .github/
│   └── workflows/
│       └── build-and-test.yml        # GitHub Action: compila y corre tests
├── src/
│   └── main/
│       ├── java/com/taller/backend/
│       │   ├── BackendApplication.java     # Punto de entrada de la aplicación
│       │   ├── entity/                     # Clases que representan las tablas
│       │   │   ├── Usuario.java
│       │   │   ├── Producto.java
│       │   │   └── Pedido.java
│       │   ├── repository/                 # Acceso a la base de datos
│       │   │   ├── UsuarioRepository.java
│       │   │   ├── ProductoRepository.java
│       │   │   └── PedidoRepository.java
│       │   ├── service/                    # Lógica del negocio
│       │   │   ├── UsuarioService.java
│       │   │   ├── ProductoService.java
│       │   │   └── PedidoService.java
│       │   └── controller/                 # Endpoints REST (lo que consume el frontend)
│       │       ├── UsuarioController.java
│       │       ├── ProductoController.java
│       │       └── PedidoController.java
│       └── resources/
│           └── application.properties      # Configuración de la BD
└── pom.xml                                 # Dependencias del proyecto (Maven)
```

---

## 🧠 ¿Cómo funciona? (Explicado simple)

El backend sigue una arquitectura en **3 capas**. Imagina que es un restaurante:

```
Frontend (cliente)
      ↓ hace un pedido (HTTP Request)
  Controller  ← El mesero: recibe la orden y la pasa a la cocina
      ↓
   Service    ← El chef: aplica la lógica y reglas del negocio
      ↓
  Repository  ← El almacén: busca o guarda los ingredientes en la BD
      ↓
  PostgreSQL  ← La despensa: donde están todos los datos guardados
```

---

## 🔑 Archivos más importantes

### 1. `Entity` — La foto de la tabla en la base de datos

Cada entidad es una clase Java que representa una tabla. Por ejemplo, `Usuario.java`:

```java
@Entity                         // Le dice a Spring que esta clase es una tabla
@Table(name = "usuario")        // Nombre exacto de la tabla en PostgreSQL
public class Usuario {

    @Id                                          // Esta es la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El ID se genera solo (autoincremental)
    private Long id;

    private String nombre;
    private String email;
    private String password;

    @Column(name = "fecha_creacion", updatable = false) // No se puede modificar después de creada
    private LocalDateTime fechaCreacion;

    @PrePersist                       // Se ejecuta automáticamente ANTES de guardar
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now(); // Pone la fecha actual al momento de crear
    }
}
```

> **¿Por qué así?** Con `@Entity` y `@Table`, Spring Boot sabe cómo conectar esta clase con la tabla `usuario` de PostgreSQL. No hay que escribir SQL manual.

---

### 2. `Repository` — El acceso a la base de datos

Es una interfaz que Spring implementa automáticamente. Solo con extender `JpaRepository` ya tienes operaciones CRUD gratis:

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository ya incluye estos métodos sin escribir nada:
    // findAll()        → trae todos los usuarios
    // findById(id)     → busca uno por ID
    // save(usuario)    → crea o actualiza
    // deleteById(id)   → elimina por ID
}
```

> **¿Por qué así?** Spring Boot genera el SQL automáticamente. No necesitas escribir `SELECT * FROM usuario`, él lo hace por ti.

---

### 3. `Service` — La lógica del negocio

Hace de intermediario entre el Controller y el Repository:

```java
@Service   // Le dice a Spring que esta clase tiene lógica de negocio
public class UsuarioService {

    @Autowired  // Spring inyecta el Repository automáticamente (no hay que hacer "new")
    private UsuarioRepository usuarioRepository;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();  // Llama al repo para traer todos
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id); // Busca uno, puede que no exista
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario); // Crea o actualiza
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id); // Elimina por ID
    }
}
```

> **¿Por qué así?** Separar la lógica en una capa `Service` permite que si en el futuro quieres agregar validaciones (ej: "no crear usuario si el email ya existe"), lo pones aquí sin tocar el Controller.

---

### 4. `Controller` — Los endpoints que consume el Frontend

Define las URLs de la API. Por ejemplo, `UsuarioController.java`:

```java
@RestController                    // Devuelve JSON automáticamente
@RequestMapping("/api/usuarios")   // Todas las rutas de este controller empiezan con /api/usuarios
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping                    // GET /api/usuarios → trae todos
    public List<Usuario> getAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")           // GET /api/usuarios/1 → trae el usuario con id=1
    public ResponseEntity<Usuario> getById(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404
    }

    @PostMapping                   // POST /api/usuarios → crea uno nuevo
    public Usuario create(@RequestBody Usuario usuario) {
        return usuarioService.save(usuario);
    }

    @PutMapping("/{id}")           // PUT /api/usuarios/1 → actualiza el usuario con id=1
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody Usuario datos) {
        return usuarioService.findById(id)
                .map(u -> {
                    u.setNombre(datos.getNombre());
                    u.setEmail(datos.getEmail());
                    return ResponseEntity.ok(usuarioService.save(u));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")        // DELETE /api/usuarios/1 → elimina el usuario con id=1
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(u -> {
                    usuarioService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

### 5. `application.properties` — Configuración de la conexión a la BD

```properties
# URL de la base de datos (puerto 5433 porque el local ya usa el 5432)
spring.datasource.url=jdbc:postgresql://localhost:5433/taller_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Le decimos a Hibernate que NO cree tablas (Liquibase ya lo hace)
spring.jpa.hibernate.ddl-auto=none

# Muestra el SQL que ejecuta Spring en la consola (útil para debug)
spring.jpa.show-sql=true
```

> **¿Por qué `ddl-auto=none`?** Porque las tablas ya las crea **Liquibase** en el repo de base de datos. Si dejamos que Hibernate también las cree, habría conflictos.

---

## 🌐 Endpoints disponibles

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/usuarios` | Lista todos los usuarios |
| `GET` | `/api/usuarios/{id}` | Obtiene un usuario por ID |
| `POST` | `/api/usuarios` | Crea un nuevo usuario |
| `PUT` | `/api/usuarios/{id}` | Actualiza un usuario |
| `DELETE` | `/api/usuarios/{id}` | Elimina un usuario |
| `GET` | `/api/productos` | Lista todos los productos |
| `GET` | `/api/productos/{id}` | Obtiene un producto por ID |
| `POST` | `/api/productos` | Crea un nuevo producto |
| `PUT` | `/api/productos/{id}` | Actualiza un producto |
| `DELETE` | `/api/productos/{id}` | Elimina un producto |
| `GET` | `/api/pedidos` | Lista todos los pedidos |
| `GET` | `/api/pedidos/{id}` | Obtiene un pedido por ID |
| `POST` | `/api/pedidos` | Crea un nuevo pedido |
| `PUT` | `/api/pedidos/{id}` | Actualiza un pedido |
| `DELETE` | `/api/pedidos/{id}` | Elimina un pedido |

---

## ▶️ Cómo correr el proyecto

### Requisitos previos
- Java 17+
- Maven
- PostgreSQL corriendo en el puerto 5433 (ver repo de base de datos)

### Pasos

```bash
# 1. Asegúrate que Docker con la BD esté corriendo
docker start pg-taller

# 2. Entra a la carpeta del proyecto
cd backend-repo

# 3. Compila y corre el proyecto
./mvnw spring-boot:run

# 4. Verifica que esté corriendo
# Abre el navegador en:
# http://localhost:8080/api/usuarios
```

---

## 🧪 Ejemplo de prueba con JSON

Para crear un usuario (usando Thunder Client, Postman o Hoppscotch):

```json
POST http://localhost:8080/api/usuarios
Content-Type: application/json

{
  "nombre": "Valery García",
  "email": "valery@email.com",
  "password": "123456"
}
```

Respuesta esperada:
```json
{
  "id": 1,
  "nombre": "Valery García",
  "email": "valery@email.com",
  "password": "123456",
  "fechaCreacion": "2026-04-25T19:08:46.310287"
}
```

---

## ⚙️ GitHub Action

El archivo `.github/workflows/build-and-test.yml` se ejecuta automáticamente en cada `push` a `main` y:

1. Levanta un contenedor de **PostgreSQL** temporal
2. Instala **Java 17**
3. Compila el proyecto con **Maven**
4. Corre los **tests** automáticamente

Si algo falla, el pipeline se pone en ❌ rojo y no permite hacer merge.

---

## 🔗 Repositorios relacionados

| Repo | Descripción |
|------|-------------|
| `taller-db` | Base de datos con PostgreSQL + Liquibase |
| `taller-backend` | Este repositorio — API REST con Spring Boot |
| `taller-frontend` | Interfaz visual con React + Vite |
