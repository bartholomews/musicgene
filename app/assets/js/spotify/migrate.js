const selectedPlaylists = [];

function onPlaylistRowClick(playlistRow) {
    const playlistId = playlistRow.getAttribute('id');
    document.getElementById('migrate-playlist-source-select-all').checked = false;
    const isSelected = toggleClass(playlistRow, 'selected');
    isSelected ?
    selectedPlaylists.push(playlistId) :
    selectedPlaylists.splice(selectedPlaylists.indexOf(playlistId), 1);
}

function onSelectAllPlaylists(selectAllCheckbox) {
    const onToggledPlaylistRow = row => {
        if (selectAllCheckbox.checked) {
            row.classList.add('selected');
            selectedPlaylists.push(row.getAttribute('id'));
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

function migrate() {
    const route = jsRoutesControllers.SpotifyController.migratePlaylists();
    jsonRequest(route, selectedPlaylists,
                err => console.log(err),
                playlistResponse => {
                    console.log(playlistResponse)
                }
    );
}