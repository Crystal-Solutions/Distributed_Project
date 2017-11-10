var urlList = {}

var addNode = function(i,host,port){
    const infoUrl = "http://"+host+":"+port
    urlList[i] = infoUrl
    $('#nodeList').append(`<div id="box` + i + `" class="col-md-4">
        <ul class="list-group">
            <li class="list-group-item active">Node- `+host +`:` + (port) +`</li>
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
    },2000)
}

$("#generateButton").click(function () {
    var nodeCount = parseInt($('#nodeCount').val())
    console.log(nodeCount)
    for (let i = 0; i < nodeCount; i++) {
        var port =  (10001+i)
        addNode(i,'localhost',port);
    }
})



$("#generateCustomButton").click(function () {
    var nodes = $('#customNodes').val().split(" ")
    console.log(nodes)
    let i=0;
    for (n of nodes) {
        const node = n.split(":")
        const port = parseInt(node[1]);
        const host = node[0]
        //var port =  (10001+i)
        addNode(i,host,port);
        i++;
    }
})

var search = function(index){
    $.post(urlList[index]+"/search/"+$('#searchText'+index).val(), function(data){
        console.log("Response from Search:",data)
    })
    $('#searchText'+index).val("")
}