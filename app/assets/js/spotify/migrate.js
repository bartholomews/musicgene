const actionElements = Array.from(document.getElementsByClassName('playlist-table-action'));
const spinnerDelete = createSpinner('delete');
const spinnerMigrate = createSpinner('migrate');
const selectedMainPlaylists = [];
const selectedSrcPlaylists = [];

function createSpinner(action) {
    const spinner = document.createElement('i');
    spinner.id = `playlist-table-action-spinner-${action}`
    spinner.className = "fa fa-spinner fa-spin";
    return spinner;
}

function getSpinner(action) {
    switch(action) {
        case 'delete': return spinnerDelete;
        case 'migrate': return spinnerMigrate;
    }
}

function onPlaylistRowClick(playlistRow) {
    const table = getParentTable(playlistRow);
    const action = table.dataset.action;
    table.querySelector('thead tr th .form-check .select-all-checkbox').checked = false;

    const {playlists, makePayload} = getActionPayload(action);
    const payload = makePayload(playlistRow);

    const isSelected = toggleClass(playlistRow, 'selected');
    isSelected ?
        playlists.push(payload) :
        playlists.splice(playlists.map(p => p.id).indexOf(payload.id), 1);
}

function onPlaylistTableActionClick(action, loading) {
    actionElements.forEach(element => element.toggleAttribute('disabled', loading));
    const spinner = getSpinner(action);
    loading ?
        document.getElementById(`playlist-table-action-spinner-wrapper-${action}`).appendChild(spinner) :
        window.location.reload(); // document.getElementById(spinner.id).remove();
}

function getActionPayload(action) {
    switch (action) {
        case 'delete':
            return {
                makePayload: playlistUnfollowPayload,
                playlists: selectedMainPlaylists,
                route: jsRoutesControllers.SpotifyController.unfollowPlaylists()
            }

        case 'migrate':
            return {
                makePayload: playlistMigrationPayload,
                playlists: selectedSrcPlaylists,
                route: jsRoutesControllers.SpotifyController.migratePlaylists()
            }
    }
}

function playlistMigrationPayload(playlistRow) {
    return {
        id: playlistRow.getAttribute('id'),
        name: playlistRow.dataset.name,
        public: playlistRow.dataset.public === 'true',
        collaborative: playlistRow.dataset.collaborative === 'true',
        description: playlistRow.dataset.description
    }
}

function playlistUnfollowPayload(playlistRow) {
    return {id: playlistRow.getAttribute('id')};
}

function onSelectAllPlaylists(selectAllCheckbox) {
    const table = getParentTable(selectAllCheckbox);
    const action = table.dataset.action;
    const {playlists, makePayload} = getActionPayload(action);

    const onToggledPlaylistRow = row => {
        if (selectAllCheckbox.checked) {
            row.classList.add('selected');
            playlists.push(makePayload(row));
        } else {
            row.classList.remove('selected');
        }
    }

    flushArray(playlists);
    for (let row of table.querySelector('tbody').rows) {
        onToggledPlaylistRow(row);
    }
}

function onSubmit(btn) {
    const action = btn.dataset.action;
    const {playlists, route} = getActionPayload(action);
    onPlaylistTableActionClick(action, true);
    jsonRequest(route, {user_id: btn.dataset.user, playlists},
        err => console.log(err),
        response => {console.log(response) },
        () => onPlaylistTableActionClick(action, false)
    );
}