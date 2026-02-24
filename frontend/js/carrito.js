import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

document.addEventListener('DOMContentLoaded', () => {
    // Auth Check
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    UI.renderMenuCart();
    cargarCarrito();
    setupEventListeners();
});

function setupEventListeners() {
    // Phase 2: Checkout implementation
    const btnCheckout = document.getElementById('btnCheckout');
    if (btnCheckout) {
        btnCheckout.addEventListener('click', realizarCheckout);
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

function renderizarCarrito(carrito) {
    const tbody = document.getElementById('listaCarrito');
    if (!tbody) return;

    tbody.innerHTML = '';

    // Group products
    const productosMap = new Map();
    const productos = carrito.productos || [];

    productos.forEach(p => {
        if (productosMap.has(p.id)) {
            productosMap.get(p.id).cantidad++;
        } else {
            productosMap.set(p.id, { ...p, cantidad: 1 });
        }
    });

    let subtotal = 0;

    const emptyCartMsg = document.getElementById('emptyCart');
    if (productosMap.size === 0) {
        if (emptyCartMsg) emptyCartMsg.classList.remove('hidden');
        tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4">El carrito está vacío</td></tr>';
        actualizarTotales(0);
        return;
    }
    if (emptyCartMsg) emptyCartMsg.classList.add('hidden');

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

    actualizarTotales(subtotal);
}

function actualizarTotales(subtotal) {
    const impuestos = subtotal * 0.15; // 15% tax
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
        UI.showNotification('Cantidad actualizada');
    } catch (error) {
        UI.showNotification('Error al actualizar', 'error');
    }
};

window.eliminarDelCarrito = async (productoId) => {
    try {
        await Api.delete(`/carrito/productos/${productoId}`);
        cargarCarrito();
        UI.showNotification('Producto eliminado');
    } catch (error) {
        UI.showNotification('Error al eliminar', 'error');
    }
};

async function realizarCheckout() {
    if (!confirm('¿Confirmar compra?')) return;

    try {
        await Api.post('/ordenes/checkout');
        // Redirect or show success
        UI.showNotification('¡Compra realizada con éxito!', 'success');
        setTimeout(() => {
            window.location.href = 'ordenes.html';
        }, 1500);
    } catch (error) {
        UI.showNotification('Error en el checkout: ' + error.message, 'error');
    }
}

async function vaciarCarrito() {
    if (!confirm('¿Vaciar el carrito completo?')) return;

    try {
        await Api.delete('/carrito');
        cargarCarrito();
        UI.showNotification('Carrito vaciado');
    } catch (error) {
        UI.showNotification('Error al vaciar carrito', 'error');
    }
}
