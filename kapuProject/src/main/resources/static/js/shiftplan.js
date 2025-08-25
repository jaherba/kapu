// Obtain CSRF token from HTML
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

let selectedShifts = [];

// shift selection control
document.querySelectorAll('.selectable-cell').forEach(cell => {
    cell.addEventListener('click', () => {

        const toggle = document.getElementById("shiftExchangeToggle");
        if (!toggle || !toggle.checked) return;

        const shiftId = cell.getAttribute("data-shift-id");

        // visual Selection / deselection 
        if (selectedShifts.includes(shiftId)) {
            selectedShifts = selectedShifts.filter(id => id !== shiftId);
            cell.classList.remove('selected');
        } else {
            if (selectedShifts.length < 2) {
                selectedShifts.push(shiftId);
                cell.classList.add('selected');
            }
        }

        // if there are two shifts selected , show confirmation
        if (selectedShifts.length === 2) {
            showConfirmationDialog(selectedShifts[0], selectedShifts[1]);
        }
    });
});

// show messege on screen
function showAlert(message, type = "error") {
    const alertBox = document.getElementById("shiftplan-alert-area");
    alertBox.textContent = message;
    alertBox.style.display = "block";
    alertBox.className = type === "success" ? "alert-success" : "alert-error";

    setTimeout(() => {
        alertBox.style.display = "none";
    }, 10000); // ← show messege during 10 seconds
}

// confirmation modal
function showConfirmationDialog(id1, id2) {
    const shiftTexts = {};

    document.querySelectorAll('.selectable-cell').forEach(cell => {
        const id = cell.getAttribute("data-shift-id");
        if (id === id1 || id === id2) {
            shiftTexts[id] = cell.innerText.trim() + " (" + getDateFromCell(cell) + ")";
        }
    });

    const overlay = document.createElement("div");
    overlay.style.position = "fixed";
    overlay.style.top = 0;
    overlay.style.left = 0;
    overlay.style.width = "100vw";
    overlay.style.height = "100vh";
    overlay.style.backgroundColor = "rgba(0,0,0,0.7)";
    overlay.style.display = "flex";
    overlay.style.justifyContent = "center";
    overlay.style.alignItems = "center";
    overlay.style.zIndex = "9999";

    const box = document.createElement("div");
    box.style.backgroundColor = "white";
    box.style.padding = "2rem";
    box.style.borderRadius = "8px";
    box.style.textAlign = "center";
    box.style.maxWidth = "90%";

    const msg = document.createElement("p");
    msg.textContent = "Are you sure you want to send this swap request?";
    msg.style.fontWeight = "bold";

    const details = document.createElement("p");
    details.innerHTML = `
        <strong>From:</strong> ${shiftTexts[id1]}<br>
        <strong>To:</strong> ${shiftTexts[id2]}
    `;

    const btnYes = document.createElement("button");
    btnYes.className = "btn btn-success me-2";
    btnYes.textContent = "Yes";
    btnYes.onclick = () => {
        sendSwapRequest(id1, id2);
        document.body.removeChild(overlay);
    };

    const btnNo = document.createElement("button");
    btnNo.className = "btn btn-danger";
    btnNo.textContent = "No";
    btnNo.onclick = () => {
        document.body.removeChild(overlay);
        selectedShifts = [];
        document.querySelectorAll('.selectable-cell').forEach(c => c.classList.remove('selected'));
    };

    box.appendChild(msg);
    box.appendChild(details);
    box.appendChild(btnYes);
    box.appendChild(btnNo);
    overlay.appendChild(box);
    document.body.appendChild(overlay);
}

// extract corresponding date from the modal
function getDateFromCell(cell) {
    const row = cell.closest("tr");
    if (row) {
        const dateCell = row.querySelector("td");
        return dateCell ? dateCell.innerText.trim() : "";
    }
    return "";
}

// send exchange request
function sendSwapRequest(id1, id2) {
    fetch('/shift-exchange/swap', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        credentials: 'include',
        body: JSON.stringify({
            fromShiftId: id1,
            toShiftId: id2
        })
    })
    .then(response => response.text().then(msg => {
        if (response.ok) {
            showAlert("✅ " + msg, "success");
            setTimeout(() => location.reload(), 1000);
        } else {
            showAlert("❌ " + msg, "error");
        }
    }))
    .catch(() => showAlert("⚠️ Unexpected error", "error"));

    selectedShifts = [];
    document.querySelectorAll('.selectable-cell').forEach(c => c.classList.remove('selected'));
}

// accept exchange request
function acceptSwap(button) {
    const id = button.getAttribute("data-id");

    fetch(`/shift-exchange/swap/accept/${id}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        credentials: 'include'
    })
    .then(response => response.text().then(msg => {
        if (response.ok) {
            showAlert("✅ " + msg, "success");
            setTimeout(() => location.reload(), 1000);
        } else {
            showAlert("❌ " + msg, "error");
        }
    }))
    .catch(() => showAlert("⚠️ Unexpected error", "error"));
}

// reject exchange request
function rejectSwap(button) {
    const id = button.getAttribute("data-id");

    fetch(`/shift-exchange/swap/reject/${id}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        credentials: 'include'
    })
    .then(response => response.text().then(msg => {
        if (response.ok) {
            showAlert("🛑 " + msg, "success");
            setTimeout(() => location.reload(), 1000);
        } else {
            showAlert("❌ " + msg, "error");
        }
    }))
    .catch(() => showAlert("⚠️ Unexpected error", "error"));
}
