package org.usfirst.frc.team4585.model.auto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class VisionCom {

	// http://10.45.85.2:5800/stream.mjpg
	// http://10.45.85.2:5800/stream.mjpg
	private final String hostName = "10.45.85.80";
	private final int portNumber = 5801;

	private final int CAMERA_ANGLE = 90;

	private UsbCamera source;
	private MjpegServer server;
	private double exposure = 50;

	private double cubeAngle = 0;

	public void beginCamera() {
		source = CameraServer.getInstance().startAutomaticCapture();
		source.setResolution(480, 320);
		source.setFPS(60);

		server = CameraServer.getInstance().addServer("VisionCam", 5800);
		server.setSource(source);
		server.getListenAddress();
	}

	public void updateExposure() {
		// SmartDashboard.putString("MJPEG URL", server.getListenAddress());
		try {
			double brightness = 50;
			brightness = Integer.parseInt(get(Requests.AVERAGE_BRIGHTNESS));
//			exposure += 100 - (brightness * 0.1);
//			exposure = (exposure * 0.95) + ((100 - (brightness) * .5));
//			exposure =
			
			exposure -= (brightness - 50) * 0.01;
			/*
			if (brightness > 50) {
//				exposure -= 1.0d / 50.0d;
				exposure -= brightness - 50;
			}
			else if (brightness < 50) {
//				exposure += 1.0d / 50.0d;
				exposure += brightness - 50;
			}
			*/
			
			if (exposure > 100) {
				exposure = 100;
			}
			if (exposure < 0) {
				exposure = 0;
			}
			SmartDashboard.putNumber("brightness", brightness);
			source.setExposureManual((int) exposure);
		} catch (Exception e) {
			e.printStackTrace();
			source.setExposureAuto();
		}
		SmartDashboard.putNumber("exposure", exposure);
	}

	public void doStuff() {

		try {
			SmartDashboard.putString("vision cube distance", get(Requests.NEAREST_CUBE_DISTANCE));

			double width = Integer.parseInt(get(Requests.WIDTH));
			String cubeXY = get(Requests.NEAREST_CUBE);
			double cubeX = Integer.parseInt(cubeXY.substring(0, cubeXY.indexOf(",")));
			double cubeAngle = ((cubeX * CAMERA_ANGLE) / width) - (CAMERA_ANGLE / 2);

			SmartDashboard.putNumber("angle", cubeAngle);
			SmartDashboard.putNumber("cubeX", cubeX);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public double getAngleToCube() {

		try {
			SmartDashboard.putString("cube distance", get(Requests.NEAREST_CUBE_DISTANCE));

			double width = Integer.parseInt(get(Requests.WIDTH));
			String cubeXY = get(Requests.NEAREST_CUBE);
			double cubeX = Integer.parseInt(cubeXY.substring(0, cubeXY.indexOf(",")));
			cubeAngle = ((cubeX * CAMERA_ANGLE) / width) - (CAMERA_ANGLE / 2);

			SmartDashboard.putNumber("angle", cubeAngle);
			SmartDashboard.putNumber("cubeX", cubeX);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cubeAngle;
	}

	public String get(int message) throws IOException {

		String output = "";
		// connect
		try {
			Socket socket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// send and receive
			out.println(message);
			// System.out.println("Server: " + in.readLine());
			output = in.readLine();

			socket.close();

		} catch (ConnectException e) {

		}
		return output;

	}

}
