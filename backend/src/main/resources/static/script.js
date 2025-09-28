console.log("Script loaded");

document.getElementById("leaveForm").addEventListener("submit", function (e) {
  e.preventDefault();

  const leaveData = {
    employeeName: document.getElementById("employeeName").value,
    startDate: document.getElementById("fromDate").value, // HTML date input returns YYYY-MM-DD format
    endDate: document.getElementById("toDate").value, // HTML date input returns YYYY-MM-DD format
    reason: document.getElementById("reason").value,
    status: document.getElementById('status').value || "Pending"
  };

  console.log("Submitting leave request:", leaveData); // Log the data being sent

          fetch("http://localhost:8082/leaves", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(leaveData),
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('Failed to submit leave');
      }
      return response.json();
    })
    .then(data => {
      console.log("Success:", data);
      alert("Leave request submitted successfully");
      loadLeaves();
      document.getElementById("leaveForm").reset();
    })
    .catch(err => console.error("Error:", err));
});

function loadLeaves() {
      fetch("http://localhost:8082/leaves")
    .then(res => res.json())
    .then(data => {
      const tbody = document.getElementById("leaveTableBody");
      tbody.innerHTML = "";
      data.forEach(leave => {
        const row = `<tr>
          <td>${leave.id}</td>
          <td>${leave.employeeName}</td>
          <td>${leave.startDate}</td>
          <td>${leave.endDate}</td>
          <td>${leave.reason}</td>
          <td>${leave.status}</td>
          <td>
            ${leave.status === 'Pending' ? `
              <button onclick="updateStatus(${leave.id}, 'approve')">Approve</button>
              <button onclick="updateStatus(${leave.id}, 'reject')">Reject</button>
            ` : ''}
          </td>
        </tr>`;
        tbody.innerHTML += row;
      });
    })
    .catch(err => console.error("Error fetching leaves:", err));
}

function updateStatus(id, action) {
      fetch(`http://localhost:8082/leaves/${id}/${action}`, {
    method: "PUT"
  })
    .then(() => loadLeaves())
    .catch(error => console.error("Error updating status:", error));
}

window.onload = loadLeaves;
