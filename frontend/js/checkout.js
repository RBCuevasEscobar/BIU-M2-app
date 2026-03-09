import Api from './api.js'
import { UI } from './ui.js'
import Auth from './auth.js'

const Checkout = {

    orden: null,
    direccion: null,
    metodoPago: "Tarjeta",

    async init() {

        if (!Auth.requireAuth(["CUSTOMER", "ADMIN"])) return

        UI.renderNavBar({
            containerId: "mainNav",
            context: "checkout"
        })

        const params = new URLSearchParams(window.location.search)
        const id = params.get("ordenId")

        if (!id) {
            window.location.href = "ordenes.html"
            return
        }

        await this.loadData(id)

        this.bindEvents()

    },

    async loadData(id) {

        const [orden, direcciones] = await Promise.all([
            Api.get("/ordenes"),
            Api.get("/direcciones/mis-direcciones")
        ])

        this.orden = orden.find(o => o.id == id)

        this.renderResumen()
        this.renderDirecciones(direcciones)

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

    renderResumen() {

        const orden = this.orden

        document.getElementById("loadingResumen").classList.add("hidden")
        document.getElementById("resumenContent").classList.remove("hidden")

        document.getElementById("ordenIdDisplay").textContent = orden.id
        document.getElementById("ordenEstadoDisplay").textContent = orden.estado

        const ul = document.getElementById("listaResumenItems")
        ul.innerHTML = ""

        let subtotal = 0

        orden.detalles.forEach(d => {

            const nombre = d.productoNombre || (d.producto?.nombre) || "Producto"
            const sub = d.subtotal || (d.precio * d.cantidad)

            subtotal += sub

            const li = document.createElement("li")

            li.className = "flex justify-between text-sm"

            li.innerHTML = `
                <span>${d.cantidad}x ${nombre}</span>
                <span>${UI.formatCurrency(sub)}</span>
            `

            ul.appendChild(li)

        })

        const tax = subtotal * 0.16
        const total = subtotal + tax

        document.getElementById("resumenSubtotal").textContent = UI.formatCurrency(subtotal)
        document.getElementById("resumenImpuestos").textContent = UI.formatCurrency(tax)
        document.getElementById("resumenTotal").textContent = UI.formatCurrency(total)

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

            const res = await Api.post("/payments/procesar", {
                ordenId: this.orden.id,
                metodoPago: this.metodoPago
            })

            if (res.estado === "PAID") {

                UI.showNotification("Pago exitoso", "success")

                setTimeout(() => {
                    window.location.href = "ordenes.html"
                }, 1500)

            }

        } catch (err) {

            UI.showNotification(err.message, "error")
            btn.disabled = false

        }

    }

}

document.addEventListener("DOMContentLoaded", () => Checkout.init())