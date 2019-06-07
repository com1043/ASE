package ase.appServiceO2.serviceInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;

public class SensorListSender extends ServiceInstance
{
	public static final String KEY = "O2SensorListRequest";

	private final SensorManager sensorManager;

	public SensorListSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
	}

	@Override
	protected void onDestroy()
	{
		
	}

	@Override
	protected void onStartService()
	{

	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		String s = event.getStringPayload();
		if(!s.equals("getdata"))
		{
			return;
		}
		JsonObject json = new JsonObject();
		JsonArray dataSensorList = new JsonArray();

		json.add("data", dataSensorList);
		for (Sensor sensor : this.sensorManager.sensorMap.values())
		{
			JsonObject data = new JsonObject();
			data.addProperty("id", sensor.ID);
			dataSensorList.add(data);
		}
		this.channel.sendData(json.toString());
	}
}
