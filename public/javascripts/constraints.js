/**
 *
 * @param ms
 */
function convertDuration(ms) {
    var minutes = (ms / (1000*60)) % 60;
    var seconds = (ms / 1000) % 60;
    return minutes + ":" + seconds
}

function selectNumberOfTracks() {
    var numberOfTracks = document.getElementById('input-numberOfTracks').value;
    var n = document.getElementById('numberOfTracks');
    n.setAttribute('value', numberOfTracks);
    changeSlidersMaxValue(parseInt(numberOfTracks));
    // should check also paragraphs out of range and modify/delete them accordingly
    n.innerHTML = numberOfTracks + " tracks";
    $('#modal-tracks').modal('hide');
    return true;
}

/**
 * build a Constraint String and add append a <p> element to the jumbotron
 * // TODO make just one generified method!
 */
function parseConstraints(name, input) {
    var para;
    if(name == 'unary') {
        para = parseUnaryConstraints(input)
    } else {
        para = parseMonotonicConstraints(name);
    }
    // number of tracks => setNumberOfTracks(n) TODO
    setNumberOfTracks(40, para);
}

/**
 * create a new <p> element parsing values to form its attributes
 *
 * @param input
 * @returns {Element}
 */
function parseUnaryConstraints(input) {
    var selection = document.getElementById("attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;

    var from = $('#slider-unary-from').val();
    var to = $('#slider-unary-to').val();
    console.log(from + " to " + to);
    // if from == to Unary else Range(from, to)

    var para = document.createElement('p');
    para.setAttribute("type", "indexed");
    para.setAttribute("from-index", (from - 1) + "");
    para.setAttribute("to-index", (to - 1) + "");

    var constraintName = getRadioElementValue('unary-include');
    para.setAttribute("constraint-name", constraintName);
    para.setAttribute("attribute-name", attributeName);
    para.setAttribute("attribute-value", input);

    var trackN;
    if(from == to) trackN = '#' + from;
    else trackN = '#' + from + " to #" + to;
    var text = trackN + " having " + attributeName + " " + getTextFromRadioValue(constraintName) + " " + input;

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
        case "ConstantRange":
            return "constant";
            break;
        case "IncreasingRange":
            return "increasing";
            break;
        case "DecreasingRange":
            return "decreasing";
            break;
    }
}

/**
 *
 * @param name the name of a Constraint
 * @param input the input value of a Constraint
 */
function parseMonotonicConstraints(name) {
    var selection = document.getElementById("monotonic-attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;
    var from = $('#slider-monotonic-from').val();
    var to = $('#slider-monotonic-to').val();
    console.log(from + " to " + to);

    var para = document.createElement('p');
    para.setAttribute("type", "indexed");
    para.setAttribute("from-index", (from - 1) + "");
    para.setAttribute("to-index", (to - 1) + "");

    var constraintName = getRadioElementValue('monotonic');
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

/**
 * when the first new Constraint is added, the first <p> in jumbotron
 * is changed to be syntactically correct
 *
 * @param numberOfTracks the number of tracks of the new playlist
 * @param newConstraint the <p> element holding the new Constraint String
 */
function setNumberOfTracks(numberOfTracks, newConstraint) {
    var div = document.getElementById('input-constraints');
    // first constraint to append:
    // change 'a [n] playlist with no constraints'
    // into 'a [n] playlist with the following constraints:'
    if (isDataClean(div)) {
        document.getElementById("constraints-firstLine-2").innerHTML = "playlist with the following constraints:";
    }
    div.appendChild(newConstraint);
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
 *
 */
function resetConstraints() {
    // create fresh firstLine
    document.getElementById('constraints-firstLine-2').innerHTML = "playlist with no constraints";
    // clean 'input-constraints' div and set 'data-clean' to true
    var constraints = document.getElementById('input-constraints');
    constraints.innerHTML = "";
    constraints.setAttribute('data-clean', 'true');
}