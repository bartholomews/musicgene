/**
 *
 */
function checkSubmitModal() {
    $('#form-tempo').submit(function(event) {
        console.log("form submitted;")
    });
}

/**
 * Triggered when a modal is closed
 */
function resetModal() {
    $('body').on('hidden.bs.modal', '.modal', function(){
        console.log("Modal hidden");
    });
}

/**
 * When a modal is saved, parse the Constraints in input;
 * alert the user if modal input is empty
 *
 * @param name the name of a modal with id = "[name]-input"
 * @returns {boolean}
 */
function saveModal(name) {
    var attrVal = document.getElementById(name + "-input").value;
    // check if value is empty
    if (attrVal == "") {
        alert("Enter a valid value");
        return false;
    } else {
        parseConstraints(name, attrVal);
        $('#modal-' + name).modal('hide');
        return true;
    }
}

/*
 <!-- Menu Toggle Script -->
 $("#menu-toggle").click(function(e) {
 e.preventDefault();
 $("#wrapper").toggleClass("toggled");
 });
 */