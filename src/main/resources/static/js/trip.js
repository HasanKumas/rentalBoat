var tripTable
var numberOfPersons
var tripId

$(document).ready(function () {
    getTrips();
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
                                'duration': Math.floor(json[i].duration / 60) + " h " + json[i].duration % 60 + " m",
                                'price': json[i].totalPrice,
                                'status': json[i].status,
                                'deleteBtn': "<button class='btn btn-danger deleteButton' tripId=' " + json[i].id + " ' >delete</button>",
                                'stopBtn': "<button class='btn btn-primary stopBtn' statusCheck=' " +  json[i].status + " ' tripId=' " + json[i].id + " '> stop </button>"
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
                    openStartTripDialog();
                },
            },
            {
                text: "Set Actual Price",
                action: function (e, dt, node, config) {
                    openSetActualPriceDialog();
                }
            },
            {
                text: "Set Minimum Price",
                action: function (e, dt, node, config) {
                    openSetMinPriceDialog();
                }
            }
        ]
    });

    /*Start Trip listeners
    checks suitable boat through called method*/
    $("#checkModalBtn").click(function () {
        checkSuitableBoat();
        $("#okDelModalBtn").hide();
        $("#startModalBtn").show();
        $("#checkModalBtn").hide();
        $("#stopModalBtn").hide();
        $("#setActualPriceBtn").hide();
        $("#setMinPriceBtn").hide();
    });
    //start the trip with the called method inside by setting the current time as starting time
    $("#startModalBtn").click(function () {
        startTrip(numberOfPersons);
        $("#exampleModal").modal("hide");
    });

    //Stop Trip listeners
    $("#tableShowTrip")
        .off()
        .on("click", "button.stopBtn", function () {
        var s= $(this).attr("statusCheck");
        if(s.trim() === "ended"){
            alert("this trip has already ended..")
        }else {
            $("#exampleModal").modal("show");
            $("#exampleModal .modal-body").text("You are about to stop this trip!");
            $("#exampleModal .modal-title").text("Stop Trip");
            $("#okDelModalBtn").hide();
            $("#startModalBtn").hide();
            $("#checkModalBtn").hide();
            $("#stopModalBtn").show();
            $("#setActualPriceBtn").hide();
            $("#setMinPriceBtn").hide();
            tripId = $(this).attr("tripId");
        }
    });

    //stop the trip with the called method inside by setting the current time as starting time
    $("#stopModalBtn").click(function () {
        stopTrip(tripId);
        $("#exampleModal").modal("hide");
    });

    //set actual price
    $("#setActualPriceBtn").click(function () {
        var actualPrice = $("#actualPriceInput").val();
        $("#exampleModal").modal("hide");
        setActualPrice(actualPrice);

    });
    //set min price
    $("#setMinPriceBtn").click(function () {
        var minPrice = $("#minPriceInput").val();
        $("#exampleModal").modal("hide");
        setMinPrice(minPrice);

    });

    //delete trip listeners
    $("#tableShowTrip tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            $("#exampleModal").modal("show");
            $("#exampleModal .modal-body").text("Are you sure to delete this trip?");
            $("#exampleModal .modal-title").text("Delete Confirmation!");
            $("#okDelModalBtn").show();
            $("#startModalBtn").hide();
            $("#checkModalBtn").hide();
            $("#stopModalBtn").hide();
            $("#setActualPriceBtn").hide();
            $("#setMinPriceBtn").hide();
            tripId = $(this).attr("tripId");
        });
     //confirmation for deletion
    $("#okDelModalBtn").click(function () {
        deleteTrip(tripId);
        $("#exampleModal").modal("hide");
    });
}
//open dialog box for input to start a trip
function openStartTripDialog() {
        var content = $("#formInputTrips").html();
        $("#exampleModal .modal-body").html(content);
        $("#exampleModal .modal-title").text("Start Trip");
        $("#exampleModal").modal("show");
        $("#okDelModalBtn").hide();
        $("#startModalBtn").hide();
        $("#checkModalBtn").show();
        $("#stopModalBtn").hide();
        $("#setActualPriceBtn").hide();
        $("#setMinPriceBtn").hide();
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
                                                    if(result == "There is no suitable boat..."){
                                                        $("#exampleModal").modal("hide");
                                                        alert("There is no suitable boat...");
                                                    }
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
//the trip stops and duration and price are calculated
function stopTrip(tripId) {
    $.ajax({
        url: 'api/trips/' + tripId,
        type: "PUT",
        contentType: "application/json",
        success: function () {
            alert("The trip has ended ");
            tripTable.ajax.reload();
        },
        error: function () {
            alert('try again');
        }
    });
}

//open dialog box for input to set actual price
function openSetActualPriceDialog() {
        $("#exampleModal").modal("show");
        var content = $("#formInputActualPrice").html();
        $("#exampleModal .modal-body").html(content);
        $("#exampleModal .modal-title").text("Set Actual Price");
        $("#okDelModalBtn").hide();
        $("#startModalBtn").hide();
        $("#checkModalBtn").hide();
        $("#stopModalBtn").hide();
        $("#setMinPriceBtn").hide();
        $("#setActualPriceBtn").show();
}
//set actual price
function setActualPrice(actualPrice){
    $.ajax({
            url: 'api/boats/setActualPrice/?actualPrice=' + actualPrice,
            type: "PUT",
            contentType: "application/json",
            success: function () {
                alert("The actual price has been set to "+ actualPrice);
                tripTable.ajax.reload();
            },
            error: function () {
                alert('try again');
            }
    });
}
//open dialog box for input to set min price
function openSetMinPriceDialog() {
        $("#exampleModal").modal("show");
        var content = $("#formInputMinPrice").html();
        $("#exampleModal .modal-body").html(content);
        $("#exampleModal .modal-title").text("Set Minimum Price");
        $("#okDelModalBtn").hide();
        $("#startModalBtn").hide();
        $("#checkModalBtn").hide();
        $("#stopModalBtn").hide();
        $("#setActualPriceBtn").hide();
        $("#setMinPriceBtn").show();
}
//set minimum price
function setMinPrice(minPrice){
    $.ajax({
            url: 'api/boats/setMinPrice/?minPrice=' + minPrice,
            type: "PUT",
            contentType: "application/json",
            success: function () {
                alert("The minimum price has been set to "+ minPrice);
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