import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';
import ConfigService from './config.service.js';

let ordenesGlobal = [];
let usuariosGlobal = [];
let roleActual = '';
let userActual = {};

// Configuración del sistema (IVA + moneda)
let ivaSistema = 0.16;
let monedaSistema = 'MXN';

document.addEventListener('DOMContentLoaded', async () => {

    if (!Auth.requireAuth(["CUSTOMER", "ADMIN"])) return;

    userActual = Auth.getCurrentUser();
    roleActual = userActual.role;

    UI.renderNavBar({
        containerId: 'mainNav',
        context: 'orders'
    });

    const cfg = await ConfigService.load();

    ivaSistema = cfg.iva;
    monedaSistema = cfg.monedaSistema;

    if (roleActual === 'ADMIN') {
        document.getElementById('thUsuario').classList.remove('hidden');
        document.getElementById('filtroUsuario').classList.remove('hidden');
        cargarFiltroUsuarios();
        // Verificar notificaciones pendientes para el ADMIN (Req. 9)
        verificarNotificacionesPendientes();
    }

    setupEventListeners();
    await cargarOrdenes();
});

/**
 * Verifica si hay notificaciones pendientes para el ADMIN y muestra un diálogo ACK por cada una.
 */
async function verificarNotificacionesPendientes() {
    try {
        const pendientes = await Api.get('/notificaciones/pendientes');
        if (!pendientes || pendientes.length === 0) return;

        // Mostrar cada notificación una por una en un diálogo modal
        for (const notif of pendientes) {
            await mostrarDialogoNotificacion(notif);
        }

        // ACK — marcar todas como leídas
        const ids = pendientes.map(n => n.id);
        await Api.post('/notificaciones/ack', { ids });
    } catch (e) {
        console.error('[ordenes] Error al verificar notificaciones pendientes:', e);
    }
}

/**
 * Muestra un diálogo modal con el mensaje de notificación y espera a que el ADMIN lo confirme.
 */
function mostrarDialogoNotificacion(notif) {
    return new Promise((resolve) => {
        const overlay = document.createElement('div');
        overlay.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50';
        overlay.innerHTML = `
            <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 overflow-hidden">
                <div class="bg-yellow-50 border-b border-yellow-100 px-6 py-4 flex items-center gap-3">
                    <i class="fas fa-bell text-yellow-500 text-xl"></i>
                    <h3 class="text-lg font-bold text-gray-800">Notificación del Sistema</h3>
                </div>
                <div class="px-6 py-5">
                    <p class="text-sm text-gray-600 mb-1">${new Date(notif.fecha).toLocaleString('es-ES')}</p>
                    <p class="text-gray-800 font-medium">${notif.mensaje}</p>
                </div>
                <div class="px-6 py-4 bg-gray-50 flex justify-end">
                    <button id="btnAckNotif_${notif.id}" class="bg-blue-600 hover:bg-blue-700 text-white font-bold px-6 py-2 rounded-lg transition">
                        OK
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(overlay);
        document.getElementById(`btnAckNotif_${notif.id}`).addEventListener('click', () => {
            overlay.remove();
            resolve();
        });
    });
}

async function cargarFiltroUsuarios() {
    try {
        usuariosGlobal = await Api.get('/usuarios');
        const select = document.getElementById('filtroUsuario');
        usuariosGlobal.filter(u => u.role === 'CUSTOMER').forEach(u => {
            const opt = document.createElement('option');
            opt.value = u.id; // Warning: ordenDTO currently lacks usuarioId, but backend filter maps it? Let's check backend. Wait, filtering is in JS! We need `orden.id` or we can filter by exact string if backend didn't provide it? Let's just put u.nombre and match o.usuarioNombre 
            opt.textContent = u.nombre;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error('Error cargando usuarios para el filtro', e);
    }
}

function setupEventListeners() {
    document.getElementById('filtroEstado').addEventListener('change', renderizarOrdenes);
    document.getElementById('filtroUsuario').addEventListener('change', renderizarOrdenes);

    // Modal de Despacho
    window.cerrarModalDespacho = () => {
        document.getElementById('modalDespacho').classList.add('hidden');
        document.getElementById('formDespacho').reset();
        resetValidation('courier');
        resetValidation('trackingNumber');
    };

    const formDespacho = document.getElementById('formDespacho');
    if (formDespacho) {
        formDespacho.addEventListener('submit', handleSubmitDespacho);

        // Dynamic validation
        ['courier', 'trackingNumber'].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('input', () => validarInputDespacho(el));
                el.addEventListener('blur', () => validarInputDespacho(el));
            }
        });
    }

    // Modal Detalle
    window.cerrarModalDetalle = () => {
        document.getElementById('modalDetalle').classList.add('hidden');
    };
}

async function cargarOrdenes() {
    try {
        ordenesGlobal = await Api.get('/ordenes');

        // Sort by ID descending (newest first)
        ordenesGlobal.sort((a, b) => b.id - a.id);

        renderizarOrdenes();
    } catch (error) {
        UI.showNotification('Error al cargar órdenes. ' + error.message, 'error');
    }
}

function renderizarOrdenes() {
    const estadoFil = document.getElementById('filtroEstado').value;
    const userNameFil = document.getElementById('filtroUsuario').options[document.getElementById('filtroUsuario').selectedIndex].text;
    const isUserFilterActive = document.getElementById('filtroUsuario').value !== 'Todos';

    let filtradas = ordenesGlobal;

    if (estadoFil !== 'Todos') {
        filtradas = filtradas.filter(o => o.estado === estadoFil);
    }
    if (isUserFilterActive) {
        filtradas = filtradas.filter(o => o.usuarioNombre === userNameFil);
    }

    const tbody = document.getElementById('listaOrdenes');
    tbody.innerHTML = '';

    if (!filtradas || filtradas.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        document.querySelector('table').classList.add('hidden');
        return;
    }

    document.getElementById('emptyState').classList.add('hidden');
    document.querySelector('table').classList.remove('hidden');

    filtradas.forEach(orden => {
        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-50';

        const fecha = new Date(orden.fechaCreacion).toLocaleDateString('es-ES', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });

        const badgeHtml = getBadgeHTML(orden.estado);
        const actionHtml = getActionHTML(orden);
        const userHtml = roleActual === 'ADMIN' ? `<td class="py-3 px-6 text-left font-medium text-gray-700">${orden.usuarioNombre || 'Desconocido'}</td>` : '';

        row.innerHTML = `
            <td class="py-3 px-6 text-left whitespace-nowrap font-medium text-gray-800">#${orden.id}</td>
            ${userHtml}
            <td class="py-3 px-6 text-left">${badgeHtml}</td>
            <td class="py-3 px-6 text-left text-xs">${fecha}</td>
            <td class="py-3 px-6 text-right font-bold text-gray-700">${UI.formatCurrency(orden.total)}</td>
            <td class="py-3 px-6 text-center">
                <div class="flex item-center justify-center gap-2">
                    <button onclick="window.verDetalles(${orden.id})" class="text-blue-500 hover:text-blue-700 p-1" title="Ver Detalles">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${actionHtml}
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function getBadgeHTML(estado) {
    const colorMap = {
        'CREATED': 'bg-gray-200 text-gray-800',
        'PAYMENT_PENDING': 'bg-yellow-100 text-yellow-800',
        'PAID': 'bg-green-100 text-green-800',
        'OUT_OF_STOCK': 'bg-red-100 text-red-800',
        'CANCELLED': 'bg-gray-200 text-red-800',
        'SHIPPED': 'bg-blue-100 text-blue-800',
        'DELIVERED': 'bg-purple-100 text-purple-800'
    };
    const css = colorMap[estado] || 'bg-gray-100 text-gray-800';
    return `<span class="px-3 py-1 rounded-full text-xs font-bold ${css}">${estado}</span>`;
}

function getActionHTML(orden) {
    let html = '';
    const estado = orden.estado;

    if (roleActual === 'CUSTOMER') {
        UI.updateCartBadge();
        const puedePagar = estado === 'CREATED' || estado === 'PAYMENT_PENDING' || estado === 'OUT_OF_STOCK';
        if (puedePagar) {
            html += `<button onclick="window.iniciarPago(${orden.id})" class="text-green-500 hover:text-green-700 p-1" title="Pagar">
                        <i class="fas fa-credit-card"></i> Pagar
                     </button>`;
        }

        const puedeCancelar = estado === 'CREATED' || estado === 'PAYMENT_PENDING' || estado === 'OUT_OF_STOCK';
        if (puedeCancelar) {
            html += `<button onclick="window.cancelarOrden(${orden.id})" class="text-red-500 hover:text-red-700 p-1 ml-2" title="Cancelar">
                        <i class="fas fa-ban"></i>
                     </button>`;
        }
    } else if (roleActual === 'ADMIN') {
        const puedePagar = estado === 'CREATED' || estado === 'PAYMENT_PENDING' || estado === 'OUT_OF_STOCK';
        if (puedePagar) {
            html += `<button onclick="window.iniciarPago(${orden.id})" class="text-green-500 hover:text-green-700 p-1" title="Pagar">
                        <i class="fas fa-credit-card"></i> Pagar
                     </button>`;
        }

        const puedeCancelar = estado === 'CREATED' || estado === 'PAYMENT_PENDING' || estado === 'OUT_OF_STOCK';
        if (puedeCancelar) {
            html += `<button onclick="window.cancelarOrden(${orden.id})" class="text-red-500 hover:text-red-700 p-1 ml-2" title="Cancelar">
                        <i class="fas fa-ban"></i>
                     </button>`;
        }

        const hasPhysicalProducts = orden.detalles && orden.detalles.some(d => d.tipoProducto === 'FISICO');
        if (hasPhysicalProducts && estado === 'PAID') {
            html += `<button onclick="window.abrirModalDespacho(${orden.id})" class="text-blue-500 hover:text-blue-700 p-1" title="Despachar">
                        <i class="fas fa-truck"></i> Despachar
                     </button>`;
        } else if (hasPhysicalProducts && estado === 'SHIPPED') {
            html += `<button onclick="window.marcarEntregado(${orden.id})" class="text-purple-500 hover:text-purple-700 p-1" title="Marcar Entregado">
                        <i class="fas fa-box-open"></i> Entregar
                     </button>`;
        }
    }
    return html;
}

// ---------------- ACCIONES ----------------

window.iniciarPago = async (id) => {
    try {
        await Api.post(`/ordenes/${id}/pending`);
        window.location.href = `checkout.html?ordenId=${id}`;
    } catch (e) {
        UI.showNotification('Error al iniciar el pago: ' + e.message, 'error');
    }
};

window.cancelarOrden = async (id) => {
    if (!confirm('¿Estás seguro que deseas cancelar esta orden?')) return;
    try {
        await Api.post(`/ordenes/${id}/cancelar`);
        UI.showNotification('Orden cancelada exitosamente.', 'success');
        cargarOrdenes();
    } catch (e) {
        UI.showNotification('Error al cancelar: ' + e.message, 'error');
    }
};

window.marcarEntregado = async (id) => {
    if (!confirm('¿Confirmar que la orden fue entregada al cliente?')) return;
    try {
        await Api.post(`/shipments/entregar/${id}`);
        UI.showNotification('Orden marcada como entregada.', 'success');
        cargarOrdenes();
    } catch (e) {
        UI.showNotification('Error al entregar: ' + e.message, 'error');
    }
}

window.verDetalles = (id) => {
    const orden = ordenesGlobal.find(o => o.id === id);
    if (!orden) return;

    document.getElementById('detalleId').textContent = orden.id;
    const cont = document.getElementById('detalleContenido');

    let html = `
        <div class="grid grid-cols-2 gap-4 mb-4 text-sm">
            <div>
                <p class="text-gray-500 mb-1">Estado Actual</p>
                ${getBadgeHTML(orden.estado)}
            </div>
    `;

    if (orden.direccionEnvio) {
        const dir = orden.direccionEnvio;
        html += `
            <div>
                <p class="text-gray-500 mb-1">Dirección de Envío</p>
                <div class="bg-gray-50 p-2 rounded text-xs border border-gray-200">
                    <p class="font-bold">${dir.alias || 'Dirección'}</p>
                    <p>${dir.calle} ${dir.numeroExterior}${dir.numeroInterior ? ' Int ' + dir.numeroInterior : ''}</p>
                    <p>${dir.colonia}</p>
                    <p>${dir.ciudad}, ${dir.estado} CP ${dir.codigoPostal}</p>
                </div>
            </div>
        `;
    }

    html += `</div>`;

    // ------- RFC: usar el guardado en la orden (post-PAID) o el del usuario -------
    const estadosPagados = ['PAID', 'SHIPPED', 'DELIVERED'];
    const esPagada = estadosPagados.includes(orden.estado);

    const ivaTasa = esPagada && orden.ivaTasa != null ? orden.ivaTasa : ivaSistema;
    const ivaPct = Math.round(ivaTasa * 100);
    const subtotalProductos = orden.subtotalProductos != null
        ? orden.subtotalProductos
        : orden.detalles.reduce((s, d) => s + (d.subtotal || 0), 0);
    const montoIva = subtotalProductos * ivaTasa;
    const totalCalculado = subtotalProductos + montoIva;

    // RFC mostrado
    const rfcMostrar = esPagada && orden.rfcCliente ? orden.rfcCliente : orden.usuarioRfcCurp;
    if (rfcMostrar) {
        html += `
            <div class="bg-blue-50 p-3 rounded-lg mb-4 text-sm border border-blue-200">
                <h4 class="font-bold text-blue-800 mb-1"><i class="fas fa-id-card mr-1"></i> RFC/CURP: </h4>
                <p class="font-mono text-gray-700 font-semibold">${rfcMostrar}</p>
                ${esPagada && orden.rfcCliente
                ? '<p class="text-xs text-green-600 mt-1"><i class="fas fa-lock mr-1"></i>Capturado al momento del pago</p>'
                : ''}
            </div>
        `;
    }

    function getNombreProducto(detalle) {
        return detalle.productoNombre
            || (detalle.producto ? detalle.producto.nombre : 'Producto');
    }

    if (orden.paymentTransaction) {
        const trx = orden.paymentTransaction;
        html += `
            <div class="bg-gray-50 p-4 rounded-lg mb-4 text-sm border border-gray-200">
                <h4 class="font-bold text-gray-700 mb-2 border-b pb-1"><i class="fas fa-money-check-alt mr-2 text-green-600"></i> Información de Pago</h4>
                <div class="grid grid-cols-2 gap-2">
                    <p><span class="font-medium">Método:</span> ${trx.paymentMethod}</p>
                    <p><span class="font-medium">Estado:</span> ${trx.status}</p>
                    <p><span class="font-medium">Transacción ID:</span> #${trx.id}</p>
                    <p><span class="font-medium">Pagado el:</span> ${new Date(trx.transactionDate).toLocaleString('es-ES')}</p>
                </div>
            </div>
        `;
    }

    if (orden.shipment) {
        const shp = orden.shipment;
        html += `
            <div class="bg-blue-50 p-4 rounded-lg mb-4 text-sm border border-blue-100">
                <h4 class="font-bold text-blue-800 mb-2 border-b border-blue-200 pb-1"><i class="fas fa-truck mr-2"></i> Información de Envío</h4>
                <div class="grid grid-cols-2 gap-2">
                    <p><span class="font-medium">Courier:</span> ${shp.courier}</p>
                    <p><span class="font-medium">Tracking:</span> <a href="#" class="text-blue-600 underline">${shp.trackingNumber}</a></p>
                    <p><span class="font-medium">Despachado:</span> ${new Date(shp.dateDispatch).toLocaleString('es-ES')}</p>
                    <p><span class="font-medium">Entregado:</span> ${shp.dateDelivered ? new Date(shp.dateDelivered).toLocaleString('es-ES') : '<span class="italic text-gray-500">Pendiente</span>'}</p>
                </div>
            </div>
        `;
    }

    const digitalProducts = orden.detalles.filter(d => d.tipoProducto === 'DIGITAL' && d.urlDescarga);
    if (digitalProducts.length > 0) {
        html += `
            <div class="bg-indigo-50 p-4 rounded-lg mb-4 text-sm border border-indigo-100">
                <h4 class="font-bold text-indigo-800 mb-2 border-b border-indigo-200 pb-1"><i class="fas fa-cloud-download-alt mr-2"></i> Descargas Digitales</h4>
                <ul class="space-y-1">
        `;
        digitalProducts.forEach(d => {
            html += `
                    <li class="flex justify-between items-center py-1 border-b border-indigo-100 last:border-0">
                        <span class="font-medium text-gray-800">${getNombreProducto(d)}</span>
                        ${esPagada ? `<a href="${d.urlDescarga}" target="_blank" class="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1 rounded text-xs transition"><i class="fas fa-download mr-1"></i> Descargar</a>` : `<span class="text-xs text-gray-500 italic">Disponible tras pago</span>`}
                    </li>
            `;
        });
        html += `
                </ul>
            </div>
        `;
    }

    html += `
        <div>
            <h4 class="font-bold text-gray-700 mb-2 text-sm">Productos (${orden.detalles.length})</h4>
            <div class="max-h-48 overflow-y-auto border rounded-lg p-2 bg-white">
                <ul class="space-y-2">
    `;

    orden.detalles.forEach(d => {

        const subtotal =
            d.subtotal ||
            (d.precio * d.cantidad);

        html += `
            <li class="flex justify-between items-center text-sm p-2 hover:bg-gray-50 rounded">
                <div>
                    <span class="font-bold text-gray-800 mr-2">${d.cantidad} x</span> 
                    <span>${getNombreProducto(d)}</span>
                </div>
                <span class="font-medium">${UI.formatCurrency(subtotal)}</span>
            </li>
        `;
    });

    html += `</ul></div></div>
            <div class="mt-4 bg-gray-50 rounded-lg border border-gray-200 p-3 text-sm">
                <div class="flex justify-between mb-1">
                    <span class="text-gray-600">Subtotal (${monedaSistema})</span>
                    <span class="font-medium">${UI.formatCurrency(subtotalProductos)}</span>
                </div>
                <div class="flex justify-between mb-2">
                    <span class="text-gray-600">IVA (${ivaPct}%)${esPagada ? ' <span class="text-xs text-green-600 ml-1">(tasa al pago)</span>' : ''}</span>
                    <span class="font-medium">${UI.formatCurrency(montoIva)}</span>
                </div>
                <div class="flex justify-between font-bold border-t pt-2">
                    <span class="text-gray-800">Total (${monedaSistema})</span>
                    <span class="text-blue-600 text-base">${UI.formatCurrency(totalCalculado)}</span>
                </div>
            </div>`;

    cont.innerHTML = html;
    document.getElementById('modalDetalle').classList.remove('hidden');
}


// ---------------- MODAL DESPACHO ADMIN ----------------

const despachoValidation = {
    courier: false,
    trackingNumber: false
};

window.abrirModalDespacho = (id) => {
    const orden = ordenesGlobal.find(o => o.id === id);
    document.getElementById('despachoOrdenId').textContent = id;

    // Si la orden tiene direccion, mostrarla para que el admin sepa a donde enviar
    const despachoInfo = document.getElementById('despachoDireccionInfo');
    if (despachoInfo) {
        if (orden && orden.direccionEnvio) {
            const dir = orden.direccionEnvio;
            despachoInfo.innerHTML = `
                <div class="bg-blue-50 p-3 rounded-lg mb-4 text-sm border border-blue-200">
                    <h4 class="font-bold text-blue-800 mb-1"><i class="fas fa-map-marker-alt mr-1"></i> Destino del Envío</h4>
                    <p>${dir.calle} ${dir.numeroExterior}${dir.numeroInterior ? ' Int ' + dir.numeroInterior : ''}</p>
                    <p>${dir.colonia}, ${dir.municipio}</p>
                    <p>${dir.ciudad}, ${dir.estado}. CP: ${dir.codigoPostal}</p>
                    <p class="text-xs text-blue-600 mt-1">Tel: ${dir.telefonos?.join(', ') || 'N/A'}</p>
                </div>
            `;
            despachoInfo.classList.remove('hidden');
        } else {
            despachoInfo.classList.add('hidden');
        }
    }

    window.ordenDespachoActualId = id;
    cerrarModalDespacho(); // Reset
    document.getElementById('modalDespacho').classList.remove('hidden');
    checkFormDespacho();
};

function validarInputDespacho(input) {
    const id = input.id;
    const value = input.value.trim();
    let isValid = false;
    const errorMsg = document.getElementById(`error-${id}`);

    if (id === 'courier') {
        isValid = value.length >= 2;
    } else if (id === 'trackingNumber') {
        isValid = value.length >= 5;
    }

    despachoValidation[id] = isValid;

    if (isValid) {
        input.classList.remove('border-red-500', 'invalid');
        input.classList.add('border-green-500', 'valid');
        if (errorMsg) errorMsg.classList.add('hidden');
    } else {
        input.classList.remove('border-green-500', 'valid');
        input.classList.add('border-red-500', 'invalid');
        if (errorMsg) errorMsg.classList.remove('hidden');
    }

    checkFormDespacho();
}

function checkFormDespacho() {
    const valid = despachoValidation.courier && despachoValidation.trackingNumber;
    document.getElementById('btnConfirmarDespacho').disabled = !valid;
}

function resetValidation(id) {
    despachoValidation[id] = false;
    const el = document.getElementById(id);
    if (el) {
        el.classList.remove('border-red-500', 'border-green-500', 'valid', 'invalid');
    }
    const err = document.getElementById(`error-${id}`);
    if (err) err.classList.add('hidden');
}

async function handleSubmitDespacho(e) {
    e.preventDefault();
    if (!despachoValidation.courier || !despachoValidation.trackingNumber) return;

    const btn = document.getElementById('btnConfirmarDespacho');
    btn.disabled = true;
    btn.innerHTML = 'Procesando...';

    const courier = document.getElementById('courier').value.trim();
    const trackingNumber = document.getElementById('trackingNumber').value.trim();
    const id = window.ordenDespachoActualId;

    try {
        await Api.post(`/shipments/despachar/${id}`, { courier, trackingNumber });
        UI.showNotification('Orden despachada con éxito', 'success');
        cerrarModalDespacho();
        cargarOrdenes();
    } catch (error) {
        UI.showNotification('Error: ' + error.message, 'error');
        btn.disabled = false;
        btn.innerHTML = 'Confirmar Despacho';
    }
}
