/**
 * reference
 * @see https://github.com/plamere/SmarterPlaylists/blob/master/web/playlist.js
 */
let audio = null;

/*
 * if preview_url == null (e.g. Xtal) catch DOMException: Failed to load because no supported source was found.
 * TODO loop would be nice, with fade even nicer
 */

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.playable')
        .forEach(input => input.addEventListener('click', () => {
            const url = input.getAttribute('data-preview');
            return playPreview(url);
        }));
})

//  * TODO clear input modals when either 'undo' or 'save' button pressed
// playPreviewTable();  // listen for mouse click on track rows
// resetModal(); // listen for a closed modal
// updateDescriptionListener("monotonicValue");
// updateDescriptionListener("monotonicTransition");
// initSliders(1, parseInt(document.getElementById('length').value));
// focusInputOnOpenModal();
// // make tooltip active
// $(function () {
//     $('[data-toggle="tooltip"]').tooltip()
// })

function playPreview(url) {
    if (audio == null) audio = $("<audio>");
    if (isPlaying()) {
        // pause the current playing track
        audio.get(0).pause();
        // if the click is on same track, just return
        if (audio.attr('src') === url) return;
    }
    // bind the audio src to the track preview and play
    audio.attr('src', url);
    audio.get(0).play();
}

function isPlaying() {
    return !audio.get(0).paused;
}