# E-Commerce Frontend

## 🎯 Propósito del Proyecto

Este frontend forma parte de una solución completa de **E-Commerce** que permite gestionar productos, carrito de compras, órdenes y usuarios con control de acceso basado en roles (RBAC).

### Problema que Resuelve
- **Gestión de inventario** para administradores y proveedores
- **Experiencia de compra** fluida para clientes
- **Control de acceso** granular según el tipo de usuario
- **Administración centralizada** de usuarios, productos y órdenes

### Alcance Funcional
- ✅ Autenticación y gestión de sesiones
- ✅ CRUD de productos (físicos y digitales)
- ✅ Carrito de compras personalizado
- ✅ Historial de órdenes
- ✅ Gestión de usuarios (ADMIN únicamente)
- ✅ Menú dinámico según rol del usuario

### Tipos de Usuarios
| Rol | Permisos |
|-----|----------|
| **ADMIN** | Gestión completa: productos, usuarios, órdenes |
| **SUPPLIER** | Edición de productos, visualización de órdenes |
| **CUSTOMER** | Compra de productos, gestión de carrito, visualización de propias órdenes |

---

## 🏗️ Arquitectura del Frontend

### Modelo SPA (Single Page Application)
El frontend está construido como una **SPA parcial** usando HTML5, CSS3, y JavaScript ES6+ modular:

- **Páginas independientes**: Cada vista es un archivo HTML separado
- **Módulos JavaScript**: Lógica organizada en módulos ES6 (`import`/`export`)
- **Estado compartido**: `localStorage` almacena sesión del usuario
- **Navegación dinámica**: Menú se adapta según rol del usuario autenticado

### Comunicación con API REST
```javascript
// api.js - Cliente HTTP centralizado
const Api = {
    async get(endpoint) { /* ... */ },
    async post(endpoint, data) { /* ... */ },
    async put(endpoint, data) { /* ... */ },
    async delete(endpoint) { /* ... */ }
};
```

**Headers automáticos:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}` (si el usuario está autenticado)

**Manejo de errores:**
- Redirección automática a `/login.html` en errores 401/403
- Notificaciones visuales en operaciones CRUD

### Control de Estado
**Autenticación (localStorage):**
```json
{
  "user": {
    "id": 1,
    "nombre": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN"
  },
  "token": "mock-jwt-token-1"
}
```

### Manejo de Autenticación
```javascript
// auth.js
const Auth = {
    login(user, token) { /* Guardar en localStorage */ },
    logout() { /* Limpiar localStorage */ },
    isAuthenticated() { /* Verificar token */ },
    getCurrentUser() { /* Obtener usuario actual */ }
};
```

**Flujo de autenticación:**
1. Usuario envía credenciales a `/auth/login`
2. Backend valida y retorna `{id, nombre, role, token}`
3. Frontend almacena en `localStorage`
4. Todas las peticiones incluyen `Authorization: Bearer {token}`
5. TokenAuthenticationFilter valida en backend
6. Si token inválido → 401 → Redirect a login

---

## 🎨 Diseño de Interfaces

### HTML5 Semántico
Uso de etiquetas semánticas para mejor accesibilidad y SEO:
```html
<nav>   <!-- Navegación principal -->
<main>  <!-- Contenido principal -->
<header><!-- Encabezados de sección -->
<form>  <!-- Formularios interactivos -->
<table> <!-- Tablas de datos -->
```

### CSS3 + Tailwind CSS
**Framework**: Tailwind CSS v3 (utility-first)
**Compilación**: `npx tailwindcss -i ./css/styles.css -o ./css/output.css --watch`

**Clases comunes:**
- Layout: `flex`, `grid`, `container`, `mx-auto`
- Spacing: `px-6`, `py-4`, `gap-4`
- Colors: `bg-blue-600`, `text-gray-700`, `hover:bg-blue-700`
- Shadow: `shadow-md`, `shadow-xl`
- Transitions: `transition`, `duration-300`

### Responsividad
Diseño Mobile-First con breakpoints de Tailwind:
```css
sm:  /* 640px  - Tablets */
md:  /* 768px  - Tablets landscape */
lg:  /* 1024px - Desktop */
xl:  /* 1280px - Desktop grande */
```

Ejemplo:
```html
<div class="flex flex-col md:flex-row gap-4">
    <!-- En móvil: columna | En desktop: fila -->
</div>
```

### Interactividad
**Modales dinámicos:**
```javascript
UI.toggleModal('modalProducto', true);  // Abrir
UI.toggleModal('modalProducto', false); // Cerrar
```

**Notificaciones:**
```javascript
UI.showNotification('Guardado exitosamente', 'success');
UI.showNotification('Error al guardar', 'error');
```

---

## 📋 Validador de Formularios Dinámico

### Caso de Uso: Registro de Nuevo Usuario

**Archivo**: `usuarios.html` + `usuarios.js`

#### Campos Validados
| Campo | Tipo | Validación | Requerido |
|-------|------|------------|-----------|
| Nombre | text | No vacío | Sí |
| Email | email | Formato email válido | Sí |
| Contraseña | password | No vacío (solo en creación) | Condicional |
| Rol | select | Uno de: ADMIN, SUPPLIER, CUSTOMER | Sí |
| Fecha Nacimiento | date | Formato ISO (YYYY-MM-DD) | No |

#### Validaciones por Campo

**1. Nombre**
```javascript
<input type="text" id="nombre" required>
```
- ✅ HTML5 `required` attribute
- ❌ Vacío → "Por favor, rellene este campo"

**2. Email**
```javascript
<input type="email" id="email" required>
```
- ✅ HTML5 email validation
- ❌ Formato inválido → "Incluya un '@' en la dirección"
- ❌ Vacío → "Por favor, rellene este campo"

**3. Contraseña**
```javascript
// En creación:
passwordField.setAttribute('required', 'required');

// En edición:
passwordField.removeAttribute('required');
if (!password.value) {
    delete datosUsuario.password; // No enviar si está vacío
}
```
- ✅ Requerida solo al crear
- ✅ Opcional al editar (mantiene la existente si vacío)

**4. Rol**
```javascript
<select id="role" required>
    <option value="">Seleccionar rol...</option>
    <option value="ADMIN">Administrador</option>
    <option value="SUPPLIER">Proveedor</option>
    <option value="CUSTOMER">Cliente</option>
</select>
```
- ✅ Debe seleccionar un valor
- ❌ Valor vacío → "Seleccione un elemento de la lista"

**5. Fecha de Nacimiento** (Opcional)
```javascript
<input type="date" id="fechaNacimiento">
const fecha = document.getElementById('fechaNacimiento').value || null;
```
- ✅ Formato ISO automático
- ✅ Envía `null` si no se proporciona

#### Validación en Submit
```javascript
formUsuario.addEventListener('submit', async (e) => {
    e.preventDefault(); // Evitar recarga de página
    
    // HTML5 validations se ejecutan automáticamente antes de este punto
    
    const datosUsuario = {
        nombre: document.getElementById('nombre').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: document.getElementById('role').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value || null
    };
    
    // Validación adicional: eliminar contraseña vacía en edición
    if (usuarioId && !datosUsuario.password) {
        delete datosUsuario.password;
    }
    
    try {
        if (usuarioId) {
            await Api.put(`/usuarios/${usuarioId}`, datosUsuario);
        } else {
            await Api.post('/usuarios/admin', datosUsuario);
        }
        UI.showNotification('Usuario guardado', 'success');
    } catch (error) {
        UI.showNotification('Error: ' + error.message, 'error');
    }
});
```

#### Mensajes de Error (UX)
**Validaciones HTML5 (nativas del navegador):**
- "Por favor, rellene este campo"
- "Incluya un '@' en la dirección de correo electrónico"
- "Seleccione un elemento de la lista"

**Validaciones del servidor (desde backend):**
- ❌ "El email ya está registrado" (409 Conflict)
- ❌ "Usuario no encontrado" (404 Not Found)
- ❌ "Credenciales inválidas" (401 Unauthorized)

**Notificación visual:**
```javascript
// Éxito
UI.showNotification('Usuario creado exitosamente', 'success');

// Error
UI.showNotification('Error al guardar usuario: ' + error.message, 'error');
```

---

## 🛠️ Tecnologías Usadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| HTML5 | - | Estructura semántica |
| CSS3 | - | Estilos base |
| Tailwind CSS | 3.x | Framework utility-first |
| JavaScript | ES6+ | Lógica de aplicación |
| FontAwesome | 6.4.0 | Iconografía |
| Fetch API | - | Peticiones HTTP |

---

## 🚀 Instalación / Ejecución

### Requisitos
- Node.js >= 16.x (para Tailwind CLI)
- Backend ejecutándose en `http://localhost:8080`

### Pasos

1. **Instalar dependencias**:
```bash
npm install
```

2. **Compilar Tailwind CSS**:
```bash
# Modo desarrollo (watch)
npm run dev

# Producción (minificado)
npm run build
```

3. **Servir el frontend**:
```bash
# Con Live Server (VS Code extension)
# O con Python:
python -m http.server 5500

# O con Node.js:
npx serve .
```

4. **Acceder**:
```
http://localhost:5500
```

### Uso del Validador

1. **Login como ADMIN**:
   - Email: `admin@example.com`
   - Password: `admin123`

2. **Ir a "Usuarios"** en el menú

3. **Click en "Nuevo Usuario"**

4. **Probar validaciones**:
   - Dejar campos vacíos → Ver mensajes de error
   - Email sin @ → Error de formato
   - Crear usuario sin contraseña → Error
   - Editar usuario sin contraseña → Mantiene la actual

---

## 📁 Estructura de Archivos

```
frontend/
├── css/
│   ├── styles.css     # Tailwind @directives
│   └── output.css     # CSS compilado
├── js/
│   ├── api.js         # Cliente HTTP
│   ├── auth.js        # Autenticación
│   ├── ui.js          # Utilidades UI
│   ├── productos.js   # Lógica de productos
│   ├── carrito.js     # Lógica de carrito
│   ├── ordenes.js     # Lógica de órdenes
│   └── usuarios.js    # Lógica de usuarios (ADMIN)
├── index.html         # Landing page
├── login.html         # Login
├── register.html      # Registro
├── productos.html     # Gestión productos
├── carrito.html       # Carrito de compras
├── ordenes.html       # Historial de órdenes
├── usuarios.html      # Gestión usuarios (ADMIN)
└── package.json       # Dependencias
```

---

## 🔐 Seguridad

- ✅ Protección de rutas por rol (JavaScript)
- ✅ Validación de token en cada request
- ✅ Redirección automática en errores 401/403
- ✅ Contraseñas nunca expuestas en localStorage
- ⚠️ Token mock (migrar a JWT real en producción)

---

## 📝 Licencia

MIT License - Proyecto Académico
