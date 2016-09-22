function generateFloatChart(obj) {
    var chart = c3.generate({
        bindto: '#playlist-float-chart',
        data: {
            columns: [
                obj.acousticness,
                obj.energy,
                obj.liveness,
                obj.speechiness,
                obj.danceability
            ],
            colors: {
                acousticness: '#8b4513',
                energy: '#9933FF',
                liveness: '#000066',
                speechiness: '#7335FA'
            },

            // smooth plotting
            type: 'spline',
            onclick: function (d) {
                playPreviewGraph(d.index)
            }
        },
        axis: {
            x: {
                tick: {
                    culling: false,
                    format: function (x) {
                        return x + 1;
                    }
                }
            },
            y: {
                //    label: '0 (min) to 1 (max)',
                //    position: 'outer-middle',
                max: 1.0,
                min: 0.0,
                padding: {top: 0, bottom: 0},
                show: true
            },
            y2: {
                max: 1.0,
                min: 0.0,
                padding: {top: 0, bottom: 0},
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
    });
    chart.hide(['liveness']);
    chart.hide(['speechiness']);
    chart.hide(['danceability']);
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
            // smooth plotting
            type: 'spline',
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