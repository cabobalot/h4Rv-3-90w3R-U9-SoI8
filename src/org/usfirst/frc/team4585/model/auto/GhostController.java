package org.usfirst.frc.team4585.model.auto;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import org.usfirst.frc.team4585.model.*;

import GridNav.Vertex;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GhostController implements HuskyClass {
	
	private final int SWITCH_DROP_DISTANCE = 1000;
	private final int SCALE_DROP_DISTANCE = 0;
	
	private ArrayList<AutoTask> taskList = new ArrayList<AutoTask>();
	
	private int counter;
	private double mapTargX;
	private double mapTargY;
	private double oldTargX;
	private double oldTargY;
	private int mapUpdate;
	private ArrayDeque<Vertex> route;
	
	private double[] chassisInfo;
	
	private double[] clawInfo;
	private double[] posInfo;
	private double[] teleTargPos;	//experemental
	private double teleTargAngle;
	
	private double[] targPoint = {-1, -1};
	private Iterator<Vertex> itr;
	private double oldTime = -1;
	
	private Chassis chassis;
	private Arm arm;
	private ArmExtender actuator;
	private Winch winch;
	private Claw claw;
	private PositionTracker tracker;
	private HuskyJoy driveJoy;
	private HuskyJoy weaponsJoy;
	
	private SendableChooser<String> firstAutoChooser = new SendableChooser<>();
	private SendableChooser<String> secondAutoChooser = new SendableChooser<>();
	private SendableChooser<Boolean> twoCubeChooser = new SendableChooser<>();
	private HuskyPID anglePID = new HuskyPID(1.0d/90.0d, 0, 0, 0, 1000);
	private VisionCom visCom = new VisionCom();
	private Timer timer = new Timer();
	private HuskyPathFinder pathFinder = new HuskyPathFinder("/h4Rv-3-P0w3R-U9/src/fieldMap.map");
//	private HuskyPID drivePID = new HuskyPID(2.0, 0.03, 0.8, 0.8, 0.03);
	private HuskyPID drivePID = new HuskyPID(2.0, 0.03, 0.8, 0.8, 0.03);
	
//	/h4Rv-3-P0w3R-U9/src/fieldMap.map
//	./src/fieldMap.map
	
	
	
	
	public GhostController(Chassis Ch, Arm A, Claw Cl, ArmExtender AA, Winch W, PositionTracker T,  HuskyJoy DJ, HuskyJoy WJ) {
		chassis = Ch;
		arm = A;
		
		actuator = AA;
		winch = W;
		
		claw = Cl;
		tracker = T;
		driveJoy = DJ;
		weaponsJoy = WJ;
		
		
	}
	
	public void dashInit() {
		firstAutoChooser.addDefault("Switch inside", "sw_in");
		firstAutoChooser.addObject("Switch outside (not center)", "sw_out");
		firstAutoChooser.addObject("Scale outside", "sc_out");
		firstAutoChooser.addObject("Auto run", "auto_run");
		SmartDashboard.putData("First auto", firstAutoChooser);
		
		secondAutoChooser.addDefault("Switch inside", "sw_in");
		secondAutoChooser.addObject("Switch outside (not center)", "sw_out");
		secondAutoChooser.addObject("Scale outside", "sc_out");
		secondAutoChooser.addObject("Auto run", "auto_run");
		SmartDashboard.putData("Second auto", secondAutoChooser);
		
		twoCubeChooser.addDefault("No", false);
		twoCubeChooser.addDefault("Yes", true);
		SmartDashboard.putData("Do a two cube auto?", twoCubeChooser);
		
	}
	
	@Override
	public void teleopInit() {
		teleTargPos = new double[] {0, 0};
		teleTargAngle = 0;
		
		

	}

	@Override
	public void doTeleop() {
		
		posInfo = tracker.getInfo();
		actuator.giveArmAngle(arm.getInfo()[0]);
		
		SmartDashboard.putNumber("sonar inch", posInfo[3]);
		
			//normal drive (slider)
//		chassis.giveInfo(new double[] {-driveJoy.getSliderScaled(1), driveJoy.getSliderScaled(2)});
		
			//non slider drive
		if(weaponsJoy.getRawButton(9) || driveJoy.getRawButton(9)) {
			chassis.giveInfo(new double[] {-1.0, 0.0});
			arm.setAntiFall(true);
		}
		else {
			chassis.giveInfo(new double[] {-driveJoy.getDeadAxis(1, 0.1, 0.1), driveJoy.getRawAxis(2)});
			arm.setAntiFall(false);
		}
		
		
			//climb
		if (weaponsJoy.getRawButton(1)) {
			double[] actInfo = actuator.getInfo();
			double[] winchInfo = winch.getInfo();
			
			
			//(((-getRawAxis(3) + 1) / 4) + 0.5)
//			winch.giveInfo(new double[] {-(((-weaponsJoy.getRawAxis(3) + 1) / 4) + 0.5)});
			winch.giveInfo(new double[] {-1});
			actuator.setCliming(true);
			//actuator.giveInfo(winchInfo);
		}
		else if (weaponsJoy.getRawButton(7)) {
			winch.giveInfo(new double[] {(((-weaponsJoy.getRawAxis(3) + 1) / 4) + 0.5)});
		}
		else {
			winch.giveInfo(new double[] {0});
			actuator.setCliming(false);
			actuator.giveArmAngle(arm.getInfo()[0]);
		}
		
		
		
		
		/*	//angle accel turn
		teleTargAngle += joy.getSliderScaled(2) * 5;
		
		SmartDashboard.putNumber("joy", joy.getSliderScaled(2));
		
		chassis.giveInfo(new double[] {-joy.getSliderScaled(1),
				angleAccel(posInfo[2], teleTargAngle)});
		*/
		
		
		//fake mecanum
		/*
		teleTargPos[0] += (joy.getSliderScaled(0) / 10);
		teleTargPos[1] += (-joy.getSliderScaled(1) / 10);
		if(driveTo(teleTargPos)) {
			pointAt(0);
		}
//		*/
		
		
		
	}

	@Override
	public void autoInit() {
		String gameInfo = DriverStation.getInstance().getGameSpecificMessage();
		taskList.clear();
		
		double x = tracker.getInfo()[0];
		taskList.add(new AutoTask(TaskType.goTo, new double[] {x, 2}));
		addToAuto(firstAutoChooser.getSelected(), gameInfo);
		
		if (twoCubeChooser.getSelected()) {
			
			taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {-5}));
			taskList.add(new AutoTask(TaskType.goToReverse, new double[] {12.5, 5}));
			taskList.add(new AutoTask(TaskType.pointAt, new double[] {0}));
			taskList.add(new AutoTask(TaskType.goToReverse, new double[] {12.5, 3}));
			taskList.add(new AutoTask(TaskType.pointAt, new double[] {0}));
			taskList.add(new AutoTask(TaskType.setArmDist, new double[] {13}));
			taskList.add(new AutoTask(TaskType.goToExact, new double[] {12.5, 7}));
			taskList.add(new AutoTask(TaskType.pointAt, new double[] {0}));
			taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {-15}));
			taskList.add(new AutoTask(TaskType.setClaw, new double[] {1}));
			taskList.add(new AutoTask(TaskType.pause, new double[] {0.5}));
			taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));
			taskList.add(new AutoTask(TaskType.goToReverse, new double[] {12.5, 4}));
			
			
			addToAuto(secondAutoChooser.getSelected(), gameInfo);
		}
		
		
		/*		//debug
		taskList.clear();
		taskList.add(new AutoTask(TaskType.goToExact, new double[] {13, 3}));
//		taskList.add(new AutoTask(TaskType.goToReverse, new double[] {13, 1.5}));
		
		
		/*
		double P = SmartDashboard.getNumber("P", 1.0);
		double I = SmartDashboard.getNumber("I", 0.0);
		double D = SmartDashboard.getNumber("D", 0.0);
		double slew = SmartDashboard.getNumber("slew", 1);
		drivePID = new HuskyPID(P, I, D, 0.8, slew);
		
		SmartDashboard.putNumber("P", P);
		SmartDashboard.putNumber("I", I);
		SmartDashboard.putNumber("D", D);
		SmartDashboard.putNumber("slew", slew);
		
//		*/
		
		
		
		taskList.add(new AutoTask(TaskType.stop, new double[] {0}));
		counter = 0;
		targPoint[0] = -1.0d;
		targPoint[1] = -1.0d;
		mapUpdate = 0;
		oldTargX = 0;
		oldTargY = 0;
		oldTime = -1.0;
		SmartDashboard.putNumber("mapUpdate", mapUpdate);
		timer.reset();
		timer.start();

	}

	@Override
	public void doAuto() {
		/*
		if (timer.get() > 13) {
			claw.giveInfo(new double[] {0});
		}
//		*/
		
//		arm.giveInfo(new double[] {30});
//		dropCube(new double[] {0, 0});
		
		//arm.giveInfo(new double[] {0});
		/*
		actuator.giveArmAngle(arm.getInfo()[0]);
		actuator.giveInfo(new double[] {20});
		*/
		
		/*
		//claw.giveInfo(new double[] {1});
		if (claw.getInfo()[0] != 1) {
			claw.giveInfo(new double[] {1});
		}
		*/
		
		//pointAtCube();
		//goToCube();
		
		//25' 3.5"
		//left -3452.0
		//right -3464.0
		
		//102' 10"
		//right -14032.0
		//left -13976.0
		
		/*		calibrate encoders
		posInfo = tracker.getInfo();
		if ((posInfo[1]-1) < 90) { // 25 3.5
			chassis.giveInfo(new double[] {0.7, angleAccel(posInfo[2], 0)});
			
		}
		else if ((posInfo[1]-1) < 100) {
			chassis.giveInfo(new double[] {0.5, angleAccel(posInfo[2], 0)});
		}
		else {
			chassis.giveInfo(new double[] {0, 0});
		}
		*/
		
		
//		driveTo(new double[] {13, 6.5}, false);
//		SmartDashboard.putBoolean("at targ?", pointAt(45));
		
//		/*				//main stuff
		actuator.giveArmAngle(arm.getInfo()[0]);
		
		if(counter < taskList.size()) {
			
			SmartDashboard.putNumber("auto item #", counter);
			
			switch(taskList.get(counter).getType()){
			
			case goTo:
				if(driveTo(taskList.get(counter).getInfo(), false)) {
					counter++;
				}
				break;
				
			case goToExact:
				if(driveTo(taskList.get(counter).getInfo(), false, true)) {
					counter++;
				}
				break;
			
			case goToMapping:
				if(driveToMaping(taskList.get(counter).getInfo(), false, false)) {
					counter++;
				}
				break;
				
			case goToReverse:
				if(driveTo(taskList.get(counter).getInfo(), true)) {
					counter++;
				}
				break;
			
			case goToReverseExact:
				if(driveTo(taskList.get(counter).getInfo(), true, true)) {
					counter++;
				}
				break;
			
			case pointAt:
				if(pointAt(taskList.get(counter).getInfo()[0])) {
					counter++;
				}
				break;
				
			case getCube:
				if(goToCube()) {
					counter++;
				}
				break;
				
			case dropCube:
//				claw.giveInfo(new double[] {1});
//				counter++;
				if (dropCube(taskList.get(counter).getInfo())) {
					counter++;
				}
				
				break;
				
			case setClaw:
				claw.giveInfo(taskList.get(counter).getInfo());
				counter++;
				break;
			
			case setArmDeg:
				arm.giveInfo(taskList.get(counter).getInfo());
				counter++;
				break;
				
			case setArmDist:
				actuator.giveInfo(taskList.get(counter).getInfo());
				counter++;
				break;
				
			case stop:
				chassis.giveInfo(new double[] {0, 0});
				break;
			
			case pause:
				chassis.giveInfo(new double[] {0, 0});
				if (oldTime < 0) {
					oldTime = timer.get();
				}
				if (timer.get() >= oldTime + taskList.get(counter).getInfo()[0]) {
					oldTime = -1.0;
					counter++;
				}
				
				break;
			
			default:
				chassis.giveInfo(new double[] {0, 0});
				break;
				
			}
		}
//		*/
		
		
		
	}


	@Override
	public double[] getInfo() {
		
		return null;
	}

	@Override
	public void giveInfo(double[] info) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean driveTo(double[] I, boolean reverse, boolean exact) {
		double[] buffer = {0, 0};
		double targAngle;
		posInfo = tracker.getInfo();
		
		targAngle = Math.toDegrees(Math.atan2(I[0] - posInfo[0], I[1] - posInfo[1]));
		
		SmartDashboard.putNumber("targX", I[0]);
		SmartDashboard.putNumber("targY", I[1]);
		SmartDashboard.putNumber("TargAngle", targAngle);
		
		if (I[0] != oldTargX || I[1] != oldTargY) {
//			drivePID.reset();
			oldTargX = I[0];
			oldTargX = I[1];
		}
		
		if (reverse) {
//			buffer[0] = ((Math.round(posInfo[0]) == Math.round(I[0])) && (Math.round(posInfo[1]) == Math.round(I[1])) || 
//					Math.abs(HuskyMath.modAngDiff(posInfo[2], targAngle + 180)) > 45)? 0:-0.6;
			
			if ((Math.abs(HuskyMath.modAngDiff(posInfo[2], targAngle + 180)) < 45) &&
					((HuskyMath.roundHalf(posInfo[0]) != HuskyMath.roundHalf(I[0])) || (HuskyMath.roundHalf(posInfo[1]) != HuskyMath.roundHalf(I[1])))) {
				 buffer[0] = -drivePID.calculate(HuskyMath.distance(posInfo[0], posInfo[1], I[0], I[1]), 0.0);
				 SmartDashboard.putNumber("drive PID out", buffer[0]);
			}
			else {
				buffer[0] = 0.0;
			}
			
			buffer[1] = angleAccel(posInfo[2], targAngle + 180);
		}
		else {
//			buffer[0] = ((Math.round(posInfo[0]) == Math.round(I[0])) && (Math.round(posInfo[1]) == Math.round(I[1])) || 
//					Math.abs(HuskyMath.modAngDiff(posInfo[2], targAngle)) > 45)? 0:0.6;
			
			
			
			if ((Math.abs(HuskyMath.modAngDiff(posInfo[2], targAngle)) < 45) &&
					((HuskyMath.roundHalf(posInfo[0]) != HuskyMath.roundHalf(I[0])) || (HuskyMath.roundHalf(posInfo[1]) != HuskyMath.roundHalf(I[1])))) {
				 buffer[0] = drivePID.calculate(HuskyMath.distance(posInfo[0], posInfo[1], I[0], I[1]), 0.0);
				 SmartDashboard.putNumber("drive PID out", buffer[0]);
			}
			else {
				buffer[0] = 0.0;
			}
			SmartDashboard.putNumber("Drive out", buffer[0]);
			
			
			buffer[1] = angleAccel(posInfo[2], targAngle);
		}
		
		if (exact) {
			buffer[0] *= 0.9;
			buffer[1] *= 0.9;
		}
		
		chassis.giveInfo(buffer);
		
		if (exact) {		//(ish)
			return HuskyMath.kindaEquals(posInfo[0], HuskyMath.roundHalf(I[0]), 0.6) && 
					HuskyMath.kindaEquals(posInfo[1], HuskyMath.roundHalf(I[1]), 0.6);
		}
		else {
			return (HuskyMath.kindaEquals(posInfo[0], Math.round(I[0]), 0.7) && 
					(HuskyMath.kindaEquals(posInfo[1], Math.round(I[1]), 0.7)));
		}
		
	}
	private boolean driveTo(double[] I, boolean reverse) {
		return driveTo(I, reverse, false);
	}
	
	private boolean driveToMaping(double[] I, boolean reverse, boolean centerBlocked) {
		posInfo = tracker.getInfo();
		
		if ((targPoint[0] != I[0]) || (targPoint[1] != I[1])) {
			mapUpdate = 100;
			
			pathFinder.setEndPoints(posInfo[0], posInfo[1], I[0], I[1]);
			try {
				pathFinder.calculatePath();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			route = pathFinder.getPathList();
			targPoint = I.clone();
			SmartDashboard.putStringArray("map path array", pathFinder.printPath());
		}
		
		SmartDashboard.putNumber("mapUpdate", mapUpdate);
		mapUpdate--;
		
		if (!route.isEmpty()) {
			
			Vertex next = route.getFirst();
			
			mapTargX = next.getX();
			mapTargY = next.getY();
		}
		
		if(driveTo(new double[] {mapTargX, mapTargY}, reverse)) {
			if ((mapTargX == I[0]) && (mapTargY == I[1])) {
				route.pop();
				return true;
			}
			else {
				route.pop();
				return false;
			}
			
		}
		else {
			return false;
		}

	}
	
	
	/*private boolean driveFeet(double feet) {
		double[] buffer = {0, 0};
		
		buffer[0] = ((Math.round(posInfo[0]) == Math.round(I[0])) && (Math.round(posInfo[1]) == Math.round(I[1])))? 0:0.6;
		buffer[1] = angleAccel(posInfo[2], targAngle);
		
		chassis.giveInfo()
		
		return true;
	}*/
	
	private boolean pointAt(double targAngle) {
		double[] buffer = {0, 0};
		posInfo = tracker.getInfo();
		
		buffer[1] = angleAccel(posInfo[2], targAngle);
		//buffer[1] = anglePID.calculate(posInfo[2], targAngle);
		
		chassis.giveInfo(buffer);
		
		return (posInfo[2] < targAngle + 8) && (posInfo[2] > targAngle - 8);
	}
	
	private boolean pointAtCube() {
		posInfo = tracker.getInfo();
		
		double angle = visCom.getAngleToCube();
		
		if (Math.round(angle) != 0) {
			pointAt(angle + posInfo[2]);
		}
		else {
			chassis.giveInfo(new double[] {0, 0.5});
		}
		return true;
	}
	
	private boolean goToCube() {
		boolean atCube = false;
		
		posInfo = tracker.getInfo();
		
		SmartDashboard.putNumber("sonar inch", posInfo[3]);
		
		double angle = visCom.getAngleToCube();
		
		if (angle != 0) {
			if (posInfo[3] > 15) {
				chassis.giveInfo(new double[] {0.55, angleAccel(posInfo[2], angle + posInfo[2])});
			}
			else {
				if (angle + posInfo[2] < posInfo[2] + 5 && angle + posInfo[2] > posInfo[2] - 5) {
					chassis.giveInfo(new double[] {0, 0});
					atCube = true;
				}
				else {
					chassis.giveInfo(new double[] {0, angleAccel(posInfo[2], angle + posInfo[2])});
					
				}
				
			}
		}
		else {
			chassis.giveInfo(new double[] {0, 0.5});
		}
			
		
		
		return atCube;
		
	}
	
	private boolean getCube() {
		
		arm.giveInfo(new double[] {0});
		actuator.giveInfo(new double[] {10});
		
		if (goToCube()) {
			arm.giveInfo(new double[] {-10});
			
			claw.giveInfo(new double[] {1});
			return true;
		}
		
		return false;
	}
	
	private boolean dropCube(double[] info) {
		posInfo = tracker.getInfo();
		
		double angle = info[0];
		boolean dropped = false;
		
		if (posInfo[2] < angle + 8 && posInfo[2] > angle -8) {
			if (info[1] == 0) {
				if (posInfo[3] > SWITCH_DROP_DISTANCE) {
					chassis.giveInfo(new double[] {0.5, angleAccel(posInfo[2], angle)});
				}
				else {
					chassis.giveInfo(new double[] {0, 0});
					claw.giveInfo(new double[] {0});
					dropped = true;
				}
			}
			else {
				if (posInfo[4] < SCALE_DROP_DISTANCE) {
					chassis.giveInfo(new double[] {0.5, angleAccel(posInfo[2], angle)});
				}
				else {
					chassis.giveInfo(new double[] {0, 0});
					claw.giveInfo(new double[] {0});
					dropped = true;
				}
			}
		}
		else {
			chassis.giveInfo(new double[] {0, angleAccel(posInfo[2], angle)});
		}
		
		
		return dropped;
	}
	
	private double angleAccel(double inAngle, double targAngle) {
		double output;
		
//		/*
		/*
		if (inAngle < 0) {
			inAngle = 360 - Math.abs(inAngle);
		}
		if (targAngle < 0) {
			targAngle = 360 - Math.abs(targAngle);
		}
		*/
		/*
		double angleDiff = (targAngle - inAngle);
		double modAngleDiff;
		if (angleDiff >= 0) {
			modAngleDiff = ((angleDiff + 180) % 360) - 180;
		}
		else {
			modAngleDiff = -(((-angleDiff + 180) % 360) - 180);
//			modAngleDiff = -(((-modAngleDiff + 180) % 360) - 180);
		}
//		*/
		
		double angleDiff = (targAngle - inAngle);
		double modAngleDiff = ((angleDiff + 180) % 360) - 180;
		
//		double modAngleDiff = HuskyMath.gSmallAngDiff(inAngle, targAngle);
		
		SmartDashboard.putNumber("angle diff", modAngleDiff);
		
		output = (modAngleDiff / 45);
		/*
		if (output < 0.3 && !(output < 0.1)) {
			output = 0.3;
		}
		else if (output > -0.3 && !(output > -0.001)) {
			output = -0.3;
		}
		
		else */if (output < 0.6 && !(output < 0.1)) {
			output = 0.6;
		}
		else if (output > -0.6 && !(output > -0.1)) {
			output = -0.6;
		}
		else if (Math.abs(output) < 0.1) {
			output = 0;
		}
		
//		if (output > 0.7) {
//			output = 0.7;
//		}
//		else if (output < -0.7) {
//			output = -0.7;
//		}
		
		output = HuskyMath.limitRange(output, -0.8, 0.8);
		
		SmartDashboard.putNumber("angle accel out", output);
		
		return output;
	}
	
	private void addToAuto(String options, String gameInfo) {
		double x = tracker.getInfo()[0];
		double y = tracker.getInfo()[1];
		switch (options) {
		case "sw_in":
			if (gameInfo.charAt(0) == 'L') {
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));
				taskList.add(new AutoTask(TaskType.goTo, new double[] {8, 7}));
//				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));//othger
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {15}));
				taskList.add(new AutoTask(TaskType.pointAt, new double[] {0}));
				taskList.add(new AutoTask(TaskType.goToExact, new double[] {8, 9}));
				taskList.add(new AutoTask(TaskType.setClaw, new double[] {0}));
				taskList.add(new AutoTask(TaskType.pause, new double[] {0.5}));
				taskList.add(new AutoTask(TaskType.goToReverse, new double[] {8, 7}));
			} else {
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));
				taskList.add(new AutoTask(TaskType.goTo, new double[] {17, 7}));
//				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));//othger
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {15}));
				taskList.add(new AutoTask(TaskType.pointAt, new double[] {0}));
				taskList.add(new AutoTask(TaskType.goToExact, new double[] {17, 9}));
				taskList.add(new AutoTask(TaskType.setClaw, new double[] {0}));
				taskList.add(new AutoTask(TaskType.pause, new double[] {0.5}));
				taskList.add(new AutoTask(TaskType.goToReverse, new double[] {17, 7}));
			}
			
			break;
		
		case "sw_out":
			if (gameInfo.charAt(0) == 'L') {
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {10}));
				taskList.add(new AutoTask(TaskType.goTo, new double[] {x, 13}));
				if (x < 13) {
					taskList.add(new AutoTask(TaskType.pointAt, new double[] {90}));
					taskList.add(new AutoTask(TaskType.goTo, new double[] {x + 2, 13}));
					taskList.add(new AutoTask(TaskType.dropCube, new double[] {90, 0}));
//					taskList.add(new AutoTask(TaskType.goToReverse, new double[] {4, 13}));
				}
			} else {
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {45}));
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {10}));
				taskList.add(new AutoTask(TaskType.goTo, new double[] {x, 13}));
				if (x > 13) {
					taskList.add(new AutoTask(TaskType.pointAt, new double[] {-90}));
					taskList.add(new AutoTask(TaskType.goTo, new double[] {x - 2, 13}));
					taskList.add(new AutoTask(TaskType.dropCube, new double[] {-90, 0}));
//					taskList.add(new AutoTask(TaskType.goToReverse, new double[] {22, 13}));
				}
				
			}
			
			break;
		
		case "sc_out":
			if (gameInfo.charAt(1) == 'L') {
				taskList.add(new AutoTask(TaskType.goToMapping, new double[] {4, 26}));
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {85}));
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {15}));
				taskList.add(new AutoTask(TaskType.pointAt, new double[] {90}));
				taskList.add(new AutoTask(TaskType.dropCube, new double[] {90, 1}));
			} else {
				taskList.add(new AutoTask(TaskType.goToMapping, new double[] {22, 26}));
				taskList.add(new AutoTask(TaskType.setArmDeg, new double[] {85}));
				taskList.add(new AutoTask(TaskType.setArmDist, new double[] {15}));
				taskList.add(new AutoTask(TaskType.pointAt, new double[] {-90}));
				taskList.add(new AutoTask(TaskType.dropCube, new double[] {-90, 1}));
			}
			
			break;
		
		case "auto_run":
			taskList.add(new AutoTask(TaskType.goTo, new double[] {x, 13}));
			
			break;
			
		default:
			
			break;
		
		}
	}
	
}



