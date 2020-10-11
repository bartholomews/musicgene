const selectedMainPlaylists = [];
const selectedSrcPlaylists = [];

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

function getActionPayload(action) {
    switch (action) {
        case 'main':
            return {
                makePayload: playlistUnfollowPayload,
                playlists: selectedMainPlaylists,
                route: jsRoutesControllers.SpotifyController.unfollowPlaylists()
            }

        case 'source':
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
    const {playlists, route} = getActionPayload(btn.dataset.action);
    jsonRequest(route, {user_id: btn.dataset.user, playlists},
        err => console.log(err),
        response => {
            console.log(response)
        }
    );
}