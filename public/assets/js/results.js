
  
$(document).ready(function () {

    $.ajax({
        type: "GET",
        url: "/api/v1/results",
        success: function (result) {
            console.log(result);
            var data = [
                {
                    x: result.names,
                    y: result.scores,
                    type: 'bar'
                }
                ];
                
            Plotly.newPlot('results-div', data);
        },
        error: function (result) {
            console.log(result);
        }
    });

});
