import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

// ── Estado admin address panel ────────────────────────────────────────────────
let adminTargetUserId = null;
let adirTelefonos = [];  // teléfonos en el sub-form de dirección
let usuariosGlobal = []; // Stores all users for filtering

document.addEventListener('DOMContentLoaded', () => {

    if (!Auth.requireAuth(["ADMIN"])) return;

    const user = Auth.getCurrentUser();
    if (user.role !== 'ADMIN') {
        window.location.href = 'productos.html';
        return;
    }

    document.getElementById('btnNuevoUsuario').classList.remove('hidden');
    document.getElementById('btnNuevoUsuario').classList.add('flex');

    UI.renderNavBar({ containerId: 'mainNav', context: 'users' });
    cargarUsuarios();
    setupEventListeners();
});

// ── Event Listeners ────────────────────────────────────────────────────────────
function setupEventListeners() {
    // Modal usuario
    document.getElementById('btnNuevoUsuario').addEventListener('click', () => abrirModal());
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('btnCancelar').addEventListener('click', cerrarModal);
    document.getElementById('formUsuario').addEventListener('submit', guardarUsuario);
    document.getElementById('role').addEventListener('change', window.toggleUserTypeFields);
    document.getElementById('filtroRol').addEventListener('change', renderizarUsuarios);
    UI.setupModalCloser('modalUsuario');

    // Modal direcciones (admin)
    document.getElementById('btnCerrarDirModal').addEventListener('click', cerrarModalDirecciones);
    UI.setupModalCloser('modalDirecciones');
    document.getElementById('btnNuevaDirAdmin').addEventListener('click', () => abrirFormDir());
    document.getElementById('btnCancelarDirAdmin').addEventListener('click', cerrarFormDir);
    document.getElementById('formDirAdmin').addEventListener('submit', guardarDireccionAdmin);

    // Teléfonos en form dir admin
    document.getElementById('adirBtnAgregarTel').addEventListener('click', agregarTelAdmin);
    document.getElementById('adirNuevoTel').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') { e.preventDefault(); agregarTelAdmin(); }
    });
}

// ── Usuarios ──────────────────────────────────────────────────────────────────
async function cargarUsuarios() {
    try {
        usuariosGlobal = await Api.get('/usuarios');
        renderizarUsuarios();
    } catch (error) {
        UI.showNotification('Error al cargar usuarios: ' + error.message, 'error');
    }
}

function renderizarUsuarios() {
    const filtro = document.getElementById('filtroRol').value;
    
    let filtrados = usuariosGlobal;
    if (filtro && filtro !== 'Todos') {
        filtrados = usuariosGlobal.filter(u => u.role === filtro);
    }

    const tbody = document.getElementById('listaUsuarios');
    tbody.innerHTML = '';

    if (!filtrados || filtrados.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        return;
    }
    document.getElementById('emptyState').classList.add('hidden');

    const roleNames = { ADMIN: 'Administrador', SUPPLIER: 'Proveedor', CUSTOMER: 'Cliente' };
    const roleColors = {
        ADMIN: 'bg-red-100 text-red-800',
        SUPPLIER: 'bg-blue-100 text-blue-800',
        CUSTOMER: 'bg-green-100 text-green-800'
    };

    filtrados.forEach(usuario => {
        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-50';
        const roleDisplay = roleNames[usuario.role] || usuario.role;
        const roleColor = roleColors[usuario.role] || 'bg-gray-100 text-gray-800';

        row.innerHTML = `
            <td class="py-3 px-6 font-medium">#${usuario.id}</td>
            <td class="py-3 px-6">${usuario.nombre}</td>
            <td class="py-3 px-6">${usuario.email}</td>
            <td class="py-3 px-6">
                <span class="px-2 py-1 rounded text-xs font-semibold ${roleColor}">${roleDisplay}</span>
            </td>
            <td class="py-3 px-6 text-center">
                <!-- Editar usuario -->
                <button onclick="window.editarUsuario(${usuario.id})"
                    class="text-yellow-500 hover:text-yellow-700 mx-1" title="Editar usuario">
                    <i class="fas fa-edit"></i>
                </button>
                <!-- Gestionar direcciones (icono engrane) -->
                <button onclick="window.gestionarDirecciones(${usuario.id}, '${usuario.nombre}')"
                    class="text-gray-500 hover:text-blue-600 mx-1" title="Gestionar direcciones">
                    <i class="fas fa-cog"></i>
                </button>
                <!-- Eliminar usuario -->
                <button onclick="window.eliminarUsuario(${usuario.id})"
                    class="text-red-500 hover:text-red-700 mx-1" title="Eliminar usuario">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// ── Modal Usuario ─────────────────────────────────────────────────────────────
function abrirModal(usuario = null) {
    const form = document.getElementById('formUsuario');
    const passwordField = document.getElementById('password');
    form.reset();
    document.getElementById('error-rfcCurp').classList.add('hidden');
    document.getElementById('rfcCurpContainer').classList.add('hidden');
    document.getElementById('empresaContainer').classList.add('hidden');
    document.getElementById('vigenciaContainer').classList.add('hidden');

    if (usuario) {
        document.getElementById('modalTitle').textContent = 'Editar Usuario';
        document.getElementById('usuarioId').value = usuario.id;
        document.getElementById('nombre').value = usuario.nombre;
        document.getElementById('email').value = usuario.email;
        document.getElementById('role').value = usuario.role;
        if (usuario.fechaNacimiento) document.getElementById('fechaNacimiento').value = usuario.fechaNacimiento;
        if (usuario.rfcCurp) document.getElementById('rfcCurp').value = usuario.rfcCurp;
        if (usuario.empresa) document.getElementById('empresa').value = usuario.empresa;
        if (usuario.validUntil) document.getElementById('validUntil').value = usuario.validUntil;
        passwordField.removeAttribute('required');
        window.toggleUserTypeFields();
    } else {
        document.getElementById('modalTitle').textContent = 'Nuevo Usuario';
        document.getElementById('usuarioId').value = '';
        passwordField.setAttribute('required', 'required');
    }
    UI.toggleModal('modalUsuario', true);
}

function cerrarModal() { UI.toggleModal('modalUsuario', false); }

async function guardarUsuario(e) {
    e.preventDefault();
    const usuarioId = document.getElementById('usuarioId').value;
    const datos = {
        nombre: document.getElementById('nombre').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: document.getElementById('role').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value || null
    };
    // Add role-specific details
    if (datos.role === 'CUSTOMER') {
        const rfcCurp = document.getElementById('rfcCurp').value.trim();
        const regexRfc = /^[A-Za-z]{4}[0-9]{6}[A-Za-z0-9]{3}$/;
        const regexCurp = /^[A-Za-z]{4}[0-9]{6}[HM][A-Za-z]{5}[0-9]{2}$/;
        
        if (!regexRfc.test(rfcCurp) && !regexCurp.test(rfcCurp)) {
            document.getElementById('error-rfcCurp').classList.remove('hidden');
            return;
        }
        document.getElementById('error-rfcCurp').classList.add('hidden');
        datos.rfcCurp = rfcCurp;
    } else if (datos.role === 'SUPPLIER') {
        datos.empresa = document.getElementById('empresa').value.trim();
    } else if (datos.role === 'ADMIN') {
        datos.validUntil = document.getElementById('validUntil').value;
    }

    if (usuarioId && !datos.password) delete datos.password;

    try {
        if (usuarioId) {
            await Api.put(`/usuarios/${usuarioId}`, datos);
            UI.showNotification('Usuario actualizado exitosamente');
        } else {
            const endpointMap = { ADMIN: '/usuarios/admin', SUPPLIER: '/usuarios/proveedor', CUSTOMER: '/usuarios/cliente' };
            const endpoint = endpointMap[datos.role] || '/usuarios/cliente';
            await Api.post(endpoint, datos);
            UI.showNotification('Usuario creado exitosamente');
        }
        cerrarModal();
        cargarUsuarios();
    } catch (error) {
        UI.showNotification('Error al guardar usuario: ' + error.message, 'error');
    }
}

// ── Modal Direcciones (Admin) ──────────────────────────────────────────────────
window.gestionarDirecciones = async (usuarioId, nombre) => {
    adminTargetUserId = usuarioId;
    document.getElementById('modalDirTitle').textContent = `Direcciones de ${nombre}`;
    cerrarFormDir();
    await cargarDireccionesDeUsuario(usuarioId);
    UI.toggleModal('modalDirecciones', true);
};

async function cargarDireccionesDeUsuario(usuarioId) {
    try {
        const dirs = await Api.get(`/direcciones/usuario/${usuarioId}`);
        renderizarListaAdmin(dirs);
    } catch (err) {
        UI.showNotification('Error al cargar direcciones: ' + err.message, 'error');
    }
}

function renderizarListaAdmin(dirs) {
    const lista = document.getElementById('listaDireccionesAdmin');
    const empty = document.getElementById('emptyDirAdmin');
    lista.innerHTML = '';

    if (!dirs || dirs.length === 0) {
        empty.classList.remove('hidden');
        return;
    }
    empty.classList.add('hidden');

    dirs.forEach(dir => {
        const tels = (dir.telefonos || []).map(t =>
            `<span class="tel-item"><i class="fas fa-phone text-blue-400 mr-1 text-xs"></i>${t}</span>`
        ).join('');

        const card = document.createElement('div');
        card.className = `addr-card${dir.preferida ? ' preferida' : ''}`;
        card.innerHTML = `
            <div class="flex justify-between items-start">
                <div>
                    ${dir.alias ? `<span class="font-medium text-gray-800 text-sm">${dir.alias}</span> ` : ''}
                    ${dir.preferida ? '<span class="badge-preferida">⭐ Preferida</span>' : ''}
                    <div class="text-xs text-gray-600 mt-1">
                        ${dir.calle} ${dir.numeroExterior}${dir.numeroInterior ? ', Int. ' + dir.numeroInterior : ''},
                        ${dir.colonia}, ${dir.municipio}, ${dir.estado} – C.P. ${dir.codigoPostal}
                    </div>
                    ${tels ? `<div class="flex flex-wrap gap-1 mt-1.5">${tels}</div>` : ''}
                </div>
                <div class="flex gap-1 shrink-0 ml-2">
                    <button onclick="window.editarDirAdmin(${dir.id})"
                        class="text-yellow-500 hover:text-yellow-700 text-sm" title="Editar">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="window.eliminarDirAdmin(${dir.id})"
                        class="text-red-400 hover:text-red-600 text-sm" title="Eliminar">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </div>
        `;
        lista.appendChild(card);
    });
}

// Sub-form crear / editar
function abrirFormDir(dir = null) {
    const wrapper = document.getElementById('formDirAdminWrapper');
    document.getElementById('formDirAdmin').reset();
    adirTelefonos = [];
    document.getElementById('adirId').value = '';
    document.getElementById('formDirAdminTitle').textContent = dir ? 'Editar Dirección' : 'Nueva Dirección';

    if (dir) {
        document.getElementById('adirId').value = dir.id;
        document.getElementById('adirAlias').value = dir.alias || '';
        document.getElementById('adirPreferida').checked = !!dir.preferida;
        document.getElementById('adirCalle').value = dir.calle || '';
        document.getElementById('adirNumExt').value = dir.numeroExterior || '';
        document.getElementById('adirNumInt').value = dir.numeroInterior || '';
        document.getElementById('adirRef').value = dir.referencia || '';
        document.getElementById('adirColonia').value = dir.colonia || '';
        document.getElementById('adirMunicipio').value = dir.municipio || '';
        document.getElementById('adirEstado').value = dir.estado || '';
        document.getElementById('adirCP').value = dir.codigoPostal || '';
        adirTelefonos = [...(dir.telefonos || [])];
    }
    renderizarTelAdmin();
    wrapper.classList.remove('hidden');
    wrapper.scrollIntoView({ behavior: 'smooth' });
}

function cerrarFormDir() {
    document.getElementById('formDirAdminWrapper').classList.add('hidden');
    adirTelefonos = [];
}

function cerrarModalDirecciones() {
    UI.toggleModal('modalDirecciones', false);
    adminTargetUserId = null;
    cerrarFormDir();
}

// Teléfonos en form admin
function agregarTelAdmin() {
    const input = document.getElementById('adirNuevoTel');
    const val = input.value.trim();
    if (!/^\d{10}$/.test(val)) {
        UI.showNotification('Teléfono: exactamente 10 dígitos', 'error');
        return;
    }
    if (adirTelefonos.includes(val)) { UI.showNotification('Ese número ya está en la lista', 'error'); return; }
    adirTelefonos.push(val);
    input.value = '';
    renderizarTelAdmin();
}

function renderizarTelAdmin() {
    const container = document.getElementById('adirTelContainer');
    container.innerHTML = '';
    adirTelefonos.forEach((tel, idx) => {
        const item = document.createElement('div');
        item.className = 'tel-item';
        item.innerHTML = `
            <i class="fas fa-phone text-blue-400 text-xs"></i>
            <span class="flex-1 text-xs">${tel}</span>
            <button type="button" onclick="window.adminQuitarTel(${idx})" class="text-red-400 hover:text-red-600 text-xs">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(item);
    });
}

window.adminQuitarTel = (idx) => {
    adirTelefonos.splice(idx, 1);
    renderizarTelAdmin();
};

async function guardarDireccionAdmin(e) {
    e.preventDefault();
    if (!adminTargetUserId) return;

    const cp = document.getElementById('adirCP').value.trim();
    if (!/^\d{5}$/.test(cp)) { UI.showNotification('C.P.: exactamente 5 dígitos', 'error'); return; }
    if (adirTelefonos.length === 0) { UI.showNotification('Agrega al menos un teléfono', 'error'); return; }

    const id = document.getElementById('adirId').value;
    const body = {
        preferida: document.getElementById('adirPreferida').checked,
        alias: document.getElementById('adirAlias').value.trim(),
        calle: document.getElementById('adirCalle').value.trim(),
        numeroExterior: document.getElementById('adirNumExt').value.trim(),
        numeroInterior: document.getElementById('adirNumInt').value.trim() || null,
        referencia: document.getElementById('adirRef').value.trim() || null,
        colonia: document.getElementById('adirColonia').value.trim(),
        municipio: document.getElementById('adirMunicipio').value.trim(),
        estado: document.getElementById('adirEstado').value.trim(),
        codigoPostal: cp,
        telefonos: [...adirTelefonos]
    };

    try {
        if (id) {
            await Api.put(`/direcciones/${id}`, body);
            UI.showNotification('Dirección actualizada');
        } else {
            await Api.post(`/direcciones/usuario/${adminTargetUserId}`, body);
            UI.showNotification('Dirección creada');
        }
        cerrarFormDir();
        await cargarDireccionesDeUsuario(adminTargetUserId);
    } catch (err) {
        UI.showNotification('Error al guardar dirección: ' + err.message, 'error');
    }
}

window.editarDirAdmin = async (id) => {
    try {
        const dir = await Api.get(`/direcciones/${id}`);
        abrirFormDir(dir);
    } catch (err) {
        UI.showNotification('Error al cargar dirección', 'error');
    }
};

window.eliminarDirAdmin = async (id) => {
    if (!confirm('¿Eliminar esta dirección? Esta acción no se puede deshacer.')) return;
    try {
        await Api.delete(`/direcciones/${id}`);
        UI.showNotification('Dirección eliminada');
        await cargarDireccionesDeUsuario(adminTargetUserId);
    } catch (err) {
        UI.showNotification('Error al eliminar dirección: ' + err.message, 'error');
    }
};

// ── Funciones globales usuario ────────────────────────────────────────────────
window.editarUsuario = async (id) => {
    try { const u = await Api.get(`/usuarios/${id}`); abrirModal(u); }
    catch (error) { UI.showNotification('Error al cargar usuario', 'error'); }
};

window.eliminarUsuario = async (id) => {
    if (!confirm('¿Eliminar este usuario?\n\nEsta acción no se puede deshacer.')) return;
    try {
        await Api.delete(`/usuarios/${id}`);
        UI.showNotification('Usuario eliminado exitosamente');
        cargarUsuarios();
    } catch (error) {
        UI.showNotification('Error al eliminar usuario: ' + error.message, 'error');
    }
};

window.toggleUserTypeFields = () => {
    const role = document.getElementById('role').value;
    
    const rfcCurpC = document.getElementById('rfcCurpContainer');
    const empresaC = document.getElementById('empresaContainer');
    const vigenciaC = document.getElementById('vigenciaContainer');
    
    const rfcCurpI = document.getElementById('rfcCurp');
    const empresaI = document.getElementById('empresa');
    const validUntilI = document.getElementById('validUntil');
    
    // Reset presentation and required attributes
    rfcCurpC.classList.add('hidden');
    empresaC.classList.add('hidden');
    vigenciaC.classList.add('hidden');
    
    rfcCurpI.removeAttribute('required');
    empresaI.removeAttribute('required');
    validUntilI.removeAttribute('required');
    
    if (role === 'CUSTOMER') {
        rfcCurpC.classList.remove('hidden');
        rfcCurpI.setAttribute('required', 'required');
    } else if (role === 'SUPPLIER') {
        empresaC.classList.remove('hidden');
        empresaI.setAttribute('required', 'required');
    } else if (role === 'ADMIN') {
        vigenciaC.classList.remove('hidden');
        validUntilI.setAttribute('required', 'required');
    }
};
