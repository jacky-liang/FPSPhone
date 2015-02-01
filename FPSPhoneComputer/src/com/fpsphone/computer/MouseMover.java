package com.fpsphone.computer;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;

class MouseMover extends Thread {
	Robot robot;
	double xVel, yVel;
	private final static int DELAY = 5;
	private final static double PIXELS_PER_METER = 800, FACTOR = (DELAY / 1000d) * PIXELS_PER_METER; 
	
	MouseMover(Robot r)
	{
		robot = r;
		xVel = yVel = 0;
	}
	
	@Override
	public void run()
	{
		Point location = MouseInfo.getPointerInfo().getLocation();
		double x = location.x, y = location.y;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screen.getWidth(), height = screen.getHeight();
		
		while(true)
		{
			if(xVel != 0 && yVel != 0) //if velocity is 0, don't do anything
			{
				x = Math.max(0, Math.min(width, x + xVel * FACTOR));
				//note the y-axis is 0 at the top of the screen, and positive is downward
				y = Math.max(0, Math.min(height, y - yVel * FACTOR));
				robot.mouseMove((int) x, (int) y);
			}
			try {
				sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
