/*! jQuery v1.12.4 | (c) jQuery Foundation | jquery.org/license */

$(document).ready(function(){
$("button").click(function(e) {
    var operation = this.id;
    var signal = $(this).parent().parent().attr('id');
    var source = $( "#sourceSelection option:selected" ).text();
    e.preventDefault();
    $.ajax({
        type: "POST",
        url: "http://localhost:5000/api/v1/control",
        data: {
            "operation": operation,
            "signal": signal,
            "source":source
        },
        success: function(result) {
            console.log(result);
        },
        error: function(result) {
            console.log(result);
        }
    });
});
});