function onPlaylistRowClick(element) {
    const playlistId = element.getAttribute('id');
    console.log(`TODO: Migrate playlists: ${playlistId}`);
    document.getElementById('migrate-playlist-source-select-all').checked = false;
    toggleClass(element, 'selected');
}

function onSelectAllPlaylists(element) {
    const updateClassName = row => element.checked
                                   ? row.classList.add('selected')
                                   : row.classList.remove('selected');

    const rows = document.getElementById("migrate-source-playlist-tbody").rows;
    for (let row of rows) {
        updateClassName(row);
    }
}