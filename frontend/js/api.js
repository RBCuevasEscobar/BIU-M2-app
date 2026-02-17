const API_BASE_URL = 'http://localhost:8080/api';

class Api {
    static async request(endpoint, method = 'GET', body = null, options = {}) {
        const isPublic = options.public === true;

        const headers = {};

        // Solo agregar token si NO es endpoint público
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
            headers,
        };

        if (body) {
            fetchOptions.body = JSON.stringify(body);
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, fetchOptions);

        // ❗ Manejo de seguridad SOLO para endpoints protegidos
        if (!isPublic && (response.status === 401 || response.status === 403)) {
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            window.location.href = 'login.html';
            throw new Error('Sesión expirada o no autorizada');
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
                // Ignorar errores de parseo
            }

            throw new Error(message);
        }

        return response.json();
    }

    static get(endpoint) {
        return this.request(endpoint, 'GET');
    }

    static post(endpoint, body, options = {}) {
        return this.request(endpoint, 'POST', body, options);
    }

    static put(endpoint, body) {
        return this.request(endpoint, 'PUT', body);
    }

    static delete(endpoint) {
        return this.request(endpoint, 'DELETE');
    }
}

export default Api;