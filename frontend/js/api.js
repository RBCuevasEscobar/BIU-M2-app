const API_BASE_URL = 'http://localhost:8080/api';

class Api {

    static async request(endpoint, method = 'GET', body = null, options = {}) {

        const isPublic = options.public === true;

        const headers = {};

        if (!isPublic) {
            const token = localStorage.getItem('token');

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
        }

        if (body) {
            headers['Content-Type'] = 'application/json';
        }

        const fetchOptions = {
            method,
            headers
        };

        if (body) {
            fetchOptions.body = JSON.stringify(body);
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, fetchOptions);

        /* --------------------------------------------------
           MANEJO DE AUTENTICACIÓN CORREGIDO
        -------------------------------------------------- */

        // SOLO 401 significa token inválido
        if (!isPublic && response.status === 401) {

            console.warn("Token inválido o expirado");

            localStorage.removeItem('user');
            localStorage.removeItem('token');

            window.location.href = 'login.html';

            throw new Error('Sesión expirada');
        }

        // 403 = permiso denegado
        if (response.status === 403) {

            console.warn("Acceso denegado:", endpoint);

            throw new Error('No tienes permisos para realizar esta acción');
        }

        if (response.status === 204) {
            return null;
        }

        if (!response.ok) {

            let message = `Error ${response.status}`;

            try {
                const errorData = await response.json();

                if (errorData && errorData.message) {
                    message = errorData.message;
                }

            } catch {
                // ignore
            }

            throw new Error(message);
        }

        return response.json();
    }

    static get(endpoint, options = {}) {
        return this.request(endpoint, 'GET', null, options);
    }

    static post(endpoint, body, options = {}) {
        return this.request(endpoint, 'POST', body, options);
    }

    static put(endpoint, body, options = {}) {
        return this.request(endpoint, 'PUT', body, options);
    }

    static delete(endpoint, options = {}) {
        return this.request(endpoint, 'DELETE', null, options);
    }
}

export default Api;