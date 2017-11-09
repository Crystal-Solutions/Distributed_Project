var urlList = {}
$("#generateButton").click(function () {
    var nodeCount = parseInt($('#nodeCount').val())
    console.log(nodeCount)
    for (let i = 0; i < nodeCount; i++) {
        var port =  (10001+i)
        urlList[i] = "http://localhost:"+port
        const infoUrl = "http://localhost:"+port
        $('#nodeList').append(`<div id="box` + i + `" class="col-md-4">
        <ul class="list-group">
            <li class="list-group-item active"> localhost:` + (port) +`</li>
            <li class="list-group-item"><input id="searchText`+i+`"/> <button onClick="search(`+i+`)">Search</button></li> 
            <li class="list-group-item"><textarea wrap='off' id="results`+i+`" class="form-control" readonly></textarea></li> 
        </ul>
        </div>`)

        $('#results'+i).html("Loading...");

        setInterval(function(){
            console.log(infoUrl)
            $.get(infoUrl+"/info", function( data ) {
                $('#results'+i).html(data);
            })
        },1000)
    }
})

var search = function(index){
    $.post(urlList[index]+"/search/"+$('#searchText'+index).val(), function(data){
        console.log("Response from Search:",data)
    })
    $('#searchText'+index).val("")
}