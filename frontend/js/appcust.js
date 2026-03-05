import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

document.addEventListener('DOMContentLoaded', async () => {

    UI.renderNavBar({ containerId: 'mainNav', context: 'products' });

    const productList = document.getElementById('product-list-customer');

    try {
        const products = await Api.get('/productos');
        if (!products) throw new Error('Error fetching products');

        if (products.length === 0) {
            productList.innerHTML = '<p class="col-span-full text-center text-gray-500 py-12">No hay productos disponibles.</p>';
            return;
        }

        // Renderizar tarjetas en GRID (layout de productoscustomer)
        productList.innerHTML = products.map(product => buildProductCard(product)).join('');

    } catch (error) {
        console.error(error);
        productList.innerHTML = '<p class="col-span-full text-center text-red-500 py-8">No se pudieron cargar los productos. Asegúrate de que el backend esté corriendo.</p>';
    }

    // Delegación de eventos: Agregar al carrito
    productList.addEventListener('click', async (e) => {
        const btn = e.target.closest('button[data-action="carrito"]');
        if (!btn) return;
        try {
            await Api.post(`/carrito/productos/${btn.dataset.id}`);
            UI.showNotification('Producto agregado al carrito');
        } catch (err) {
            UI.showNotification('No se pudo agregar al carrito', 'error');
        }
    });

    // Delegación de eventos: Abrir galería de imágenes al click
    productList.addEventListener('click', (e) => {
        const imgEl = e.target.closest('[data-action="galeria"]');
        if (!imgEl) return;
        const productId = imgEl.dataset.productId;
        const allImgEls = productList.querySelectorAll(`[data-galeria-src][data-product-id="${productId}"]`);
        const urls = Array.from(allImgEls).map(el => el.dataset.galeriaSrc);
        abrirGaleria(urls, parseInt(imgEl.dataset.imgIndex || '0'));
    });

    // Logout global
    window.logout = () => Auth.logout();
});

// ── Construir tarjeta de producto (grid) ──────────────────────────────────────
function buildProductCard(product) {
    const imagenes = product.imagenes || [];
    const imgDefault = imagenes.find(i => i.isDefault) || imagenes[0];
    const imgSrc = imgDefault ? imgDefault.imagenUrl : null;
    const tieneVariasImagenes = imagenes.length > 1;

    // Datos de galería embebidos como atributos data para delegación
    const galeriaAttrs = imagenes.map((img, idx) =>
        `<span style="display:none" data-galeria-src="${img.imagenUrl}" data-product-id="${product.id}"></span>`
    ).join('');

    const tipo = product.tipo || (product.peso !== undefined && product.urlDescarga === undefined ? 'Físico' : 'Digital');
    const tipoBadge = tipo === 'Fisico' || tipo === 'Físico'
        ? '<span class="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full">Físico</span>'
        : '<span class="inline-block bg-purple-100 text-purple-700 text-xs px-2 py-0.5 rounded-full">Digital</span>';

    const descripcionHtml = product.descripcion
        ? `<p class="mt-1 text-sm text-gray-500 line-clamp-2" title="${product.descripcion}">${product.descripcion}</p>`
        : '';

    const proveedorHtml = product.proveedor
        ? `<p class="mt-1 text-xs text-gray-400 truncate" title="${product.proveedor}">
               <i class="fas fa-store text-gray-300 mr-1"></i>${product.proveedor}
           </p>`
        : '';

    // Indicador de múltiples imágenes
    const multiImgBadge = tieneVariasImagenes
        ? `<span class="absolute top-2 right-2 bg-black bg-opacity-50 text-white text-xs px-1.5 py-0.5 rounded-full">
               <i class="fas fa-images mr-0.5"></i>${imagenes.length}
           </span>`
        : '';

    const imgContainerCursor = tieneVariasImagenes ? 'cursor-pointer' : '';
    const imgClickAttr = tieneVariasImagenes
        ? `data-action="galeria" data-product-id="${product.id}" data-img-index="0"`
        : '';

    return `
        <article class="group relative bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-lg transition-shadow duration-200 flex flex-col"
                 itemscope itemtype="https://schema.org/Product">
            ${galeriaAttrs}

            <!-- Imagen principal -->
            <div class="relative aspect-square w-full overflow-hidden rounded-t-xl bg-gray-100">
                ${imgSrc
            ? `<img src="${imgSrc}" alt="${product.nombre}" loading="lazy"
                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 ${imgContainerCursor}"
                            ${imgClickAttr}
                            onerror="this.onerror=null;this.src='https://via.placeholder.com/300?text=Sin+imagen'"
                            itemprop="image">`
            : `<div class="flex items-center justify-center h-full text-gray-300 ${imgContainerCursor}" ${imgClickAttr}>
                           <i class="fas fa-image text-5xl"></i>
                       </div>`
        }
                ${multiImgBadge}
                ${tieneVariasImagenes
            ? `<div class="absolute inset-0 flex items-end justify-center pb-2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                           <span class="bg-black bg-opacity-60 text-white text-xs px-3 py-1 rounded-full">
                               <i class="fas fa-search-plus mr-1"></i> Ver galería
                           </span>
                       </div>`
            : ''}
            </div>

            <!-- Información del producto -->
            <div class="p-4 flex flex-col flex-1">
                <div class="flex items-start justify-between gap-2 mb-1">
                    <h3 class="text-base font-semibold text-gray-900 line-clamp-2" itemprop="name">${product.nombre}</h3>
                    ${tipoBadge}
                </div>

                ${descripcionHtml}
                ${proveedorHtml}

                <div class="mt-auto pt-3">
                    <div class="flex items-center justify-between">
                        <span class="text-xl font-bold text-gray-900" itemprop="offers" itemscope itemtype="https://schema.org/Offer">
                            <span itemprop="price" content="${product.precio}">${UI.formatCurrency(product.precio)}</span>
                            <meta itemprop="priceCurrency" content="MXN">
                        </span>
                    </div>
                    <button class="mt-3 w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-2"
                            data-action="carrito" data-id="${product.id}">
                        <i class="fas fa-cart-plus"></i> Agregar al Carrito
                    </button>
                </div>
            </div>
        </article>
    `;
}

// ── Galería / Popup de imágenes ────────────────────────────────────────────────
let galeriaActual = [];
let galeriaIndex = 0;

function abrirGaleria(urls, startIndex = 0) {
    galeriaActual = urls;
    galeriaIndex = startIndex;

    // Crear overlay si no existe
    let overlay = document.getElementById('galeriaOverlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'galeriaOverlay';
        overlay.className = 'fixed inset-0 bg-black bg-opacity-90 z-50 flex items-center justify-center';
        overlay.innerHTML = `
            <div class="relative w-full max-w-2xl mx-4 flex flex-col items-center gap-4">

                <!-- Botón cerrar -->
                <button id="btnGaleriaClose"
                    class="absolute -top-10 right-0 text-white text-3xl hover:text-gray-300 transition"
                    aria-label="Cerrar galería">
                    <i class="fas fa-times"></i>
                </button>

                <!-- Contenedor imagen -->
                <div class="relative w-full flex items-center justify-center bg-white rounded-xl overflow-hidden shadow-2xl" style="min-height:300px; max-height:70vh">
                    <button id="btnGaleriaPrev"
                        class="absolute left-2 z-10 bg-black bg-opacity-50 text-white w-10 h-10 rounded-full flex items-center justify-center hover:bg-opacity-80 transition"
                        aria-label="Anterior">
                        <i class="fas fa-chevron-left"></i>
                    </button>

                    <img id="galeriaImg" src="" alt="Imagen del producto"
                        class="max-h-full max-w-full object-contain"
                        style="max-height:65vh"
                        onerror="this.src='https://via.placeholder.com/600?text=Error+al+cargar'">

                    <button id="btnGaleriaNext"
                        class="absolute right-2 z-10 bg-black bg-opacity-50 text-white w-10 h-10 rounded-full flex items-center justify-center hover:bg-opacity-80 transition"
                        aria-label="Siguiente">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>

                <!-- Contador -->
                <div id="galeriaCounter" class="text-white text-sm"></div>

                <!-- Miniaturas -->
                <div id="galeriaThumbs" class="flex gap-2 flex-wrap justify-center"></div>
            </div>
        `;
        document.body.appendChild(overlay);

        // Eventos
        document.getElementById('btnGaleriaClose').addEventListener('click', cerrarGaleria);
        overlay.addEventListener('click', (e) => { if (e.target === overlay) cerrarGaleria(); });
        document.getElementById('btnGaleriaPrev').addEventListener('click', () => navegarGaleria(-1));
        document.getElementById('btnGaleriaNext').addEventListener('click', () => navegarGaleria(1));

        // Teclado
        document.addEventListener('keydown', galeriaKeyHandler);
    }

    overlay.classList.remove('hidden');
    actualizarGaleria();
}

function cerrarGaleria() {
    const overlay = document.getElementById('galeriaOverlay');
    if (overlay) overlay.classList.add('hidden');
}

function navegarGaleria(dir) {
    galeriaIndex = (galeriaIndex + dir + galeriaActual.length) % galeriaActual.length;
    actualizarGaleria();
}

function actualizarGaleria() {
    const img = document.getElementById('galeriaImg');
    const counter = document.getElementById('galeriaCounter');
    const thumbs = document.getElementById('galeriaThumbs');

    img.src = galeriaActual[galeriaIndex];
    counter.textContent = `${galeriaIndex + 1} / ${galeriaActual.length}`;

    // Actualizar miniaturas
    thumbs.innerHTML = galeriaActual.map((url, i) => `
        <img src="${url}" alt="Miniatura ${i + 1}" loading="lazy"
             class="w-14 h-14 object-cover rounded cursor-pointer border-2 transition-all
                    ${i === galeriaIndex ? 'border-blue-400 scale-110' : 'border-transparent opacity-60 hover:opacity-100'}"
             onclick="window.__galeriaGoTo(${i})"
             onerror="this.src='https://via.placeholder.com/56'">`
    ).join('');

    // Ocultar prev/next si solo hay una imagen
    const hiddenNav = galeriaActual.length <= 1;
    document.getElementById('btnGaleriaPrev').classList.toggle('hidden', hiddenNav);
    document.getElementById('btnGaleriaNext').classList.toggle('hidden', hiddenNav);
}

window.__galeriaGoTo = (idx) => {
    galeriaIndex = idx;
    actualizarGaleria();
};

function galeriaKeyHandler(e) {
    const overlay = document.getElementById('galeriaOverlay');
    if (!overlay || overlay.classList.contains('hidden')) return;
    if (e.key === 'ArrowLeft') navegarGaleria(-1);
    if (e.key === 'ArrowRight') navegarGaleria(1);
    if (e.key === 'Escape') cerrarGaleria();
}
