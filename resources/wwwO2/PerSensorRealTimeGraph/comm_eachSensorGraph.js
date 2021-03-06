// 통신모듈
import * as COMM from '/js/CommModule.js';

// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{//최초연결
},
// 연결 끊김
function disconnect(){
    infoDisconnect();
},
// 재접속
function reconnect(){
    infoReconnect();
});

var sendSensorID;

window.onload = function()
{
	initCanvas();
	console.log("window onload");
	var sensorID = getParameter("key"); //GET 방식으로 붙인 key 값을 가져옴
	
	dataSetKey(sensorID);
	
	var PreviouseDataRequestCh = commModule.createChannel("PreviouseO2DataRequest", ()=>	// 센서의 이전 데이터 요청
	{
		PreviouseDataRequestCh.send(sensorID + "/" + String(50)); 
	}, (e) =>
	{
		var data = JSON.parse(e.data);
		console.log(data);
		
		if(data.result = true)
        {		
			for(var i in data.sensorData)
			{
				var sensorData = data.sensorData[i];
				var sensorTime = sensorData.time;
				//console.log(typeof(sensorTime));
				var splitTime = sensorData.time.split("/"); //오류 터짐
				//console.log("xTime:", xTime);
				//console.log(sensorData.value);
				updateValue(sensorID, splitTime[0], splitTime[1] ,splitTime[2], splitTime[3], splitTime[4], splitTime[5], splitTime[6], sensorData.value);
			}
        }
        else
        {
            beforeSensorData.close();
        }
		
	});
	
	var RealTimeDataRequestCh = commModule.createChannel("RealtimeO2ValueRequest",()=>
	{
		RealTimeDataRequestCh.send(sensorID); //request sensor
		
	}, (e)=>
	{
		var data = JSON.parse(e.data);
		console.log(data);
		
		// 진우오빠가 만든 함수에 값 넣어주기
		if(data.result = true)
        {		
				console.log(data.time);
				var splitTime = data.time.split("/");
				
				updateValue(sensorID, splitTime[0], splitTime[1] ,splitTime[2], splitTime[3], splitTime[4], splitTime[5], splitTime[6], data.value);
        }
        else
        {
            beforeSensorData.close();
        }
		
		
	});
	
	
	// 서버로부터 실시간으로 센서의 상태 받기
	var O2SensorRealStatusCh = commModule.createChannel("RealtimeAllO2SensorSafetyRequest", ()=>
	{
		O2SensorRealStatusCh.send("getData");
	}, (e) =>
	{
		
		var data = e.data.split("/");
		if(sensorID == data[0])
			updateButtonState(data[1]);
		
		console.log(data);
		
	});
	
	sendSensorID = commModule.createChannel("SensorSetting");
	
	callFunc(sendNick);
}


function sendNick(id, result)
{
		console.log("inputSendNick");
		console.log(id);
		console.log(result);
		
			var nickJson = 
			{
				"settingKey": "sensorAlias",
				"settingValue" : {
				"sensorID" : id,
				"sensorAlias" : result
				}
			};
			
			var nickStr = JSON.stringify(nickJson);
			console.log("sendNick" + nickStr);
			sendSensorID.send(nickStr);
		
}




