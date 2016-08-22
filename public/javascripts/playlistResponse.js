/**
 * reference
 * @see https://github.com/plamere/SmarterPlaylists/blob/master/web/playlist.js
 */
var audio = null;

/**
 * if preview_url == null (e.g. Xtal) catch DOMException: Failed to load because no supported source was found.
 * TODO loop would be nice, with fade even nicer
 */
function playPreviewTable() {
    $(document).on('click', '.playable', function() {
        var url = $(this).attr('data-preview');
        playPreview(url);
    });
}

function playPreviewGraph(i) {
    var url = $('#new-playlist-table').find('> tbody > tr').eq(i).attr("data-preview");
    playPreview(url)
}

function playPreview(url) {
    if (audio == null) {
        audio = $("<audio>");
    }
    if (isPlaying()) {
        // pause the current playing track
        audio.get(0).pause();
        // if the click is on same track, just return
        if (audio.attr('src') == url) return;
    }
    // bind the audio src to the track preview and play
    audio.attr('src', url);
    audio.get(0).play();
}

function isPlaying() {
    return !audio.get(0).paused;
}

/*
 Object { name: "playlist-name", tracks: { id: "id", number: "index" } }
 */
function getNewPlaylist(json) {
    console.log("Ready to save new playlist to DOM");
    //   console.log(json);
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
    console.log("pushing track " + ID);
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

function generateFloatChart(obj) {
    var chart = c3.generate({
        bindto: '#playlist-float-chart',
        data: {
            columns: [
                obj.acousticness,
                obj.energy
             //   obj.liveness
             //   obj.speechiness
            ],
            colors: {
                acousticness: '#8b4513',
                energy: '#9933FF'
             //   liveness: '#000066'
             //   speechiness: '#CCFFFF'
            },
            onclick: function(d) { playPreviewGraph(d.index) }
        },
        axis: {
            x: {
                tick: {
                    culling: false,
                    format: function(x) { return x+1; }
                }
            },
            y: {
            //    label: '0 (min) to 1 (max)',
            //    position: 'outer-middle',
                max: 1.0,
                min: 0.0,
                show: true
            },
            y2: {
                show: false
            }
        },
        tooltip: {
            format: {
                title: function (i) {
                    return obj.name[i]
                },
                value: d3.format(',')
            }
        }
    })
}

// http://c3js.org/samples/tooltip_format.html
function generateChart(obj) {
    var chart = c3.generate({
        bindto: '#playlist-chart',
        data: {
            columns: [
                obj.tempo,
                obj.loudness
            ],
            axes: {
                'tempo': 'y', //bpm
                'loudness': 'y2'   //dB
            },
            onclick: function(d) { playPreviewGraph(d.index) }
        },
        axis: {
            x: {
                tick: {
                    // tick all indexes without skips
                    culling: false,
                    // start from index 1
                    format: function(x) { return x+1; }
                }
            },
            y: {
                label: 'BPM',
                position: 'outer-middle',
                max: 230,
                min: 40,
                show: true,
                tick: {
                    format: d3.format("s")
                }
            },
            y2: {
                label: 'Db',
                position: 'outer-middle',
                max: 0, // extremely loud
                min: -60, // silence
                show: true
            }
        },
        tooltip: {
            format: {
                title: function (i) {
                    return obj.name[i]
                },
                /*
                 value: function (value, ratio, id) {
                 var format = id === 'data1' ? d3.format(',') : d3.format('$');
                 return format(value);
                 }
                 */
                value: d3.format(',')
            }
        }
    });
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
