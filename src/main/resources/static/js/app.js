// Sunrise Dental Clinic - Interactive UI Helpers

document.addEventListener('DOMContentLoaded', () => {
    // Auto-dismiss toast notifications after 5 seconds
    const toasts = document.querySelectorAll('.toast-alert');
    toasts.forEach(toast => {
        setTimeout(() => {
            toast.classList.add('opacity-0', 'translate-y-2');
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    });

    // Mobile sidebar toggle handler
    const sidebarToggleBtn = document.getElementById('sidebar-toggle');
    const mobileSidebar = document.getElementById('mobile-sidebar');
    const mobileSidebarBackdrop = document.getElementById('mobile-sidebar-backdrop');

    if (sidebarToggleBtn && mobileSidebar && mobileSidebarBackdrop) {
        sidebarToggleBtn.addEventListener('click', () => {
            mobileSidebar.classList.toggle('hidden');
            mobileSidebarBackdrop.classList.toggle('hidden');
        });

        mobileSidebarBackdrop.addEventListener('click', () => {
            mobileSidebar.classList.add('hidden');
            mobileSidebarBackdrop.classList.add('hidden');
        });
    }

    // Frontend Form Validation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        // We add novalidate to handle validation UI manually and match our theme
        form.setAttribute('novalidate', 'true');
        
        form.addEventListener('submit', (e) => {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                
                // Highlight invalid fields
                const inputs = form.querySelectorAll('input, select, textarea');
                let firstInvalid = null;
                
                inputs.forEach(input => {
                    if (!input.checkValidity()) {
                        input.classList.add('border-rose-400', 'bg-rose-50/50');
                        input.classList.remove('border-slate-200', 'bg-slate-50');
                        
                        let errorMsg = input.parentElement.querySelector('.js-error-msg');
                        if (!errorMsg) {
                            errorMsg = document.createElement('p');
                            errorMsg.className = 'js-error-msg text-xs text-rose-600 mt-1.5 font-medium';
                            input.parentElement.appendChild(errorMsg);
                        }
                        errorMsg.textContent = input.validationMessage;
                        
                        if (!firstInvalid) {
                            firstInvalid = input;
                        }
                    } else {
                        input.classList.remove('border-rose-400', 'bg-rose-50/50');
                        input.classList.add('border-slate-200', 'bg-slate-50');
                        const errorMsg = input.parentElement.querySelector('.js-error-msg');
                        if (errorMsg) {
                            errorMsg.remove();
                        }
                    }
                });
                
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            }
        });

        // Add input listeners to clear errors on typing
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                if (input.checkValidity()) {
                    input.classList.remove('border-rose-400', 'bg-rose-50/50');
                    input.classList.add('border-slate-200', 'bg-slate-50');
                    const errorMsg = input.parentElement.querySelector('.js-error-msg');
                    if (errorMsg) {
                        errorMsg.remove();
                    }
                }
            });
        });
    });

    // Confirmation Modal System for Delete Actions
    const confirmModal = document.getElementById('confirm-modal');
    const confirmModalBox = document.getElementById('confirm-modal-box');
    const confirmModalTitle = document.getElementById('confirm-modal-title');
    const confirmModalMessage = document.getElementById('confirm-modal-message');
    const confirmModalCancel = document.getElementById('confirm-modal-cancel');
    const confirmModalOk = document.getElementById('confirm-modal-ok');
    let pendingForm = null;

    function openModal(title, message, form) {
        if (!confirmModal || !confirmModalBox) {
            if (confirm(message)) {
                form.submit();
            }
            return;
        }

        if (title && confirmModalTitle) confirmModalTitle.textContent = title;
        if (message && confirmModalMessage) confirmModalMessage.textContent = message;
        pendingForm = form;

        confirmModal.classList.remove('hidden');
        requestAnimationFrame(() => {
            confirmModalBox.classList.remove('scale-95', 'opacity-0');
            confirmModalBox.classList.add('scale-100', 'opacity-100');
        });
    }

    function closeModal() {
        if (!confirmModal || !confirmModalBox) return;
        confirmModalBox.classList.remove('scale-100', 'opacity-100');
        confirmModalBox.classList.add('scale-95', 'opacity-0');
        setTimeout(() => {
            confirmModal.classList.add('hidden');
            pendingForm = null;
        }, 150);
    }

    if (confirmModalCancel) {
        confirmModalCancel.addEventListener('click', closeModal);
    }

    if (confirmModalOk) {
        confirmModalOk.addEventListener('click', () => {
            if (pendingForm) {
                const formToSubmit = pendingForm;
                closeModal();
                formToSubmit.submit();
            }
        });
    }

    if (confirmModal) {
        confirmModal.addEventListener('click', (e) => {
            if (e.target === confirmModal) {
                closeModal();
            }
        });
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && confirmModal && !confirmModal.classList.contains('hidden')) {
            closeModal();
        }
    });

    // Intercept forms with .js-delete-form
    document.addEventListener('submit', (e) => {
        const form = e.target;
        if (form && form.classList.contains('js-delete-form')) {
            e.preventDefault();
            e.stopPropagation();
            const title = form.getAttribute('data-title') || 'Confirm Deletion';
            const message = form.getAttribute('data-message') || 'Are you sure you want to delete this record? This action cannot be undone.';
            openModal(title, message, form);
        }
    });
});

function dismissToast(button) {
    const toast = button.closest('.toast-alert');
    if (toast) {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }
}
