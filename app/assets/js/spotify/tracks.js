
const defaultPlaylistSize = 10;
document.getElementById("playlist-input-size").value = defaultPlaylistSize;

const constraints = [];
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

function onPlaylistConstraintUpdate() {
    document.getElementById('playlist-generation-no-constraints').hidden =
        constraints.length > 0;
}

function onDeleteLi(li) {
    const index = +li.dataset.index;
    constraints.splice(index, 1);
    // update all <li> after the removed one, decrementing the index
    for (let i = index + 1; i <= constraints.length; i++) {
        const li = document.getElementById(`playlist-generation-constraint-${i}`);
        const newIndex = (i - 1).toString();
        li.id = `playlist-generation-constraint-${newIndex}`;
        li.setAttribute('data-index', newIndex);
    }
    document.getElementById(`playlist-generation-constraint-${index}`).remove();
    onPlaylistConstraintUpdate();
}

function constraintDeleteIcon(li) {
    const deleteIcon = document.createElement('i');
    deleteIcon.className = 'fa fa-trash-alt';
    const span = document.createElement('span');
    span.className = 'playlist-generation-constraint-delete';
    span.onclick = () => onDeleteLi(li);
    span.appendChild(deleteIcon);
    return span;
}

function appendPlaylistConstraintListItem(description) {
    const li = document.createElement('li');
    const index = constraints.length - 1;
    li.id = `playlist-generation-constraint-${index}`;
    li.setAttribute('data-index', index.toString());
    li.textContent = description;
    li.appendChild(constraintDeleteIcon(li));
    document.getElementById('playlist-generation-constraints-list').appendChild(li);
    onPlaylistConstraintUpdate();
}

function applyPlaylistConstraints() {
    const constraintsForm = document.getElementById('playlist-constraints-form');
    const formData = new FormData(constraintsForm);

    const constraintType = formData.get('constraint_type');
    const attributeType = formData.get('attribute_type');
    const indexRange = constraintsIndexRangeSlider.getValue();

    const description = `with ${constraintType} ${attributeType} from track ${indexRange[0]} to ${indexRange[1]}`

    const constraint = {
        type: constraintType,
        // convert 1-index slider values to 0-index
        index_range: indexRange.map(value => value - 1),
        attribute: attributeType //{
            // 'type': attributeType,
            // 'value': +(formData.get('attribute_value'))
        // }
    };

    // const jsonPayload =
    //     Object.assign(
    //         {'index_range': constraintsIndexRangeSlider.getValue()},
    //         Object.fromEntries(new FormData(constraintsForm))
    //     );
    constraints.push(constraint);
    appendPlaylistConstraintListItem(description);
}