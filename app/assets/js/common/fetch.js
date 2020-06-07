// https://developer.mozilla.org/en-US/docs/Web/API/Body
function handleResponse(response) {
    if (!response.ok) throw Error(response.statusText);
    else return response.json();
}

function jsonRequest(route, payload) {
    console.log(payload);
    fetch(route.url, {
        method: 'post',
        headers: {
            ...csrfTokenHeader(),
            // 'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)

    })
        .then(handleResponse)
        .then(res => {
            console.log(res)
        })
        .catch(err => console.log(err.message));
}