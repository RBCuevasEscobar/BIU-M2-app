import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

document.addEventListener('DOMContentLoaded', () => {
    // CRITICAL: Only ADMIN can access this page
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    const user = Auth.getCurrentUser();
    if (user.role !== 'ADMIN') {
        // Redirect non-admin users
        window.location.href = 'productos.html';
        return;
    }

    UI.renderMenuUsers();
    cargarUsuarios();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('btnNuevoUsuario').addEventListener('click', () => abrirModal());
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('btnCancelar').addEventListener('click', cerrarModal);
    document.getElementById('formUsuario').addEventListener('submit', guardarUsuario);
    UI.setupModalCloser('modalUsuario');
}

async function cargarUsuarios() {
    try {
        const usuarios = await Api.get('/usuarios');
        renderizarUsuarios(usuarios);
    } catch (error) {
        UI.showNotification('Error al cargar usuarios: ' + error.message, 'error');
    }
}

function renderizarUsuarios(usuarios) {
    const tbody = document.getElementById('listaUsuarios');
    tbody.innerHTML = '';

    if (!usuarios || usuarios.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        return;
    }
    document.getElementById('emptyState').classList.add('hidden');

    usuarios.forEach(usuario => {
        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-50';

        // Get role display name
        const roleNames = {
            'ADMIN': 'Administrador',
            'SUPPLIER': 'Proveedor',
            'CUSTOMER': 'Cliente'
        };
        const roleDisplay = roleNames[usuario.role] || usuario.role;

        // Get role color
        const roleColors = {
            'ADMIN': 'bg-red-100 text-red-800',
            'SUPPLIER': 'bg-blue-100 text-blue-800',
            'CUSTOMER': 'bg-green-100 text-green-800'
        };
        const roleColor = roleColors[usuario.role] || 'bg-gray-100 text-gray-800';

        row.innerHTML = `
            <td class="py-3 px-6 font-medium">#${usuario.id}</td>
            <td class="py-3 px-6">${usuario.nombre}</td>
            <td class="py-3 px-6">${usuario.email}</td>
            <td class="py-3 px-6">
                <span class="px-2 py-1 rounded text-xs font-semibold ${roleColor}">
                    ${roleDisplay}
                </span>
            </td>
            <td class="py-3 px-6 text-center">
                <button onclick="window.editarUsuario(${usuario.id})" 
                    class="text-yellow-500 hover:text-yellow-700 mx-1" title="Editar">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="window.eliminarUsuario(${usuario.id})" 
                    class="text-red-500 hover:text-red-700 mx-1" title="Eliminar">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function abrirModal(usuario = null) {
    const modal = document.getElementById('modalUsuario');
    const form = document.getElementById('formUsuario');
    const title = document.getElementById('modalTitle');
    const passwordField = document.getElementById('password');

    form.reset();

    if (usuario) {
        // Edit mode
        title.textContent = 'Editar Usuario';
        document.getElementById('usuarioId').value = usuario.id;
        document.getElementById('nombre').value = usuario.nombre;
        document.getElementById('email').value = usuario.email;
        document.getElementById('role').value = usuario.role;

        if (usuario.fechaNacimiento) {
            document.getElementById('fechaNacimiento').value = usuario.fechaNacimiento;
        }

        passwordField.removeAttribute('required');
    } else {
        // Create mode
        title.textContent = 'Nuevo Usuario';
        document.getElementById('usuarioId').value = '';
        passwordField.setAttribute('required', 'required');
    }

    UI.toggleModal('modalUsuario', true);
}

function cerrarModal() {
    UI.toggleModal('modalUsuario', false);
}

async function guardarUsuario(e) {
    e.preventDefault();

    const usuarioId = document.getElementById('usuarioId').value;
    const datosUsuario = {
        nombre: document.getElementById('nombre').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: document.getElementById('role').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value || null
    };

    // Remove empty password on edit
    if (usuarioId && !datosUsuario.password) {
        delete datosUsuario.password;
    }

    try {
        if (usuarioId) {
            // Update existing user
            await Api.put(`/usuarios/${usuarioId}`, datosUsuario);
            UI.showNotification('Usuario actualizado exitosamente', 'success');
        } else {
            // Create new user based on role
            let endpoint = '/usuarios/cliente'; // default
            if (datosUsuario.role === 'ADMIN') {
                endpoint = '/usuarios/admin';
            } else if (datosUsuario.role === 'SUPPLIER') {
                endpoint = '/usuarios/proveedor';
            }

            await Api.post(endpoint, datosUsuario);
            UI.showNotification('Usuario creado exitosamente', 'success');
        }

        cerrarModal();
        cargarUsuarios();
    } catch (error) {
        UI.showNotification('Error al guardar usuario: ' + error.message, 'error');
    }
}

// Global functions for inline onclick handlers
window.editarUsuario = async (id) => {
    try {
        const usuario = await Api.get(`/usuarios/${id}`);
        abrirModal(usuario);
    } catch (error) {
        UI.showNotification('Error al cargar usuario', 'error');
    }
};

window.eliminarUsuario = async (id) => {
    if (!confirm('¿Estás seguro de que deseas eliminar este usuario?\n\nEsta acción no se puede deshacer.')) {
        return;
    }

    try {
        await Api.delete(`/usuarios/${id}`);
        UI.showNotification('Usuario eliminado exitosamente', 'success');
        cargarUsuarios();
    } catch (error) {
        UI.showNotification('Error al eliminar usuario: ' + error.message, 'error');
    }
};
