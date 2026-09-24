package Server.TCP;

import Server.Common.ResourceManager;

/** Starts one concurrent TCP inventory resource manager. */
public class TCPResourceManagerServer
{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.err.println("Usage: java Server.TCP.TCPResourceManagerServer <name> <port>");
			System.exit(1);
		}

		try
		{
			String name = args[0];
			int port = Integer.parseInt(args[1]);
			ResourceManager manager = new ResourceManager(name);
			TCPRequestDispatcher dispatcher = new TCPRequestDispatcher(manager, manager);
			new TCPRequestServer(name, port, dispatcher).start();
		}
		catch (Exception e)
		{
			System.err.println("TCP resource manager failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
