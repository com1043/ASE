package ase.web;

//
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.clientSession.ChannelDataEvent;
import ase.console.LogWriter;
import ase.sensorReadServer.ServerCore;
import ase.util.observer.Observable;
import ase.web.httpServer.HTTPServer;
import ase.web.webSocket.WebChannelEvent;
import ase.web.webSocket.WebSessionManager;
import ase.web.webSocket.WebSocketHandler;

public class WebManager
{
	public static final String PROP_WEBSERVERPORT = "WebServerPort";
	public static final String PROP_WEBSOCKETPORT = "WebSocketPort";
	
	private static final Logger logger = LogWriter.createLogger(WebManager.class, "WebService");
	
	private final int webServerPort;
	private final int webSocketPort;

	private final HTTPServer httpServer;
	public final WebSocketHandler webSocketHandler;
	public final WebSessionManager webSessionManager;

	public WebManager()
	{
		this.webServerPort = Integer.parseInt(ServerCore.getProp(PROP_WEBSERVERPORT));
		this.webSocketPort = Integer.parseInt(ServerCore.getProp(PROP_WEBSOCKETPORT));
		
		this.httpServer = new HTTPServer(this.webServerPort);
		this.webSocketHandler = new WebSocketHandler(this.webSocketPort);
		this.webSessionManager = new WebSessionManager(this.webSocketHandler.channelObservable);
	}

	public static void main(String[] args)
	{
		WebManager webServer = new WebManager();
		webServer.webSocketHandler.addChannelObserver((Observable<WebChannelEvent> o1, WebChannelEvent e1)->{
			System.out.println("채널오픈" + e1.channel.toString());
			e1.channel.addDataReceiveObserver((Observable<ChannelDataEvent> o, ChannelDataEvent e)->{
				System.out.println("채널 데이타 수신" + e1.channel.toString() + " " + e.toString());
			});
		});
		webServer.startModule();
	}

	public boolean startModule()
	{
		logger.log(Level.INFO, "웹 서비스 시작");
		try
		{
			this.webSocketHandler.start(-1);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 시작중 오류", e);
			return false;
		}
		this.httpServer.start();
		this.webSessionManager.start();
		return true;
	}

	public void stopModule()
	{
		this.webSessionManager.stop();
		this.webSocketHandler.stop();
		this.httpServer.stop();
	}

}
