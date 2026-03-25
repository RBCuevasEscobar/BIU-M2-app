import Api from './api.js';

const Auth = {

    login: async (email, password) => {
        try {

            const response = await Api.post(
                '/auth/login',
                { email, password },    // 🔑 CLAVE
                { public: true }
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
        // Capturar el token ANTES de eliminarlo para el request de limpieza al servidor
        const tokenParaLimpieza = localStorage.getItem('token');

        // Limpiar sesión JWT
        localStorage.removeItem('user');
        localStorage.removeItem('token');

        // Limpiar historial del chatbot del localStorage (chat limpio en nuevo login)
        localStorage.removeItem('chatState');

        // Notificar al servidor para limpiar el historial en memoria del ChatMemoryService
        // (fire-and-forget: no bloqueamos el redirect si falla)
        if (tokenParaLimpieza) {
            fetch('http://localhost:8080/api/chat/historial', {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${tokenParaLimpieza}` }
            }).catch(() => { /* silencio si falla */ });
        }

        window.location.href = 'index.html';
    },

    getCurrentUser: () => {

        const userStr = localStorage.getItem('user');

        if (!userStr) return null;

        try {
            return JSON.parse(userStr);
        } catch {
            return null;
        }
    },

    getToken: () => {
        return localStorage.getItem('token');
    },

    isAuthenticated: () => {

        const token = Auth.getToken();

        if (!token) return false;

        return token !== null && token !== undefined && token.length > 0;
    },

    requireAuth: (allowedRoles = []) => {

        const user = Auth.getCurrentUser();

        if (!Auth.isAuthenticated() || !user) {
            window.location.href = 'login.html';
            return false;
        }

        // Validación ADMIN expirado
        if (user.role === 'ADMIN' && user.validUntil) {

            const validUntil = new Date(user.validUntil);

            if (validUntil < new Date()) {

                alert('Tu rol de administrador ha expirado');

                Auth.logout();

                return false;
            }
        }

        // Validación de roles permitidos
        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {

            alert('Acceso no autorizado para tu rol.');

            window.location.href = 'index.html'; // Fallback

            return false;
        }

        return true;
    },

    redirectBasedOnRole: (role) => {

        if (role === 'ADMIN' || role === 'SUPPLIER') {

            window.location.href = 'productos.html';

        } else {

            window.location.href = 'productoscustomer.html';

        }
    }

};


// -----------------------------
// LOGIN FORM
// -----------------------------

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


// Exponer logout globalmente
window.logout = Auth.logout;

export default Auth;