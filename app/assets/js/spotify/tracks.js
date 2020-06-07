function generatePlaylist(name, length) {
    const route = jsRoutes.controllers.SpotifyController.generatePlaylist();
    const tracks = Array.from(document.getElementsByClassName('spotify-track-row'))
        .map(el => el.id)
        .filter(id => !!id);

    jsonRequest(route, {name, length, tracks},
        err => console.log(err),
        res => console.log(res)
    );
}