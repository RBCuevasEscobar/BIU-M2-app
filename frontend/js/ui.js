export const UI = {
    renderMenuProducts() {
        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;
        const navContainer = document.getElementById('mainNavProducts');

        if (!navContainer) return;

        navContainer.innerHTML = '';

        // Contenedor principal FLEX
        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === IZQUIERDA (Logo) ===
        const left = document.createElement('div');
        left.className = 'flex items-center';

        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === DERECHA (Opciones dinámicas) ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">
                    Iniciar Sesión
                </a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {
            /*
                        // Link común
                        right.innerHTML += `
                            <a href="productos.html" class="text-gray-700 hover:text-blue-600 font-medium">
                                Productos
                            </a>
                        `;
            */
            // === ROLE BASED LINKS ===
            if (user.role === 'CUSTOMER') {
                right.innerHTML += `
                    <a href="carrito.html" class="text-gray-700 hover:text-blue-600 font-medium">
                       Carrito
                    </a>
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Mis Órdenes
                    </a>
                `;
            }

            if (user.role === 'ADMIN') {
                right.innerHTML += `
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Órdenes
                    </a>
                    <a href="usuarios.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión deUsuarios
                    </a>
                `;
            }

            if (user.role === 'SUPPLIER') {
                right.innerHTML += `
                    <a href="productos.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Mis Productos
                    </a>
                `;
            }

            // Saludo
            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
            `;

            // Logout
            right.innerHTML += `
                <button onclick="window.logout()" 
                    class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    renderMenuUsers() {
        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;
        const navContainer = document.getElementById('mainNavUsers');

        if (!navContainer) return;

        navContainer.innerHTML = '';

        // Contenedor principal FLEX
        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === IZQUIERDA (Logo) ===
        const left = document.createElement('div');
        left.className = 'flex items-center';

        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === DERECHA (Opciones dinámicas) ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">
                    Iniciar Sesión
                </a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {

            if (user.role === 'ADMIN') {
                right.innerHTML += `
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Órdenes
                    </a>
                    <a href="productos.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión deProductos
                    </a>
                `;
            }

            // Saludo
            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
            `;

            // Logout
            right.innerHTML += `
                <button onclick="window.logout()" 
                    class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    renderMenuCart() {
        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;
        const navContainer = document.getElementById('mainNavCart');

        if (!navContainer) return;

        navContainer.innerHTML = '';

        // Contenedor principal FLEX
        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === IZQUIERDA (Logo) ===
        const left = document.createElement('div');
        left.className = 'flex items-center';

        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === DERECHA (Opciones dinámicas) ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">
                    Iniciar Sesión
                </a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {

            if (user.role === 'CUSTOMER') {
                right.innerHTML += `
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Mis Órdenes
                    </a>
                    <a href="productoscustomer.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Productos
                    </a>
                `;
            }

            if (user.role === 'ADMIN') {
                right.innerHTML += `
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Órdenes
                    </a>
                    <a href="productos.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Productos
                    </a>
                `;
            }

            // Saludo
            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
            `;

            // Logout
            right.innerHTML += `
                <button onclick="window.logout()" 
                    class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    renderMenuOrders() {
        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;
        const navContainer = document.getElementById('mainNavOrders');

        if (!navContainer) return;

        navContainer.innerHTML = '';

        // Contenedor principal FLEX
        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === IZQUIERDA (Logo) ===
        const left = document.createElement('div');
        left.className = 'flex items-center';

        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === DERECHA (Opciones dinámicas) ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">
                    Iniciar Sesión
                </a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {

            if (user.role === 'CUSTOMER') {
                right.innerHTML += `
                    <a href="carrito.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Carrito
                    </a>
                    <a href="productoscustomer.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Productos
                    </a>
                `;
            }

            if (user.role === 'ADMIN') {
                right.innerHTML += `
                    <a href="usuarios.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Usuarios
                    </a>
                    <a href="productos.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Gestión de Productos
                    </a>
                `;
            }

            // Saludo
            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
            `;

            // Logout
            right.innerHTML += `
                <button onclick="window.logout()" 
                    class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    renderMenuProductsCustomer() {
        const userStr = localStorage.getItem('user');
        const user = userStr ? JSON.parse(userStr) : null;
        const navContainer = document.getElementById('mainNavProductsCustomer');

        if (!navContainer) return;

        navContainer.innerHTML = '';

        // Contenedor principal FLEX
        const wrapper = document.createElement('div');
        wrapper.className = 'container mx-auto px-6 py-4 flex justify-between items-center';

        // === IZQUIERDA (Logo) ===
        const left = document.createElement('div');
        left.className = 'flex items-center';

        left.innerHTML = `
            <a class="text-2xl font-bold text-blue-600">
                E-Shop
            </a>
        `;

        // === DERECHA (Opciones dinámicas) ===
        const right = document.createElement('div');
        right.className = 'flex items-center gap-6';

        if (!user) {
            right.innerHTML = `
                <a href="login.html" class="text-gray-700 hover:text-blue-600 font-medium">
                    Iniciar Sesión
                </a>
                <a href="register.html" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Registrarse
                </a>
            `;
        } else {

            // === ROLE BASED LINKS ===
            if (user.role === 'CUSTOMER') {
                right.innerHTML += `
                    <a href="carrito.html" class="text-gray-700 hover:text-blue-600 font-medium">
                       Carrito
                    </a>
                    <a href="ordenes.html" class="text-gray-700 hover:text-blue-600 font-medium">
                        Mis Órdenes
                    </a>
                `;
            }

            // Saludo
            right.innerHTML += `
                <span class="text-sm text-gray-500">
                    Hola, ${user.nombre}
                </span>
            `;

            // Logout
            right.innerHTML += `
                <button onclick="window.logout()" 
                    class="text-red-500 hover:text-red-700 font-medium">
                    <i class="fas fa-sign-out-alt"></i> Salir
                </button>
            `;
        }

        wrapper.appendChild(left);
        wrapper.appendChild(right);
        navContainer.appendChild(wrapper);
    },

    showNotification(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `fixed top-5 right-5 px-6 py-3 rounded shadow-lg text-white z-50 transition-opacity duration-300 ${type === 'success' ? 'bg-green-500' : 'bg-red-500'
            }`;
        notification.textContent = message;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('opacity-0');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
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

    formatCurrency(amount) {
        return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount);
    }
};
