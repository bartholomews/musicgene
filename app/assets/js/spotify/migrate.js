const selectedMainPlaylists = [];
const selectedSrcPlaylists = [];

function onPlaylistRowClick(playlistRow) {
    const table = getParentTable(playlistRow);
    console.log(table);
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
            return {makePayload: playlistUnfollowPayload, playlists: selectedMainPlaylists, action: 'unfollow'}

        case 'source':
            return {makePayload: playlistMigrationPayload, playlists: selectedSrcPlaylists, action: 'migrate'}
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
    console.log(table);
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
    const {playlists, action} = getActionPayload(btn.dataset.action);
    const user_id = btn.dataset.user;
    console.log(user_id);
    switch (action) {
        case 'unfollow':
            console.log('TODO: unfollow');
            console.log(playlists);
            return 0;
        case 'migrate':
            console.log('TODO: migrate');
            console.log(playlists);
            return 0;
    }
    // const route = jsRoutesControllers.SpotifyController.migratePlaylists();
    // jsonRequest(route, {user_id: btn.dataset.user, playlists: selectedSrcPlaylists},
    //     err => console.log(err),
    //     playlistResponse => {
    //         console.log(playlistResponse)
    //     }
    // );
}