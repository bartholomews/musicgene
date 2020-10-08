const selectedPlaylists = [];

function onPlaylistRowClick(playlistRow) {
    const playlistId = playlistRow.getAttribute('id');
    const name = playlistRow.dataset.name;
    const isPublic = playlistRow.dataset.public;
    const collaborative = playlistRow.dataset.collaborative;
    const description = playlistRow.dataset.description;
    console.log(name);
    console.log(isPublic);
    console.log(collaborative);
    console.log(description);
    const tracks = document.getElementsByClassName(`playlist-${playlistId}-track`);
    for (let track of tracks) {
        console.log(track.id);
    }
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

function migrate(e) {
    console.log(e);
    const route = jsRoutesControllers.SpotifyController.migratePlaylists();
    jsonRequest(route, {
                    user_id: 'ME!',
                    playlist_name: 'Some playlist',
                    public: true,
                    collaborative: false,
                    description: 'Optional string description',
                    uris: []
                },
                err => console.log(err),
                playlistResponse => {
                    console.log(playlistResponse)
                }
    );
}