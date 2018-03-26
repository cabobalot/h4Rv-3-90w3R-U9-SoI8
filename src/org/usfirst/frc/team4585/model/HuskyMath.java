package org.usfirst.frc.team4585.model;

public class HuskyMath {
	
	public static double distance(double x0, double y0,double x1, double y1) {
		return Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
	}
	
	public static double roundHalf(double input) {
		double out = Math.round(input * 10);
		out = Math.round(out / 5);
		out *= 5;
		out /= 10;
		
		return out;
	}
	
	public static boolean kindaEquals(double a, double b, double err) {
		return (Math.abs(a - b) <= err + 0.000000001);		//floats are stupid, arbitrary amount of 0s
	}
	
	public static double gMod360(double in) {
		double out = in;
		if (in > 360) {
			out = in;
		}
		else if (in >= 360) {
			out = in - 360;
		}
		else if (in < 0) {
			out = in + 360;
		}
		
		return out;
	}
	
	public static double gSmallAngDiff(double a, double b) {
		double out = gMod360(b - a);
		if (out >= 180) {
			out = out - 360;
		}
		
		return out;
	}
	
	public static double modAngDiff(double in, double targ) {
		double angleDiff = (targ - in);
		double modAngleDiff = ((angleDiff + 180) % 360) - 180;
		return modAngleDiff;
	}
	
	public static double limitRange(double in, double min, double max) {
		double out = in;
		
		if (out >= max) {
			out = max;
		}
		if (out <= min) {
			out = min;
		}
		
		return out;
	}
	
	public static double map(double x, double inMin, double inMax, double outMin, double outMax) {
		return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
	}
	
}
