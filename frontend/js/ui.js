import { API_BASE_URL } from './config.js';
export const UI = {

    renderNavBar({ containerId, context }) {
        const navContainer = document.getElementById(containerId);
        if (!navContainer) return;

        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;

        navContainer.innerHTML = '';

        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === LOGO ===
        const left = document.createElement('div');
        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === LINKS ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">Iniciar Sesión</a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {
            const links = this.getLinksByRoleAndContext(user.role, context);
            links.forEach(l => {
                if (l.isDropdown) {
                    const dropdownId = 'dropdown-' + Math.random().toString(36).substr(2, 9);
                    let dropdownHtml = `
                    <div class="relative">
                        <button onclick="document.getElementById('${dropdownId}').classList.toggle('hidden')" class="text-gray-700 font-medium hover:text-blue-600 focus:outline-none flex items-center">
                            ${l.label} <i class="fas fa-chevron-down text-xs ml-1"></i>
                        </button>
                        <div id="${dropdownId}" class="hidden absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg border border-gray-100 z-50">
                            <div class="py-1">`;
                    l.items.forEach(sub => {
                        dropdownHtml += `<a href="${sub.href}" class="block px-4 py-2 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600">${sub.label}</a>`;
                    });
                    dropdownHtml += `</div></div></div>`;
                    right.innerHTML += dropdownHtml;
                } else if (l.isBadgeCart) {
                    // Enlace especial del Carrito con ID para badge dinámico
                    right.innerHTML += `
                        <a id="cartNavLink" href="${l.href}" class="text-gray-700 hover:text-blue-600 font-medium">
                            ${l.label}
                        </a>
                    `;
                } else {
                    right.innerHTML += `
                        <a href="${l.href}" class="text-gray-700 hover:text-blue-600 font-medium">
                            ${l.label}
                        </a>
                    `;
                }
            });

            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
                <button onclick="window.logout()" class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;

            // Auto-actualizar badge del carrito solo para CUSTOMER
            if (user.role === 'CUSTOMER') {
                this.updateCartBadge();
            }
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    getLinksByRoleAndContext(role, context) {
        const config = {
            CUSTOMER: {
                products: [
                    { label: 'Carrito', href: 'carrito.html', isBadgeCart: true },
                    { label: 'Mis Órdenes', href: 'ordenes.html' },
                    { label: 'Mis Direcciones', href: 'direcciones.html' }
                ],
                cart: [
                    { label: 'Productos', href: 'productoscustomer.html' },
                    { label: 'Mis Órdenes', href: 'ordenes.html' },
                    { label: 'Mis Direcciones', href: 'direcciones.html' }
                ],
                orders: [
                    { label: 'Productos', href: 'productoscustomer.html' },
                    { label: 'Carrito', href: 'carrito.html', isBadgeCart: true },
                    { label: 'Mis Direcciones', href: 'direcciones.html' }
                ],
                addresses: [
                    { label: 'Productos', href: 'productoscustomer.html' },
                    { label: 'Carrito', href: 'carrito.html', isBadgeCart: true },
                    { label: 'Mis Órdenes', href: 'ordenes.html' }
                ]
            },
            ADMIN: {
                products: [
                    { label: 'Gestion Órdenes', href: 'ordenes.html' },
                    { label: 'Gestion Usuarios', href: 'usuarios.html' },
                    {
                        isDropdown: true,
                        label: '<i class="fas fa-cog"></i> Configuración',
                        items: [
                            { label: 'Opciones JWT', href: 'config-auth.html' },
                            { label: 'Variables Sistema', href: 'config-sistema.html' }
                        ]
                    }
                ],
                users: [
                    { label: 'Gestion Productos', href: 'productos.html' },
                    { label: 'Gestion Órdenes', href: 'ordenes.html' },
                    {
                        isDropdown: true,
                        label: '<i class="fas fa-cog"></i> Configuración',
                        items: [
                            { label: 'Opciones JWT', href: 'config-auth.html' },
                            { label: 'Variables Sistema', href: 'config-sistema.html' }
                        ]
                    }
                ],
                orders: [
                    { label: 'Gestion Usuarios', href: 'usuarios.html' },
                    { label: 'Gestion Productos', href: 'productos.html' },
                    {
                        isDropdown: true,
                        label: '<i class="fas fa-cog"></i> Configuración',
                        items: [
                            { label: 'Opciones JWT', href: 'config-auth.html' },
                            { label: 'Variables Sistema', href: 'config-sistema.html' }
                        ]
                    }
                ],
                'config-auth': [
                    { label: 'Gestion Usuarios', href: 'usuarios.html' },
                    { label: 'Gestion Productos', href: 'productos.html' },
                    { label: 'Gestion Órdenes', href: 'ordenes.html' },
                    {
                        isDropdown: true,
                        label: '<i class="fas fa-cog"></i> Configuración',
                        items: [
                            { label: 'Opciones JWT', href: 'config-auth.html' },
                            { label: 'Variables Sistema', href: 'config-sistema.html' }
                        ]
                    }
                ],
                'config-sistema': [
                    { label: 'Gestion Usuarios', href: 'usuarios.html' },
                    { label: 'Gestion Productos', href: 'productos.html' },
                    { label: 'Gestion Órdenes', href: 'ordenes.html' },
                    {
                        isDropdown: true,
                        label: '<i class="fas fa-cog"></i> Configuración',
                        items: [
                            { label: 'Opciones JWT', href: 'config-auth.html' },
                            { label: 'Variables Sistema', href: 'config-sistema.html' }
                        ]
                    }
                ]
            },
            SUPPLIER: {
                products: [
                    { label: 'Mis Direcciones', href: 'direcciones.html' }
                ],
                addresses: [
                    { label: 'Mis Productos', href: 'productos.html' }
                ]
            }
        };

        return config[role]?.[context] || [];
    },

    // === Helpers existentes (SIN CAMBIOS) ===
    showNotification(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `fixed top-5 right-5 px-6 py-3 rounded shadow-lg text-white z-50 ${type === 'success' ? 'bg-green-500' : 'bg-red-500'
            }`;
        notification.textContent = message;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('opacity-0');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    },

    formatCurrency(amount) {
        return new Intl.NumberFormat('es-MX', {
            style: 'currency',
            currency: 'MXN'
        }).format(amount);
    },

    toggleModal(modalId, show = true) {
        const modal = document.getElementById(modalId);
        if (modal) {
            if (show) {
                modal.classList.remove('hidden');
                modal.classList.add('flex');
            } else {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            }
        }
    },

    setupModalCloser(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.toggleModal(modalId, false);
                }
            });
        }
    },

    /**
     * Actualiza el badge del carrito en el enlace con id="cartNavLink".
     * Obtiene la cantidad de productos via GET /api/carrito.
     * Muestra "Carrito (N)" si hay productos, o "Carrito" si vacío.
     * Solo actúa si el enlace existe en el DOM (solo páginas CUSTOMER).
     */
    async updateCartBadge() {
        const link = document.getElementById('cartNavLink');
        if (!link) return; // No es una página de CUSTOMER o no aplica

        try {
            const token = localStorage.getItem('token');
            if (!token) return;

            const response = await fetch(`${API_BASE_URL}/carrito`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                link.textContent = 'Carrito';
                return;
            }

            const carrito = await response.json();
            const count = carrito?.productos?.length ?? 0;
            link.textContent = count > 0 ? `Carrito (${count})` : 'Carrito';

        } catch {
            // En caso de error de red, se muestra el label base sin badge
            link.textContent = 'Carrito';
        }
    },
};
