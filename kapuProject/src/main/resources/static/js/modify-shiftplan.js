
/**
 * 
 */
document.addEventListener("DOMContentLoaded", function() {
	const modifyBtn = document.getElementById("modifyShiftsBtn");
	const openBtn = document.getElementById("openShiftsBtn");
	let mode = "modify"; // modo activo por defecto

	modifyBtn.addEventListener("click", function() {
		mode = "modify";
		modifyBtn.classList.add("active", "btn-outline-success");
		openBtn.classList.remove("active", "btn-outline-success");
		openBtn.classList.add("btn-outline-secondary");
	});

	openBtn.addEventListener("click", function() {
		mode = "open";
		openBtn.classList.add("active", "btn-outline-success");
		modifyBtn.classList.remove("active", "btn-outline-success");
		modifyBtn.classList.add("btn-outline-secondary");
	});

	// 🎯 activate shift edition if we are in mode : "modify"
	document.querySelectorAll(".selectable-cell").forEach(cell => {
		cell.addEventListener("click", function() {
			if (mode !== "modify") return;

			const originalName = cell.textContent.trim();
			if (originalName === "-") return; // ignore empty cells

			const shiftId = cell.getAttribute("data-shift-id");

			const input = document.createElement("input");
			input.type = "text";
			input.value = originalName;
			input.classList.add("form-control", "form-control-sm");
			input.style.maxWidth = "120px";

			// replace content with imput
			cell.innerHTML = "";
			cell.appendChild(input);
			input.focus();

			
			// when exiting or pressing enter
			const save = () => {
				const newName = input.value.trim();
				if (!newName || newName === originalName) {
					cell.textContent = originalName;
					return;
				}

				// search for coincidences in employees regardless of capital letters
				const matchedUser = employees.find(
					user => user.name.toLowerCase() === newName.toLowerCase()
				);

				if (!matchedUser) {
					alert("No such employee found.");
					cell.textContent = originalName;
					return;
				}

				const confirmedName = matchedUser.name; // exact name
				cell.textContent = confirmedName;

				// save in database
				fetch("/modify-shiftplan/update-employee", {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						[document.querySelector("meta[name='_csrf_header']").content]:
							document.querySelector("meta[name='_csrf']").content
					},
					body: JSON.stringify({
						shiftId: shiftId,
						userName: confirmedName
					})
				})
					.then(response => {
						if (!response.ok) throw new Error("Failed to update shift");
						return response.text();
					})
					.then(data => {
						console.log("Update successful:", data);
					})
					.catch(error => {
						console.error("Error:", error);
						alert("An error occurred while saving.");
						cell.textContent = originalName;
					});
			};



			input.addEventListener("blur", save);
			input.addEventListener("keydown", function(e) {
				if (e.key === "Enter") {
					input.blur();
				}
			});
		});
	});
});
