// https://developer.mozilla.org/en-US/docs/Web/API/Body
function handleResponse(response) {
    if (!response.ok) throw Error(response.statusText);
    else return response.json();
}

function jsonRequest(route, payload, onError, onSuccess) {
    console.log("~~~~~~~~~~ Json request ~~~~~~~~~~")
    console.log(payload);
    console.log("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
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
        .then(onSuccess)
        .catch(onError)
}