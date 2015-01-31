package com.fpsphone.computer;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.HashSet;
import java.util.Set;

class Parser {
	Robot robot;
	Set<Integer> pressedKeys;
	StringBuilder message;
	MouseMover mouseMover;
	static enum Mode //what part of the message we should currently be reading
	{
		START,
		TYPE,
		KEY,
		BUTTON,
		MOVE,
		END
	};
	Mode mode;

	int messageCount;
	
	Parser(Robot r)
	{
		robot = r;
		pressedKeys = new HashSet<Integer>();
		message = new StringBuilder();
		mouseMover = new MouseMover(robot);
		mouseMover.start();
		mode = Mode.START;
		
		messageCount = 0;
	}
	
	void parse(char b)
	{
//		System.err.println("Received character: " + b);
		switch(mode)
		{
		case START:
			if(b == '*')
			{
				mode = Mode.TYPE;
//				System.out.println("Message start");
				if(++messageCount % 100 == 0)
					System.out.println(messageCount + " messages has been received");
			}else
			{
//				System.out.println("Invalid input: no start-of-message");
			}
			break;

		case TYPE:
			switch(b)
			{
			case '#':
				mode = Mode.KEY;
//				System.out.println("Type: key");
				break;
			case '!':
				mode = Mode.BUTTON;
//				System.out.println("Type: button");
				break;
			case '~':
				mode = Mode.MOVE;
//				System.out.println("Type: move");
				break;
			default:
				System.out.println("Invalid input: " + b + " is not a message type");	
			}
			break;

		case KEY:
			int keyCode = (int) Character.toUpperCase(b); //for now, assume it corresponds to ascii code of the uppercase
			if(pressedKeys.contains(keyCode))
			{
				robot.keyRelease(keyCode);
				pressedKeys.remove(keyCode);
				System.out.print("Released key ");
			}else
			{
				robot.keyPress(keyCode);
				pressedKeys.add(keyCode);
				System.out.print("Pressed key ");
			}
			System.out.println(b);
			mode = Mode.END;
			break;

		case BUTTON:
			if(b == 'L' || b == 'R')
			{
				int button = InputEvent.getMaskForButton((b == 'L') ? 1 : 3);
				robot.mousePress(button);
				robot.mouseRelease(button);
				System.out.println("Clicked button " + b);
			}else if(b == 'U')
			{
				int amount = -1; //the number of "notches", negative = up
				robot.mouseWheel(amount);
				System.out.println("Scrolled mouse wheel " + amount + " notches");
			}else
			{
				System.out.println("Invalid input: " + b + " is not a mouse button/wheel command");
				break;
			}
			mode = Mode.END;
			break;
			
		case MOVE:
			if(b == '&')
			{
				String[] velocities = message.toString().split("\\|");
				if(velocities.length == 2)
				{
					try{
						double x = Double.parseDouble(velocities[0]), y = Double.parseDouble(velocities[1]);
						mouseMover.xVel = x;
						mouseMover.yVel = y;
//						System.out.println("Set mouse velocity to <" + x + ", " + y + ">");
					}catch(NumberFormatException e)
					{
						System.out.println("Invalid input: " + velocities[0] + " and/or " + velocities[1] + " are not valid numbers; discarding message");
					}
				}else
				{
					System.out.println("Invalid input: " + message.toString() + " does not have exactly one |; discarding message");
				}
				message = new StringBuilder();
				mode = Mode.START;
			}else
			{
				message.append(b);
			}
			break;
			
		case END:
			if(b == '&')
			{
				mode = Mode.START;
			}else
			{
				System.out.println("Extraneous input, expecting end-of-message; ignoring");
			}
			break;
		}
	}
}