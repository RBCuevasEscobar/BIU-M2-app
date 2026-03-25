import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';
import ConfigService from './config.service.js';

// ---- Configuración del sistema (IVA + moneda) ----
let ivaSistema = 0.16; // valor por defecto hasta que se cargue el API
let monedaSistema = 'MXN';
let maxProductosOrden = 999; // sin límite hasta que se cargue

document.addEventListener('DOMContentLoaded', async () => {

    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    UI.renderNavBar({
        containerId: 'mainNav',
        context: 'cart'
    });

    const cfg = await ConfigService.load();

    ivaSistema = cfg.iva;
    monedaSistema = cfg.monedaSistema;
    maxProductosOrden = cfg.maxProductosOrden;

    actualizarLabels();

    cargarCarrito();
    setupEventListeners();
});

function actualizarLabels() {
    const lbSub = document.getElementById('subtotalLabel');
    const lbIva = document.getElementById('ivaLabel');
    const lbTot = document.getElementById('totalLabel');

    if (lbSub) lbSub.textContent = `Subtotal (${monedaSistema})`;
    if (lbIva) lbIva.textContent = `IVA (${Math.round(ivaSistema * 100)}%)`;
    if (lbTot) lbTot.textContent = `Total (${monedaSistema})`;
}

function setupEventListeners() {
    // Phase 2: Order implementation
    const btnOrder = document.getElementById('btnOrder');
    if (btnOrder) {
        btnOrder.addEventListener('click', realizarOrder);
    }

    const btnVaciar = document.getElementById('btnVaciar');
    if (btnVaciar) {
        btnVaciar.addEventListener('click', vaciarCarrito);
    }
}

async function cargarCarrito() {
    try {
        const carrito = await Api.get('/carrito');
        renderizarCarrito(carrito);
    } catch (error) {
        // If 404, it might mean empty logic so we show empty state
        if (error.message && error.message.includes('404')) {
            renderizarCarrito({ productos: [] });
        } else {
            // UI.showNotification('Error al cargar el carrito ' + error.message, 'error');
            // Fallback for empty
            renderizarCarrito({ productos: [] });
        }
    }
}

function actualizarEstadoAccionesCarrito(hayProductos) {
    const btnOrder = document.getElementById('btnOrder');
    const btnVaciar = document.getElementById('btnVaciar');

    [btnOrder, btnVaciar].forEach(btn => {
        if (!btn) return;

        btn.disabled = !hayProductos;

        if (!hayProductos) {
            btn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            btn.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    });
}

function renderizarCarrito(carrito) {
    const tbody = document.getElementById('listaCarrito');
    if (!tbody) return;

    const emptyCartMsg = document.getElementById('emptyCart');

    tbody.innerHTML = '';

    const productos = carrito.productos || [];

    // ✅ Caso carrito vacío
    if (productos.length === 0) {
        if (emptyCartMsg) emptyCartMsg.classList.remove('hidden');

        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4 text-gray-500">
                    El carrito está vacío
                </td>
            </tr>
        `;

        actualizarTotales(0);
        actualizarEstadoAccionesCarrito(false);
        return;
    }

    // ✅ Hay productos
    if (emptyCartMsg) emptyCartMsg.classList.add('hidden');

    // Agrupar productos
    const productosMap = new Map();

    productos.forEach(p => {
        if (productosMap.has(p.id)) {
            productosMap.get(p.id).cantidad++;
        } else {
            productosMap.set(p.id, { ...p, cantidad: 1 });
        }
    });

    let subtotal = 0;

    productosMap.forEach(p => {
        const totalProducto = p.precio * p.cantidad;
        subtotal += totalProducto;

        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200';
        row.innerHTML = `
            <td class="py-3 px-6 text-left font-medium">${p.nombre}</td>
            <td class="py-3 px-6 text-left">${UI.formatCurrency(p.precio)}</td>
            <td class="py-3 px-6 text-center">
                <div class="flex items-center justify-center gap-2">
                    <button class="bg-gray-200 px-2 rounded hover:bg-gray-300" onclick="window.eliminarDelCarrito(${p.id})">-</button>
                    <span>${p.cantidad}</span>
                    <button class="bg-gray-200 px-2 rounded hover:bg-gray-300" onclick="window.agregarAlCarrito(${p.id})">+</button>
                </div>
            </td>
            <td class="py-3 px-6 text-right font-bold">${UI.formatCurrency(totalProducto)}</td>
            <td class="py-3 px-6 text-center">
                <button class="text-red-500 hover:text-red-700" onclick="window.eliminarDelCarrito(${p.id})">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

    // Validar máximo de ítems (Req. 10)
    const cantidadTotal = productos.length;
    const maxItemsWarning = document.getElementById('maxItemsWarning');
    const maxItemsMsg = document.getElementById('maxItemsMsg');
    const btnOrder = document.getElementById('btnOrder');

    if (cantidadTotal >= maxProductosOrden) {
        if (maxItemsWarning) maxItemsWarning.classList.remove('hidden');
        if (maxItemsMsg) maxItemsMsg.textContent =
            `El carrito tiene ${cantidadTotal} ítems. El máximo permitido por orden es ${maxProductosOrden}. Por favor reduce la cantidad.`;
        if (btnOrder) {
            btnOrder.disabled = true;
            btnOrder.classList.add('opacity-50', 'cursor-not-allowed');
        }
    } else {
        if (maxItemsWarning) maxItemsWarning.classList.add('hidden');
        if (btnOrder) {
            btnOrder.disabled = false;
            btnOrder.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    }

    actualizarTotales(subtotal);
    actualizarEstadoAccionesCarrito(true);
}

function actualizarTotales(subtotal) {
    const impuestos = subtotal * ivaSistema;
    const total = subtotal + impuestos;

    const elSub = document.getElementById('subtotal');
    const elImp = document.getElementById('impuestos');
    const elTot = document.getElementById('total');

    if (elSub) elSub.textContent = UI.formatCurrency(subtotal);
    if (elImp) elImp.textContent = UI.formatCurrency(impuestos);
    if (elTot) elTot.textContent = UI.formatCurrency(total);
}

window.agregarAlCarrito = async (productoId) => {
    try {
        await Api.post(`/carrito/productos/${productoId}`);
        cargarCarrito();
        UI.updateCartBadge();
        UI.showNotification('Cantidad actualizada');
    } catch (error) {
        UI.showNotification('Error al actualizar', 'error');
    }
};

window.eliminarDelCarrito = async (productoId) => {
    try {
        await Api.delete(`/carrito/productos/${productoId}`);
        cargarCarrito();
        UI.updateCartBadge();
        UI.showNotification('Producto eliminado');
    } catch (error) {
        UI.showNotification('Error al eliminar', 'error');
    }
};

async function realizarOrder() {
    if (!confirm('¿Confirmar pedido?')) return;

    try {
        await Api.post('/ordenes/checkout');
        // Redirect or show success
        UI.showNotification('¡Pedido realizado con éxito!', 'success');
        setTimeout(() => {
            window.location.href = 'ordenes.html';
        }, 1500);
    } catch (error) {
        UI.showNotification('Error en la generacion del pedido: ' + error.message, 'error');
    }
}

async function vaciarCarrito() {
    if (!confirm('¿Vaciar el carrito completo?')) return;

    try {
        await Api.delete('/carrito');
        cargarCarrito();
        UI.updateCartBadge();
        UI.showNotification('Carrito vaciado');
    } catch (error) {
        UI.showNotification('Error al vaciar carrito', 'error');
    }
}