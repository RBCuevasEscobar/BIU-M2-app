/**
 * app.js – Landing Page (index.html)
 * ─────────────────────────────────────────────────────────────
 * Muestra productos para usuarios NO autenticados.
 * IMPORTANTE: El botón "Agregar al Carrito" NO se muestra en
 * esta vista. El usuario debe iniciar sesión para comprar.
 * ─────────────────────────────────────────────────────────────
 */
document.addEventListener('DOMContentLoaded', async () => {
    const productList = document.getElementById('product-list');

    try {
        const response = await fetch('http://localhost:8080/api/productos');
        if (!response.ok) throw new Error('Error fetching products');

        const products = await response.json();

        if (products.length === 0) {
            productList.innerHTML = '<p class="col-span-full text-center text-gray-500 py-12">No hay productos disponibles.</p>';
            return;
        }

        productList.innerHTML = products.map(product => {
            const imagenes = product.imagenes || [];
            const imgDefault = imagenes.find(i => i.isDefault) || imagenes[0];
            const imgSrc = imgDefault ? imgDefault.imagenUrl : null;

            const tipo = product.tipo || (product.peso !== undefined && !product.urlDescarga ? 'Físico' : 'Digital');
            const tipoBadge = (tipo === 'Fisico' || tipo === 'Físico')
                ? '<span class="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full">Físico</span>'
                : '<span class="inline-block bg-purple-100 text-purple-700 text-xs px-2 py-0.5 rounded-full">Digital</span>';

            const descripcionHtml = product.descripcion
                ? `<p class="mt-1 text-sm text-gray-500 line-clamp-2">${product.descripcion}</p>`
                : '';

            return `
            <div class="group bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition-shadow duration-200 flex flex-col">
                <div class="relative aspect-square w-full overflow-hidden rounded-t-xl bg-gray-100">
                    ${imgSrc
                    ? `<img src="${imgSrc}" alt="${product.nombre}" loading="lazy"
                                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                onerror="this.onerror=null;this.src='https://via.placeholder.com/300?text=Sin+imagen'">`
                    : `<div class="flex items-center justify-center h-full text-gray-300">
                               <svg class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                   <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                       d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                               </svg>
                           </div>`
                }
                </div>
                <div class="p-4 flex flex-col flex-1">
                    <div class="flex justify-between items-start gap-1 mb-1">
                        <h3 class="text-base font-semibold text-gray-900 line-clamp-2">${product.nombre}</h3>
                        ${tipoBadge}
                    </div>
                    ${descripcionHtml}
                    <div class="mt-auto pt-3 flex items-center justify-between">
                        <span class="text-xl font-bold text-gray-900">$${product.precio.toFixed(2)}</span>
                    </div>
                    <!-- El botón de carrito NO aparece para usuarios no autenticados -->
                    <a href="login.html"
                        class="mt-3 w-full text-center bg-gray-100 hover:bg-blue-600 hover:text-white text-gray-700 py-2 rounded-lg text-sm font-medium transition-colors duration-200 border border-gray-200">
                        Iniciar sesión para comprar
                    </a>
                </div>
            </div>`;
        }).join('');

    } catch (error) {
        console.error(error);
        productList.innerHTML = '<p class="col-span-full text-center text-red-500">No se pudieron cargar los productos. Asegúrate de que el backend esté corriendo.</p>';
    }
});
