/**
 * TODO clear input modals when either 'undo' or 'save' button pressed
 */
$(document).ready(function () {
    playPreviewTable();  // listen for mouse click on track rows
    resetModal(); // listen for a closed modal
    //checkSubmitModal();
    initSlider();
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
 * Triggered when a modal is closed: TODO empty text input field
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
    var trackSlider = $('#slider-range').slider();
    $('#slider-from').val(trackSlider.slider('getValue')[0]);
    $('#slider-to').val(trackSlider.slider('getValue')[1]);
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

/**
 * see http://stackoverflow.com/a/13415189
 *
 * @returns {boolean}
 */
function initSlider() {
    $('#slider-range').slider({
        min: 1, max: 20, value: [1, 1], focus: true, step: 1,
        start: function (event, ui) {
            event.stopPropagation();
        },
        formatter: function (value) {
            return "range: " + value;
        }
    });
}

/*
 slide: function (event, ui) {
 console.log("SLIDE");
 $('#slider-from').val(ui.values[0]);
 $('#slider-to').val(ui.values[1]);
 console.log("[0]: " + ui.values[0]);
 console.log('[1]: ' + ui.values[1]);
 },
 change: function (event, ui) {
 console.log("CHANGE");
 console.log("[0]: " + ui.values[0]);
 console.log('[1]: ' + ui.values[1]);
 $('#slider-from').val(ui.values[0]);
 $('#slider-to').val(ui.values[1]);
 }
 */