// Obtain CSRF token from HTML
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

console.log("✅ notifications.js loaded");


// show message on screen
function showAlert(message, type = "error") {
    const alertBox = document.createElement("div");
    alertBox.className = `alert alert-${type} mt-3`;
    alertBox.textContent = message;

    const container = document.querySelector(".container");
    container.insertBefore(alertBox, container.firstChild);

    setTimeout(() => {
        alertBox.remove();
    }, 5000);
}

let currentAction = null;
let currentRequestId = null;

// show confirmation modal
function showConfirmationModal(message, requestId, actionType) {
    document.getElementById("confirmModalMessage").textContent = message;
    const modal = new bootstrap.Modal(document.getElementById("confirmModal"));
    modal.show();

    currentRequestId = requestId;
    currentAction = actionType;
}

// listen to "click" in th boton "yes" of modal
document.getElementById("confirmModalYesBtn").addEventListener("click", () => {
    if (!currentRequestId || !currentAction) return;

    const url = `/shift-exchange/swap/${currentAction}/${currentRequestId}`;
    fetch(url, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        credentials: 'include'
    })
    .then(response => response.text().then(msg => {
        if (response.ok) {
            const type = currentAction === 'accept' ? "success" : "warning";
            showAlert((currentAction === 'accept' ? "✅ " : "🛑 ") + msg, type);

            // hide notification in inbox
            const notifElement = document.querySelector(`[data-id="${currentRequestId}"]`)?.closest('.notification-box');
            if (notifElement) notifElement.remove();
        } else {
            showAlert("❌ " + msg, "danger");
        }
    }))
    .catch(() => showAlert("⚠️ Unexpected error", "danger"));

    // Reset
    currentAction = null;
    currentRequestId = null;

    // hide modal
    const modal = bootstrap.Modal.getInstance(document.getElementById("confirmModal"));
    modal.hide();
});

// original botons redirect to modal
function acceptSwap(button) {
    const requestId = button.getAttribute("data-id");
    showConfirmationModal("Are you sure you want to ACCEPT this shift swap request?", requestId, "accept");
}

function rejectSwap(button) {
    const requestId = button.getAttribute("data-id");
    showConfirmationModal("Are you sure you want to REJECT this shift swap request?", requestId, "reject");
}
