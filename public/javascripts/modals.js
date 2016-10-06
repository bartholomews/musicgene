/**
 * TODO clear input modals when either 'undo' or 'save' button pressed
 */
$(document).ready(function () {
    playPreviewTable();  // listen for mouse click on track rows
    resetModal(); // listen for a closed modal
    updateDescriptionListener("monotonicValue");
    updateDescriptionListener("monotonicTransition");
    initSliders(1, parseInt(document.getElementById('length').value));
    focusInputOnOpenModal();
    // make tooltip active
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    })
});

function initSliders(min, max) {
    initSlider('monotonicValue', min, max);
    initSlider('monotonicTransition', min, max);
    initSlider('indexedConstraint', min, max);
}

function changeSlidersMaxValue(maxValue) {
    changeSliderMaxValue('slider-monotonicTransition', maxValue);
    changeSliderMaxValue('slider-monotonicValue', maxValue);
    changeSliderMaxValue('slider-indexedConstraint', maxValue)
}

function changeSliderMaxValue(name, maxValue) {
    //  $('#' + 'slider-monotonic').slider("remove");
    $('#' + name + "-div").after("<input id="+name + "type='text' style='display: none'/>");
    initSliders(1, maxValue);
    $slider = $('#' + name);
    $slider.slider('refresh');
}

/**
 * http://stackoverflow.com/a/23571595
 */
function focusInputOnOpenModal() {
    $('.modal').on('shown.bs.modal', function () {
        $(this).find('input:text:visible:first').focus();
    })
}

function updateDescriptionListener(constraintName) {
    $('#' + constraintName + "-attr-select").change(function () {
        document.getElementById(constraintName + "-description").innerHTML = getAudioDescription($(this).val());
    });
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
 * Triggered when a modal is closed
 */
function resetModal() {
    $('body').on('hidden.bs.modal', '.modal', function(){
        $(this).removeData();
       // console.log("Modal hidden");
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

    if(name == 'monotonicTransition') {
        if(from == to) {
            alert("Please enter a range with 1 as minimum step");
            return false;
        }
    }

    var input = document.getElementById(name + "-input");
    if(input == null) input = 0; // for constraint with irrelevant attribute value wrapped (i.e. no input element)
    else input = input.value;
    if(name == 'monotonicValue' || name == 'indexedConstraint') {
        if (input == "") {
            alert("Enter a valid value");
            return false;
        }
    }
    appendConstraint(parseIndexedConstraint(name, input, from, to));
    $('#modal-' + name).modal('hide');
    return true;
}

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
function initSlider(name, minRange, maxRange) {
    $('#slider-' + name).slider({
        min: minRange, max: maxRange, value: [1, maxRange], focus: true, step: 1,
        start: function (event, ui) {
            event.stopPropagation();
        },
        formatter: function (value) {
            return "range: " + value;
        }
    });
}

function getAudioDescription(attribute) {
    switch(attribute) {
        case "Tempo":
            return "Tempo is the pace of a track. It is derived from the beat duration, " +
                "and calculated in beats per minute (BPM). The values range are about 40 to 240 BPM.";
        case "Loudness":
            return "Loudness defines physical strength (amplitude) and is calculated in decibels (dB), " +
                "with values assigned between -60 dB (silence) and 0 dB (extremely loud).";
        case "Acousticness":
            return "Acousticness is the likelihood a track was recorded only by acoustic means (with values closer to 0.0) " +
                "as opposed to tracks which has been synthesized, amplified or recorded with electronic instruments " +
                "(with values closer to 1.0)";
        case "Liveness":
            return "Liveness detects the presence of an audience in the recording. The more confident that the track is live, " +
                "the closer to 1.0 the attribute value. A value above 0.8 provides strong likelihood that the track is live. " +
                "Tracks with values between 0.6 and 0.8 may or may not be live or contain simulated audience sounds. " +
                "Values below 0.6 most likely represent studio recordings.";
        case "Danceability":
            return "Combining beat strength, tempo stability, overall tempo and regularity " +
                "in order to estimate how likely a track could be perceived as 'danceable' (the more suitable for dancing, " +
                "the closer to 1.0 the value).";
        case "Energy":
            return "Energy represents a perceptual measure of intensity, describing how fast, loud " +
                "and noisy a track is. Perceptual features contributing to this attribute include dynamic range, " +
                "perceived loudness, timbre, onset rate, and general entropy. The closer to 1.0, the more energetic the track is.";
        case "Speechiness":
            return "Speechiness detects the presence of spoken words in a track. The more exclusively speech-like the recording " +
                "(e.g. audio book, stand-up), the closer to 1.0 the attribute value. A track above 0.66 is probably made entirely " +
                "of spoken words, between 0.33 and 0.66 may contain both music and speech (e.g. rap music).";
        case "Valence":
            return "Valence describes the musical positiveness conveyed by a track: a high-valence value (closer to 1.0) defines more positive sounds " +
                "(e.g. happy, cheerful, euphoric). This attribute in combination with energy is a strong indicator of acoustic mood.";
        default:
            return "";
    }
}