const CONTROL_GET_UUID_REQUEST = "control_get_uuid";
const COOKIE_KEY_SESSION = "sessionUID";
const CONTROL_CHANNEL_KEY = "control";
export class CommModule
{
	constructor(startCallback)
	{
		console.log("loaded4");
		this.ip = "172.16.1.4";
		this.sessionUUID = this.getCookie(COOKIE_KEY_SESSION);
		this.controlChannel = this.createChannel(CONTROL_CHANNEL_KEY, startCallback);
	}

	httpGet(theUrl, callback, params)
	{
		var xmlHttp = new XMLHttpRequest();
		var paramStr="";
		for(var key in params)
		{
			paramStr+=key+"="+params[key]+"&";
		}
		if(params)
		{
			paramStr = "?"+paramStr.slice(0, -1);
		}
		xmlHttp.onreadystatechange = function()
		{
			if(xmlHttp.readyState == 4 && xmlHttp.status == 200)
				callback(xmlHttp.responseText);
		}
		xmlHttp.open("GET", theUrl+paramStr);
		xmlHttp.send(null);
		return xmlHttp.responseText;
	}

	getCookie(cookieName)
	{
		var search = cookieName + "=";
		var cookie = document.cookie;
		if(cookie.length > 0)
		{
			var startIndex = cookie.indexOf(cookieName);

			if(startIndex != -1)
			{
				startIndex += cookieName.length;
				var endIndex = cookie.indexOf(";", startIndex);
				if(endIndex == -1) endIndex = cookie.length;
				return unescape(cookie.substring(startIndex + 1, endIndex));
			}
		}
		return false;
	}
	
	createChannel(key, onOpen, onMessage, onClose)
	{
		var ws = new WebSocket("ws://" + this.ip + ":8080");
		ws.onopen = () =>
		{
			ws.send(key);
			if(onOpen != null) onOpen();
		};
		ws.onmessage = onMessage;
		ws.onclose = onClose;
		return ws;
	}
	
}

//gg