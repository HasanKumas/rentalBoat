var boatId
var boatTable

function getBoats() {
    $("#tableShowBoat").dataTable().fnDestroy();
    $("#formInputBoats").hide();
    $("#tableShowBoat").show();

    boatTable = $("#tableShowBoat").DataTable({
        ajax: {
            url: "api/boats",
            dataSrc: function (json) {
                        var return_data = new Array();
                        for (var i = 0; i < json.length; i++) {
                            return_data.push({
                                'id': json[i].id,
                                'boatNumber': json[i].boatNumber,
                                'type': json[i].type,
                                'numberOfSeats': json[i].numberOfSeats,
                                'minPrice': json[i].minPrice,
                                'actualPrice': json[i].actualPrice,
                                'deleteBtn': "<button class='btn btn-danger deleteButton' boatId=' " + json[i].id + " ' >delete</button>",
                                'editBtn': "<button class='btn btn-primary editBtn' boatId=' " + json[i].id + " '> edit </button>"
                            });
                        }
                        return return_data;
                    }
        },
        columns: [
            { data: "boatNumber" },
            { data: "type" },
            { data: "numberOfSeats" },
            { data: "minPrice" },
            { data: "actualPrice" },
            { data: "deleteBtn" },
            { data: "editBtn" }
        ],
        dom: "Bfrtip",
        buttons: [
            {
                text: "Add a boat",
                action: function (e, dt, node, config) {
                    var content = $("#formInputBoats").html();
                    $("#exampleModal .modal-body").html(content);
                    $("#exampleModal .modal-title").text("Boat Registration Form");
                    $("#exampleModal").modal("show");
                    $("#okDelModalBtn").hide();
                    $("#saveEdtModalBtn").hide();
                    $("#saveCrtModalBtn").show();
                },
            }
        ]
    });

    $("#saveCrtModalBtn").click(function () {
        postBoat();
        $("#exampleModal").modal("hide");
    });

    $("#tableShowBoat tbody")
        .off()
        .on("click", "button.deleteButton", function () {
            $("#exampleModal").modal("show");
            $("#exampleModal .modal-body").text("Are you sure to delete this boat?");
            $("#exampleModal .modal-title").text("Delete Confirmation!");
            $("#okDelModalBtn").show();
            $("#saveEdtModalBtn").hide();
            $("#saveCrtModalBtn").hide();
            boatId = $(this).attr("boatId");
        });
    $("#okDelModalBtn").click(function () {
        console.log(boatId);
        deleteBoat(boatId);
        $("#exampleModal").modal("hide");
    });

    $("#tableShowBoat")
        .off()
        .on("click", "button.editBtn", function () {
            console.log(boatTable.row($(this).parents("tr")));
            var data1 = boatTable.row($(this).parents("tr")).data();

            $("#exampleModal").modal("show");
            var content = $("#formInputBoats").html();
            $("#exampleModal .modal-body").html(content);
            $("#exampleModal .modal-title").text("Boat Modification Form");
            $("#okDelModalBtn").hide();
            $("#saveEdtModalBtn").show();
            $("#saveCrtModalBtn").hide();

            document.getElementById("boatNumberInput").value = data1.boatNumber;
            document.getElementById("boatTypeInput").value = data1.type;
            document.getElementById("maxSeatsInput").value = data1.numberOfSeats;
            document.getElementById("boatMinPriceInput").value = data1.minPrice;
            document.getElementById("boatActPriceInput").value = data1.actualPrice;
            boatId = data1.id;
        });
    $("#saveEdtModalBtn").click(function () {
        changeBoat(boatId);
        $("#exampleModal").modal("hide");
    });
}

function postBoat() {
    var boat = {
        boatNumber: $("#boatNumberInput").val(),
        type: $("#boatTypeInput").val(),
        numberOfSeats: Number($("#maxSeatsInput").val()),
        minPrice: Number($("#boatMinPriceInput").val()),
        actualPrice: Number($("#boatActPriceInput").val()),
    };

    var jsonObject = JSON.stringify(boat);

    $.ajax({
        url: "api/boats",
        type: "POST",
        contentType: "application/json",
        data: jsonObject,
        success: function (message) {
            alert(message);
            $("#boatNumberInput").val("");
            $("#boatTypeInput").val("");
            $("#maxSeatsInput").val("");
            $("#boatMinPriceInput").val("");
            $("#boatActPriceInput").val("");
            boatTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}

function deleteBoat(boatId) {
    $.ajax({
        url: "api/boats/" + boatId,
        type: "DELETE",
        success: function () {
            alert("The boat has been deleted!");
            boatTable.ajax.reload();
        }
    });
}

function changeBoat(boatId) {
    var boat = {
            boatNumber: $("#boatNumberInput").val(),
            type: $("#boatTypeInput").val(),
            numberOfSeats: Number($("#maxSeatsInput").val()),
            minPrice: Number($("#boatMinPriceInput").val()),
            actualPrice: Number($("#boatActPriceInput").val())
        };
    var jsonObject = JSON.stringify(boat);

    $.ajax({
        url: "api/boats/" + boatId,
        type: "PUT",
        contentType: "application/json",
        data: jsonObject,
        success: function () {
            alert("The boat has been modified!");
            boatTable.ajax.reload();
        },
        error: function () {
            alert("try again");
        },
    });
}
$(document).ready(function () {
    getBoats();
});
