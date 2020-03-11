var net = require('net'); 
var HOST = '192.168.178.50'; 
var PORT = 16445; 
var datestart = Date.now();

net.createServer(function(sock) { 
    console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort); 

    sock.on('data', function(data) { 
        var datenow = Date.now() - datestart;
        console.log(data.toString('utf8') + "(" + datenow + ")");
    });
    
    sock.on('close', function(data) { 
        console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort); 
    }); 

    sock.on('error', function(error) {
        console.log(error);
    });
}).listen(PORT, HOST); 

console.log('Server listening on ' + HOST +':'+ PORT);


