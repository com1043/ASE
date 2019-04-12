package ase.hardware;

import ase.util.observer.Observer;

public class DisplayDeviceState
{
	private static final int GRAPH_WIDTH = 30;
	
	private final Observer<DeviceStateEvent> deviceStateObserver;
	private DisplayObject dispCpu;
	private DisplayObject barCpu;
	private DisplayObject dispMem;
	private DisplayObject barMem;
	
	
	public DisplayDeviceState()
	{
		this.deviceStateObserver = this::deviceStateObserver;
	}
	
	public boolean startModule()
	{
		this.dispCpu = DisplayControl.inst().showString(0, 0, "CPU");
		this.barCpu = DisplayControl.inst().showRect(23, 0, GRAPH_WIDTH, 12);
		this.dispMem = DisplayControl.inst().showString(0, 13, "MEM");
		this.barMem = DisplayControl.inst().showRect(23, 13, GRAPH_WIDTH, 12);
		
		DeviceStateMonitor.inst().addObserver(this.deviceStateObserver);
		return true;
	}
	
	public void stopModule()
	{
		DisplayControl.inst().removeShape(this.dispCpu);
		DisplayControl.inst().removeShape(this.barCpu);
		DisplayControl.inst().removeShape(this.dispMem);
		DisplayControl.inst().removeShape(this.barMem);
		DeviceStateMonitor.inst().removeObserver(this.deviceStateObserver);
	}
	
	public void deviceStateObserver(DeviceStateEvent event)
	{
		System.out.println(String.format("%f, %f", event.cpuLoad, (((double)event.totalMemByte / (double)event.useMemByte)) * 100));
		int cpuPixel = (int) ((event.cpuLoad / 100) * GRAPH_WIDTH);
		int memPixel = (int) (((double)event.totalMemByte / (double)event.useMemByte) * GRAPH_WIDTH);
		this.barCpu = DisplayControl.inst().replaceShape(this.barCpu, this.getBar(GRAPH_WIDTH, 12, cpuPixel));
		this.barMem = DisplayControl.inst().replaceShape(this.barMem, this.getBar(GRAPH_WIDTH, 12, memPixel));
	}
	
	private boolean[][] getBar(int width, int height, int fill)
	{
		boolean[][] result = new boolean[width][height];
		for(int j = 0; j < width; ++j)
		{
			result[j][0] = true;
		}
		for(int j = 0; j < width; ++j)
		{
			result[j][height - 1] = true;
		}
		for(int i = 0; i < height; ++i)
		{
			result[0][i] = true;
			result[width - 1][i] = true;
			if(i <= fill)
			{
				for(int j = 1; j < width - 1; ++j)
				{
					result[j][i] = true;
				}
			}
		}
		
		return result;
	}
	
	
}
