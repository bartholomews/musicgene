document.getElementById("playlist-input-length").value = 10;
const actionElements = Array.from(document.getElementsByClassName('playlist-generation-action'));
const spinner = createSpinner();

// https://seiyria.com/bootstrap-slider
const constraintsIndexRangeSlider = new Slider("#constraints-index-range",
    {min: 0, max: 10, value: [0, 10], focus: true});

function createSpinner() {
    const spinner = document.createElement('i');
    spinner.id = 'playlist-generation-spinner'
    spinner.className = "fa fa-spinner fa-spin fa-2x";
    return spinner;
}

function onPlaylistGenerationActionClick(loading) {
    actionElements.forEach(element => element.toggleAttribute('disabled', loading));
    loading ?
        document.getElementById('playlist-generation-spinner-wrapper').appendChild(spinner) :
        document.getElementById('playlist-generation-spinner').remove();
}

function generatePlaylist(nameAndLength) {
    onPlaylistGenerationActionClick(true);
    const route = jsRoutesControllers.SpotifyController.generatePlaylist();
    const constraints2 = [
        // TODO
        {
            type: 'include_all',
            attribute: {type: 'tempo', value: 220}
        }
    ]

    const constraints = applyPlaylistConstraints();

    console.log(constraints2);
    console.log(constraints);

    const tracks = Array.from(document.getElementsByClassName('spotify-track-row'))
        .map(el => el.id)
        .filter(id => !!id);

    jsonRequest(route, {...nameAndLength, tracks, constraints},
        err => console.log(err),
        playlistResponse => {
            console.log(playlistResponse)
            generateConfidenceChart(playlistResponse.songs)
            generateBpmDbChart(playlistResponse.songs)
        },
        () => onPlaylistGenerationActionClick(false)
    );
}

/*
constraints: [{
  "type": "increasing",
  "index_range": [1, 2],
  "attribute": {
    "type: "tempo",
    "value": 240.0
  }
}]
 */

function applyPlaylistConstraints() {
    const constraintsForm = document.getElementById('playlist-constraints-form');
    const formData = new FormData(constraintsForm);
    const jsonPayload = {
        'type': formData.get('constraint_type'),
        'index_range': constraintsIndexRangeSlider.getValue(),
        'attribute': {
            'type': formData.get('attribute_type'),
            'value': +(formData.get('attribute_value'))
        }
    };

    // const jsonPayload =
    //     Object.assign(
    //         {'index_range': constraintsIndexRangeSlider.getValue()},
    //         Object.fromEntries(new FormData(constraintsForm))
    //     );

    console.log(jsonPayload);
    return [jsonPayload];
}