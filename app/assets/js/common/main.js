const jsRoutesControllers = jsRoutes.io.bartholomews.musicgene.controllers

function redirect(href) {
    location.href = href;
}

function csrfTokenHeader() {
    return {"Csrf-Token": document.body.getAttribute('data-token')};
}