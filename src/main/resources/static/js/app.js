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
});

function dismissToast(button) {
    const toast = button.closest('.toast-alert');
    if (toast) {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }
}
