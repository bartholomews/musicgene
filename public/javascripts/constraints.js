/**
 * Clear the div with the constraints paragraphs' original status
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
 * return false if a constraint is already appended to the <div>
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
 * append a <p> element (referencing a Constraint) to the jumbotron
 */
function appendConstraint(para) {
    var div = document.getElementById('input-constraints');
    if (isDataClean(div)) { // first constraint to add: change the first line of the <div>
        document.getElementById("constraints-firstLine-2").innerHTML = "playlist with the following constraints:";
    }
    div.appendChild(para);
}

/**
 * create a new <p> element for a Constraint setting constructor values as element attributes
 *
 * @param type 'valueConstraint'/'rangeConstraint'
 * @param input the input value
 * @param from the lower bound of the index range
 * @param to the upper bound of the index range
 */
function parseIndexedConstraint(type, input, from, to) {
    var selection = document.getElementById(type + "-attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;
    // console.log("from " + from + " to " + to);
    var para = document.createElement('li');
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
    var text = trackN;
    switch(type) {
        case "monotonicValue":
            text += " having " + attributeName + " " + getTextFromRadioValue(constraintName) +
                " " + getUnit(input, attributeName);
            break;
        case "monotonicTransition":
            text += " having " + getTextFromRadioValue(constraintName) + " " + attributeName;
            break;
        case "indexedConstraint":
            text += " " + getTextFromRadioValue(constraintName) + " " + attributeName + " '" + input + "'";
            break;
    }
    var node = document.createTextNode(text);
    para.appendChild(node);
    para.appendChild(getRemoveGlyph());
    return para;
}

function getRemoveGlyph() {
    var button = document.createElement('button');
    button.setAttribute('type', 'button');
    button.className = "btn btn-link btn-sm";
    button.addEventListener("click", deleteConstraint, false);
    var span = document.createElement('span');
    span.className = 'fa fa-trash-o';
    button.appendChild(span);
    return button;
}

function deleteConstraint() {
    $("li").click(function() {
        $(this).remove();
        if ($('#input-constraints').find('li').length == 0) {
            resetConstraints();
        }
    });
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
        case "Include":
            return "having";
        case "Exclude":
            return "not having";
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