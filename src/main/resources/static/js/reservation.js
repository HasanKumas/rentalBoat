var suitableBoatNumbersRes
var reservationTable

$(document).ready(function () {
    getReservations();
});

function getCurrentDate(){
    var d = new Date();
    var datestring = d.getFullYear() + "-" +
                    ("0"+(d.getMonth()+1)).slice(-2) + "-" +
                     ("0" + d.getDate()).slice(-2) + "T" +
                    ("0" + (d.getHours()+1)).slice(-2) + ":" +
                    ("0" + (d.getMinutes()+1)).slice(-2);
//'2017-06-01T08:30'
    return datestring;
}

function getBoatNumbers(boats){
   var column = [];
   for(var i=0; i<boats.length; i++){
      column.push(boats[i].boatNumber);
   }
   return column;
}
/**
 * list all the reservations requested from backend
 * in a data table. And allow the user to make a reservation,
 * and to delete a reservation.
*/
function getReservations() {
    $("#tableShowReservation").dataTable().fnDestroy();
    $("#formReservationInput").hide();
    $("#tableShowReservation").show();

    reservationTable = $("#tableShowReservation").DataTable({
        ajax: {
            url: "api/reservations",
            dataSrc: function (json) {
                var return_data = new Array();
                //store the data to a list called return_data after manipulation of each
                for (var i = 0; i < json.length; i++) {
                    return_data.push({
                        id: json[i].id,
                        startDate: json[i].startDate,
                        startTime: json[i].startTime,
                        endTime: json[i].endTime,
                        name:json[i].guest.name,
                        boatNumber:getBoatNumbers(json[i].boats),
                        reservationPrice: json[i].reservationPrice.toFixed(2),
                        deleteBtn:
                            "<button onclick='openCancelConfirmationDialog()' class='btn btn-danger deleteButton' reservationId=' " +
                            json[i].id +
                            " ' >cancel</button>",
                    });
                }
                return return_data;
            }
        },
        columns: [
            { data: "id" },
            { data: "startDate" },
            { data: "startTime" },
            { data: "endTime" },
            { data: "name" },
            { data: "boatNumber" },
            { data: "reservationPrice" },
            { data: "deleteBtn" }
        ],
        dom: "Bfrtip",
        buttons: [
        //created 3 buttons inside data table at the top
            {
                text: "Make a reservation",
                action: function (e, dt, node, config) {
                    openResDialog();
                },
            }
        ]
    });
}
/**
 * opens a modal dialog box for input to make a reservation.
 * Updates the body of modal and adds listeners.
*/
function openResDialog() {
    modalDisplay();
    var content = $("#formReservationInput").html();
    $("#exampleModal .modal-body").html(content);
    $("#exampleModal .modal-title").text("Make a Reservation");
    var dateControl = document.querySelector('input[type="datetime-local"]');
    dateControl.value = getCurrentDate();
    dateControl.min = getCurrentDate();
    $("#checkModalBtn").show();
    //listener for checking suitable boats through called method
    $("#checkModalBtn").one("click", function () {
        checkSuitableBoats();
        $("#completeModalBtn").show();
        $("#checkModalBtn").hide();
    });
    //close the above listener to avoid multiple executions
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#checkModalBtn").off();
    });
}

/**
 * Requests suitable boat numbers according to
 * number of persons, boat type, start date and time and
 * duration. If  suitable boat numbers returned complete reservation..
 * @author Hasan Kumas
 */
function checkSuitableBoats() {
    //request the suitable boat numbers
    var numberOfPersons = Number(document.getElementById("numOfGuestsResInput").value);
    var boatType = $("#boatTypeResInput").val();
    var reservationDuration = document.getElementById("resDurationInput").value;
    //i love moment...
    var resStartDate = document.getElementById("resDateInput").value;
    var reservationStartDateTime = moment(resStartDate).format('DD-MM-YYYY HH:mm');

    $.get(
        "api/boats/suitableBoatsforReservation/?numOfPersons=" + numberOfPersons +
                                                "&boatType=" + boatType +
                                                "&reservationStartDateTime=" + reservationStartDateTime +
                                                "&duration=" + reservationDuration ,  function (suitableBoats) {
            if (suitableBoats == false) {
                $("#exampleModal").modal("hide");
                $("#completeModalBtn").off();
                $(".cancelModal").off();
                alert("There is no available boats..");
            } else {
                $("#exampleModal .modal-title").text("Complete Reservation");
                document.getElementById("suitBoatsResInput").value = suitableBoats;
                suitableBoatNumbersRes = suitableBoats;
            }
        }
    );
    //completes the reservation with the called method inside
    $("#completeModalBtn").one("click", function () {
        completeReservation(numberOfPersons, reservationStartDateTime, reservationDuration);
        $("#exampleModal").modal("hide");
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#completeModalBtn").off();
    });
}


function completeReservation(numberOfPersons, reservationStartDateTime, reservationDuration){
    var guestName = $("#guestNameResInput").val();
    var phoneNumber = $("#phoneNumberResInput").val();
    if (!(guestName && phoneNumber)) {
        alert('The guest name and phone number should be set..');
        return;
    }
    //first save guest
    var guest = {
            name: guestName,
            phoneNumber: phoneNumber
    }

    var jsonObject = JSON.stringify(guest);

    $.ajax({
        url: "api/guests",
        type: "POST",
        contentType: "application/json",
        data: jsonObject,
        success: function (guestId) {
        //if succeeded include post request for reservation in success function
            $.ajax({
                url:
                    "api/reservations/?suitableBoatNumbers=" + suitableBoatNumbersRes +
                                    "&numOfPersons=" + numberOfPersons +
                                    "&reservationStartDateTime=" + reservationStartDateTime +
                                    "&resDuration=" + reservationDuration +
                                    "&guestId=" + guestId,
                type: "POST",
                contentType: "application/json",
                success: function (message) {
                    alert(message);
                    reservationTable.ajax.reload();
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
 * opens a modal dialog box
 * for cancel confirmation.
 */
function openCancelConfirmationDialog(){
    //cancel reservations listeners
    $("#tableShowReservation tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            modalDisplay();
            $("#exampleModal .modal-body").text("Are you sure to cancel this reservation?");
            $("#exampleModal .modal-title").text("Cancel Confirmation!");
            $("#okDelModalBtn").show();
            reservationId = $(this).attr("reservationId");
        });
    //confirmation listener for cancel
    $("#okDelModalBtn").one("click", function () {
        cancelReservation(reservationId);
        $("#exampleModal").modal("hide");
        $(".cancelModal").off();
    });
    $(".cancelModal").one("click", function () {
        $("#exampleModal").modal("hide");
        $("#okDelModalBtn").off();
    });
}
/**
 * cancels the reservation by making
 * a delete request to backend.
 */
function cancelReservation(reservationId) {
    $.ajax({
        url: "api/reservations/" + reservationId,
        type: "DELETE",
        success: function () {
            alert("The reservation has been cancelled!");
            reservationTable.ajax.reload();
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
    $("#completeModalBtn").hide();
    $("#checkModalBtn").hide();
}