package ase.web.webSocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

/*
 * WebSocket의 데이터를 정의한 클래스
 * @extends WebSocket
 */
public class WebChannel extends WebSocket implements IChannel
{
	public static final Logger logger = WebSocketHandler.logger;
	private Observable<WebChannelEvent> openCloseWSProvider;
	private Observable<ChannelDataEvent> dataReceiveProvider;
	private String key;
	private WebSession assignSession;
	
	public WebChannel(IHTTPSession session, Observable<WebChannelEvent> channelObservable)
	{
		super(session);
		this.dataReceiveProvider = new Observable<>();
		this.openCloseWSProvider = channelObservable;
		this.key = null;
	}
	
	public void assignSession(WebSession session)
	{
		this.assignSession = session;
	}
	
	public WebSession getAssignSession()
	{
		return this.assignSession;
	}
	
	@Override
	public String getKey()
	{
		return this.key;
	}
	
	@Override
	public void addDataReceiveObserver(Observer<ChannelDataEvent> observer)
	{
		this.dataReceiveProvider.addObserver(observer);
	}
	
	@Override
	public void removeDataReceiveObserver(Observer<ChannelDataEvent> observer)
	{
		this.dataReceiveProvider.removeObserver(observer);
	}
	
	@Override
	protected void onOpen() 
	{
		
	}
	
	@Override
	protected synchronized void onClose(CloseCode code, String reason, boolean initiatedByRemote) 
	{
		if(this.key != null)
		{
			this.dataReceiveProvider.clearObservers();
			WebChannelEvent channelEvent = new WebChannelEvent(this, false);
			this.openCloseWSProvider.notifyObservers(channelEvent);
		}
	}
	
	@Override
	protected synchronized void onMessage(WebSocketFrame frame) 
	{
		if(this.key == null)
		{
			String key = frame.getTextPayload();
			this.key = key;
			WebChannelEvent channelEvent = new WebChannelEvent(this, true);
			this.openCloseWSProvider.notifyObservers(channelEvent);
			return;
		}
		
		ChannelDataEvent channelDataEvent = new ChannelDataEvent(this, frame.getBinaryPayload());
		this.dataReceiveProvider.notifyObservers(channelDataEvent);
	}
	
	@Override
	protected void onPong(WebSocketFrame pong) 
	{

	}
	
	@Override
	protected void onException(IOException e)
	{
		
	}
	
	@Override
	protected void debugFrameReceived(WebSocketFrame frame) 
	{

	}
	
	@Override
	protected void debugFrameSent(WebSocketFrame frame) 
	{

	}
	
	@Override
	public String toString()
	{
		return "WS Channel key:"+this.key+" isOpen:"+this.isOpen()+" hashcode:"+this.hashCode();
	}

	@Override
	public void close()
	{
		try
		{
			super.close(CloseCode.NormalClosure, "normal Close", false);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 종료중 오류", e);
		}
	}

	@Override
	public void sendData(byte[] data)
	{
		try
		{
			this.send(data);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 기록중 오류", e);
		}
		
	}
	
	@Override
	public void sendData(String data)
	{
		try
		{
			this.send(data);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 기록중 오류", e);
		}
	}
}