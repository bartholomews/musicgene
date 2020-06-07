console.log('App started.');

function redirect(href) {
    location.href = href;
}

function csrfTokenHeader() {
    return {"Csrf-Token": document.body.getAttribute('data-token')};
}