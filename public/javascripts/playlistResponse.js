/**
 *
 */
function playPreview() {
    $(document).on('click', '.playable', function() {
        window.alert("PLAY!");
    });
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
        tempo: ['tempo'],
        acousticness: ['acousticness'],
        loudness: ['loudness']
    };
    // TODO find better performance iteration over whole table
    for(var i = 0; i < tracks.length; i++) {
        var index = i + 1;
        // TODO: what if id == null (that is, the song is NOT on the user collection?)
        // need to create copy, don't override track index of music collection
        var row = document.getElementById(tracks[i]).cloneNode(true);
        // TODO check if it is really playable first
        row.setAttribute("class", "playable");
        // push data into datum to feed the graph
        pushGraphData(obj, tracks[i]);
        row.children[0].innerHTML = "" + index;
        newPlaylistTableBody.appendChild(row);
    }
    generateChart(obj);
    generateFloatChart(obj);
}

function pushGraphData(obj, ID) {
    obj.name.push(($('#'+ID+"-title").text()));
    obj.tempo.push(($('#'+ID+"-tempo").text()));
    obj.acousticness.push(($('#'+ID+"-acousticness").text()));
    obj.loudness.push(($('#'+ID+"-loudness").text()));
    return obj;
}

function generateFloatChart(obj) {
    var chart = c3.generate({
        bindto: '#playlist-float-chart',
        data: {
            columns: [
                obj.acousticness
            ],
            axes: {
                'acousticness': 'y'
            },
            colors: {
                acousticness: '8b4513'
            }
        },
        axis: {
            y: {
                label: '0 (min) to 1 (max)',
                position: 'outer-middle',
                show: true
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
            }
        },
        axis: {
            y: {
                label: 'BPM',
                position: 'outer-middle',
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

function resetConstraints() {
    // create fresh FirstLine
    var para = document.createElement('p');
    para.setAttribute('id', 'constraints-first');
    var text = "A 20 tracks playlist with no constraints";
    var node = document.createTextNode(text);
    para.appendChild(node);

    var div = document.getElementById('query');
    div.innerHTML = "";
    div.appendChild(para);
    div.setAttribute('class', 'constraints');
    div.setAttribute('id', 'query');
    div.setAttribute('data-clean', 'true');
}
