
const defaultPlaylistSize = 10;
document.getElementById("playlist-input-size").value = defaultPlaylistSize;

const actionElements = Array.from(document.getElementsByClassName('playlist-generation-action'));
const spinner = createSpinner();

// https://seiyria.com/bootstrap-slider
const constraintsIndexRangeSlider = new Slider("#constraints-index-range",
    {min: 1, max: defaultPlaylistSize, value: [1, defaultPlaylistSize], focus: true});

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

function getPlaylistSize() {
    return +document.getElementById('playlist-input-size').value;
}

// FIXME: Playlist size MUST be also <= songs size, otherwise the gen will throw an IndexOutOfBounds at the moment
function isPlaylistSizeValid(size) {
    return size <= 50 && size >= 5;
}

function onPlaylistSizeChange() {
    const newPlaylistSize = getPlaylistSize();
    document.getElementById('playlist-generation-add-constraints-button')
        .toggleAttribute('disabled', !isPlaylistSizeValid(newPlaylistSize));

    constraintsIndexRangeSlider.setAttribute('max', newPlaylistSize);
    constraintsIndexRangeSlider.setValue([0, newPlaylistSize]);
}


function generatePlaylist() {
    onPlaylistGenerationActionClick(true);
    const route = jsRoutesControllers.SpotifyController.generatePlaylist();
    // const constraints2 = [
    //     // TODO
    //     {
    //         type: 'include_all',
    //         attribute: {type: 'tempo', value: 220}
    //     }
    // ]

    const size = getPlaylistSize();
    const constraints = applyPlaylistConstraints();

    const tracks = Array.from(document.getElementsByClassName('spotify-track-row'))
        .map(el => el.id)
        .filter(id => !!id)

    jsonRequest(route, { name: 'My Playlist', size, tracks, constraints},
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
    const constraint = {
        'type': formData.get('constraint_type'),
        // convert 1-index slider values to 0-index
        'index_range': constraintsIndexRangeSlider.getValue().map(value => value - 1),
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

    console.log('TODO: save this constraint to a "li" which display english and have this json data attributes:')
    return [constraint];
}