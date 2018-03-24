package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.Joystick;

@SuppressWarnings("reportJoystickUnpluggedWarning")
public class HuskyJoy extends Joystick {
	
	
	public HuskyJoy(int port) {
		super(port);
		// TODO Auto-generated constructor stub
	}
	
	
	public double getSliderScaled(int axis) {
		double val;
		val = getRawAxis(axis);
		
		if (Math.abs(val) < 0.18) { //deadzone
			val = 0;
		}
		
		return val * (((-getRawAxis(3) + 1) / 4) + 0.5);	//scale to .5 to 1;
		
	}
	
	public double getDeadAxis(int axis, double deadMin, double deadMax){
		double out = getRawAxis(axis);
		
		if (out < deadMax && out > deadMin) {
			out = 0;
		}
		
		return out;
	}
	public double getDeadAxis(int axis, double deadMin, double deadMax, double center){
		double out = getRawAxis(axis);
		
		if (out < deadMax && out > deadMin) {
			out = center;
		}
		
		return out;
	}
	
	
}
