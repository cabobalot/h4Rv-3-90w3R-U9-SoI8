package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Arm implements HuskyClass {
	
	private final int ARM_PORT = 7;
	private final int POT_PORT = 6;
	private final double SLEW_MAX = 0.01;
	private final double MIN_ANGLE = -20;
	private final double MAX_ANGLE = 90;
	
	private double targAngle = 0;
	private double angle;
	private boolean oldPOV;
	private double oldSpeed;
	private double oldJoy;
	private boolean fakeEstop = false;
	private boolean antiFall = false;
	
	private Spark arm = new Spark(ARM_PORT);
	private PowerDistributionPanel powReg = new PowerDistributionPanel();
	private HuskyPID armPid = new HuskyPID(2.5, 0, 0, 50, 1000);
	
	private HuskyJoy joy;
	private AnalogPotentiometer pot = new AnalogPotentiometer(POT_PORT, 3600, Constants.ARM_POT_OFFSET);
	
	
	public Arm(HuskyJoy J) {
		joy = J;
	}
	
	@Override
	public void teleopInit() {
		targAngle = pot.get();
		oldPOV = true;
		oldSpeed = 0;
		oldJoy = 0;
		antiFall = false;
	}

	@Override
	public void doTeleop() {
		
		SmartDashboard.putNumber("targ angle", targAngle);
		SmartDashboard.putNumber("arm pot", pot.get());
		/*
		if (joy.getPOV(0) == 0.0) {
			if (pot.get() < 94) {
				arm.set(slewLimit( - (((-joy.getRawAxis(3) + 0) / 4) + 0.4) * 0.7, SLEW_MAX));
			}
			else {
//				arm.set(0);
			}
			
			//arm.set(.5);
			
			oldPOV = true;
		}
		else if (joy.getPOV(0) == 180.0) {
			arm.set(slewLimit((((-joy.getRawAxis(3) + 1) / 4) + 0.2) * 0.2, SLEW_MAX));
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
//			arm.set(slewLimit(armPid.calculate(pot.get(), targAngle) / 90.0d, 0.05));
			arm.set(armPid.calculate(pot.get(), targAngle) / 90.0d);
		}
//		*/
		
//		arm.set(slewLimit(armPid.calculate(pot.get(), targAngle) / 90.0d, 0.05));
		
//		arm.set(armPid.calculate(pot.get(), HuskyMath.map(joy.getRawAxis(1), -1, 1, -10, 94)) / 90.0d);
		
		double joyVal = HuskyMath.limitRange(slewLimit(HuskyMath.map(joy.getRawAxis(3), 1, -1, MIN_ANGLE, MAX_ANGLE), oldJoy, 0.8), MIN_ANGLE, MAX_ANGLE);
		
		double PIDout = armPid.calculate(pot.get(), joyVal) / 90.0d;
		
		
		if (joy.getRawButton(9) || antiFall) {
			arm.set(1.0);	//anti fall
		}
		else if (joy.getRawButton(11)) {
//			arm.set(HuskyMath.limitRange(joy.getDeadAxis(3, 0, 0.1, 0.1), -0.4, 0.1));
			
			if (joy.getPOV(0) == 0.0) {
				arm.set(-0.4);
			}
			else if (joy.getPOV(0) == 180.0) {
				arm.set(0.1);
			}
			else if (joy.getPOV(0) == -1.0) {
				arm.set(-0.1);
			}
		}
		else {
			arm.set(HuskyMath.limitRange(PIDout, -0.5, 0.2));
		}
		
		
		
		oldJoy = joyVal;
		oldSpeed = arm.get();
		SmartDashboard.putNumber("arm pid out", PIDout);
		SmartDashboard.putNumber("old arm Speed", oldSpeed);
		SmartDashboard.putNumber("changed joy val", joyVal);
		
//		SmartDashboard.putNumber("arm amps", powReg.getCurrent(13));
	}

	@Override
	public void autoInit() {
		targAngle = pot.get();
		oldSpeed = 0;
		fakeEstop = false;

	}

	@Override
	public void doAuto() {
		SmartDashboard.putNumber("pot", pot.get());
		
		double currentAngle = pot.get();
		//double output = -(targAngle - currentAngle) / 90;
		
		
		if (!fakeEstop) {
			arm.set(HuskyMath.limitRange(armPid.calculate(currentAngle, targAngle) / 90.0d, -0.45, 0.1));
		}
		else {
			arm.set(0);
		}

	}
	
	private double slewLimit(double in, double old, double limit) {
		double out = in;
		if (Math.abs(in - old) > limit) {
			out = old + Math.copySign(limit, in - old);
			
		}
		SmartDashboard.putNumber("Slew thing out", out);
		return out;
	}
	private double slewLimit(double in) {
		double out = in;
		if (Math.abs(in - oldSpeed) > SLEW_MAX) {
			out = oldSpeed + Math.copySign(SLEW_MAX, oldSpeed - in);
			
		}
		SmartDashboard.putNumber("Slew thing out", out);
		return out;
	}

	public void setAntiFall(boolean state) {
		antiFall = state;
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
