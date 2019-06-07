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

window.onload = function()
{
	console.log("window onload");
	var sensorID = getParameter("key"); //GET 방식으로 붙인 key 값을 가져옴

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
				console.log("확인해야 하는 값이야"+i);
				console.log(sensorData);
				var splitTime = sensorData.time.split("/");
				var xTime = "'" + splitTime[3] + "시" + splitTime[4] + "분" + splitTime[5] + "::" + splitTime[6]+"'"; //
				console.log(xTime);
				console.log(sensorData.value);
				updateValue(sensorID, xTime, sensorData.value);
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
		
		
		
	});
	
}