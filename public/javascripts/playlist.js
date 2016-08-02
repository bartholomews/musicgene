
define(["jquery"], function($) {

    var bus = $(window) ;

    function trigger(eventType) {
        $(bus).trigger(eventType) ;
    }

    function addSong(song) {
        $("#playlist").prepend('<li> Thats: @song </li>')
    }

    return {
        "selectSong": addSong
    } ;

});

require(["jquery", "bootstrap"], function($) {

    $(function () {

        $("#element").carousel();

    });
});