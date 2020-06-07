function playPreviewGraph(i) {
    console.log('TODO: PLAY PREVIEW:');
    console.log(i);
    // var url = $('#new-playlist-table').find('> tbody > tr').eq(i).attr("data-preview");
    // playPreview(url)
}

function revertPlaylistParagraph() {
    document.getElementById('playlist-paragraph').innerHTML =
        "Select restriction on the playlist to be generated. Click on a track to listen to a short preview. " +
        "After sending a request, a new playlist will return shortly, together with the plotting of its audio analysis features.";
}

/*
 Object { name: "playlist-name", tracks: { id: "id", number: "index" } }
 */
function getNewPlaylist(json) {
   revertPlaylistParagraph();
    $('#playlist-title').text(json.name);
    createPlaylistTable(json.ids);
}

function createPlaylistTable(tracks) {
    var collection = document.getElementById('music-collection');
    collection.style.display = 'none';
    var newPlaylist = document.getElementById('new-playlist');
    newPlaylist.style.display = 'inline';
    var newPlaylistTableBody = newPlaylist.getElementsByTagName('tbody')[0];
    var obj = {
        name: [],
        // BPM
        tempo: ['tempo'],
        // dB
        loudness: ['loudness'],
        // acoustic attributes with range 0.0 to 1.0
        acousticness: ['acousticness'],
        danceability: ['danceability'],
        energy: ['energy'],
        liveness: ['liveness'],
        speechiness: ['speechiness']
    };
    // TODO find better performance iteration over whole table
    for(var i = 0; i < tracks.length; i++) {
        var index = i + 1;
        // TODO: what if id == null (that is, the song is NOT on the user collection?)
        // need to create copy, don't override track index of music collection
        var row = document.getElementById(tracks[i]).cloneNode(true);
        // push data into datum to feed the graph
        pushGraphData(obj, tracks[i]);
        // write the track# on first child rows
        row.children[0].innerHTML = "" + index;
        newPlaylistTableBody.appendChild(row);
    }
    generateChart(obj);
    generateFloatChart(obj);
}

function pushGraphData(obj, ID) {
    obj.name.push(($('#'+ID+"-title").text()));
    obj.tempo.push(($('#'+ID+"-tempo").text()));
    obj.loudness.push(($('#'+ID+"-loudness").text()));
    obj.energy.push(($('#'+ID+"-energy").text()));
    obj.acousticness.push(($('#'+ID+"-acousticness").text()));
    obj.danceability.push(($('#'+ID+"-danceability").text()));
    obj.liveness.push(($('#'+ID+"-liveness").text()));
    obj.speechiness.push(($('#'+ID+"-speechiness").text()));

    return obj;
}

function clearNewPlaylist() {
    var newPlaylistBody = document.getElementById('new-playlist')
        .getElementsByTagName('tbody')[0];
    while(newPlaylistBody.firstChild) {
        newPlaylistBody.removeChild(newPlaylistBody.firstChild);
    }
}

/*
 function getConstraint(id) {
 var input = document.getElementById(id);
 if(input == null) return "NO CONSTRAINTS FOR " + id;
 // ...
 else return input.value;
 }
 */
