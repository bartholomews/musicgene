/**
 * TODO clear input modals when either 'undo' or 'save' button pressed
 */
$(document).ready(function () {
    playPreview();  // listen for mouse click on track rows
    resetModal(); // listen for a closed modal
    checkSubmitModal();
});

/**
 * When an 'equals' or 'contains' button is clicked, its input text box is enabled
 * and on focus. TODO
 */
function enableInput(inputID) {
    var input = document.getElementById(inputID);
    input.disabled = false;
    input.focus();
}

/**
 * On each input box in a modal,
 * if 'Enter' is pressed the 'Save' button will be triggered.
 * ----------
 * reference:
 * {@link http://stackoverflow.com/a/155265}
 */
function isEnterKeyPressed(e, id) {
    if (e.keyCode == 13) {  // 'Enter' is pressed
        document.getElementById(id).click();
        return false;
    }
    return true;
}

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

/*
 function initSwitches() {
 $("[name='radio-quantity']").bootstrapSwitch();
 }
*/

/**
 * @see http://stackoverflow.com/a/1953093
 */
function checkAttributeFireSlider() {
    $("#attr").change(function () {
        var id = $(this).find("option:selected").attr("id");
        var sliderDiv = document.getElementById('wrapper-attr-slider');
        switch (id) {
            case "attr-year":
                console.log("ATTR_YEAR SELECTED");
                sliderDiv.style.visibility = 'visible';
                $("#attr-slider").slider({});
                break;
            case "attr-duration":
                console.log("ATTR_DURATION SELECTED");
                break;
            case "attr-tempo":
                console.log("ATTR_TEMPO SELECTED");
                break;
        }
    });
}