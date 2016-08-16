/**
 * TODO clear input modals when either 'undo' or 'save' button pressed
 */
$(document).ready(function () {
    playPreviewTable();  // listen for mouse click on track rows
    resetModal(); // listen for a closed modal
    initSliders(1, parseInt(document.getElementById('numberOfTracks').value));
    focusInputOnOpenModal();
});

/**
 * http://stackoverflow.com/a/23571595
 */
function focusInputOnOpenModal() {
    $('.modal').on('shown.bs.modal', function () {
        $(this).find('input:text:visible:first').focus();
    })
}

/**
 * On each input box in a modal,
 * if 'Enter' is pressed the 'Save' button will be triggered.2
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
 * Triggered when a modal is closed: TODO empty text input field
 */
function resetModal() {
    $('body').on('hidden.bs.modal', '.modal', function(){
        $(this).removeData();
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
    var trackSlider = $('#slider-' + name).slider();
    var from = trackSlider.slider('getValue')[0];
    var to = trackSlider.slider('getValue')[1];
    if(name == 'rangeConstraint') {
        if(from == to) {
            alert("Please enter a range with 1 as minimum step");
            return false;
        }
    }
    $('#slider-' + name + '-from').val(trackSlider.slider('getValue')[0]);
    $('#slider-' + name + '-to').val(trackSlider.slider('getValue')[1]);
    if(name == 'valueConstraint') {
        var attrVal = document.getElementById(name + "-input").value;
        // check if value is empty
        if (attrVal == "") {
            alert("Enter a valid value");
            return false;
        } else {
            parseConstraints(name, attrVal);
        }
    } else {
        parseConstraints(name);
    }
    $('#modal-' + name).modal('hide');
    return true;
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
function initSliders(minRange, maxRange) {

    $('#slider-valueConstraint').slider({
        min: minRange, max: maxRange, value: [1, maxRange], focus: true, step: 1,
        start: function (event, ui) {
            event.stopPropagation();
        },
        formatter: function (value) {
            return "range: " + value;
        }
    });
    // TODO should enforce two values NOT to be same: it has to be a range of min step 1
    $('#slider-rangeConstraint').slider({
        min: minRange, max: maxRange, value: [1, maxRange], focus: true, step: 1,
        start: function (event, ui) {
            event.stopPropagation();
        },
        formatter: function (value) {
            return "range: " + value;
        }
    });
}

function changeSlidersMaxValue(maxValue) {
    changeSliderMaxValue('slider-rangeConstraint', maxValue);
    changeSliderMaxValue('slider-valueConstraint', maxValue);
}

function changeSliderMaxValue(name, maxValue) {
  //  $('#' + 'slider-monotonic').slider("remove");
    $('#' + name + "-div").after("<input id="+name + "type='text' style='display: none'/>");
    initSliders(1, maxValue);
    $slider = $('#' + name);
    $slider.slider('refresh');
}
//}
        /*
        $slider = $('#slider-monotonic');
        var value = $slider.data('slider').getValue();
        $slider.data('slider').max = 500;
        $slider.slider('setValue', value);
        $slider.slider('refresh');
        console.log("slider set?");
    });
    */
    /*
    $('#slider-value).html(
    $('#slider).slider('value');
    )
     */

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