var guestId
var guestTable

function getGuests() {
    $("#tableShowGuest").dataTable().fnDestroy();
    $("#formInputGuests").hide();
    $("#tableShowGuest").show();

    guestTable = $("#tableShowGuest").DataTable({
        ajax: {
            url: "api/guests",
            dataSrc: function (json) {
                        var return_data = new Array();
                        for (var i = 0; i < json.length; i++) {
                            return_data.push({
                                'id': json[i].id,
                                'guestName': json[i].name,
                                'idType': json[i].idType,
                                'idNumber': json[i].idNumber,
                                'phoneNumber': json[i].phoneNumber,
                                'deleteBtn': "<button class='btn btn-danger deleteButton' guestId=' " + json[i].id + " ' >delete</button>",
                                'editBtn': "<button class='btn btn-primary editBtn' guestId=' " + json[i].id + " '> edit </button>"
                            });
                        }
                        return return_data;
                    }
        },
        columns: [
            { data: "guestName" },
            { data: "idType" },
            { data: "idNumber" },
            { data: "phoneNumber" },
            { data: "deleteBtn" },
            { data: "editBtn" }
        ],
        dom: "Bfrtip",
        buttons: [
            {
                text: "Add a guest",
                action: function (e, dt, node, config) {
                    var content = $("#formInputGuests").html();
                    $("#exampleModal .modal-body").html(content);
                    $("#exampleModal .modal-title").text("Guest Registration Form");
                    $("#exampleModal").modal("show");
                    $("#okDelModalBtn").hide();
                    $("#saveEdtModalBtn").hide();
                    $("#saveCrtModalBtn").show();
                },
            }
        ]
    });

    $("#saveCrtModalBtn").click(function () {
        postGuest();
        $("#exampleModal").modal("hide");
    });

    $("#tableShowGuest tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            $("#exampleModal").modal("show");
            $("#exampleModal .modal-body").text("Are you sure to delete this guest?");
            $("#exampleModal .modal-title").text("Delete Confirmation!");
            $("#okDelModalBtn").show();
            $("#saveEdtModalBtn").hide();
            $("#saveCrtModalBtn").hide();
            guestId = $(this).attr("guestId");
        });
    $("#okDelModalBtn").click(function () {
        deleteGuest(guestId);
        $("#exampleModal").modal("hide");
    });

    $("#tableShowGuest")
        .off()
        .on("click", "button.editBtn", function () {
            var data1 = guestTable.row($(this).parents("tr")).data();

            $("#exampleModal").modal("show");
            var content = $("#formInputGuests").html();
            $("#exampleModal .modal-body").html(content);
            $("#exampleModal .modal-title").text("Guest Modification Form");
            $("#okDelModalBtn").hide();
            $("#saveEdtModalBtn").show();
            $("#saveCrtModalBtn").hide();

            document.getElementById("guestNameInput").value = data1.guestName;
            document.getElementById("idTypeInput").value = data1.idType;
            document.getElementById("idNumberInput").value = data1.idNumber;
            document.getElementById("phoneNumberInput").value = data1.phoneNumber;
            guestId = data1.id;
        });
    $("#saveEdtModalBtn").click(function () {
        changeGuest(guestId);
        $("#exampleModal").modal("hide");
    });
}

function postGuest() {
    var guest = {
        name: $("#guestNameInput").val(),
        idType: $("#idTypeInput").val(),
        idNumber: $("#idNumberInput").val(),
        phoneNumber: $("#phoneNumberInput").val()
    };

    var jsonObject = JSON.stringify(guest);

    $.ajax({
        url: "api/guests",
        type: "POST",
        contentType: "application/json",
        data: jsonObject,
        success: function () {
            alert("The guest has added..");
            $("#guestNameInput").val("");
            $("#idTypeInput").val("");
            $("#idNumberInput").val("");
            $("#phoneNumberInput").val("");
            guestTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}

function deleteGuest(guestId) {
    $.ajax({
        url: "api/guests/" + guestId,
        type: "DELETE",
        success: function () {
            alert("The guest has been deleted!");
            guestTable.ajax.reload();
        },
        error: function () {
            alert("The guest cannot be deleted! Try to delete related trips first!");
        }
    });
}

function changeGuest(guestId) {
    var guest = {
            name: $("#guestNameInput").val(),
            idType: $("#idTypeInput").val(),
            idNumber: $("#idNumberInput").val(),
            phoneNumber: $("#phoneNumberInput").val()
        };
    var jsonObject = JSON.stringify(guest);

    $.ajax({
        url: "api/guests/" + guestId,
        type: "PUT",
        contentType: "application/json",
        data: jsonObject,
        success: function () {
            alert("The guest has been modified!");
            guestTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}
$(document).ready(function () {
    getGuests();
});
