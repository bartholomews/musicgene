
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
    var db = document.querySelectorAll('#music-collection-table tbody tr'), i;
    for(i = 0; i < db.length; i++) {
        var obj = {};
        obj.id = db[i].getAttribute('id');
        js.ids.push(obj);
    }
}

// TODO parse numberOfTracks/Duration
function pushConstraint(js) {
    // var array = document.getElementById("query").getElementsByTagName("*");
    // get all <p> elements in div <input-constraints>
    var constraints = document.getElementById('input-constraints').getElementsByTagName('p');
    for (var i = 0; i < constraints.length; i++) {
        var constraintsArray = getIndexedConstraints(constraints[i]);
        for(var j = 0; j < constraintsArray.length; j++) {
            var obj = {};
            obj.constraint = constraintsArray[j];
            js.constraints.push(obj);
        }
    }
}

function getConstraints(element) {
    switch (element.getAttribute("type")) {
        case "indexed":
            console.log("indexed constraint");
            var c = getIndexedConstraints(element);
            break;
        case "simple":
            console.log("simple constraint");
            c = getUnaryConstraint(element);
            break;
    }
    console.log("name: " + c.name);
    console.log("attr: " + c.attribute);
    return c;
}

function getUnaryConstraint(element) {
    // validate name to be a String,
    // attributes to be an Array of Strings
    var obj = {};
    obj.name = getName(element);
    obj.attribute = getAttribute(element);
    return obj
}

function getIndexedConstraints(element) {
    var from = element.getAttribute("from-index");
    var to = element.getAttribute("to-index");
    var array = [];
    for(var i=from; i<=to; i++) {
        var obj = {};
        obj.name = getName(element);
        obj.attribute = getAttribute(element);
        obj.index = "" + i;
        array.push(obj);
    }
    //console.log("track: " + obj.index);
    return array;
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
        // TODO http://stackoverflow.com/a/450540
        error: function (xhr, ajaxOptions, thrownError) {
            console.log("Error");
            alert("There was a problem generating you playlist. xhr status: " + xhr.status);
            alert(thrownError)
        }
    });
}