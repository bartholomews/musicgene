/**
 * Clear the div with the constraints paragraphs its original status
 */
function resetConstraints() {
    // create fresh firstLine
    document.getElementById('constraints-firstLine-2').innerHTML = "playlist with no constraints";
    // clean 'input-constraints' div and set 'data-clean' to true
    var constraints = document.getElementById('input-constraints');
    constraints.innerHTML = "";
    constraints.setAttribute('data-clean', 'true');
}

/**
 * Function fired from the 'numberOfTracks' modal when a new number of track selection is submitted
 *
 * @returns {boolean}
 */
function selectNumberOfTracks() {
    var length = document.getElementById('input-length').value;
    if(length < 5 || length > 20) {
        alert("A new playlist can have between 5 and 20 tracks;");
        return false;
    } else {
        var n = document.getElementById('length');
        n.setAttribute('value', length);
        resetConstraints();
        changeSlidersMaxValue(parseInt(length));
        // should check also paragraphs out of range and modify/delete them accordingly
        n.innerHTML = length + " tracks";
        $('#modal-tracks').modal('hide');
        return true;
    }
}

/**
 * build a Constraint String and add append a <p> element to the jumbotron
 * // TODO make just one generified method!
 */
function parseConstraints(name, input) {
    var para;
    switch(name) {
        case "valueConstraint":
            para = parseValueConstraints(input);
            break;
        case "rangeConstraint":
            para = parseRangeConstraints(input);
            break;
    }
    var div = document.getElementById('input-constraints');
    if (isDataClean(div)) {
        document.getElementById("constraints-firstLine-2").innerHTML = "playlist with the following constraints:";
    }
    div.appendChild(para);
}
/**
 * create a new <p> element parsing values to form its attributes
 *
 * @param input
 * @returns {Element}
 */
function parseValueConstraints(input) {
    var selection = document.getElementById("attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;

    var from = $('#slider-valueConstraint-from').val();
    var to = $('#slider-valueConstraint-to').val();
    console.log(from + " to " + to);
    // if from == to Unary else Range(from, to)

    var para = document.createElement('p');
    para.setAttribute("type", "IndexedConstraint");
    para.setAttribute("from-index", (from - 1) + "");
    para.setAttribute("to-index", (to - 1) + "");

    // TODO add here with more constraints
    var constraintName = getRadioElementValue('monotonicValue');
    para.setAttribute("constraint-name", constraintName);
    para.setAttribute("attribute-name", attributeName);
    para.setAttribute("attribute-value", input);

    var trackN;
    if(from == to) trackN = '#' + from;
    else trackN = '#' + from + " to #" + to;
    var text = trackN + " having " + attributeName + " " + getTextFromRadioValue(constraintName) + " " + getUnit(input, attributeName);

    var node = document.createTextNode(text);
    para.appendChild(node);
    return para;
}

/**
 *
 * @param name the name of a Constraint
 * @param input the input value of a Constraint
 */
function parseRangeConstraints(name) {
    var selection = document.getElementById("rangeConstraint-attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;
    var from = $('#slider-rangeConstraint-from').val();
    var to = $('#slider-rangeConstraint-to').val();
    console.log(from + " to " + to);

    var para = document.createElement('p');
    para.setAttribute("type", "IndexedConstraint");
    para.setAttribute("from-index", (from - 1) + "");
    para.setAttribute("to-index", (to - 1) + "");

    var constraintName = getRadioElementValue('rangeConstraint');
    para.setAttribute("constraint-name", constraintName);
    para.setAttribute("attribute-name", attributeName);
    var trackN;
    if(from == to) trackN = '#' + from;
    else trackN = '#' + from + " to #" + to;
    var text = trackN + " having " + getTextFromRadioValue(constraintName) + " " + attributeName;
    console.log("==> " + text);

    var node = document.createTextNode(text);
    para.appendChild(node);
    return para;
}

// NO NEED TO SWITCH, YOU CAN USE THE TEXT PARA IN THIS CASE
function getTextFromRadioValue(text) {
    switch(text) {
        case "IncludeLarger":
            return "no less than";
            break;
        case "IncludeSmaller":
            return "no more than";
            break;
        case "IncludeEquals":
            return "around";
            break;
        case "ConstantTransition":
            return "constant";
            break;
        case "IncreasingTransition":
            return "increasing";
            break;
        case "DecreasingTransition":
            return "decreasing";
            break;
    }
}

function getUnit(input, attribute) {
    switch(attribute) {
        case "Tempo":
            return input + " BPM";
            break;
        case "Loudness":
            return input + " dB";
            break;
        default:
            return parseFloat(input) * 100 + "%";
            break;
    }
}

/**
 * IncludeAll if radio box 'All' is checked,
 * IncludeAny if radio box 'Any' is checked,
 * ExcludeAll if radio box 'None' is checked
 *
 * undefined if nothing is checked
 */
function getRadioElementValue(name) {
    var radios = document.getElementsByName(name);
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) {
            return radios[i].value;
        }
    }
    return false;
}

/**
 * @returns true if no Constraint is present in 'div'
 * (having 'data-clean' attribute set to true) and switch it to false
 */
function isDataClean(div) {
    switch (div.getAttribute('data-clean').toLowerCase()) {
        case "true":
            div.setAttribute('data-clean', 'false');
            return true;
        default:
            return false;
    }
}