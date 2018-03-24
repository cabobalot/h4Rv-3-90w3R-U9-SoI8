package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class ArmExtender implements HuskyClass {
	
	private final double MAX_AMPS = 2;
	private final double MAX_BEYOND = 13;
	private final double MAX_MAX = 17;
	private final int POT_PORT = 1;
	private final int ARMEXT_PORT = 6;
	
	private final double ACT_SPEED = 0.1;
	
	private AnalogPotentiometer pot = new AnalogPotentiometer(POT_PORT, -20.0d * Math.PI * 1.5d, Constants.EXTEND_POT_OFFSET);
	private PowerDistributionPanel powReg = new PowerDistributionPanel();
	
	private double targPos;
	private double winchPos;
	private double ampOffSet = 0;
	private double armAngle;
	private boolean climbing = false;
	private boolean moving;
	private boolean oldMoving;
	
	
	private Joystick joy;
	private Spark extender = new Spark(ARMEXT_PORT);
	
	
	public ArmExtender(Joystick J) {
		
		joy = J;
	}

	@Override
	public void teleopInit() {
		targPos = pot.get();
		
		climbing = false;
		moving = false;
		oldMoving = false;

	}

	@Override
	public void doTeleop() {
//		/*
		/*if (climbing) {
			actuator.set(-0.2);
		}
		else */
//		/*
		if (joy.getRawButton(6) && !joy.getRawButton(4)) {
			targPos += ACT_SPEED;
			moving = true;
			if (climbing || joy.getRawButton(12)) {
				extender.set(-0.4);
			}
		}
		else if (!joy.getRawButton(6) && joy.getRawButton(4)) {
			targPos -= ACT_SPEED;
			moving = true;
			if (climbing || joy.getRawButton(12)) {
				extender.set(0.4);
			}
		}
		else {
			moving = false;
			
			if (oldMoving && !moving) {
				targPos = pot.get();
			}
//			actuator.set((pot.get() - distanceLimit(targPos) ) / 4);
			if (climbing || joy.getRawButton(12)) {
				extender.set(0);
			}
//			actuator.set(0);
		}
		if (!climbing && !joy.getRawButton(12)) {
			extender.set(HuskyMath.limitRange((pot.get() - distanceLimit(targPos)) / 4, -0.35, 0.35));
//			actuator.set(joy.getRawAxis(0));
		}
		
		
		
//		if (oldMoving && !moving) {
//			targPos = pot.get();
//		}
//		*/
//		actuator.set((distanceLimit(targPos) - pot.get()) / 2);
//		actuator.set(0);
//		actuator.set(joy.getRawAxis(1));
		
		oldMoving = moving;
		
		SmartDashboard.putNumber("distance limit", distanceLimit(targPos));
				
		SmartDashboard.putNumber("Targ Extend", targPos);
//		*/
		SmartDashboard.putNumber("extend pot", pot.get());
		SmartDashboard.putNumber("extend speed", extender.get());
		/*
		
		if (joy.getRawButton(4) == true && joy.getRawButton(6) == false) {
			actuator.set(0.5);
		}
		if (joy.getRawButton(4) == false && joy.getRawButton(6) == true) {
			actuator.set(-0.5);
		}
		if (joy.getRawButton(4) == true && joy.getRawButton(6) == true
     		|| joy.getRawButton(4) == false && joy.getRawButton(6) == false) {
			actuator.set(0);
		}
		*/

	}

	@Override
	public void autoInit() {
		targPos = pot.get();

	}

	@Override
	public void doAuto() {
		extender.set((pot.get() - distanceLimit(targPos)) / 5);

	}
	
	private double distanceLimit(double targDist) {
		double limitDist = targDist;
		if (targDist * Math.cos(Math.toRadians(armAngle)) >= MAX_BEYOND) {
			limitDist = MAX_BEYOND / Math.cos(Math.toRadians(armAngle));
		}
		if (limitDist > MAX_MAX) {
			limitDist = MAX_MAX;
		}
		if (limitDist < 0.5) {
			limitDist = 0.5;
		}
		
		return limitDist;
	}
	
	private double addAmpLimit(double input) {
		
		double amps = powReg.getCurrent(11);
		if (amps > MAX_AMPS) {
			ampOffSet += 0.1;
		}
		else {
			ampOffSet -= 0.1;
		}
		if (ampOffSet < 0){
			ampOffSet = 0;
		}
		SmartDashboard.putNumber("actuator amps", amps);
		return input - ampOffSet;
	}
	
	public boolean setCliming(boolean state) {
		climbing = state;
		return climbing;
	}
	
	public void giveArmAngle(double AA) {
		armAngle = AA;
	}
	
	@Override
	public double[] getInfo() {
		return new double[] {pot.get()};
	}

	@Override
	public void giveInfo(double[] info) {
		targPos = info[0];
		//winchPos = info[0];

	}

}
