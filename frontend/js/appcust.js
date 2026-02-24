
import Api from './api.js';
import { UI } from './ui.js';

document.addEventListener('DOMContentLoaded', async () => {
    const productList = document.getElementById('product-list-customer');

    try {

        const products = await Api.get('/productos');

        productList.innerHTML = products.map(product => `
            <div class="group relative bg-white border rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
                <div class="aspect-w-1 aspect-h-1 w-full overflow-hidden rounded-t-lg bg-gray-200 lg:aspect-none lg:h-60">
                    <div class="flex items-center justify-center h-full text-gray-400">
                        <!-- Placeholder Image -->
                        <svg class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                </div>
                <div class="p-4">
                    <h3 class="text-lg font-medium text-gray-900">
                        <a href="#">
                            <span aria-hidden="true"></span>
                            ${product.nombre}
                        </a>
                    </h3>
                    <p class="mt-1 text-sm text-gray-500">${product.tipo || (product.stock !== undefined ? 'Físico' : 'Digital')}</p>
                    <p class="mt-2 text-xl font-bold text-gray-900">$${product.precio.toFixed(2)}</p>
                    <button class="mt-4 w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700" data-id="${product.id}">Agregar al Carrito</button>
                </div>
            </div>
        `).join('');

    } catch (error) {
        console.error(error);
        productList.innerHTML = '<p class="text-center text-red-500">No se pudieron cargar los productos. Asegúrate de que el backend esté corriendo.</p>';
    }

    productList.addEventListener('click', async (e) => {
        const btn = e.target.closest('button[data-id]');
        if (!btn) return;

        try {
            await Api.post(`/carrito/productos/${btn.dataset.id}`);
            UI.showNotification('Producto agregado al carrito');
        } catch (err) {
            UI.showNotification('No se pudo agregar al carrito', 'error');
        }
    });

});
