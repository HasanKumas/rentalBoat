var totalTime
var totalIncome

$(document).ready(function () {
    showTripOverview();
    showBoatOverview();
});
/**
 * display a general overview of
 * the trips by making a get request to backend.
 */
function showTripOverview() {
    $("#tableShowTripOverview").show();
    $("#tableTripOverviewBody").empty();

    $.get('api/overviews/tripOverviews', function(tripOverview){
             $('#tableTripOverviewBody').append('<tr><td>' + tripOverview.numberOfTripsEnded + '</td>' + '<td>' +
             tripOverview.numberOfTripsOngoing + '</td><td>' + Math.floor(tripOverview.averageDuration/60) + " h " +
             (tripOverview.averageDuration % 60 ).toFixed(2)+ " m" + '</td><td>' +
             tripOverview.numberOfUsedBoats + '</td><td>' + tripOverview.totalIncome.toFixed(2) + '</td></tr>');
    });
}
/**
 * display a general overview of
 * the boats by making a get request to backend.
 */
function showBoatOverview() {
    $("#tableShowBoatOverview").show();
    $("#tableBoatOverviewBody").empty();
    totalTime =0;
    totalIncome=0;
    $.get('api/overviews/boatOverviews', function(boatUsages){
        $.each(boatUsages, function(index, boatUsage) {
             $('#tableBoatOverviewBody').append('<tr><td>' + boatUsage.boatNumber + '</td>' + '<td>' +
              boatUsage.type + '</td>' + '<td>' +
              boatUsage.numberOfSeats + '</td><td>' + Math.floor(boatUsage.totalTime/60) + " h " + boatUsage.totalTime % 60 + " m" + '</td><td>' +
              boatUsage.income.toFixed(2) + '</td></tr>');
              //total overviews accumulated inside the loop
              totalTime += boatUsage.totalTime;
              totalIncome += boatUsage.income;
        });
        $('#tableBoatOverviewBody').append('<tr style="background-color:powderblue; color:red;font-size:25px"><td></td><td></td>' +
                                            '<td>' + "Total" + '</td><td>' + Math.floor(totalTime/60) + " h " + totalTime % 60 + " m" +
                                            '</td><td>' + totalIncome.toFixed(2) + '</td></tr>');
    });
}