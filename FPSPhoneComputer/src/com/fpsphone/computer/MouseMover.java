package com.fpsphone.computer;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

class MouseMover extends Thread {
	Robot robot;
	double xVel, yVel;
	private final static int DELAY = 20;
	private final static double PIXELS_PER_METER = 800, FACTOR = (DELAY / 1000d) * PIXELS_PER_METER; 
	
	MouseMover(Robot r)
	{
		robot = r;
		xVel = 0;
		yVel = 0;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			if(xVel != 0 && yVel != 0) //if velocity is 0, don't do anything
			{
				Point location = MouseInfo.getPointerInfo().getLocation();
				//note the y-axis is 0 at the top of the screen, and positive is downward
				robot.mouseMove(location.x + (int) (xVel * FACTOR), location.y - (int) (yVel * FACTOR));
			}
			try {
				sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
