var tripTable;
var numberOfPersons;
var tripId;

$(document).ready(function () {
    getTrips();
});
/**
 * list all the trips requested from backend
 * in a data table. And allow the user to start a trip,
 * to set minimum price and actual price, to delete a trip,
 * and to stop a trip.
*/
function getTrips() {
    $("#tableShowTrip").dataTable().fnDestroy();
    $("#formInputTrips").hide();
    $("#tableShowTrip").show();

    tripTable = $("#tableShowTrip").DataTable({
        ajax: {
            url: "api/trips",
            dataSrc: function (json) {
                var return_data = new Array();
                //store the data to a list called return_data after manipulation of each
                for (var i = 0; i < json.length; i++) {
                    return_data.push({
                        id: json[i].id,
                        boatNumber: json[i].boat.boatNumber,
                        type: json[i].boat.type,
                        numberOfPersons: json[i].numberOfPersons,
                        name:json[i].guest.name,
                        phoneNumber:json[i].guest.phoneNumber,
                        startDate: json[i].startDate,
                        startTime: json[i].startTime,
                        duration:
                            Math.floor(json[i].duration / 60) +
                            " h " +
                            (json[i].duration % 60) +
                            " m",
                        price: json[i].totalPrice.toFixed(2),
                        status: json[i].status,
                        deleteBtn:
                            "<button onclick='openDeleteConfirmationDialog()' class='btn btn-danger deleteButton' tripId=' " +
                            json[i].id +
                            " ' >delete</button>",
                        stopBtn:
                            "<button onclick='openStopTripConfirmationDialog()' class='btn btn-primary stopBtn' statusCheck=' " +
                            json[i].status +
                            " ' tripId=' " +
                            json[i].id +
                            " '> stop </button>",
                    });
                }
                return return_data;
            }
        },
        columns: [
            { data: "boatNumber" },
            { data: "type" },
            { data: "numberOfPersons" },
            { data: "name" },
            { data: "phoneNumber" },
            { data: "startDate" },
            { data: "startTime" },
            { data: "duration" },
            { data: "price" },
            { data: "status" },
            { data: "deleteBtn" },
            { data: "stopBtn" },
        ],
        dom: "Bfrtip",
        buttons: [
        //created 3 buttons inside data table at the top
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
                },
            },
            {
                text: "Set Minimum Price",
                action: function (e, dt, node, config) {
                    openSetMinPriceDialog();
                },
            },
        ]
    });
}
/**
 * opens a modal dialog box for input to start a trip.
 * Updates the body of modal and adds listeners.
*/
function openStartTripDialog() {
    modalDisplay();
    var content = $("#formInputTrips").html();
    $("#exampleModal .modal-body").html(content);
    $("#exampleModal .modal-title").text("Start Trip");
    $("#checkModalBtn").show();
    //listener for checking suitable boat through called method
    $("#checkModalBtn").one("click", function () {
        checkSuitableBoat();
        $("#startModalBtn").show();
        $("#checkModalBtn").hide();
    });
    //close the above listener to avoid multiple executions
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#checkModalBtn").off();
    });
}
/**
 * Requests a suitable boat number according to number of persons and
 * if a suitable boat number returned ask for the guest info.
 * @author Hasan Kumas
 */
function checkSuitableBoat() {
    //request the suitable boat number
    numberOfPersons = document.getElementById("numberOfPersonsInput").value;
    $.get(
        "api/boats/suitableBoats?numOfPersons=" + numberOfPersons, function (result) {
            if (result == "There is no suitable boat...") {
                $("#exampleModal").modal("hide");
                alert("There is no suitable boat...");
            } else {
                //call guest registration form if there is a suitable boat
                var content = $("#formInputGuests").html();
                $("#exampleModal .modal-body").html(content);
                document.getElementById("suitableBoatNumberInput").value = result;
            }
        }
    );
    //start the trip with the called method inside by setting the current time as starting time
    $("#startModalBtn").one("click", function () {
        startTrip(numberOfPersons);
        $("#exampleModal").modal("hide");
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#startModalBtn").off();
    });
}
/**
 * Sends a post request to save the guest info if succeeded send a post request for
 * a trip  (the trip started by setting the start time as the current time in backend)
 * including reference id to the guest info.
 * @author Hasan Kumas
 */
function startTrip(numberOfPersons) {
    //first save guest
    var guest = {
            name: $("#guestNameInput").val(),
            idType: $("#idTypeInput").val(),
            idNumber: $("#idNumberInput").val(),
            phoneNumber: $("#phoneNumberInput").val()
    }

    var jsonObject = JSON.stringify(guest);

    $.ajax({
        url: "api/guests",
        type: "POST",
        contentType: "application/json",
        data: jsonObject,
        success: function (guestId) {
        //if succeeded include post request for trip in success function
            $.ajax({
                url:
                    "api/trips?suitableBoatNumber=" +
                    document.getElementById("suitableBoatNumberInput").value +
                    "&numOfPersons=" + numberOfPersons +
                    "&guestId=" + guestId,
                type: "POST",
                contentType: "application/json",
                success: function () {
                    alert("The trip has started..");
                    tripTable.ajax.reload();
                },
                error: function () {
                    alert("try again..");
                }
            });
        },
        error: function () {
            alert("try again");
        }
    });
}
/**
 * opens a modal dialog box for confirmation
 * to stop the trip.
 */
function openStopTripConfirmationDialog(){
    //Stop Trip confirmation listener
    $("#tableShowTrip")
        .one("click", "button.stopBtn", function () {
            var s = $(this).attr("statusCheck");
            if (s.trim() === "ended") {
                alert("this trip has already ended..");
                $("#exampleModal").modal("hide");
                $("#stopModalBtn").off();
                $(".cancelModal").off();
            } else {
                modalDisplay();
                $("#exampleModal .modal-body").text("You are about to stop this trip!");
                $("#exampleModal .modal-title").text("Stop Trip");
                $("#stopModalBtn").show();
                tripId = $(this).attr("tripId");
                //stop the trip execution listener
                $("#stopModalBtn").one("click", function () {
                    stopTrip(tripId);
                    $("#exampleModal").modal("hide");
                    $(".cancelModal").off();
                });
                $(".cancelModal").one("click", function () {
                    $("#exampleModal").modal("hide");
                    $("#stopModalBtn").off();
                });
            }
    });
}

/**
 * Sends a put request to stop the trip (the trip stops
 * by setting the end time to current time in the backend).
 * The duration and price are calculated and the information
 * about the trip updated in the frontend (Data Table).
 * @author Hasan Kumas
 */
function stopTrip(tripId) {
    $.ajax({
        url: "api/trips/" + tripId,
        type: "PUT",
        contentType: "application/json",
        success: function (guestDetails) {
            alert("The trip has ended.\n" +
              "Guest name: " + guestDetails.guest.name + "\n" +
              "ID Type: " + guestDetails.guest.idType + "\n" +
              "ID Number: " + guestDetails.guest.idNumber + "\n" +
              "Phone Number: " + guestDetails.guest.phoneNumber);
            tripTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}

/**
 * opens a modal dialog box for
 * receiving input to set actual price.
 */
function openSetActualPriceDialog() {
    modalDisplay();
    var content = $("#formInputActualPrice").html();
    $("#exampleModal .modal-body").html(content);
    $("#exampleModal .modal-title").text("Set Actual Price");
    $("#setActualPriceBtn").show();
     //set actual price execution button and cancel button listeners
    $("#setActualPriceBtn").one("click", function () {
        var actualPrice = $("#actualPriceInput").val();
        $("#exampleModal").modal("hide");
        setActualPrice(actualPrice);
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#setActualPriceBtn").off();
    });
}
/**
 * sets the actual price through
 * sending a put request to backend.
 */
function setActualPrice(actualPrice) {
    $.ajax({
        url: "api/boats/setActualPrice/?actualPrice=" + actualPrice,
        type: "PUT",
        contentType: "application/json",
        success: function () {
            alert("The actual price has been set to " + actualPrice);
            tripTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}
/**
 * opens a modal dialog box for
 * receiving input to set min price
 */
function openSetMinPriceDialog() {
    modalDisplay();
    var content = $("#formInputMinPrice").html();
    $("#exampleModal .modal-body").html(content);
    $("#exampleModal .modal-title").text("Set Minimum Price");
    $("#setMinPriceBtn").show();
     //set min price execution button listener
    $("#setMinPriceBtn").one("click", function () {
        var minPrice = $("#minPriceInput").val();
        $("#exampleModal").modal("hide");
        setMinPrice(minPrice);
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#setMinPriceBtn").off();
    });
}
/**
 * sets minimum price through
 * sending a put request to backend.
 */
function setMinPrice(minPrice) {
    $.ajax({
        url: "api/boats/setMinPrice/?minPrice=" + minPrice,
        type: "PUT",
        contentType: "application/json",
        success: function () {
            alert("The minimum price has been set to " + minPrice);
            tripTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}
/**
 * opens a modal dialog box
 * for delete confirmation.
 */
function openDeleteConfirmationDialog(){
    //delete trip listeners
    $("#tableShowTrip tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            modalDisplay();
            $("#exampleModal .modal-body").text("Are you sure to delete this trip?");
            $("#exampleModal .modal-title").text("Delete Confirmation!");
            $("#okDelModalBtn").show();
            tripId = $(this).attr("tripId");
        });
    //confirmation listener for deletion
    $("#okDelModalBtn").one("click", function () {
        deleteTrip(tripId);
        $("#exampleModal").modal("hide");
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#okDelModalBtn").off();
    });
}
/**
 * deletes the trip by making
 * a delete request to backend.
 */
function deleteTrip(tripId) {
    $.ajax({
        url: "api/trips/" + tripId,
        type: "DELETE",
        success: function () {
            alert("The trip has been deleted!");
            tripTable.ajax.reload();
        },
    });
}
/**
 * display the modal and includes
 * all optional buttons in order
 * to be called in related functions.
 */
function modalDisplay(){
    $("#exampleModal").modal("show");
    $("#okDelModalBtn").hide();
    $("#startModalBtn").hide();
    $("#checkModalBtn").hide();
    $("#stopModalBtn").hide();
    $("#setMinPriceBtn").hide();
    $("#setActualPriceBtn").hide();
}
