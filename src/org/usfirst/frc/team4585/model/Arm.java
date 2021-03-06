package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Arm implements HuskyClass {
	
	private final int ARM_PORT = 7;
	private final int POT_PORT = 6;
	
	private double targAngle = 0;
	private double angle;
	private boolean oldPOV;
	
	private Spark arm = new Spark(ARM_PORT);
	private PowerDistributionPanel powReg = new PowerDistributionPanel();
	private HuskyPID armPid = new HuskyPID(2.5, 0, 0, 50);
	
	private HuskyJoy joy;
	private AnalogPotentiometer pot = new AnalogPotentiometer(POT_PORT, 3600, Constants.ARM_POT_OFFSET);
	
	
	public Arm(HuskyJoy J) {
		joy = J;
	}
	
	@Override
	public void teleopInit() {
		targAngle = pot.get();
		oldPOV = true;
		
	}

	@Override
	public void doTeleop() {
		
		SmartDashboard.putNumber("targ angle", targAngle);
		SmartDashboard.putNumber("arm pot", pot.get());
		if (joy.getPOV(0) == 0.0) {
			if (pot.get() < 88) {
				arm.set( - (((-joy.getRawAxis(3) + 1) / 4) + 0.2) * 0.7);
			}
			else {
				arm.set(0);
			}
			
			//arm.set(.5);
			
			oldPOV = true;
		}
		else if (joy.getPOV(0) == 180.0) {
			arm.set((((-joy.getRawAxis(3) + 1) / 4) + 0.2) * 0.4);
			//arm.set(-.5);
			
			oldPOV = true;
		}
		
		else if (oldPOV) {
			targAngle = pot.get();
			oldPOV = false;
		}
		
		else if (joy.getPOV(0) == -1.0) {
			//arm.set(0);
			//arm.set(-(targAngle - pot.get()) / 90);
			arm.set(armPid.calculate(pot.get(), targAngle) / 90.0d);
		}
//		SmartDashboard.putNumber("arm amps", powReg.getCurrent(13));
	}

	@Override
	public void autoInit() {
		targAngle = pot.get();

	}

	@Override
	public void doAuto() {
		SmartDashboard.putNumber("pot", pot.get());
		
		double currentAngle = pot.get();
		//double output = -(targAngle - currentAngle) / 90;
		
		arm.set((armPid.calculate(currentAngle, targAngle) / 90.0d) * 1);

	}

	@Override
	public double[] getInfo() {
		return new double[] {pot.get()};
	}

	@Override
	public void giveInfo(double[] info) {
		targAngle = info[0];
	}

}
