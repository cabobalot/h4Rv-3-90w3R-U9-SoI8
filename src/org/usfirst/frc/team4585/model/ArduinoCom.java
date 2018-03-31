package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ArduinoCom {

	private boolean state = true;		//auto false, teleop true
	
	private DigitalOutput colorPin = new DigitalOutput(8);
	private DigitalOutput clawPin = new DigitalOutput(11);
	private DigitalOutput timePin = new DigitalOutput(12);
	
	private Claw claw;
	private Timer timer;
	
	public ArduinoCom(Claw C, Timer T) {
		claw = C;
		timer = T;
	}

	
	public void setPins() {
		colorPin.set(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Blue);
		SmartDashboard.putBoolean("colorPin", colorPin.get());
		
		timePin.set(state);
		
		clawPin.set(claw.getInfo()[0] == 0);
	}
	
	public void setTime(boolean in) {
		state = in;
	}

}
