document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const inputs = form.querySelectorAll('input');
    const userTypeSelect = document.getElementById('userType');
    const extraFieldsContainer = document.getElementById('extraFields');
    const submitBtn = document.getElementById('submitBtn');
    const message = document.getElementById('form-message');

    // Estado de validación
    const validationState = {
        nombre: false,
        email: false,
        password: false,
        confirmPassword: false,
        fechaNacimiento: false
    };

    // Regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/; // Min 8, 1 Mayus, 1 Num

    // Campos extra dinámicos
    userTypeSelect.addEventListener('change', () => {
        const type = userTypeSelect.value;
        extraFieldsContainer.innerHTML = '';
        if (type === 'cliente') {
            extraFieldsContainer.innerHTML = `
                <div>
                    <label class="block text-gray-700 font-medium mb-1">Dirección de Envío</label>
                    <input type="text" id="direccionEnvio" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                </div>
            `;
        } else if (type === 'proveedor') {
            extraFieldsContainer.innerHTML = `
                <div>
                    <label class="block text-gray-700 font-medium mb-1">Nombre de la Empresa</label>
                    <input type="text" id="empresa" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                </div>
            `;
        }
    });
    // Trigger inicial
    userTypeSelect.dispatchEvent(new Event('change'));

    // Validar Inputs
    inputs.forEach(input => {
        input.addEventListener('input', () => validateInput(input));
        input.addEventListener('blur', () => validateInput(input));
    });

    function validateInput(input) {
        const id = input.id;
        const value = input.value.trim();
        let isValid = false;
        const errorMsg = document.getElementById(`error-${id}`);

        if (!errorMsg) return; // Si es un campo dinámico sin error msg explícito (por ahora)

        switch (id) {
            case 'nombre':
                isValid = value.length > 2;
                break;
            case 'email':
                isValid = emailRegex.test(value);
                break;
            case 'password':
                isValid = passwordRegex.test(value);
                updatePasswordStrength(value);
                // Revalidar confirmación si cambia password
                const confirmInput = document.getElementById('confirmPassword');
                if (confirmInput.value) validateInput(confirmInput);
                break;
            case 'confirmPassword':
                const pass = document.getElementById('password').value;
                isValid = value === pass && value.length > 0;
                break;
            case 'fechaNacimiento':
                const date = new Date(value);
                const age = new Date().getFullYear() - date.getFullYear();
                isValid = age >= 18;
                break;
            default:
                isValid = true;
        }

        if (id in validationState) {
            validationState[id] = isValid;
        }

        if (isValid) {
            input.classList.remove('invalid');
            input.classList.add('valid');
            errorMsg.style.display = 'none';
        } else {
            input.classList.remove('valid');
            input.classList.add('invalid');
            errorMsg.style.display = 'block';
        }

        checkFormValidity();
    }

    function updatePasswordStrength(password) {
        const bar = document.getElementById('password-strength-bar');
        if (!password) {
            bar.style.width = '0%';
            return;
        }
        let strength = 0;
        if (password.length >= 8) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;

        const percent = (strength / 4) * 100;
        bar.style.width = `${percent}%`;

        if (strength <= 1) bar.className = 'h-1 rounded transition-all duration-300 bg-red-500';
        else if (strength === 2) bar.className = 'h-1 rounded transition-all duration-300 bg-yellow-500';
        else if (strength >= 3) bar.className = 'h-1 rounded transition-all duration-300 bg-green-500';
    }

    function checkFormValidity() {
        const allValid = Object.values(validationState).every(v => v);
        submitBtn.disabled = !allValid;
    }

    // Submit
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const type = userTypeSelect.value;
        const data = {
            nombre: document.getElementById('nombre').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value,
            fechaNacimiento: document.getElementById('fechaNacimiento').value
        };

        if (type === 'cliente') {
            data.direccionEnvio = document.getElementById('direccionEnvio').value;
        } else if (type === 'proveedor') {
            data.empresa = document.getElementById('empresa').value;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/usuarios/${type}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                message.textContent = 'Registro exitoso!';
                message.className = 'text-center mt-4 font-medium text-green-600';
                form.reset();
                setTimeout(() => window.location.href = 'index.html', 2000);
            } else {
                message.textContent = 'Error en el registro.';
                message.className = 'text-center mt-4 font-medium text-red-600';
            }
        } catch (error) {
            console.error(error);
            message.textContent = 'Error de conexión.';
            message.className = 'text-center mt-4 font-medium text-red-600';
        }
    });
});
