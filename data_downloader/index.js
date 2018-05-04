//Downloads certain blockchain data of the given time period and saves it as the given file (.json)
var https = require("https");
var fs = require('fs');
var util = require('util')

var datadir = '../runner/signals/'

function JSONRequest(request, callback){
	https.get(request, function (res) {
		console.log('Status code from exchange:', res.statusCode);

		var responseString = "";

		res.on('data', function (chunk) {
			responseString += chunk;
		});

		res.on('end', function () {

			console.log("Received " + responseString.length + " bytes");

			callback(JSON.parse(responseString));
		});

	}).on('error', function (e) {
		console.error(e);
		callback(undefined);
	});
}

var courses = {
	cryptocompare: function (start, end, callback) {
		var data = [];

		const firstTimestamp = 1438959600; //this timestamp is the first data that this source has

		var sendRequest = function(toTimestamp) {
			JSONRequest('https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD&limit=2000&e=CCCAGG&toTs=' + toTimestamp, function(part){
				var partData = part.Data.reverse().filter(tick => tick.time >= firstTimestamp) // remove any invalid data that is before the first timestamps
				data.push.apply(data, partData);

				console.log(util.format("Received data chunk that starts at %d and ends at %d.", part.TimeTo, part.TimeFrom));

				if(part.TimeFrom > start && part.TimeFrom > firstTimestamp) {
					sendRequest(part.TimeFrom - 1); // request the next batch
				} else {

					for(var i = 0; i < data.length-1; i++){ // validate that we have all data in the correct hourly intervals
						if (data[i].time - 3600 != data[i+1].time) {
							console.log(util.format("Mismatch between dates %d and %d with difference %d.", data[i].time, data[i+1].time, data[i+1].time - data[i].time));
						}
					}

					data.reverse();

					callback(data);
				}
			})
		}

		sendRequest(end)
	},
	poloniex: function (start, end, callback) {
		JSONRequest("https://poloniex.com/public?command=returnChartData&currencyPair=USDT_ETH&start="+start+"&end="+end+"&period=300", function (data) {
			callback(data);
		});
	}
}

function downloadCourse(start, end, source, callback) {

	courses[source](start, end, function(data) {
		if (data != undefined){
			var filename = datadir + source + "_price_data.json";
			console.log(data.length, data[0], data[data.length-1]);

			fs.writeFile(filename, JSON.stringify(data), function(err) {
				if(err) {
					console.error(err);
					return;
				}

				console.log("The data was saved as " + filename);
			});
		} else {
			console.error("Error while downloading course!");
		}
	});
}

function downloadWholeCourse(){
	downloadCourse(1, 999999999999999, "cryptocompare");
}



// function printHelp(){
// 	console.log("A tool to download cryptocurrency course and blockchain data and save them as a json");
// 	console.log("Arguments:");
// 	console.log("course - downloads the whole history of the token course from an exchange");
// }
//
// function processArgs(){
// 	if(process.argv.length == 2) printHelp();
// 	else{
// 		for(argIndex = 2; argIndex<process.argv.length; argIndex++){
// 			arg = process.argv[argIndex];
//
// 			if(arg.search('help') >=0) printHelp();
// 			else if(arg == 'course') downloadWholeCourse();
// 		}
// 	}
// }

downloadWholeCourse();
