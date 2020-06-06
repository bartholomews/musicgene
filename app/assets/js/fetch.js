// https://developer.mozilla.org/en-US/docs/Web/API/Body
function handleResponse(response) {
    if (!response.ok) throw Error(response.statusText);
    else return response.json();
}

function jsonRequest() {
    const route = jsRoutes.controllers.SpotifyController.generatePlaylist();

    fetch(route.url, {
        method: 'post',
        // https://stackoverflow.com/a/39739894
        redirect: 'follow',
        headers: {
            ...csrfTokenHeader(),
            // 'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name: 'Some Playlist', length: 10})

    })
        .then(handleResponse)
        .then(res => {
            console.log(res)
        })
        .catch(err => console.log(err.message));
}