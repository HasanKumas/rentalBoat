var tripTable
var numberOfPersons
var tripId
$(document).ready(function () {
    getTrips();

    $("#startTrip").click(openTripDialog);
    $("#stopTrip").click(stopTrip);
    $("#setActualPrice").click(setActualPrice);
    $("#setMinPrice").click(setMinPrice);
})
//list all the trips in a data table
function getTrips() {
    $("#tableShowTrip").dataTable().fnDestroy();
    $("#formInputTrips").hide();
    $("#tableShowTrip").show();

    tripTable = $("#tableShowTrip").DataTable({
        ajax: {
            url: "api/trips",
            dataSrc: function (json) {
                        var return_data = new Array();
                        for (var i = 0; i < json.length; i++) {
                            return_data.push({
                                'id': json[i].id,
                                'boatNumber': json[i].boat.boatNumber,
                                'type': json[i].boat.type,
                                'numberOfPersons': json[i].numberOfPersons,
                                'startDate': json[i].startDate,
                                'startTime': json[i].startTime,
                                'duration': json[i].duration,
                                'price': json[i].price,
                                'status': json[i].status,
                                'deleteBtn': "<button class='btn btn-danger deleteButton' tripId=' " + json[i].id + " ' >delete</button>",
                                'stopBtn': "<button class='btn btn-primary stopBtn' tripId=' " + json[i].id + " '> stop </button>"
                            });
                        }
                        return return_data;
                    }
        },
        columns: [
            { data: "boatNumber" },
            { data: "type" },
            { data: "numberOfPersons" },
            { data: "startDate" },
            { data: "startTime" },
            { data: "duration" },
            { data: "price" },
            { data: "status" },
            { data: "deleteBtn" },
            { data: "stopBtn" }
        ],
        dom: "Bfrtip",
        buttons: [
            {
                text: "Start a trip",
                action: function (e, dt, node, config) {
                    var content = $("#formInputTrips").html();
                    $("#exampleModal .modal-body").html(content);
                    $("#exampleModal .modal-title").text("Start Trip");
                    $("#exampleModal").modal("show");
                    $("#okDelModalBtn").hide();
                    $("#startModalBtn").hide();
                    $("#checkModalBtn").show();
                },
            }
        ]
    });
    //receive input from user (number of persons) and checks suitable boat through called method
    $("#checkModalBtn").click(function () {
        checkSuitableBoat();
        $("#okDelModalBtn").hide();
        $("#startModalBtn").show();
        $("#checkModalBtn").hide();
    });
    //start the trip with the called method inside by setting the current time as starting time
    $("#startModalBtn").click(function () {
        startTrip(numberOfPersons);
        $("#exampleModal").modal("hide");
    });

    $("#tableShowTrip tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            $("#exampleModal").modal("show");
            $("#exampleModal .modal-body").text("Are you sure to delete this trip?");
            $("#exampleModal .modal-title").text("Delete Confirmation!");
            $("#okDelModalBtn").show();
            $("#startModalBtn").hide();
            $("#checkModalBtn").hide();
            tripId = $(this).attr("tripId");
        });
    $("#okDelModalBtn").click(function () {
        deleteTrip(tripId);
        $("#exampleModal").modal("hide");
    });
//
//    $("#tableShowTrip")
//        .off()
//        .on("click", "button.stopBtn", function () {
//            console.log(tripTable.row($(this).parents("tr")));
//            var data1 = tripTable.row($(this).parents("tr")).data();
//
//            $("#exampleModal").modal("show");
//            var content = $("#formInputTrips").html();
//            $("#exampleModal .modal-body").html(content);
//            $("#exampleModal .modal-title").text("Stop Trip");
//            $("#okDelModalBtn").hide();
//            $("#startModalBtn").show();
//            $("#checkModalBtn").hide();//
//
//            tripId = data1.id;
//        });

}
//open dialog box for input to start a trip
function openTripDialog() {
        var content = $("#formInputTrips").html();
        $("#exampleModal .modal-body").html(content);
        $("#exampleModal .modal-title").text("Start Trip");
        $("#exampleModal").modal("show");
        $("#okDelModalBtn").hide();
        $("#startModalBtn").hide();
        $("#checkModalBtn").show();
}
//find the suitable boat number
function checkSuitableBoat() {
    numberOfPersons = Number($('#numberOfPersonsInput').val());

    if (numberOfPersons == 0) {
        $('#error').empty().append("Please enter a bigger number!");
        throw "No number";
    }
   //request the suitable boat number
    $.get('api/boats/suitableBoats?numOfPersons=' +
                                document.getElementById("numberOfPersonsInput").value, function(result){
                                                    document.getElementById('suitableBoatNumberInput').value = result;
                                });


}
//the trip starts and starting time set
function startTrip(numberOfPersons) {

    $.ajax({
        url: 'api/trips?suitableBoatNumber=' + document.getElementById('suitableBoatNumberInput').value +
                        '&numOfPersons=' + numberOfPersons,
        type: "POST",
        contentType: "application/json",
        success: function (message) {
            alert(message);
            tripTable.ajax.reload();
        },
        error: function () {
            alert('try again');
        }
    });
}

function deleteTrip(tripId) {
    $.ajax({
        url: "api/trips/" + tripId,
        type: "DELETE",
        success: function () {
            alert("The trip has been deleted!");
            tripTable.ajax.reload();
        }
    });
}