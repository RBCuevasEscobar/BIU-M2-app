import Api from './api.js';
import { UI } from './ui.js';
import Auth from './auth.js';

let formInputValue = document.getElementById('expiryValue');
let formInputUnit = document.getElementById('expiryUnit');
let submitBtn = document.getElementById('btnGuardarConfig');
let form = document.getElementById('jwtConfigForm');
let labelHelp = document.getElementById('labelDetail');

let actualMs = 0;

document.addEventListener('DOMContentLoaded', async () => {

    if (!Auth.requireAuth(["ADMIN"])) return;

    UI.renderNavBar({
        containerId: 'mainNav',
        context: 'config-auth'
    });

    await loadInitialConfig();
    setupListeners();

});

async function loadInitialConfig() {
    try {
        const res = await Api.get('/config/jwt');
        actualMs = res.expirationTime || 86400000;
        
        applyToUI(actualMs);
        formatLabel();
    } catch (e) {
        UI.showNotification('Error obteniendo la configuración actual', 'error');
    }
}

function applyToUI(ms) {
    if (ms >= 86400000 && ms % 86400000 === 0) {
        formInputValue.value = ms / 86400000;
        formInputUnit.value = 86400000;
    } else if (ms >= 3600000 && ms % 3600000 === 0) {
        formInputValue.value = ms / 3600000;
        formInputUnit.value = 3600000;
    } else if (ms >= 60000 && ms % 60000 === 0) {
        formInputValue.value = ms / 60000;
        formInputUnit.value = 60000;
    } else {
        formInputValue.value = ms / 1000;
        formInputUnit.value = 1000;
    }
}

function setupListeners() {
    formInputValue.addEventListener('input', () => {
        checkForm();
        formatLabel();
    });
    
    formInputUnit.addEventListener('change', () => {
        checkForm();
        formatLabel();
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await saveConfig();
    });
}

function checkForm() {
    const v = parseInt(formInputValue.value);
    const msValue = v * parseInt(formInputUnit.value);
    
    if (v > 0 && msValue !== actualMs) {
        submitBtn.disabled = false;
    } else {
        submitBtn.disabled = true;
    }
}

function formatLabel() {
    const val = formInputValue.value;
    const ut = formInputUnit.options[formInputUnit.selectedIndex]?.text.toLowerCase() || "";
    labelHelp.textContent = val > 0 ? `${val} ${ut} de validez en sesión calculados.` : "Ingrese un tiempo de expiración.";
}

async function saveConfig() {
    const v = parseInt(formInputValue.value);
    const msValue = v * parseInt(formInputUnit.value);

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Procesando...';

    try {
        const res = await Api.put('/config/jwt', { expirationTime: msValue });
        
        actualMs = res.expirationTime;
        checkForm();
        
        UI.showNotification('Milisegundos Expiración ajustados exitosamente a: ' + actualMs, 'success');

        submitBtn.innerHTML = '<i class="fas fa-check mr-2"></i> Guardado';
        setTimeout(() => {
            submitBtn.innerHTML = '<i class="fas fa-save mr-2"></i> Guardar Cambios';
        }, 3000);

    } catch (e) {
        UI.showNotification('Error al actualizar la configuración: ' + e.message, 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-save mr-2"></i> Guardar Cambios';
    }
}
