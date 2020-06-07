function generatePlaylist() {
    const route = jsRoutes.controllers.SpotifyController.generatePlaylist();
    const tracksId = Array.from(document.getElementsByClassName('spotify-track-row'))
        .map(el => el.id)
        .filter(id => !!id);

    jsonRequest(route, {
        name: "Some playlist",
        length: 10,
        tracks: tracksId,
    });
}