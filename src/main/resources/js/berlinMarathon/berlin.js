const runners = [];

$('#resultGrid > tbody').find('tr').each(() => {
    runners.push('pickle');
});
// $('#resultGrid').find('tr').each(() => {
//     alert($(this).html());
//     // let cells = $(this).find('td');
//     // alert(cells.size());
//     // alert($(cells[3]).html());
//     // let runnerData = {
//     //     place: $(cells[2]).text(),
//     //     team: $(cells[6]).text(),
//     //     nationality: $(cells[7]).text(),
//     //     dob: $(cells[8]).text(),
//     //     gender: $(cells[9]).text(),
//     //     finishTime: $(cells[12]).text()
//     // };
//     // runners.push(runnerData)
// });
return runners;