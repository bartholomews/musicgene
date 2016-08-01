
// TODO clean div after that
function createNewPlaylist() {
    clearNewPlaylist();
    $('#modal-playlistName').modal('hide');
    console.log('Preparing JSON...');
    var nm = document.getElementById("generate-playlist").value;
    var js = {
        name: nm,
        ids: [],
        constraints: []
    };
    pushTrackIds(js);
    pushConstraint(js);
    console.log("Ready to push " + js.toString() + " via ajax");
    sendConstraints(JSON.stringify(js));
}

// TODO multiple tick or something like that
function pushTrackIds(js) {
    // for now get the whole playlists
//    var db = document.getElementsByClassName('music-collection-tbody');
    var tds = document.querySelectorAll('#music-collection-table tbody tr'), i;
    for(i = 0; i < tds.length; i++) {
        var obj = {};
        obj.id = tds[i].getAttribute('id');
        js.ids.push(obj);
    }
}

function pushConstraint(js) {
    var array = document.getElementById("query").getElementsByTagName("*");
    // skip the first <p>
    for (var i = 1; i < array.length; i++) {
        var c = getConstraint(array[i]);
        console.log("parsed " + c);
        var obj = {};
        obj.constraint = c;
        js.constraints.push(obj);
    }
}

function getConstraint(element) {
    // check if it is to get Unary or Indexed or other
    return getUnaryConstraint(element)
}

function getUnaryConstraint(element) {
    // validate name to be a String,
    // attributes to be an Array of Strings
    var obj = {};
    obj.name = getName(element);
    obj.attribute = getAttribute(element);
    return obj
}

function getAttribute(element) {
    var attribute = {};
    attribute.name = element.getAttribute("attribute-name");
    attribute.value = element.getAttribute("attribute-value");
    return attribute;
}

function getName(element) {
    return element.getAttribute("constraint-name")
}

function getIndexedConstraint(element) {
    var obj = {};
    obj.name = getName(element);
    obj.index = element.getAttribute("constraint-index");
    obj.attribute = getAttribute(element);
    return obj;
}

function pushAttribute(js) {
    js.push(attribute);
}

function sendConstraints(obj) {
    $.post({
        url: "/playlist",
        type: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: obj,
        success: function (json) {
            console.log('//playlist POST successful');
            getNewPlaylist(json);
            //alert(json)
        },
        error: function (xhr, ajaxOptions, thrownError) {
            console.log("Error");
            alert("There was a problem generating you playlist. xhr status: " + xhr.status);
            alert(thrownError)
        }
    });
}