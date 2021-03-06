package ase.sensorReader.tcpReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorReader.DevicePacket;

public class TcpSensorConnect
{	
	public static final Logger logger = LogWriter.createLogger(TcpSensorConnect.class, "tcpSensor");
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean isRun;
	private TcpSensorReadManager sensorReadManager;
	private Thread receiveThread;
	
	public TcpSensorConnect(Socket clientSocket, TcpSensorReadManager tcpSensorReadManager)
	{
		this.socket = clientSocket;
		this.sensorReadManager = tcpSensorReadManager;
		try
		{
			this.inputStream = this.socket.getInputStream();
			this.outputStream = this.socket.getOutputStream();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" IO스트림 가져오는 중 오류 TcpSensorConnect1", e);
			this.close();
			return;
		}
		this.isRun = true;
		this.receiveThread = new Thread(this::receiveData, "sensorReadConnection");
		this.receiveThread.setDaemon(true);
		this.receiveThread.start();
	}
	
	private void receiveData()
	{
		while(true)
		{
			byte[] headerBuffer = this.readData(4+4+4);
			if(headerBuffer == null) return;
			ByteBuffer byteBuffer = ByteBuffer.wrap(headerBuffer);
			byteBuffer.order(BYTE_ORDER);
			
			int id = byteBuffer.getInt();
			int size = byteBuffer.getInt();
			int count = byteBuffer.getInt();
			byte[] rawData = new byte[headerBuffer.length+(size*count)];
			System.arraycopy(headerBuffer, 0, rawData, 0, headerBuffer.length);
			System.out.printf("id%d size%d count%d", id, size, count);
			for(int i = 0; i < count; ++i)
			{
				byte[] sensorDataBuffer = this.readData(size);
				if(sensorDataBuffer == null) return;
				ByteBuffer sensorDataByteBuffer = ByteBuffer.wrap(sensorDataBuffer);
				sensorDataByteBuffer.order(BYTE_ORDER);
				int packetNumber = sensorDataByteBuffer.getInt();
				float xg = sensorDataByteBuffer.getFloat();
				float yg = sensorDataByteBuffer.getFloat();
				float xa = sensorDataByteBuffer.getFloat();
				float ya = sensorDataByteBuffer.getFloat();
				float za = sensorDataByteBuffer.getFloat();
				float al = sensorDataByteBuffer.getFloat();
				float tmp = sensorDataByteBuffer.getFloat();
				
				DevicePacket packet = new DevicePacket(id, xg, yg, xa, ya, za, al, tmp);
				System.out.println("receive: "+packet.toString());
				this.sensorReadManager.notifyObservers(ServerCore.mainThreadPool, packet);
				System.arraycopy(sensorDataBuffer, 0, rawData, headerBuffer.length+(i*size), sensorDataBuffer.length);
			}
			this.sensorReadManager.rawDataObservable.notifyObservers(ServerCore.mainThreadPool, rawData);
		}
		
	}
	
	private byte[] readData(int size)
	{
		byte[] buffer = new byte[size];
		int readSize = -1;
		try
		{
			readSize = this.inputStream.read(buffer);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" 소켓 읽기중 오류 readData1", e);
		}
		if(!this.isRun || readSize == -1)
		{
			return null;
		}
		return buffer;
	}

	public void close()
	{
		try
		{
			this.isRun = false;
			this.socket.close();
			this.sensorReadManager.socketCloseCallback(this);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" 소켓 종료중 오류 close1", e);
			return;
		}
		logger.log(Level.INFO, this.toString()+" 소켓 정상 종료");
	}

}
