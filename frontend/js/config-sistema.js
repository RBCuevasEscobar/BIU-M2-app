import Api from './api.js';
import Auth from './auth.js';
import { UI } from './ui.js';

document.addEventListener('DOMContentLoaded', () => {
    // 1. Verificación de Seguridad Rol Administrativo
    if (!Auth.requireAuth(['ADMIN'])) return;
    UI.renderNavBar({ containerId: 'mainNav', context: 'config-sistema' });
    window.logout = () => { Auth.logout(); window.location.href = 'index.html'; };

    // 3. Capturar Elementos del DOM
    const form = document.getElementById('sistemaConfigForm');
    const ivaInput = document.getElementById('ivaInput');
    const monedaInput = document.getElementById('monedaInput');
    const stockInput = document.getElementById('stockInput');
    const maxProdInput = document.getElementById('maxProdInput');
    const debugToggle = document.getElementById('debugToggle');

    // 4. Cargar Parámetros Globales (GET /api/config/sistema)
    async function loadConfig() {
        try {
            const data = await Api.get('/config/sistema');

            // Hidratar Interfaz con las primitivas del Singleton
            ivaInput.value = data.iva;
            monedaInput.value = data.monedaSistema;
            stockInput.value = data.stockMinimo;
            maxProdInput.value = data.maxProductosOrden;
            debugToggle.checked = data.modoDebug;

        } catch (error) {
            UI.showNotification('Error al cargar configuración del sistema', 'error');
            console.error('Initial Config Load Failed', error);
        }
    }

    // 5. Salvar y Alterar el Singleton Global (PUT /api/config/sistema)
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const payload = {
            iva: parseFloat(ivaInput.value),
            monedaSistema: monedaInput.value.trim(),
            stockMinimo: parseInt(stockInput.value),
            maxProductosOrden: parseInt(maxProdInput.value),
            modoDebug: debugToggle.checked
        };

        try {
            const result = await Api.put('/config/sistema', payload);
            UI.showNotification(result.message || 'Configuración guardada exitosamente.', 'success');
        } catch (error) {
            UI.showNotification(error.message || 'Fallo actualizando la configuración', 'error');
            console.error('Config Submit Failed', error);
        }
    });

    // Run Init
    loadConfig();
});
