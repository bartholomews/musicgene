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
 * @returns boolean true if no Constraint is present in 'div'
 * (having 'data-clean' attribute set to true) and switch it to false,
 * return false if data was already 'dirty'
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
    var para = parseIndexedConstraint(name, input);
    /*
    switch(name) {
        case "valueConstraint":
            para = parseValueConstraints(input);
            break;
        case "rangeConstraint":
            para = parseRangeConstraints(input);
            break;
    }
    */
    var div = document.getElementById('input-constraints');
    if (isDataClean(div)) {
        document.getElementById("constraints-firstLine-2").innerHTML = "playlist with the following constraints:";
    }
    div.appendChild(para);
}


/**
 * create a new <p> element for a Constraint setting constructor values as element attributes
 *
 * @param type 'valueConstraint'/'rangeConstraint'
 * @param input the input value
 * @returns a <p> element
 */
function parseIndexedConstraint(type, input) {
    var selection = document.getElementById(type + "-attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;
    var from = $('#slider-' + type + '-from').val();
    var to = $('#slider-' + type + '-to').val();
    var para = document.createElement('p');
    para.setAttribute("type", "IndexedConstraint");
    para.setAttribute("from-index", (from - 1) + "");
    para.setAttribute("to-index", (to - 1) + "");
    var constraintName = getRadioElementValue(type);
    para.setAttribute("constraint-name", constraintName);
    para.setAttribute("attribute-name", attributeName);
    para.setAttribute("attribute-value", input);
    var trackN;
    if(from == to) trackN = '#' + from;
    else trackN = '#' + from + " to #" + to;
    var text = trackN + " having ";
    switch(type) {
        case "valueConstraint":
            text += attributeName + " " + getTextFromRadioValue(constraintName) +
                " " + getUnit(input, attributeName);
            break;
        case "rangeConstraint":
            text += getTextFromRadioValue(constraintName) + " " + attributeName;
            break;
    }
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
            return input; // parseFloat(input) * 100 + "%";
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