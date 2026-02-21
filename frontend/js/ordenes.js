import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

document.addEventListener('DOMContentLoaded', () => {
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    UI.renderMenuOrders();
    cargarOrdenes();
});

async function cargarOrdenes() {
    try {
        const ordenes = await Api.get('/ordenes');
        renderizarOrdenes(ordenes);
    } catch (error) {
        UI.showNotification('Error al cargar órdenes', 'error');
    }
}

function renderizarOrdenes(ordenes) {
    const tbody = document.getElementById('listaOrdenes');
    tbody.innerHTML = '';

    if (!ordenes || ordenes.length === 0) {
        document.getElementById('emptyState').classList.remove('hidden');
        return;
    }
    document.getElementById('emptyState').classList.add('hidden');

    ordenes.forEach(orden => {
        const row = document.createElement('tr');
        row.className = 'border-b border-gray-200 hover:bg-gray-100';

        // Format date
        const fecha = new Date(orden.fecha).toLocaleDateString('es-ES', {
            year: 'numeric', month: 'long', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });

        row.innerHTML = `
            <td class="py-3 px-6 text-left whitespace-nowrap font-medium">#${orden.id}</td>
            <td class="py-3 px-6 text-left">${fecha}</td>
            <td class="py-3 px-6 text-right font-bold text-gray-700">${UI.formatCurrency(orden.total)}</td>
        `;
        tbody.appendChild(row);
    });
}
