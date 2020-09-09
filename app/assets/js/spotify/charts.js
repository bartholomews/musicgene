function generateConfidenceChart(audioTracks) {
    return generateChart(audioTracks, [
        {name: 'acousticness', colour: '#8b4513'},
        {name: 'energy', colour: '#9D7AFF'},
        {name: 'liveness', colour: '#000066', hidden: true},
        {name: 'speechiness', colour: '#7335FA', hidden: true},
        {name: 'danceability', colour: '#115FBC', hidden: true},
    ], {
        bindTo: '#confidence-chart',
        y: {
            max: 1.00,
            min: 0.01,
            show: true
        },
    })
}

function generateBpmDbChart(audioTracks) {
    return generateChart(audioTracks, [
        {name: 'tempo', colour: '#8b4513', axes: 'y'},
        {name: 'loudness', colour: '#9D7AFF', axes: 'y2'},
    ], {
        bindTo: '#bpm-db-chart',
        y: {
            label: 'BPM',
            position: 'outer-middle',
            max: 230,
            min: 40,
            show: true,
            // tick: {
            //     format: d3.format("s")
            // }
        },
        y2: {
            label: 'Db',
            position: 'outer-middle',
            max: 0, // extremely loud
            min: -60, // silence
            show: true
        }
    })
}

function generateChart(audioTracks, attributes, config) {
    const labels = attributes.map(attr => attr.name);
    const audioFeatures = audioTracks.reduce((acc, audioTrack) => {
        return labels
            .map((attribute, index) => {
                const attr = audioTrack[attribute];
                return [...acc[index], attr]
            })
    }, labels.map(attribute => [].concat(attribute)))

    const axes = attributes.reduce((acc, curr) => {
        return curr.axes ?
            Object.assign({[curr.name]: curr.axes}, acc)
            : acc
    }, {});

    const colors = attributes.reduce((acc, curr) => {
        return Object.assign({[curr.name]: curr.colour}, acc)
    }, {});

    const chart = c3.generate({
        bindto: config.bindTo,
        data: {
            columns: audioFeatures,
            colors,
            // smooth plotting
            type: 'spline',
            onclick: data => playPreview(audioTracks[data.index]['previewUrl']),
            axes,
        },
        axis: {
            x: {
                tick: {
                    // tick all indexes without skips
                    culling: false,
                    // start from index 1
                    format: xTick => xTick + 1
                }
            },
            y: config.y,
            y2: config.y2
        },
        tooltip: {
            format: {
                title: (index) => audioTracks[index].title,
                value: d3.format(',')
            }
        }
    });

    attributes.forEach(attribute => {
        if (attribute.hidden) chart.hide([attribute.name]);
    })
}