import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

document.addEventListener('DOMContentLoaded', () => {
    // Auth Check
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    UI.renderMenuProducts();
    setupRoleBasedUI();
    cargarProductos();
    setupEventListeners();
});

function setupRoleBasedUI() {
    const btnAgregar = document.getElementById('btnAgregar');

    if (!btnAgregar) {
        console.warn('btnAgregar no existe en el DOM');
        return;
    }

    const user = Auth.getCurrentUser();

    if (user && user.role === 'ADMIN') {
        document.getElementById('btnAgregar').classList.remove('hidden');
        document.getElementById('btnAgregar').classList.add('flex');
    }
}

const form = document.getElementById('formProducto');
const modal = document.getElementById('modalProducto');
const tipoSelect = document.getElementById('tipo');

function setupEventListeners() {
    // Modal controls
    document.getElementById('btnAgregar').addEventListener('click', () => abrirModal());
    document.getElementById('btnCerrarModal').addEventListener('click', () => UI.toggleModal('modalProducto', false));
    document.getElementById('btnCancelar').addEventListener('click', () => UI.toggleModal('modalProducto', false));

    // Close modal on outside click
    UI.setupModalCloser('modalProducto');

    // Dynamic Form Fields
    tipoSelect.addEventListener('change', toggleCamposTipo);

    // Form Submit
    form.addEventListener('submit', guardarProducto);

    // Filter
    document.getElementById('filtroTipo').addEventListener('change', (e) => {
        cargarProductos(e.target.value);
    });
}

function toggleCamposTipo() {
    const tipo = tipoSelect.value;
    const camposFisico = document.getElementById('camposFisico');
    const camposDigital = document.getElementById('camposDigital');

    if (tipo === 'Fisico') {
        camposFisico.classList.remove('hidden');
        camposDigital.classList.add('hidden');
    } else {
        camposFisico.classList.add('hidden');
        camposDigital.classList.remove('hidden');
    }
}

async function cargarProductos(filtro = 'todos') {
    try {
        const productos = await Api.get('/productos');
        renderizarTabla(productos, filtro);
    } catch (error) {
        UI.showNotification('Error al cargar productos', 'error');
    }
}

function renderizarTabla(productos, filtro) {
    const tbody = document.getElementById('listaProductos');
    tbody.innerHTML = '';

    const user = Auth.getCurrentUser();
    const role = user ? user.role : '';

    const filtrados = filtro === 'todos'
        ? productos
        : productos.filter(p => (filtro === 'Fisico' && p.peso !== undefined) || (filtro === 'Digital' && p.urlDescarga !== undefined));

    if (filtrados.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        return;
    }
    document.getElementById('emptyState').classList.add('hidden');

    filtrados.forEach(p => {
        const esFisico = p.peso !== undefined;
        const tipoBadge = esFisico
            ? '<span class="bg-blue-100 text-blue-800 py-1 px-3 rounded-full text-xs">Físico</span>'
            : '<span class="bg-purple-100 text-purple-800 py-1 px-3 rounded-full text-xs">Digital</span>';

        let buttons = '';

        if (role === 'ADMIN') {
            buttons += `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transform hover:scale-110 transition duration-300" onclick="window.editarProducto(${p.id})">
                    <i class="fas fa-edit text-yellow-500"></i>
                </button>
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transform hover:scale-110 transition duration-300 ml-2" onclick="window.eliminarProducto(${p.id})">
                    <i class="fas fa-trash-alt text-red-500"></i>
                </button>
            `;
        } else if (role === 'SUPPLIER') {
            buttons += `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transform hover:scale-110 transition duration-300" onclick="window.editarProducto(${p.id})">
                    <i class="fas fa-edit text-yellow-500"></i>
                </button>
            `;
        } else if (role === 'CUSTOMER') {
            buttons += `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transform hover:scale-110 transition duration-300 ml-2" onclick="window.agregarAlCarrito(${p.id})" title="Agregar al Carrito">
                    <i class="fas fa-cart-plus text-green-500"></i>
                </button>
            `;
        }

        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-100';
        row.innerHTML = `
            <td class="py-3 px-6 text-left whitespace-nowrap font-medium">${p.id}</td>
            <td class="py-3 px-6 text-left">
                <div class="flex items-center">
                    <span class="font-medium">${p.nombre}</span>
                </div>
            </td>
            <td class="py-3 px-6 text-left font-bold text-gray-700">${UI.formatCurrency(p.precio)}</td>
            <td class="py-3 px-6 text-left">${tipoBadge}</td>
            <td class="py-3 px-6 text-center">
                <div class="flex item-center justify-center">
                    ${buttons}
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function abrirModal(producto = null) {
    form.reset();
    document.getElementById('productoId').value = '';
    document.getElementById('modalTitle').textContent = 'Nuevo Producto';

    if (producto) {
        document.getElementById('modalTitle').textContent = 'Editar Producto';
        document.getElementById('productoId').value = producto.id;
        document.getElementById('nombre').value = producto.nombre;
        document.getElementById('precio').value = producto.precio;

        // Determine type based on properties
        const esFisico = producto.peso !== undefined;
        tipoSelect.value = esFisico ? 'Fisico' : 'Digital';
        toggleCamposTipo();

        if (esFisico) {
            document.getElementById('stock').value = producto.stock || 0;
            document.getElementById('peso').value = producto.peso || 0;
        } else {
            document.getElementById('urlDescarga').value = producto.urlDescarga || '';
            // Expiration might need handling if it's a date or int in backend
        }
    } else {
        toggleCamposTipo(); // Reset to default
    }

    UI.toggleModal('modalProducto', true);
}

async function guardarProducto(e) {
    e.preventDefault();
    const id = document.getElementById('productoId').value;
    const nombre = document.getElementById('nombre').value;
    const precio = parseFloat(document.getElementById('precio').value);
    const tipo = tipoSelect.value;

    let producto = { nombre, precio };

    if (tipo === 'Fisico') {
        producto.stock = parseInt(document.getElementById('stock').value) || 0;
        producto.peso = parseFloat(document.getElementById('peso').value) || 0;
    } else {
        producto.urlDescarga = document.getElementById('urlDescarga').value;
        // producto.fechaExpiracion = ...
    }

    try {
        if (id) {
            await Api.put(`/productos/${id}`, producto);
            UI.showNotification('Producto actualizado correctamente');
        } else {
            const endpoint = tipo === 'Fisico' ? '/productos/fisico' : '/productos/digital';
            await Api.post(endpoint, producto);
            UI.showNotification('Producto creado correctamente');
        }
        UI.toggleModal('modalProducto', false);
        cargarProductos();
    } catch (error) {
        UI.showNotification('Error al guardar producto', 'error');
    }
}

// Global functions for inline HTML events
window.editarProducto = async (id) => {
    try {
        const producto = await Api.get(`/productos/${id}`);
        abrirModal(producto);
    } catch (error) {
        UI.showNotification('Error al obtener producto', 'error');
    }
};

window.eliminarProducto = async (id) => {
    if (!confirm('¿Estás seguro de eliminar este producto?')) return;

    try {
        await Api.delete(`/productos/${id}`);
        UI.showNotification('Producto eliminado');
        cargarProductos();
    } catch (error) {
        UI.showNotification('Error al eliminar producto', 'error');
    }
};

window.agregarAlCarrito = async (productoId) => {
    try {
        await Api.post(`/carrito/productos/${productoId}`);
        UI.showNotification('Producto agregado al carrito');
    } catch (error) {
        UI.showNotification('Error al agregar al carrito: ' + error.message, 'error');
    }
};
