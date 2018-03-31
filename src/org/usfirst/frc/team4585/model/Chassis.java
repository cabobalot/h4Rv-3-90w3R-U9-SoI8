package org.usfirst.frc.team4585.model;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Chassis extends DifferentialDrive implements HuskyClass {
	
	private final static int RIGHT_DRIVE_PORT = 8;
	private final static int LEFT_DRIVE_PORT = 9;
	private final int SONAR_PORT = 0;
	private final double TURN_GAIN = 0.9;
	private final double DRIVE_GAIN = 1;
	
	private AnalogSonar sonar = new AnalogSonar(SONAR_PORT);
	
	private double[] info = {0, 0};
	private double[] oldOut;
	
	
	
	HuskyJoy joy;
	Timer timer;
	
	public Chassis(HuskyJoy J, Timer T) {
		super(new Spark(RIGHT_DRIVE_PORT), new Spark(LEFT_DRIVE_PORT));
		
		joy = J;
		timer = T;
		
	}
	
	
	@Override
	public void teleopInit() {
		oldOut = new double[] {0, 0};
	}
	
	@Override
	public void doTeleop() {
//		arcadeDrive(Math.copySign(Math.pow(info[0] * DRIVE_GAIN, 2), info[0] * DRIVE_GAIN), Math.copySign(Math.pow(info[0] * DRIVE_GAIN, 2), info[1] * DRIVE_GAIN));
//		arcadeDrive(info[0] * DRIVE_GAIN, info[1] * DRIVE_GAIN);
//		arcadeDrive(info[0] * DRIVE_GAIN, info[1] * DRIVE_GAIN, true);
		
		double driveSlew = SmartDashboard.getNumber("Drive slew", 1);
		double turnSlew = SmartDashboard.getNumber("Turn slew", 1);
	
//		double outDrive = slewLimit(info[0], oldOut[0], 0.03);
//		double outTurn = slewLimit(info[1], oldOut[1], 0.04);
		
		double outDrive = info[0] * DRIVE_GAIN;
		double outTurn = info[1] * TURN_GAIN;
		
		arcadeDrive(outDrive, outTurn, true);
		
		SmartDashboard.putNumber("Drive speed thingy", outDrive);
		SmartDashboard.putNumber("Turn speed", outTurn);
		
		oldOut[0] = outDrive;
		oldOut[1] = outTurn;
		
		SmartDashboard.putNumber("Drive slew", driveSlew);
		SmartDashboard.putNumber("Turn Slew", turnSlew);
	}
	
	@Override
	public void autoInit() {
		
	}
	
	@Override
	public void doAuto() {
		arcadeDrive(info[0], info[1]);
		
		
		SmartDashboard.putNumber("joystick axis one:", joy.getRawAxis(1));
		SmartDashboard.putNumber("in: ",sonar.getInches());
	}
	
	private double slewLimit(double in, double old, double slewMax){
		double out = in;
		
		if (Math.abs(in - old) > slewMax) {
			out = old + Math.copySign(slewMax, in - old);
		}
		return out;
		
	}

	@Override
	public double[] getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void giveInfo(double[] I) {
		info = I;
		
	}
	
	
	
}
