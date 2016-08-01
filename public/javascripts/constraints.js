/**
 *
 * @param ms
 */
function convertDuration(ms) {
    var minutes = (ms / (1000*60)) % 60;
    var seconds = (ms / 1000) % 60;
    return minutes + ":" + seconds
}

function parseConstraints(name, input) {
    if(name == 'unary') {
        parseUnaryConstraints(input)
    } else {
        parseGlobalConstraints(name, input);
    }
}

function parseUnaryConstraints(input) {
    var selection = document.getElementById("attr-select");
    var attributeName = selection.options[selection.selectedIndex].value;

    var trackNumber = document.getElementById(name + "unary-input-track").value;
    var para = document.createElement('p');
    para.setAttribute("track-number", trackNumber);
    var constraintName = getRadioVal('unary-include');
    para.setAttribute("constraint-name", constraintName);
    para.setAttribute("attribute-name", attributeName);
    para.setAttribute("attribute-value", input);
    var text = "#" + trackNumber + " having " + attributeName + " " + getCName(constraintName) + input;

    var node = document.createTextNode(text);
    para.appendChild(node);
    setNumberOfTracks(20, para);

}

// NO NEED TO SWITCH, YOU CAN USE THE TEXT PARA IN THIS CASE
function getCName(text) {
    switch(text) {
        case "IncludeLarger":
            return "no less than ";
            break;
        case "IncludeSmaller":
            return "no more than ";
            break;
        case "IncludeEquals":
            return "around ";
            break;
    }
}

/**
 * build a Constraint String and add append a <p> element to the jumbotron
 *
 * @param name the name of a Constraint
 * @param input the input value of a Constraint
 */
function parseGlobalConstraints(name, input) {
    // make the first char uppercase to parse it as Class instance
    var attrName = name.charAt(0).toUpperCase() + name.substring(1, name.length);
    // get the value of radio input "[name]-ltgt" ('UnarySmaller', 'UnaryLarger')
    var ltgt = getRadioVal(name + "-ltgt");
    var symbol = getRadioToText(ltgt);
    // get the value of radio input "radio-[name]" ('Any', 'All', 'None')
    var anyAllNone = getRadioVal("radio-" + name);
    var text = getRadioToText(anyAllNone);
    var para = document.createElement("p");
    para.setAttribute("constraint-name", ltgt + anyAllNone);
    para.setAttribute("attribute-name", attrName);
    para.setAttribute("attribute-value", input);
    text += "having " + attrName + symbol + input;
    var node = document.createTextNode(text);
    para.appendChild(node);
    // number of tracks => setNumberOfTracks(n) TODO
    setNumberOfTracks(20, para);
}

/**
 * when the first new Constraint is added, the first <p> in jumbotron
 * is changed to be syntactically correct
 *
 * @param numberOfTracks the number of tracks of the new playlist
 * @param newConstraint the <p> element holding the new Constraint String
 */
function setNumberOfTracks(numberOfTracks, newConstraint) {
    var div = document.getElementById('query');
    if (isDataClean(div)) {
        var firstLine = document.getElementById("constraints-first");
        firstLine.innerHTML = "A " + numberOfTracks + " tracks playlist with the following constraints:";
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
function getRadioVal(name) {
    var result;
    var radios = document.getElementsByName(name);
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) {
            result = radios[i].value;
            break;
        }
    }
    return result;
}

function getRadioToText(name) {
    switch (name) {
        // 'AnyAllNone' radio
        case "Any":
            return "any song ";
            break;
        case  "All":
            return "all songs ";
            break;
        case "None":
            return "no songs ";
            break;
        // 'ltgt' radio
        case "UnaryLarger":
            return " > ";
            break;
        case "UnarySmaller":
            return " < ";
            break;
        case "UnaryEquals":
            return " == ";
            break;
    }
}
