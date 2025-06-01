const button = document.querySelector("#authBtn")
const modal = document.querySelector("dialog")
const buttonClose = document.querySelector("#close-btn")

button.onclick = function() {
  modal.showModal()
  console.log(modal)
}

buttonClose.onclick = function() {
    modal.close()
}
