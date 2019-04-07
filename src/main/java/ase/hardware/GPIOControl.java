package ase.hardware;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.console.LogWriter;

public class GPIOControl
{
	public static final Logger logger = LogWriter.createLogger(GPIOControl.class, "gpio");
	
	public static final GPIOControl inst = new GPIOControl();
	private final GpioController gpio;
	private GpioPinListenerDigital gpioListener;

	private GPIOControl()
	{
		logger.log(Level.INFO, "gpio 제어기 활성화");
		this.gpio = GpioFactory.getInstance();
		this.gpioListener = this::gpioListener;
		GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_18, // PIN NUMBER
				"MyButton", // PIN FRIENDLY NAME (optional)
				PinPullResistance.PULL_UP);
		myButton.addListener(this.gpioListener);
	}
	
	private void gpioListener(GpioPinDigitalStateChangeEvent event)
	{
		logger.log(Level.INFO, event.getPin() + " " +event.getState().isHigh());
	}
}