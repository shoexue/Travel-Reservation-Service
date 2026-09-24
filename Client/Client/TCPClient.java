package Client;

import Server.TCP.TCPResourceManagerProxy;

/** Interactive client using TCP instead of RMI. */
public class TCPClient extends Client
{
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 3100;
	private final String m_host;
	private final int m_port;

	public TCPClient(String host, int port)
	{
		m_host = host;
		m_port = port;
		connectServer();
	}

	public void connectServer()
	{
		m_resourceManager = new TCPResourceManagerProxy(m_host, m_port);
	}

	public static void main(String[] args)
	{
		if (args.length > 2)
		{
			System.err.println("Usage: java Client.TCPClient [middleware-host [middleware-port]]");
			System.exit(1);
		}

		try
		{
			String host = args.length > 0 ? args[0] : DEFAULT_HOST;
			int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
			TCPResourceManagerProxy probe = new TCPResourceManagerProxy(host, port);
			System.out.println("Connected to '" + probe.getName() + "' TCP server [" + host + ":" + port + "]");
			new TCPClient(host, port).start();
		}
		catch (Exception e)
		{
			System.err.println("TCP client exception: " + e.getMessage());
			System.exit(1);
		}
	}
}
