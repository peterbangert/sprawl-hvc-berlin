
  
$(document).ready(function () {
    $.ajax({
        type: "GET",
        url: "/api/v1/results",
        success: function (result) {
            console.log(result);
        },
        error: function (result) {
            console.log(result);
        }
    });

    var data = [
        {
            x: ['giraffes', 'orangutans', 'monkeys'],
            y: [20, 14, 23],
            type: 'bar'
        }
        ];
        
    Plotly.newPlot('results-div', data);
});
