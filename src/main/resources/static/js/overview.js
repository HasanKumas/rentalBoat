var totalTime
var totalIncome
$(document).ready(function () {
    $("#tripOverviewBtn").click(showTripOverview);
    $("#boatOverviewBtn").click(showBoatOverview);

});

function showTripOverview() {
    $("#tableShowBoatOverview").hide();
    $("#tableShowTripOverview").show();
    $("#tableTripOverviewBody").empty();

    $.get('api/overviews', function(overview){
             $('#tableTripOverviewBody').append('<tr><td>' + overview.numberOfTripsEnded + '</td>' + '<td>' +
             overview.numberOfTripsOngoing + '</td><td>' + Math.floor(overview.averageDuration/60) + " h " +
             (overview.averageDuration % 60 ).toFixed(2)+ " m" + '</td><td>' +
             overview.numberOfUsedBoats + '</td><td>' + overview.totalIncome.toFixed(2) + '</td></tr>');
    });
}

function showBoatOverview() {
    $("#tableShowBoatOverview").show();
        $("#tableShowTripOverview").hide();
        $("#tableBoatOverviewBody").empty();
        totalTime =0;
        totalIncome=0;
        $.get('api/overviews/boatOverviews', function(boatUsages){
            $.each(boatUsages, function(index, boatUsage) {
                 $('#tableBoatOverviewBody').append('<tr><td>' + boatUsage.boatNumber + '</td>' + '<td>' +
                 boatUsage.numberOfSeats + '</td><td>' + Math.floor(boatUsage.totalTime/60) + " h " + boatUsage.totalTime % 60 + " m" + '</td><td>' +
                  boatUsage.income.toFixed(2) + '</td></tr>');
                  totalTime += boatUsage.totalTime;
                  totalIncome += boatUsage.income;
            });
            $('#tableBoatOverviewBody').append('<tr><td></td>' + '<td>' +
                             "Total" + '</td><td>' + Math.floor(totalTime/60) + " h " + totalTime % 60 + " m" + '</td><td>' +
                              totalIncome.toFixed(2) + '</td></tr>');
    });
}