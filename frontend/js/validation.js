document.addEventListener("DOMContentLoaded", () => {
    // Both forms
    const registerForm = document.getElementById("registerForm");
    const adminForm = document.getElementById("formUsuario");

    const state = {};

    const REGEX = {
        email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        password: /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/,
        rfc: /^[A-Za-z]{4}[0-9]{6}[A-Za-z0-9]{3}$/,
        curp: /^[A-Za-z]{4}[0-9]{6}[HM][A-Za-z]{5}[0-9]{2}$/
    };

    const RULES = {
        common: {
            nombre: ["required", "minLength:3"],
            email: ["required", "email"]
        },
        cliente: {
            fechaNacimiento: ["required", "adult"],
            rfcCurp: ["required", "rfcOrCurp"],
        },
        CUSTOMER: {
            fechaNacimiento: ["required", "adult"],
            rfcCurp: ["required", "rfcOrCurp"],
        },
        proveedor: {
            empresa: ["required", "minLength:3"],
        },
        SUPPLIER: {
            empresa: ["required", "minLength:3"],
        },
        ADMIN: {
            validUntil: ["required"]
        }
    };

    const validators = {
        required(value) { return value.trim().length > 0; },
        email(value) { return REGEX.email.test(value); },
        minLength(value, length) { return value.length >= parseInt(length); },
        passwordStrength(value) {
            updatePasswordStrength(value);
            return REGEX.password.test(value);
        },
        passwordMatch(value) {
            const p = document.getElementById("password");
            if(!p) return true;
            return value === p.value;
        },
        adult(value) {
            if(!value) return false;
            const birth = new Date(value);
            const today = new Date();
            let age = today.getFullYear() - birth.getFullYear();
            const m = today.getMonth() - birth.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
            return age >= 18;
        },
        rfcOrCurp(value) {
            const fnInput = document.getElementById("fechaNacimiento");
            if(!fnInput) return false;
            const fechaNacimiento = fnInput.value;
            if (!fechaNacimiento) return false;
            const rfc = REGEX.rfc.test(value);
            const curp = REGEX.curp.test(value);
            if (!rfc && !curp) return false;
            const fechaDoc = value.substring(4, 10);
            const fecha = fechaNacimiento.replaceAll("-", "").substring(2);
            return fechaDoc === fecha;
        }
    };

    function getActiveRules(formEl) {
        let type = "";
        let baseRules = { ...RULES.common };

        if (formEl.id === "registerForm") {
            const ut = document.getElementById("userType");
            if(ut) type = ut.value;
            baseRules.password = ["required", "passwordStrength"];
            baseRules.confirmPassword = ["required", "passwordMatch"];
        } else if (formEl.id === "formUsuario") {
            const rl = document.getElementById("role");
            if(rl) type = rl.value;
            
            const pInput = document.getElementById("password");
            if (pInput && pInput.hasAttribute("required")) {
                baseRules.password = ["required", "passwordStrength"];
            } else if (pInput && pInput.value) {
                baseRules.password = ["passwordStrength"];
            }
        }

        return {
            ...baseRules,
            ...(RULES[type] || {})
        };
    }

    function validateField(field, formEl) {
        const rules = getActiveRules(formEl);
        const name = field.id;
        if (!rules[name]) return true;

        const value = field.value.trim();
        const ruleSet = rules[name];

        for (const rule of ruleSet) {
            const [validator, param] = rule.split(":");
            const valid = validators[validator](value, param);
            if (!valid) {
                updateUI(field, false);
                state[name] = false;
                evaluateForm(formEl);
                return false;
            }
        }

        updateUI(field, true);
        state[name] = true;
        evaluateForm(formEl);
        return true;
    }

    function updateUI(field, valid) {
        const error = document.getElementById(`error-${field.id}`);
        if (valid) {
            field.classList.remove("invalid");
            field.classList.add("valid");
            if (error) error.style.display = "none";
        } else {
            field.classList.remove("valid");
            field.classList.add("invalid");
            if (error) error.style.display = "block";
        }
    }

    function updatePasswordStrength(password) {
        const passwordBar = document.getElementById("password-strength-bar");
        if(!passwordBar) return;
        if (!password) {
            passwordBar.style.width = "0%";
            return;
        }
        let score = 0;
        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;

        const percent = (score / 4) * 100;
        passwordBar.style.width = percent + "%";
        if (score <= 1) passwordBar.className = "h-1 bg-red-500";
        else if (score === 2) passwordBar.className = "h-1 bg-yellow-500";
        else passwordBar.className = "h-1 bg-green-500";
    }

    function evaluateForm(formEl) {
        const rules = getActiveRules(formEl);
        const fields = Object.keys(rules);
        const valid = fields.every(f => state[f] === true);

        let btn = null;
        if(formEl.id === "registerForm") btn = document.getElementById("submitBtn");
        if(formEl.id === "formUsuario") btn = document.getElementById("submitBtnUsuario");
        
        if (btn) btn.disabled = !valid;
    }

    // Attach to Register
    if (registerForm) {
        const userTypeSelect = document.getElementById("userType");
        const extraFieldsContainer = document.getElementById("extraFields");
        const message = document.getElementById("form-message");

        function renderExtraFields(type) {
            extraFieldsContainer.innerHTML = "";
            if (type === "cliente") {
                extraFieldsContainer.innerHTML = `
                    <div>
                        <label class="block text-gray-700 font-medium mb-1">RFC / CURP</label>
                        <input id="rfcCurp" type="text" maxlength="18" class="w-full px-4 py-2 border rounded-lg">
                        <p id="error-rfcCurp" class="error-msg">RFC/CURP inválido</p>
                    </div>`;
            }
            if (type === "proveedor") {
                extraFieldsContainer.innerHTML = `
                    <div>
                        <label class="block text-gray-700 font-medium mb-1">Empresa</label>
                        <input id="empresa" type="text" class="w-full px-4 py-2 border rounded-lg">
                        <p id="error-empresa" class="error-msg">Empresa obligatoria</p>
                    </div>`;
            }
        }

        userTypeSelect.addEventListener("change", () => {
            renderExtraFields(userTypeSelect.value);
            evaluateForm(registerForm);
        });
        userTypeSelect.dispatchEvent(new Event("change"));

        registerForm.addEventListener("input", e => {
            if (e.target.matches("input")) validateField(e.target, registerForm);
        });
        registerForm.addEventListener("blur", e => {
            if (e.target.matches("input")) validateField(e.target, registerForm);
        }, true);

        registerForm.addEventListener("submit", async e => {
            e.preventDefault();
            const type = userTypeSelect.value;
            const payload = {
                nombre: document.getElementById("nombre").value,
                email: document.getElementById("email").value,
                password: document.getElementById("password").value,
                fechaNacimiento: document.getElementById("fechaNacimiento").value
            };
            if (type === "cliente") payload.rfcCurp = document.getElementById("rfcCurp").value;
            if (type === "proveedor") payload.empresa = document.getElementById("empresa").value;

            try {
                const response = await fetch(`http://localhost:8080/api/usuarios/${type}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (response.ok) {
                    message.textContent = "Registro exitoso";
                    message.className = "text-green-600 text-center mt-4";
                    registerForm.reset();
                    setTimeout(() => { window.location.href = "login.html"; }, 2000);
                } else {
                    message.textContent = "Error en registro";
                    message.className = "text-red-600 text-center mt-4";
                }
            } catch (err) {
                console.error(err);
                message.textContent = "Error de conexión";
                message.className = "text-red-600 text-center mt-4";
            }
        });
    }

    // Attach to Admin Form
    if (adminForm) {
        adminForm.addEventListener("input", e => {
            if (e.target.matches("input, select")) validateField(e.target, adminForm);
        });
        adminForm.addEventListener("blur", e => {
            if (e.target.matches("input, select")) validateField(e.target, adminForm);
        }, true);
        const roleSel = document.getElementById("role");
        if(roleSel) {
            roleSel.addEventListener("change", () => {
                evaluateForm(adminForm);
            });
        }
    }
});