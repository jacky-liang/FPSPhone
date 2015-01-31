package com.fpsphone.computer;

import java.io.IOException;
import java.io.InputStream;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class Server extends Thread
{
	private final static String uuid = "05a50d3af0f1e77baa77cc2df5260cb5";
	
	Parser parser;
	StreamConnectionNotifier streamConnNotifier;
	InputStream in;
	
	Server(Parser p)
	{
		parser = p;
		
		String connectionString = "btspp://localhost:" + uuid + ";name=FPSPhoneHost";
		try {
			System.out.println("Attempting to start server");
			streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
			LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
			System.out.println("Waiting for connection");
			StreamConnection streamConn = streamConnNotifier.acceptAndOpen(); //wait for client connection
			
			System.out.println("accepted");
			RemoteDevice device = RemoteDevice.getRemoteDevice(streamConn);
			System.out.println("Received connection from " + device.getFriendlyName(true) + " (" + device.getBluetoothAddress() + ")");
			in = streamConn.openInputStream();
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		System.out.println("Now listening for commands");
		int b;
		try
		{
			while((b = in.read()) != -1)
			{
				parser.parse((char) b);
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally
		{
			System.out.println("Stopping server");
			try
			{
				streamConnNotifier.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
