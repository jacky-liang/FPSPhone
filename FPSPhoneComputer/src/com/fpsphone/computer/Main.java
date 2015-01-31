package com.fpsphone.computer;

import java.awt.Robot;

public class Main {

	public static void main(String[] args) throws Exception {
		Parser parser = new Parser(new Robot());
		String message = "*#w& *#w& ww*!L& *!L& *~0.5|0.1&";
		for(int i = 0; i < message.length(); i++)
			parser.parse(message.charAt(i));
		Thread.sleep(2000);
		message = "*~0|0&";
		for(int i = 0; i < message.length(); i++)
			parser.parse(message.charAt(i));
	}

}
