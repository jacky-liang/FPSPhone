package com.fpsphone.computer;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

class MouseMover extends Thread {
	Robot robot;
	double xVel, yVel;
	double xOffset, yOffset;
	private final static int DELAY = 5;
	private final static double PIXELS_PER_METER = 800, FACTOR = (DELAY / 1000d) * PIXELS_PER_METER; 
	
	MouseMover(Robot r)
	{
		robot = r;
		xVel = yVel = 0;
		xOffset = yOffset = 0;
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
				double rawX = location.x + xVel*FACTOR + xOffset, rawY = location.y - yVel*FACTOR + yOffset;
				int x = (int) Math.round(rawX), y = (int) Math.round(rawY);
				robot.mouseMove(x, y);
				xOffset = rawX - x;
				yOffset = rawY - y;
			}
			try {
				sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
