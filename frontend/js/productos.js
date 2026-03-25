import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';
import ConfigService from './config.service.js';

// ── Estado local de imágenes en el modal ──────────────────────────────────────
let imagenesModal = []; // [{ url: String, isDefault: Boolean }]
let ivaSistema = 0.16;
let monedaSistema = 'MXN';
let maxProductosOrden = 999;
let stockMinimo = 0;

document.addEventListener('DOMContentLoaded', async () => {

    if (!Auth.requireAuth(["ADMIN", "SUPPLIER"])) return;

    UI.renderNavBar({ containerId: 'mainNav', context: 'products' });
    setupRoleBasedUI();

    const cfg = await ConfigService.load();

    ivaSistema = cfg.iva;
    monedaSistema = cfg.monedaSistema;
    maxProductosOrden = cfg.maxProductosOrden;
    stockMinimo = cfg.stockMinimo;

    cargarProductos();
    setupEventListeners();
});

// ── Configuración UI por Rol ───────────────────────────────────────────────────
function setupRoleBasedUI() {
    const user = Auth.getCurrentUser();
    const role = user ? user.role : '';

    // Mostrar botón Agregar solo para ADMIN
    if (role === 'ADMIN') {
        document.getElementById('btnAgregar').classList.remove('hidden');
        document.getElementById('btnAgregar').classList.add('flex');
    }

    // El campo Proveedor solo es visible para ADMIN
    const campoProveedor = document.getElementById('campoProveedor');
    if (campoProveedor && role !== 'ADMIN') {
        campoProveedor.classList.add('hidden');
    }

    // Ocultar columna de Stock para CUSTOMER
    const thStock = document.getElementById('thStock');
    if (thStock && role === 'CUSTOMER') {
        thStock.classList.add('hidden');
    }
}

// ── Event Listeners ────────────────────────────────────────────────────────────
function setupEventListeners() {
    document.getElementById('btnAgregar').addEventListener('click', () => abrirModal());
    document.getElementById('btnCerrarModal').addEventListener('click', () => UI.toggleModal('modalProducto', false));
    document.getElementById('btnCancelar').addEventListener('click', () => UI.toggleModal('modalProducto', false));
    UI.setupModalCloser('modalProducto');

    document.getElementById('tipo').addEventListener('change', toggleCamposTipo);
    document.getElementById('formProducto').addEventListener('submit', guardarProducto);

    // Filtro tipo
    document.getElementById('filtroTipo').addEventListener('change', (e) => {
        cargarProductos(e.target.value);
    });

    // Contador descripción
    const txtDescripcion = document.getElementById('descripcion');
    if (txtDescripcion) {
        txtDescripcion.addEventListener('input', () => {
            actualizarContador('descripcion', 'contadorDescripcion', 500);
        });
    }

    // Contador proveedor
    const txtProveedor = document.getElementById('proveedor');
    if (txtProveedor) {
        txtProveedor.addEventListener('input', () => {
            actualizarContador('proveedor', 'contadorProveedor', 150);
        });
    }

    // Agregar imagen al modal
    document.getElementById('btnAgregarImagen').addEventListener('click', agregarImagenModal);

    // También agregar imagen al presionar Enter en el campo
    document.getElementById('nuevaImagenUrl').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            agregarImagenModal();
        }
    });
}

function actualizarContador(inputId, counterId, max) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    if (!input || !counter) return;
    const len = input.value.length;
    counter.textContent = `${len} / ${max}`;
    counter.classList.toggle('warn', len >= max * 0.9);
}

function toggleCamposTipo() {
    const tipo = document.getElementById('tipo').value;
    document.getElementById('camposFisico').classList.toggle('hidden', tipo !== 'Fisico');
    document.getElementById('camposDigital').classList.toggle('hidden', tipo !== 'Digital');
}

// ── Carga y renderizado de productos (TABLA para ADMIN/SUPPLIER) ───────────────
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
        : productos.filter(p => p.tipo === filtro);

    if (filtrados.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        return;
    }
    document.getElementById('emptyState').classList.add('hidden');

    filtrados.forEach(p => {
        const esFisico = p.tipo === 'Fisico';
        const tipoBadge = esFisico
            ? '<span class="bg-blue-100 text-blue-800 py-1 px-3 rounded-full text-xs">Físico</span>'
            : '<span class="bg-purple-100 text-purple-800 py-1 px-3 rounded-full text-xs">Digital</span>';

        // Imagen por defecto
        const imgDefault = p.imagenes && p.imagenes.find(i => i.isDefault);
        const imgSrc = imgDefault ? imgDefault.imagenUrl : (p.imagenes && p.imagenes.length > 0 ? p.imagenes[0].imagenUrl : null);
        const imgTag = imgSrc
            ? `<img src="${imgSrc}" alt="Imagen de ${p.nombre}" class="w-12 h-12 object-cover rounded" loading="lazy">`
            : `<div class="w-12 h-12 bg-gray-200 rounded flex items-center justify-center text-gray-400"><i class="fas fa-image"></i></div>`;

        let buttons = '';
        if (role === 'ADMIN') {
            buttons = `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transition" onclick="window.editarProducto(${p.id})" title="Editar">
                    <i class="fas fa-edit text-yellow-500"></i>
                </button>
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transition ml-1" onclick="window.eliminarProducto(${p.id})" title="Eliminar">
                    <i class="fas fa-trash-alt text-red-500"></i>
                </button>
            `;
        } else if (role === 'SUPPLIER') {
            buttons = `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transition" onclick="window.editarProducto(${p.id})" title="Editar">
                    <i class="fas fa-edit text-yellow-500"></i>
                </button>
            `;
        } else if (role === 'CUSTOMER') {
            buttons = `
                <button class="w-8 h-8 rounded-full hover:bg-gray-200 transition" onclick="window.agregarAlCarrito(${p.id})" title="Agregar al Carrito">
                    <i class="fas fa-cart-plus text-green-500"></i>
                </button>
            `;
        }

        const proveedorDisplay = p.proveedor
            ? `<span class="text-xs text-gray-500 truncate" style="max-width:120px" title="${p.proveedor}">${p.proveedor}</span>`
            : '<span class="text-xs text-gray-400 italic">—</span>';

        // Lógica de Stock (Visible solo para ADMIN / SUPPLIER)
        let stockDisplay = '';

        if (role !== 'CUSTOMER') {
            if (esFisico) {
                const stock = Number(p.stock ?? 0);
                const min = Number(stockMinimo ?? 0);
                const isOut = stock === 0;
                const isLow = stock > 0 && stock <= min;
                let cssClass = 'text-green-600';
                if (isOut) {
                    cssClass = 'text-red-600 font-bold';
                } else if (isLow) {
                    cssClass = 'text-orange-500 font-bold';
                }
                stockDisplay = `<td class="py-3 px-4"><span class="${cssClass}">${p.stock}</span></td>`;
            } else {
                stockDisplay = `<td class="py-3 px-4"><span class="text-gray-400 text-xs italic">∞ (Digital)</span></td>`;
            }
        }

        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-50';
        row.innerHTML = `
            <td class="py-3 px-4 font-medium">${p.id}</td>
            <td class="py-3 px-4">${imgTag}</td>
            <td class="py-3 px-4">
                <div class="font-medium">${p.nombre}</div>
                ${p.descripcion ? `<div class="text-xs text-gray-400 truncate" style="max-width:200px" title="${p.descripcion}">${p.descripcion}</div>` : ''}
            </td>
            <td class="py-3 px-4 font-bold text-gray-700">${UI.formatCurrency(p.precio)}</td>
            ${stockDisplay}
            <td class="py-3 px-4">${tipoBadge}</td>
            <td class="py-3 px-4">${proveedorDisplay}</td>
            <td class="py-3 px-4 text-center">
                <div class="flex items-center justify-center">${buttons}</div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// ── Gestión de imágenes en el modal ───────────────────────────────────────────
function agregarImagenModal() {
    const urlInput = document.getElementById('nuevaImagenUrl');
    const url = urlInput.value.trim();
    if (!url) {
        UI.showNotification('Ingresa una URL de imagen válida', 'error');
        return;
    }
    // Validar URL básica
    try { new URL(url); } catch {
        UI.showNotification('La URL ingresada no es válida', 'error');
        return;
    }
    const isDefault = imagenesModal.length === 0; // Primera imagen será default
    imagenesModal.push({ url, isDefault });
    urlInput.value = '';
    renderizarImagenesModal();
}

function renderizarImagenesModal() {
    const container = document.getElementById('imagenesContainer');
    const sinMsg = document.getElementById('sinImagenesMsg');
    container.innerHTML = '';

    if (imagenesModal.length === 0) {
        sinMsg.classList.remove('hidden');
        return;
    }
    sinMsg.classList.add('hidden');

    imagenesModal.forEach((img, idx) => {
        const item = document.createElement('div');
        item.className = 'imagen-item';
        item.innerHTML = `
            <img src="${img.url}" alt="Preview" onerror="this.src='https://via.placeholder.com/40'" loading="lazy">
            <label class="flex items-center gap-1 cursor-pointer" title="Marcar como imagen por defecto">
                <input type="radio" name="imgDefault" value="${idx}" ${img.isDefault ? 'checked' : ''}
                    onchange="window.setImagenDefault(${idx})">
                <span class="text-xs text-yellow-600">⭐</span>
            </label>
            <span class="flex-1 text-xs text-gray-500 truncate" title="${img.url}">${img.url}</span>
            <button type="button" onclick="window.eliminarImagenModal(${idx})"
                class="text-red-400 hover:text-red-600 text-sm" title="Eliminar imagen">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(item);
    });
}

window.setImagenDefault = (idx) => {
    imagenesModal.forEach((img, i) => { img.isDefault = (i === idx); });
    renderizarImagenesModal();
};

window.eliminarImagenModal = (idx) => {
    const wasDefault = imagenesModal[idx].isDefault;
    imagenesModal.splice(idx, 1);
    if (wasDefault && imagenesModal.length > 0) {
        imagenesModal[0].isDefault = true;
    }
    renderizarImagenesModal();
};

// ── Modal abrir/cerrar ─────────────────────────────────────────────────────────
function abrirModal(producto = null) {
    const form = document.getElementById('formProducto');
    const user = Auth.getCurrentUser();
    const role = user ? user.role : '';

    form.reset();
    imagenesModal = [];
    document.getElementById('productoId').value = '';
    document.getElementById('contadorDescripcion').textContent = '0 / 500';
    document.getElementById('contadorProveedor').textContent = '0 / 150';
    document.getElementById('modalTitle').textContent = 'Nuevo Producto';

    // Visibilidad campo proveedor por rol
    const campoProveedor = document.getElementById('campoProveedor');
    campoProveedor.classList.toggle('hidden', role !== 'ADMIN');

    if (producto) {
        document.getElementById('modalTitle').textContent = 'Editar Producto';
        document.getElementById('productoId').value = producto.id;
        document.getElementById('nombre').value = producto.nombre || '';
        document.getElementById('precio').value = producto.precio || '';
        document.getElementById('descripcion').value = producto.descripcion || '';
        actualizarContador('descripcion', 'contadorDescripcion', 500);

        if (role === 'ADMIN') {
            document.getElementById('proveedor').value = producto.proveedor || '';
            actualizarContador('proveedor', 'contadorProveedor', 150);
        }

        const esFisico = producto.tipo === 'Fisico';
        document.getElementById('tipo').value = esFisico ? 'Fisico' : 'Digital';
        toggleCamposTipo();

        if (esFisico) {
            document.getElementById('stock').value = producto.stock || 0;
            document.getElementById('peso').value = producto.peso || 0;
        } else {
            document.getElementById('urlDescarga').value = producto.urlDescarga || '';
        }

        // Cargar imágenes existentes
        if (producto.imagenes && producto.imagenes.length > 0) {
            imagenesModal = producto.imagenes.map(img => ({
                url: img.imagenUrl,
                isDefault: img.isDefault
            }));
        }
    } else {
        toggleCamposTipo();
    }

    renderizarImagenesModal();
    UI.toggleModal('modalProducto', true);
}

// ── Guardar producto (POST / PUT) ──────────────────────────────────────────────
async function guardarProducto(e) {
    e.preventDefault();

    const id = document.getElementById('productoId').value;
    const user = Auth.getCurrentUser();
    const role = user ? user.role : '';
    const tipo = document.getElementById('tipo').value;

    // Validar al menos una imagen
    if (imagenesModal.length === 0) {
        UI.showNotification('El producto debe tener al menos una imagen', 'error');
        return;
    }

    // Asegurar que haya exactamente un default
    const hayDefault = imagenesModal.some(i => i.isDefault);
    if (!hayDefault) imagenesModal[0].isDefault = true;

    const defaultIndex = imagenesModal.findIndex(i => i.isDefault);

    const body = {
        nombre: document.getElementById('nombre').value.trim(),
        precio: parseFloat(document.getElementById('precio').value),
        tipo,
        descripcion: document.getElementById('descripcion').value.trim() || null,
        imagenesUrls: imagenesModal.map(i => i.url),
        defaultImageIndex: defaultIndex
    };

    // Proveedor — solo ADMIN lo envía
    if (role === 'ADMIN') {
        body.proveedor = document.getElementById('proveedor').value.trim() || null;
    }

    if (tipo === 'Fisico') {
        body.stock = parseInt(document.getElementById('stock').value) || 0;
        body.peso = parseFloat(document.getElementById('peso').value) || 0;
    } else {
        body.urlDescarga = document.getElementById('urlDescarga').value.trim() || null;
    }

    try {
        if (id) {
            await Api.put(`/productos/${id}`, body);
            UI.showNotification('Producto actualizado correctamente');
        } else {
            await Api.post('/productos', body);
            UI.showNotification('Producto creado correctamente');
        }
        UI.toggleModal('modalProducto', false);
        cargarProductos();
    } catch (error) {
        UI.showNotification('Error al guardar producto: ' + error.message, 'error');
    }
}

// ── Funciones globales (onclick inline) ───────────────────────────────────────
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
