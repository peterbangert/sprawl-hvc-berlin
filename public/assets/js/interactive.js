/*! jQuery v1.12.4 | (c) jQuery Foundation | jquery.org/license */

$(document).ready(function () {
    $(".cntrl-btn").click(function (e) {
        var operation = this.id;
        var signal = $(this).parent().parent().attr('id');
        var source = $("#sourceSelection").find(".btn-primary").text();
        console.log(operation);
        console.log(signal);
        console.log(source);
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/api/v1/control",
            data: {
                "operation": operation,
                "signal": signal,
                "source": source
            },
            success: function (result) {
                console.log(result);
            },
            error: function (result) {
                console.log(result);
            }
        });
    });
});

$(document).ready(function () {
    $('#submit').click( function(e) {
        var name = $("#exampleFormControlInput1").val();
        if (name === '') {
            alert("Please input name before submitting");
            return;
        }

        console.log(name);
        console.log(randomized);
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/api/v1/submit",
            data: {
                "name": name,
                0: randomized[0],
                1: randomized[1],
                2: randomized[2],
                3: randomized[3],
                4: randomized[4],
                5: randomized[5],
                6: randomized[6],
                7: randomized[7],
                8: randomized[8],
                9: randomized[9],
                10: randomized[10]

            },
            success: function (result) {
                console.log(result);
            },
            error: function (result) {
                console.log(result);
            }
        });
    });
});

$(document).ready(function () {
    $('.source-select').on('click', '.btn-lg', function() {
        $(this).addClass('btn-primary').siblings().removeClass('btn-primary').addClass('btn-default');
    });
});

var list = document.getElementById('list')
var base, randomized, dragging, draggedOver;
var isRight = 'Not In Order!';

const genRandom = (array) => {
    base = array.slice()
    randomized = array.sort(() => Math.random() - 0.5)
    if (randomized.join("") !== base.join("")){
        renderItems(randomized)
    } else {
        //recursion to account if the randomization returns the original array
        genRandom()
    }
}

const renderItems = (data) =>{
    //document.getElementById('isRight').innerText = isRight
    list.innerText = ''
    data.forEach(item=>{
        var node = document.createElement("li");
        node.draggable = true
        node.classList.add("list-group-item");
        node.style.backgroundColor = item
        node.style.backgroundColor = node.style.backgroundColor.length > 0
            ? item : 'lightblue'
        node.addEventListener('drag', setDragging)
        node.addEventListener('dragover', setDraggedOver)
        node.addEventListener('drop', compare)
        node.innerText = item
        list.appendChild(node)
    })
}

const compare = (e) =>{
    var index1 = randomized.indexOf(dragging);
    var index2 = randomized.indexOf(draggedOver);
    randomized.splice(index1, 1)
    randomized.splice(index2, 0, dragging)

    isRight = randomized.join("") === base.join("")
        ? 'In Order!': 'Not In Order!'

    renderItems(randomized)
};


const setDraggedOver = (e) => {
    e.preventDefault();
    draggedOver = Number.isNaN(parseInt(e.target.innerText)) ? e.target.innerText : parseInt(e.target.innerText)
}

const setDragging = (e) =>{
    dragging = Number.isNaN(parseInt(e.target.innerText)) ? e.target.innerText : parseInt(e.target.innerText)
}

// genRandom([0, 1, 2, 3, 4, 5, 6])
genRandom(['Ben', 'Peter', 'CScherz', 'Nils', 'Valentin', 'Simon', 'Laurin','CKastner','Luzie','Roman','Henrik'])