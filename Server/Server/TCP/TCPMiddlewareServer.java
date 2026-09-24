package Server.TCP;

import Server.Common.DistributedResourceManager;

/** Starts the asynchronous TCP middleware. */
public class TCPMiddlewareServer
{
	public static void main(String[] args)
	{
		if (args.length != 7)
		{
			System.err.println("Usage: java Server.TCP.TCPMiddlewareServer <listen-port> <flight-host> <flight-port> <car-host> <car-port> <room-host> <room-port>");
			System.exit(1);
		}

		try
		{
			int listenPort = Integer.parseInt(args[0]);
			TCPResourceManagerProxy flights = new TCPResourceManagerProxy(args[1], Integer.parseInt(args[2]));
			TCPResourceManagerProxy cars = new TCPResourceManagerProxy(args[3], Integer.parseInt(args[4]));
			TCPResourceManagerProxy rooms = new TCPResourceManagerProxy(args[5], Integer.parseInt(args[6]));

			DistributedResourceManager middleware = new DistributedResourceManager(
				"Middleware",
				flights, flights,
				cars, cars,
				rooms, rooms);
			TCPRequestDispatcher dispatcher = new TCPRequestDispatcher(middleware, null);
			new TCPRequestServer("Middleware", listenPort, dispatcher).start();
		}
		catch (Exception e)
		{
			System.err.println("TCP middleware failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
