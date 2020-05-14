/**
 *
 */
function createNewPlaylist() {
    clearNewPlaylist();
    $('#modal-playlistName').modal('hide');
    var js = {
        name: document.getElementById("generate-playlist").value,
        length: parseInt(document.getElementById('length').value),
        ids: [],
        constraints: []
    };
    pushTrackIds(js);
    pushConstraint(js);
    playlistRequestDescription();
    sendConstraints(JSON.stringify(js));
}

function playlistRequestDescription() {
    var para = document.getElementById('playlist-paragraph');
    para.innerHTML = "Your playlist is being generated. It might take a few seconds. ";
    var span = document.createElement('span');
    span.className = 'fa fa-2x fa-spin fa-cog';
    para.appendChild(span);
}

/**
 * TODO for now get the whole playlists collection,
 * TODO should implement something like multiple ticks
 *
 * @param js
 */
function pushTrackIds(js) {
    // get all <tr> elements children of <music-collection> table (i.e. playlists)
    var db = document.querySelectorAll('#music-collection-table tbody tr'), i;
    for(i = 0; i < db.length; i++) {
        js.ids.push(db[i].getAttribute('id'));
    }
}

/**
 *
 * @param js
 */
function pushConstraint(js) {
    // var array = document.getElementById("query").getElementsByTagName("*");
    // get all <p> elements in div <input-constraints>
    var constraints = document.getElementById('input-constraints').getElementsByTagName('li');
    for (var i = 0; i < constraints.length; i++) {
        var c = getConstraint(constraints[i]);
        var obj = {};
        obj.constraint = c;
        js.constraints.push(obj);
    }
}

function getConstraint(element) {
    var constraint = {};
    constraint.name = element.getAttribute("constraint-name");
    constraint.from = element.getAttribute("from-index");
    constraint.to = element.getAttribute("to-index");
    constraint.type = element.getAttribute("type");
    constraint.attribute = getAttribute(element);
    return constraint;
}

function getAttribute(element) {
    var attribute = {};
    attribute.name = element.getAttribute("attribute-name");
    attribute.value = element.getAttribute("attribute-value");
    return attribute;
}

/**
 * Send an AJAX Json request to the server at the to the 'playlist' endpoint
 *
 * @param obj
 */
function sendConstraints(obj) {
    $.ajax({
        type: 'POST',
        url: '/playlist',
        contentType: 'application/json',
        dataType: 'json',
        data: obj,
        success: function (json) {
            // playlistResponse.js
            getNewPlaylist(json);
        },
        // TODO http://stackoverflow.com/a/450540
        error: function (xhr, ajaxOptions, thrownError) {
            console.log("Error");
            alert("There was a problem generating you playlist. xhr status: " + xhr.status);
            revertPlaylistParagraph();
        }
    });
}