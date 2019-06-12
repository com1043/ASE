package ase.sensorManager.accelSensorDataAnalyser;

import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.AccelDataReceiveEvent;
import ase.sensorManager.sensorDataAccel.SensorAccelData;
import ase.sensorManager.sensorDataAccel.SensorAccelDataManager;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2Data;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.util.observer.Observer;

public class AccelSensorDataAnalyser extends AbsSensorStateManager<SafeStateChangeEvent, SensorDataAnalyser>
{
	public static final Logger logger = LogWriter.createLogger(AccelSensorDataAnalyser.class, "AccelSensorDataAnalyser");
	
	public static final float SAFE_THRESHOLD = 0.20F;
	public static final float WARNING_THRESHOLD = 0.18F;
	
	private final Observer<AccelDataReceiveEvent> o2DataObserver;
	private final SensorConfigAccess configAccess;
	private final SensorAccelDataManager dataManager;
	private final SensorLogManager sensorLogManager;
	
	public AccelSensorDataAnalyser(SensorManager sensorManager, SensorConfigAccess configAccess, SensorAccelDataManager dataManager, SensorLogManager sensorLogManager)
	{
		super(sensorManager);
		this.configAccess = configAccess;
		this.o2DataObserver = this::accelDataObserver;
		this.dataManager = dataManager;
		this.sensorLogManager = sensorLogManager;
	}
	
	private void accelDataObserver(AccelDataReceiveEvent e)
	{
		if(!this.state.containsKey(e.sensorInst)) return;
		this.state.get(e.sensorInst).pushData(e.data);
	}

	@Override
	protected void onStart()
	{
		this.dataManager.addObserver(this.o2DataObserver);
		logger.log(Level.INFO, "센서 가속도 안전상태 분석기 시작");
	}

	@Override
	protected void onStop()
	{
		this.dataManager.removeObserver(this.o2DataObserver);
		logger.log(Level.INFO, "센서 가속도 안전상태 분석기 종료");
	}

	@Override
	protected SensorDataAnalyser onRegisterSensor(Sensor sensor)
	{
		return new SensorDataAnalyser(sensor, this, this.configAccess, this.sensorLogManager);
	}

	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		
	}

}
