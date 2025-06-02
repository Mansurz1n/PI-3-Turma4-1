const button = document.querySelector("#authBtn")
const modal = document.querySelector("dialog")
const buttonClose = document.querySelector("#close-btn")

button.onclick = function() {
  modal.showModal()
  console.log(modal)
}

buttonClose.onclick = function() {
  stopStatusPolling()
    modal.close()
}

// Function to stop the polling
function stopStatusPolling() {
  if (interval) {
    clearInterval(interval);
    interval = null; // Reset the interval variable
    console.log("Polling stopped.");
  }
}
