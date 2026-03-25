import Api from './api.js'
import { UI } from './ui.js'
import Auth from './auth.js'
import ConfigService from './config.service.js';

const Checkout = {

    orden: null,
    direccion: null,
    metodoPago: "Tarjeta",
    ivaSistema: 0.16,
    monedaSistema: 'MXN',

    async init() {

        if (!Auth.requireAuth(["CUSTOMER", "ADMIN"])) return

        const cfg = await ConfigService.load();

        this.ivaSistema = cfg.iva;
        this.monedaSistema = cfg.monedaSistema;

        UI.renderNavBar({
            containerId: "mainNav",
            context: "checkout"
        })

        const params = new URLSearchParams(window.location.search)
        const id = params.get("ordenId")

        await this.loadData(id)

        this.bindEvents()

    },

    async loadData(id) {
        this.ordenId = id; // Could be null
        let userIdToFetch = Auth.getCurrentUser().id;

        if (id) {
            const ordenes = await Api.get("/ordenes");
            this.orden = ordenes.find(o => o.id == id);
            if (this.orden && this.orden.usuarioId) {
                userIdToFetch = this.orden.usuarioId;
            }
            this.renderResumenFromOrden();
        } else {
            const carrito = await Api.get("/carrito");
            this.carrito = carrito;
            this.renderResumenFromCarrito();
        }

        const [direcciones, usuarioData] = await Promise.all([
            Api.get(`/direcciones/usuario/${userIdToFetch}`),
            Api.get(`/usuarios/${userIdToFetch}`)
        ]);

        this.usuarioRfc = usuarioData.rfcCurp;

        // Force re-render after capturing RFC
        if (id) {
            this.renderResumenFromOrden();
        } else {
            this.renderResumenFromCarrito();
        }

        this.renderDirecciones(direcciones);
    },

    bindEvents() {

        document
            .querySelectorAll('input[name="metodoPago"]')
            .forEach(r => {

                r.addEventListener("change", (e) => {
                    this.metodoPago = e.target.value
                    this.updateMetodoPago()
                })

            })

        document
            .getElementById("checkoutForm")
            .addEventListener("submit", (e) => this.procesarPago(e))

    },

    renderDirecciones(list) {

        const cont = document.getElementById("listaDirecciones")
        const loading = document.getElementById("loadingDirecciones")

        loading.classList.add("hidden")
        cont.classList.remove("hidden")

        cont.innerHTML = ""

        if (!list || list.length === 0) {

            document.getElementById("noDirecciones").classList.remove("hidden")
            return
        }

        list.sort((a, b) => b.preferida - a.preferida);

        list.forEach((dir, index) => {

            const card = document.createElement("div")

            card.className =
                "direccion-card border p-4 rounded-lg cursor-pointer hover:border-blue-500 transition"

            card.innerHTML = `
                <strong class="inline-block text-gray-800">${dir.alias || "Dirección"}</strong> ${dir.preferida ? '<span class="badge-preferida"><i class="fas fa-star"></i> Preferida</span>' : ''}
                <p class="text-sm text-gray-600">${dir.calle} ${dir.numeroExterior}${dir.numeroInterior ? ', Int. ' + dir.numeroInterior : ''}</p>
                <p class="text-sm text-gray-600">${dir.colonia}</p>
                <p class="text-sm text-gray-600">${dir.ciudad}</p>
                <p class="text-sm text-gray-600">${dir.municipio}, ${dir.estado}</p>
                <p class="text-sm text-gray-600">C.P. ${dir.codigoPostal}</p>
                <p class="text-sm text-gray-600">${dir.referencia ? `<p class="text-xs text-gray-400">${dir.referencia}</p>` : ''}</p>
            `
            card.addEventListener("click", () => {

                this.direccion = dir.id

                document
                    .querySelectorAll(".direccion-card")
                    .forEach(c => {
                        c.classList.remove("ring-2", "ring-blue-400", "bg-blue-50")
                    })

                card.classList.add("ring-2", "ring-blue-400", "bg-blue-50", "shadow-sm")

            })

            cont.appendChild(card)

            // seleccionar direccion preferida automaticamente
            if (index === 0) {

                this.direccion = dir.id
                card.classList.add("ring-2", "ring-blue-400", "bg-blue-50", "shadow-sm")

            }

        })

    },

    renderResumenFromOrden() {
        const orden = this.orden;

        document.getElementById("loadingResumen").classList.add("hidden")
        document.getElementById("resumenContent").classList.remove("hidden")

        if (this.usuarioRfc) {
            const container = document.getElementById("rfcContainer");
            if (container) {
                container.classList.remove("hidden");
                document.getElementById("checkoutRfcDisplay").textContent = this.usuarioRfc;
            }
        }

        document.getElementById("ordenIdDisplay").textContent = orden.id
        document.getElementById("ordenEstadoDisplay").textContent = orden.estado

        const ul = document.getElementById("listaResumenItems")
        ul.innerHTML = ""

        let subtotal = 0
        let subtotdisp = ""

        orden.detalles.forEach(d => {
            const nombre = d.productoNombre || (d.producto?.nombre) || "Producto"
            const sub = d.subtotal || (d.precio * d.cantidad)

            subtotal += sub

            subtotdisp = UI.formatCurrency(d.subtotal)

            const li = document.createElement("li")
            li.className = "flex justify-between text-sm"
            li.innerHTML = `
                <span>${d.cantidad} x ${nombre}</span>
                <span>${subtotdisp}</span>
            `
            ul.appendChild(li)
        })

        const tax = subtotal * this.ivaSistema
        const total = subtotal + tax

        document.getElementById("resumenSubtotal").textContent = UI.formatCurrency(subtotal)
        document.getElementById("resumenImpuestos").textContent = UI.formatCurrency(tax)
        document.getElementById("resumenTotal").textContent = UI.formatCurrency(total)

        // Actualizar labels con moneda y tasa IVA
        this._actualizarLabels(this.ivaSistema);
    },

    renderResumenFromCarrito() {
        if (!this.carrito || !this.carrito.productos || this.carrito.productos.length === 0) {
            UI.showNotification("Tu carrito está vacío", "warning");
            setTimeout(() => window.location.href = "carrito.html", 1500);
            return;
        }

        document.getElementById("loadingResumen").classList.add("hidden");
        document.getElementById("resumenContent").classList.remove("hidden");

        if (this.usuarioRfc) {
            const container = document.getElementById("rfcContainer");
            if (container) {
                container.classList.remove("hidden");
                document.getElementById("checkoutRfcDisplay").textContent = this.usuarioRfc;
            }
        }

        document.getElementById("ordenIdDisplay").textContent = "Nueva";
        document.getElementById("ordenEstadoDisplay").textContent = "PENDING";

        const ul = document.getElementById("listaResumenItems");
        ul.innerHTML = "";

        let subtotal = 0;
        const map = new Map();
        this.carrito.productos.forEach(p => {
            if (map.has(p.id)) map.get(p.id).cantidad++;
            else map.set(p.id, { ...p, cantidad: 1 });
        });

        map.forEach(p => {
            const sub = p.precio * p.cantidad;
            subtotal += sub;

            const li = document.createElement("li");
            li.className = "flex justify-between text-sm";
            li.innerHTML = `
                <span>${p.cantidad}x ${p.nombre}</span>
                <span>${UI.formatCurrency(sub)}</span>
            `;
            ul.appendChild(li);
        });

        const tax = subtotal * this.ivaSistema;
        const total = subtotal + tax;

        document.getElementById("resumenSubtotal").textContent = UI.formatCurrency(subtotal);
        document.getElementById("resumenImpuestos").textContent = UI.formatCurrency(tax);
        document.getElementById("resumenTotal").textContent = UI.formatCurrency(total);

        // Actualizar labels con moneda y tasa IVA
        this._actualizarLabels(this.ivaSistema);
    },

    /** Actualiza los labels de Subtotal, IVA y Total con moneda y porcentaje dinámico */
    _actualizarLabels(ivaTasa) {
        const mon = this.monedaSistema;
        const pct = Math.round(ivaTasa * 100);
        const lbSub = document.getElementById('labelResumenSubtotal');
        const lbIva = document.getElementById('labelResumenImpuestos');
        const lbTot = document.getElementById('labelResumenTotal');
        if (lbSub) lbSub.textContent = `Subtotal (${mon})`;
        if (lbIva) lbIva.textContent = `IVA (${pct}%)`;
        if (lbTot) lbTot.textContent = `Total (${mon})`;
    },

    updateMetodoPago() {

        const panel = document.getElementById("panelTarjeta")
        const msg = document.getElementById("panelMensajeStatic")
        const text = document.getElementById("mensajeEstatico")

        if (this.metodoPago === "Tarjeta") {

            panel.classList.remove("hidden")
            msg.classList.add("hidden")

        } else {

            panel.classList.add("hidden")
            msg.classList.remove("hidden")

            text.textContent =
                this.metodoPago === "PayPal"
                    ? "Serás redirigido a PayPal"
                    : "Se mostrarán datos bancarios"

        }

    },

    async procesarPago(e) {

        e.preventDefault()

        if (!this.direccion) {
            UI.showNotification("Selecciona una dirección", "warning")
            return
        }

        const btn = document.getElementById("btnProcesarPago")
        btn.disabled = true

        try {
            let actualOrdenId = this.ordenId;

            // FASE 1: Crear la orden si no existe (viniendo del carrito)
            if (!actualOrdenId) {
                const urlCheckout = `/ordenes/checkout?direccionId=${this.direccion}`;
                const nuevaOrden = await Api.post(urlCheckout);
                actualOrdenId = nuevaOrden.id;
                this.ordenId = actualOrdenId; // guardarlo por si falla el pago y quiere reintentar
            }

            // FASE 2: Enviar pago
            const res = await Api.post("/payments/procesar", {
                ordenId: actualOrdenId,
                metodoPago: this.metodoPago,
                direccionId: this.direccion
            })

            if (res.estado === "PAID" || res.estado === "PAYMENT_PENDING") {
                UI.showNotification("Proceso completado", "success")
                setTimeout(() => {
                    window.location.href = "ordenes.html"
                }, 1500)
            } else if (res.estado === "OUT_OF_STOCK") {
                UI.showNotification("Lo sentimos, no hay inventario suficiente.", "error")
                setTimeout(() => {
                    window.location.href = "ordenes.html"
                }, 2000)
            }

        } catch (err) {

            UI.showNotification(err.message, "error")
            btn.disabled = false

        }

    }

}

document.addEventListener("DOMContentLoaded", () => Checkout.init())