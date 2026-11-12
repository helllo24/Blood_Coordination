/**
 * Project: Emergency Blood Request and Donor Coordination System
 * Script: Reusable Frontend Logic, Role Switching & Validation
 */

document.addEventListener('DOMContentLoaded', function () {
    initPasswordToggle();
    initRoleToggle();
    initFormValidation();
    initBloodGroupCards();
});

/**
 * Toggle password visibility for input password fields
 */
function initPasswordToggle() {
    const toggleButtons = document.querySelectorAll('.input-icon-toggle');
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const targetInput = document.getElementById(targetId);
            if (!targetInput) return;

            if (targetInput.type === 'password') {
                targetInput.type = 'text';
                this.classList.remove('bi-eye');
                this.classList.add('bi-eye-slash');
            } else {
                targetInput.type = 'password';
                this.classList.remove('bi-eye-slash');
                this.classList.add('bi-eye');
            }
        });
    });
}

/**
 * Handle role switching in login and registration pages
 */
function initRoleToggle() {
    // Role switcher on Login or Register
    const roleButtons = document.querySelectorAll('.role-toggle-btn');
    const roleInput = document.getElementById('selectedRoleInput');
    const donorFields = document.getElementById('donorSpecificFields');

    roleButtons.forEach(button => {
        button.addEventListener('click', function () {
            roleButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            const chosenRole = this.getAttribute('data-role');
            if (roleInput) {
                roleInput.value = chosenRole;
            }

            // If on registration page, toggle donor-specific fields
            if (donorFields) {
                if (chosenRole === 'DONOR') {
                    donorFields.style.display = 'block';
                    setRequiredOnFields(donorFields, true);
                } else {
                    donorFields.style.display = 'none';
                    setRequiredOnFields(donorFields, false);
                }
            }
        });
    });
}

/**
 * Helper to toggle HTML5 required attribute on dynamic fields
 */
function setRequiredOnFields(container, isRequired) {
    const inputs = container.querySelectorAll('input, select');
    inputs.forEach(el => {
        if (el.getAttribute('data-conditional-required') === 'true') {
            el.required = isRequired;
        }
    });
}

/**
 * Client-side form validation before submitting
 */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');

    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function (event) {
            // Password confirmation check on registration
            const password = form.querySelector('#password');
            const confirmPassword = form.querySelector('#confirmPassword');
            const pwdMismatchAlert = document.getElementById('passwordMismatchAlert');

            if (password && confirmPassword) {
                if (password.value !== confirmPassword.value) {
                    event.preventDefault();
                    event.stopPropagation();
                    if (pwdMismatchAlert) {
                        pwdMismatchAlert.classList.remove('d-none');
                    }
                    confirmPassword.classList.add('is-invalid');
                    return false;
                } else {
                    if (pwdMismatchAlert) {
                        pwdMismatchAlert.classList.add('d-none');
                    }
                    confirmPassword.classList.remove('is-invalid');
                }
            }

            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * Subtle interactive highlight for blood group compatibility cards
 */
function initBloodGroupCards() {
    const cards = document.querySelectorAll('.blood-group-card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function () {
            this.style.borderColor = 'var(--primary-red)';
        });
        card.addEventListener('mouseleave', function () {
            this.style.borderColor = 'var(--border-color)';
        });
    });
}
