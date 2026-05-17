// Toggle password visibility
function togglePassword(id) {
    const input = document.getElementById(id);
    const icon  = document.getElementById(id + '-eye');
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
    if (icon) {
        icon.classList.toggle('bi-eye');
        icon.classList.toggle('bi-eye-slash');
    }
}

document.addEventListener('DOMContentLoaded', () => {

    // Auto-dismiss alerts after 4 seconds
    document.querySelectorAll('.alert-success, .alert-danger').forEach(el => {
        setTimeout(() => {
            const a = bootstrap.Alert.getOrCreateInstance(el);
            if (a) a.close();
        }, 4000);
    });

    // Default date input to today
    const d = document.getElementById('expenseDate');
    if (d && !d.value) d.value = new Date().toISOString().split('T')[0];

    // Round amount to 2 decimal places on blur
    const amt = document.getElementById('amount');
    if (amt) amt.addEventListener('blur', () => {
        const v = parseFloat(amt.value);
        if (!isNaN(v)) amt.value = v.toFixed(2);
    });
});
