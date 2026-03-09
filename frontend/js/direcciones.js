import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

// ── Estado local de teléfonos en el modal ─────────────────────────────────────
let telefonosModal = []; // string[]

document.addEventListener('DOMContentLoaded', () => {

    if (!Auth.requireAuth(['CUSTOMER', 'ADMIN', 'SUPPLIER'])) return;

    UI.renderNavBar({ containerId: 'mainNav', context: 'addresses' });
    cargarDirecciones();
    setupEventListeners();
});

// ── Event Listeners ────────────────────────────────────────────────────────────
function setupEventListeners() {
    document.getElementById('btnNuevaDireccion').addEventListener('click', () => abrirModal());
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('btnCancelar').addEventListener('click', cerrarModal);
    document.getElementById('formDireccion').addEventListener('submit', guardarDireccion);
    UI.setupModalCloser('modalDireccion');

    document.getElementById('btnAgregarTel').addEventListener('click', agregarTelefono);
    document.getElementById('nuevoTelefono').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') { e.preventDefault(); agregarTelefono(); }
    });

    // Contadores de caracteres
    const conteos = [
        ['alias', 'contadorAlias', 20],
        ['calle', 'contadorCalle', 25],
        ['numeroExterior', 'contadorNumExt', 10],
        ['numeroInterior', 'contadorNumInt', 10],
        ['referencia', 'contadorRef', 75],
        ['colonia', 'contadorColonia', 40],
        ['ciudad', 'contadorCiudad', 45],
        ['municipio', 'contadorMunicipio', 45],
        ['estado', 'contadorEstado', 25],
    ];
    conteos.forEach(([id, counterId, max]) => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('input', () => actualizarContador(id, counterId, max));
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

// ── API calls ─────────────────────────────────────────────────────────────────
async function cargarDirecciones() {
    try {
        const dirs = await Api.get('/direcciones/mis-direcciones');
        renderizarDirecciones(dirs);
    } catch (err) {
        UI.showNotification('Error al cargar direcciones: ' + err.message, 'error');
    }
}

// ── Renderizado de tarjetas ───────────────────────────────────────────────────
function renderizarDirecciones(dirs) {
    const container = document.getElementById('listaDirecciones');
    const emptyState = document.getElementById('emptyState');
    container.innerHTML = '';

    const user = Auth.getCurrentUser();
    const role = user ? user.role : '';

    if (!dirs || dirs.length === 0) {
        emptyState.classList.remove('hidden');
        return;
    }
    emptyState.classList.add('hidden');

    dirs.forEach(dir => {
        const card = document.createElement('article');
        card.className = `direccion-card${dir.preferida ? ' preferida' : ''}`;
        card.setAttribute('itemscope', '');
        card.setAttribute('itemtype', 'https://schema.org/PostalAddress');

        const tels = (dir.telefonos || []).map(t =>
            `<span class="tel-item text-xs text-gray-600"><i class="fas fa-phone text-blue-400 mr-1"></i>${t}</span>`
        ).join('');

        const botones = construirBotonesTarjeta(dir, role);

        card.innerHTML = `
            <div class="flex justify-between items-start mb-2">
                <div>
                    ${dir.alias ? `<h3 class="font-semibold text-gray-800">${dir.alias}</h3>` : ''}
                    ${dir.preferida ? '<span class="badge-preferida"><i class="fas fa-star"></i> Preferida</span>' : ''}
                </div>
                <div class="flex gap-1">${botones}</div>
            </div>
            <address class="not-italic text-sm text-gray-600 space-y-0.5">
                <p itemprop="streetAddress">
                    ${dir.calle} ${dir.numeroExterior}${dir.numeroInterior ? ', Int. ' + dir.numeroInterior : ''}
                </p>
                ${dir.referencia ? `<p class="text-xs text-gray-400">${dir.referencia}</p>` : ''}
                <p itemprop="addressLocality">${dir.colonia}</p>
                <p itemprop="addressCity">${dir.ciudad}</p>
                <p itemprop="addressRegion">${dir.municipio}, ${dir.estado}</p>
                <p itemprop="postalCode">C.P. ${dir.codigoPostal}</p>
            </address>
            ${tels ? `<div class="flex flex-wrap gap-1 mt-3">${tels}</div>` : ''}
        `;

        container.appendChild(card);
    });
}

function construirBotonesTarjeta(dir, role) {
    // CUSTOMER puede editar y eliminar sus propias direcciones
    // SUPPLIER puede editar pero NO eliminar
    let html = `
        <button onclick="window.editarDireccion(${dir.id})"
            class="w-8 h-8 rounded-full hover:bg-gray-100 text-yellow-500 transition" title="Editar">
            <i class="fas fa-edit"></i>
        </button>
    `;
    if (role !== 'SUPPLIER') {
        html += `
            <button onclick="window.eliminarDireccionPropia(${dir.id})"
                class="w-8 h-8 rounded-full hover:bg-gray-100 text-red-400 transition" title="Eliminar">
                <i class="fas fa-trash-alt"></i>
            </button>
        `;
    }
    return html;
}

// ── Teléfonos dentro del modal ────────────────────────────────────────────────
function agregarTelefono() {
    const input = document.getElementById('nuevoTelefono');
    const val = input.value.trim();

    if (!/^\d{10}$/.test(val)) {
        UI.showNotification('El teléfono debe tener exactamente 10 dígitos', 'error');
        return;
    }
    if (telefonosModal.includes(val)) {
        UI.showNotification('Ese número ya está en la lista', 'error');
        return;
    }
    telefonosModal.push(val);
    input.value = '';
    renderizarTelefonos();
}

function renderizarTelefonos() {
    const container = document.getElementById('telefonosContainer');
    const sinTels = document.getElementById('sinTelefonos');
    container.innerHTML = '';

    if (telefonosModal.length === 0) {
        sinTels.classList.remove('hidden');
        return;
    }
    sinTels.classList.add('hidden');

    telefonosModal.forEach((tel, idx) => {
        const item = document.createElement('div');
        item.className = 'tel-item';
        item.innerHTML = `
            <i class="fas fa-phone text-blue-400 text-xs"></i>
            <span class="flex-1 text-sm text-gray-700">${tel}</span>
            <button type="button" onclick="window.quitarTelefono(${idx})"
                class="text-red-400 hover:text-red-600 text-xs" title="Quitar">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(item);
    });
}

window.quitarTelefono = (idx) => {
    telefonosModal.splice(idx, 1);
    renderizarTelefonos();
};

// ── Modal ─────────────────────────────────────────────────────────────────────
function abrirModal(dir = null) {
    const form = document.getElementById('formDireccion');
    form.reset();
    telefonosModal = [];
    document.getElementById('direccionId').value = '';
    document.getElementById('modalTitle').textContent = dir ? 'Editar Dirección' : 'Nueva Dirección';

    // Reiniciar contadores
    ['contadorAlias', 'contadorCalle', 'contadorNumExt', 'contadorNumInt', 'contadorRef', 'contadorColonia', 'contadorCiudad', 'contadorMunicipio', 'contadorEstado'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = el.textContent.replace(/^\d+/, '0');
    });

    if (dir) {
        document.getElementById('direccionId').value = dir.id;
        document.getElementById('alias').value = dir.alias || '';
        document.getElementById('preferida').checked = !!dir.preferida;
        document.getElementById('calle').value = dir.calle || '';
        document.getElementById('numeroExterior').value = dir.numeroExterior || '';
        document.getElementById('numeroInterior').value = dir.numeroInterior || '';
        document.getElementById('referencia').value = dir.referencia || '';
        document.getElementById('colonia').value = dir.colonia || '';
        document.getElementById('ciudad').value = dir.ciudad || '';
        document.getElementById('municipio').value = dir.municipio || '';
        document.getElementById('estado').value = dir.estado || '';
        document.getElementById('codigoPostal').value = dir.codigoPostal || '';
        telefonosModal = [...(dir.telefonos || [])];

        // Actualizar contadores visualmente
        const campos = ['alias', 'calle', 'numeroExterior', 'numeroInterior', 'referencia', 'colonia', 'ciudad', 'municipio', 'estado'];
        const contadores = ['contadorAlias', 'contadorCalle', 'contadorNumExt', 'contadorNumInt', 'contadorRef', 'contadorColonia', 'contadorCiudad', 'contadorMunicipio', 'contadorEstado'];
        const maximos = [20, 25, 10, 10, 35, 40, 45, 45, 25];
        campos.forEach((id, i) => actualizarContador(id, contadores[i], maximos[i]));
    }

    renderizarTelefonos();
    UI.toggleModal('modalDireccion', true);
}

function cerrarModal() {
    UI.toggleModal('modalDireccion', false);
    telefonosModal = [];
}

// ── Guardar (POST / PUT) ──────────────────────────────────────────────────────
async function guardarDireccion(e) {
    e.preventDefault();

    const form = document.getElementById('formDireccion');

    // 🔥 Validación HTML5 estándar
    if (!form.checkValidity()) {
        form.reportValidity();   // Muestra errores nativos
        return;                  // ❌ NO enviar al backend
    }

    // Validar campos
    const campos = ['alias', 'calle', 'numeroExterior', 'colonia', 'ciudad', 'municipio', 'estado', 'codigoPostal'];
    const contadores = ['contadorAlias', 'contadorCalle', 'contadorNumExt', 'contadorColonia', 'contadorCiudad', 'contadorMunicipio', 'contadorEstado', 'contadorCP'];
    const maximos = [20, 25, 10, 40, 45, 45, 25, 5];
    campos.forEach((id, i) => {
        const val = document.getElementById(id).value.trim();
        if (!val) {
            UI.showNotification(`El campo "${id}" es obligatorio`, 'error');
            return;
        }
        actualizarContador(id, contadores[i], maximos[i]);
    });

    //Validar alias
    const alias = document.getElementById('alias').value.trim();
    if (!alias) {
        UI.showNotification('El alias es obligatorio', 'error');
        return;
    }

    // Validar CP
    const cp = document.getElementById('codigoPostal').value.trim();
    if (!/^\d{5}$/.test(cp)) {
        UI.showNotification('El código postal debe tener exactamente 5 dígitos', 'error');
        return;
    }

    // Validar mínimo un teléfono
    if (telefonosModal.length === 0) {
        document.getElementById('sinTelefonos').classList.remove('hidden');
        UI.showNotification('Debe agregar al menos un teléfono', 'error');
        return;
    }

    const id = document.getElementById('direccionId').value;

    const body = {
        preferida: document.getElementById('preferida').checked,
        alias: document.getElementById('alias').value.trim(),
        calle: document.getElementById('calle').value.trim(),
        numeroExterior: document.getElementById('numeroExterior').value.trim(),
        numeroInterior: document.getElementById('numeroInterior').value.trim() || null,
        referencia: document.getElementById('referencia').value.trim() || null,
        colonia: document.getElementById('colonia').value.trim(),
        ciudad: document.getElementById('ciudad').value.trim(),
        municipio: document.getElementById('municipio').value.trim(),
        estado: document.getElementById('estado').value.trim(),
        codigoPostal: cp,
        telefonos: [...telefonosModal]
    };

    try {
        if (id) {
            await Api.put(`/direcciones/${id}`, body);
            UI.showNotification('Dirección actualizada correctamente');
        } else {
            await Api.post('/direcciones', body);
            UI.showNotification('Dirección creada correctamente');
        }
        cerrarModal();
        cargarDirecciones();
    } catch (err) {
        UI.showNotification('Error al guardar: ' + err.message, 'error');
    }
}

// ── Funciones globales ─────────────────────────────────────────────────────────
window.editarDireccion = async (id) => {
    try {
        const dir = await Api.get(`/direcciones/${id}`);
        abrirModal(dir);
    } catch (err) {
        UI.showNotification('Error al cargar dirección', 'error');
    }
};

window.eliminarDireccionPropia = async (id) => {
    if (!confirm('¿Eliminar esta dirección?\n\nEsta acción no se puede deshacer.')) return;
    try {
        await Api.delete(`/direcciones/${id}`);
        UI.showNotification('Dirección eliminada');
        cargarDirecciones();
    } catch (err) {
        UI.showNotification('Error al eliminar: ' + err.message, 'error');
    }
};
