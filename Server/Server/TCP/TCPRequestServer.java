package Server.TCP;

import Server.Interface.TCPRequest;
import Server.Interface.TCPResponse;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Concurrent TCP server. Connection readers submit requests and immediately
 * continue reading; completed responses are written back asynchronously.
 */
public class TCPRequestServer
{
	private final String m_name;
	private final int m_port;
	private final TCPRequestDispatcher m_dispatcher;
	private final ExecutorService m_connections = Executors.newCachedThreadPool();
	private final ExecutorService m_requests = Executors.newFixedThreadPool(
		Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

	private volatile boolean m_running;
	private ServerSocket m_serverSocket;

	public TCPRequestServer(String name, int port, TCPRequestDispatcher dispatcher)
	{
		m_name = name;
		m_port = port;
		m_dispatcher = dispatcher;
	}

	public void start() throws Exception
	{
		m_serverSocket = new ServerSocket(m_port);
		m_running = true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				TCPRequestServer.this.stop();
			}
		});

		System.out.println("'" + m_name + "' TCP server listening on port " + m_port);
		while (m_running)
		{
			try
			{
				final Socket socket = m_serverSocket.accept();
				m_connections.execute(new Runnable() {
					public void run()
					{
						handle(socket);
					}
				});
			}
			catch (SocketException e)
			{
				if (m_running)
				{
					throw e;
				}
			}
		}
	}

	private void handle(Socket socket)
	{
		try (Socket connection = socket;
			 ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream()))
		{
			output.flush();
			try (ObjectInputStream input = new ObjectInputStream(connection.getInputStream()))
			{
				while (m_running && !connection.isClosed())
				{
					Object object = input.readObject();
					if (!(object instanceof TCPRequest))
					{
						send(output, TCPResponse.failure(-1, "Expected a TCPRequest"));
						continue;
					}

					final TCPRequest request = (TCPRequest)object;
					CompletableFuture
						.supplyAsync(() -> m_dispatcher.dispatch(request), m_requests)
						.whenComplete((response, error) -> {
							TCPResponse outgoing = response;
							if (error != null)
							{
								outgoing = TCPResponse.failure(request.getRequestId(), error.getMessage());
							}
							try
							{
								send(output, outgoing);
							}
							catch (Exception e)
							{
								if (m_running)
								{
									System.err.println(m_name + " could not send response " + request.getRequestId() + ": " + e.getMessage());
								}
							}
						});
				}
			}
		}
		catch (EOFException e)
		{
			// The peer completed its request and closed the connection.
		}
		catch (Exception e)
		{
			if (m_running)
			{
				System.err.println(m_name + " connection error: " + e.getMessage());
			}
		}
	}

	private void send(ObjectOutputStream output, TCPResponse response) throws Exception
	{
		synchronized (output)
		{
			output.writeObject(response);
			output.flush();
		}
	}

	public void stop()
	{
		m_running = false;
		try
		{
			if (m_serverSocket != null)
			{
				m_serverSocket.close();
			}
		}
		catch (Exception e)
		{
			// Server is already stopping.
		}
		m_connections.shutdownNow();
		m_requests.shutdownNow();
	}
}
