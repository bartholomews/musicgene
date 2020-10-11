const selectedPlaylists = [];

function onPlaylistRowClick(playlistRow) {
    const playlist = makePlaylistObject(playlistRow);
    document.getElementById('migrate-playlist-source-select-all').checked = false;
    const isSelected = toggleClass(playlistRow, 'selected');
    isSelected ?
    selectedPlaylists.push(playlist) :
    selectedPlaylists.splice(selectedPlaylists.map(p => p.id).indexOf(playlist.id), 1);
}

function makePlaylistObject(playlistRow) {
    return {
        id: playlistRow.getAttribute('id'),
        name: playlistRow.dataset.name,
        public: playlistRow.dataset.public === 'true',
        collaborative: playlistRow.dataset.collaborative === 'true',
        description: playlistRow.dataset.description
    }
}

function onSelectAllPlaylists(selectAllCheckbox) {
    const onToggledPlaylistRow = row => {
        if (selectAllCheckbox.checked) {
            row.classList.add('selected');
            selectedPlaylists.push(makePlaylistObject(row));
        } else {
            row.classList.remove('selected');
        }
    }

    const rows = document.getElementById("migrate-source-playlist-tbody").rows;
    selectedPlaylists.splice(0, selectedPlaylists.length)
    for (let row of rows) {
        onToggledPlaylistRow(row);
    }
}

function migrate(btn) {
    console.log(btn.dataset.user)
    const route = jsRoutesControllers.SpotifyController.migratePlaylists();
    jsonRequest(route, {user_id: btn.dataset.user, playlists: selectedPlaylists},
                err => console.log(err),
                playlistResponse => {
                    console.log(playlistResponse)
                }
    );
}