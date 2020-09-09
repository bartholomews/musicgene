function generatePlaylist(name, length) {
    const route = jsRoutes.controllers.SpotifyController.generatePlaylist();
    const constraints = [
        // TODO
        {
            type: 'include_all',
            attribute: {type: 'acousticness', confidence: 0.5}
        }
    ]
    const tracks = Array.from(document.getElementsByClassName('spotify-track-row'))
        .map(el => el.id)
        .filter(id => !!id);

    jsonRequest(route, {name, length, tracks, constraints},
                err => console.log(err),
                playlistResponse => {
                    console.log(playlistResponse)
                    generateConfidenceChart(playlistResponse.songs)
                    generateBpmDbChart(playlistResponse.songs)
                }
    );
}