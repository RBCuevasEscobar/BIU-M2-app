import Api from './api.js';

const Auth = {
    login: async (email, password) => {
        try {
            console.log('Login attempt:', { email, password });
            const response = await Api.post(
                '/auth/login',
                { email, password },
                { public: true } // 🔑 CLAVE
            );
            // Save session
            localStorage.setItem('user', JSON.stringify(response));
            localStorage.setItem('token', response.token);

            Auth.redirectBasedOnRole(response.role);
        } catch (error) {
            throw error;
        }
    },

    logout: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        window.location.href = 'index.html';
    },

    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    getToken: () => {
        return localStorage.getItem('token');
    },

    isAuthenticated: () => {
        return !!Auth.getToken();
    },

    requireAuth: (allowedRoles = []) => {
        const user = Auth.getCurrentUser();

        if (user.role === 'ADMIN') {
            const validUntil = new Date(user.validUntil);
            if (validUntil < new Date()) {
                alert('Tu rol de administrador ha expirado');
                Auth.logout();
            }
        }

        if (!user) {
            window.location.href = 'login.html';
            return;
        }

        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            alert('Acceso no autorizado para su rol.');
            window.location.href = 'index.html'; // Fallback
        }
    },

    redirectBasedOnRole: (role) => {
        if (role === 'ADMIN' || role === 'SUPPLIER') {
            window.location.href = 'productos.html';
        } else {
            window.location.href = 'productos.html'; // Customers also go to store
        }
    }
};

// Handle Login Form
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const errorMessage = document.getElementById('errorMessage');

        try {
            await Auth.login(email, password);
        } catch (error) {
            console.error(error.message);
            errorMessage.textContent = 'Credenciales inválidas o error de conexión';
            errorMessage.classList.remove('hidden');
        }
    });
}

// Global logout exposure
window.logout = Auth.logout;

export default Auth;
