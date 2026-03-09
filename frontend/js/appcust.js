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
        const fragment = document.createDocumentFragment();

        products.forEach(product => {
            const card = buildProductCard(product);
            fragment.appendChild(card);
        });

        productList.innerHTML = '';
        productList.appendChild(fragment);

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

        const img = e.target.closest('[data-action="galeria"]');
        if (!img) return;

        const card = img.closest('article');
        const urls = JSON.parse(card.dataset.images);

        abrirGaleria(urls, 0);

    });

    // Logout global
    window.logout = () => Auth.logout();
});

// ── Construir tarjeta de producto (grid) ──────────────────────────────────────
function buildProductCard(product) {

    if (!Auth.requireAuth(["CUSTOMER"])) return;

    const imagenes = product.imagenes || [];
    const imgDefault = imagenes.find(i => i.isDefault) || imagenes[0];

    const card = document.createElement('article');

    card.className = `
        group relative bg-white border border-gray-200
        rounded-xl shadow-sm hover:shadow-lg
        transition-shadow duration-200 flex flex-col
    `;

    card.dataset.productId = product.id;
    card.dataset.images = JSON.stringify(imagenes.map(i => i.imagenUrl));

    const tipo = product.tipo || (product.peso !== undefined ? 'Físico' : 'Digital');

    const badge =
        tipo === 'Fisico'
            ? `<span class="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full">Físico</span>`
            : `<span class="inline-block bg-purple-100 text-purple-700 text-xs px-2 py-0.5 rounded-full">Digital</span>`;

    const imgSrc = imgDefault ? imgDefault.imagenUrl : null;

    card.innerHTML = `
        
        <div class="relative w-full rounded-t-xl bg-white aspect-[4/3] flex items-center justify-center">

        ${imgSrc
            ? `<img
                src="${imgSrc}"
                alt="${product.nombre}"
                loading="lazy"
                decoding="async"
                class="absolute inset-0 w-full h-full object-contain p-4 transition-transform duration-300 group-hover:scale-105 cursor-pointer"
                data-action="galeria"
                onerror="this.src='https://via.placeholder.com/400?text=Sin+imagen'">`
            : `<div class="flex items-center justify-center w-full h-full text-gray-300">
                <i class="fas fa-image text-5xl"></i>
            </div>`
        }
        </div>
        <div class="p-4 flex flex-col flex-1 min-h-[140px]">

            <div class="flex items-start justify-between gap-2 mb-1">
                <h3 class="text-base font-semibold text-gray-900">
                    ${product.nombre}
                </h3>
                ${badge}
            </div>

            ${product.descripcion
            ? `<p class="mt-1 text-sm text-gray-500">${product.descripcion}</p>`
            : ''}

            <div class="mt-auto pt-3">

                <span class="text-xl font-bold text-gray-900">
                    ${UI.formatCurrency(product.precio)}
                </span>

                <button
                    class="mt-3 w-full bg-blue-600 hover:bg-blue-700
                    text-white py-2 rounded-lg text-sm font-medium
                    flex items-center justify-center gap-2"
                    data-action="carrito"
                    data-id="${product.id}">

                    <i class="fas fa-cart-plus"></i>
                    Agregar al Carrito

                </button>

            </div>

        </div>
    `;

    return card;
}

// ── Galería / Popup de imágenes ────────────────────────────────────────────────
let galeriaActual = [];
let galeriaIndex = 0;

function abrirGaleria(urls, startIndex = 0) {

    if (!urls || urls.length === 0) return;

    galeriaActual = urls;
    galeriaIndex = startIndex;

    let overlay = document.getElementById("galeriaOverlay");

    if (!overlay) {

        overlay = document.createElement("div");
        overlay.id = "galeriaOverlay";

        overlay.style.position = "fixed";
        overlay.style.inset = "0";
        overlay.style.background = "rgba(0,0,0,0.95)";
        overlay.style.zIndex = "9999";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";

        overlay.innerHTML = `

        <!-- BOTON CERRAR -->
        <button id="btnGaleriaClose"
            style="
            position:absolute;
            top:15px;
            right:25px;
            font-size:32px;
            color:white;
            background:none;
            border:none;
            cursor:pointer;
            z-index:10000;">
            <i class="fas fa-times"></i>
        </button>

        <div style="position:relative;width:100%;max-width:1100px;padding:20px">

            <!-- contenedor imagen -->
            <div style="
                position:relative;
                width:100%;
                height:520px;
                display:flex;
                align-items:center;
                justify-content:center;
                background:white;
                border-radius:10px;
                overflow:hidden;
            ">

                <button id="btnGaleriaPrev"
                    style="
                    position:absolute;
                    left:15px;
                    width:45px;
                    height:45px;
                    border-radius:50%;
                    background:rgba(252, 251, 251, 0.6);
                    color:black;
                    border:none;
                    cursor:pointer;
                    z-index:10">
                    <i class="fas fa-chevron-left"></i>
                </button>

                <img id="galeriaImg"
                    style="
                    max-width:90%;
                    max-height:90%;
                    object-fit:contain;
                    ">

                <button id="btnGaleriaNext"
                    style="
                    position:absolute;
                    right:15px;
                    width:45px;
                    height:45px;
                    border-radius:50%;
                    background:rgba(252, 251, 251, 0.6);
                    color:black;
                    border:none;
                    cursor:pointer;
                    z-index:10">
                    <i class="fas fa-chevron-right"></i>
                </button>

            </div>

            <div id="galeriaCounter"
                style="color:white;text-align:center;margin-top:8px"></div>

            <div id="galeriaThumbs"
                style="
                margin-top:12px;
                display:flex;
                gap:8px;
                justify-content:center;
                flex-wrap:wrap;
                max-height:110px;
                overflow-y:auto"></div>

        </div>
        `;

        document.body.appendChild(overlay);

        document.getElementById("btnGaleriaClose")
            .addEventListener("click", cerrarGaleria);

        document.getElementById("btnGaleriaPrev")
            .addEventListener("click", () => navegarGaleria(-1));

        document.getElementById("btnGaleriaNext")
            .addEventListener("click", () => navegarGaleria(1));

        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) cerrarGaleria();
        });

    }

    overlay.style.display = "flex";

    actualizarGaleria();
}

function cerrarGaleria() {

    const overlay = document.getElementById("galeriaOverlay");

    if (!overlay) return;

    overlay.style.display = "none";
}

function navegarGaleria(dir) {
    galeriaIndex = (galeriaIndex + dir + galeriaActual.length) % galeriaActual.length;
    actualizarGaleria();
}

function actualizarGaleria() {

    const img = document.getElementById("galeriaImg");
    const counter = document.getElementById("galeriaCounter");
    const thumbs = document.getElementById("galeriaThumbs");

    if (!img) return;

    img.src = galeriaActual[galeriaIndex];

    counter.textContent =
        `${galeriaIndex + 1} / ${galeriaActual.length}`;

    thumbs.innerHTML = "";

    galeriaActual.forEach((url, i) => {

        const thumb = document.createElement("img");

        thumb.src = url;

        thumb.style.width = "80px";
        thumb.style.height = "80px";
        thumb.style.objectFit = "cover";
        thumb.style.borderRadius = "6px";
        thumb.style.cursor = "pointer";
        thumb.style.border =
            i === galeriaIndex
                ? "3px solid #3b82f6"
                : "2px solid transparent";

        thumb.onclick = () => {
            galeriaIndex = i;
            actualizarGaleria();
        };

        thumbs.appendChild(thumb);

    });

    const hideNav = galeriaActual.length <= 1;

    document.getElementById("btnGaleriaPrev").style.display =
        hideNav ? "none" : "block";

    document.getElementById("btnGaleriaNext").style.display =
        hideNav ? "none" : "block";
}

window.__galeriaGoTo = (idx) => {
    galeriaIndex = idx;
    actualizarGaleria();
};
