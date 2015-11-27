$(document).ready(function () {
    formatDates();
});

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();

        var monthNames = [
            "Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul",
            "Aug", "Sept", "Oct",
            "Nov", "Dec"
        ];

        var date = new Date(parseInt(timeStamp));
        var day = date.getDate();
        var monthIndex = date.getMonth() + 1;
        if (monthIndex < 10)monthIndex = "0" + monthIndex;
        var year = date.getFullYear();

        var hours = date.getHours();
        var amPm = hours < 12 ? "AM" : "PM";
        if (amPm) hours -= 12;
        if (hours == 0)hours = 12;
        //+ ' @' + hours + ':' + date.getMinutes()+amPm
        $(this).html(day + '-' + monthNames[monthIndex - 1] + '-' + year);
    });
}