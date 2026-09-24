package Server.RMI;

import Server.Common.DistributedResourceManager;
import Server.Interface.IInventoryResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/** RMI transport adapter for the shared distributed middleware. */
public class RMIMiddleware extends DistributedResourceManager
{
	private static final int RMI_PORT = 1099;
	private static final String RMI_PREFIX = "group_46_";
	private static final String MIDDLEWARE_NAME = "Middleware";

	public RMIMiddleware(String flightHost, String carHost, String roomHost) throws Exception
	{
		this(connect(flightHost, "Flights"), connect(carHost, "Cars"), connect(roomHost, "Rooms"));
	}

	private RMIMiddleware(Remote flights, Remote cars, Remote rooms)
	{
		super(
			MIDDLEWARE_NAME,
			(IResourceManager)flights,
			(IInventoryResourceManager)flights,
			(IResourceManager)cars,
			(IInventoryResourceManager)cars,
			(IResourceManager)rooms,
			(IInventoryResourceManager)rooms);
	}

	private static Remote connect(String host, String name) throws Exception
	{
		boolean first = true;
		while (true)
		{
			try
			{
				Registry registry = LocateRegistry.getRegistry(host, RMI_PORT);
				Remote remote = registry.lookup(RMI_PREFIX + name);
				if (!(remote instanceof IResourceManager) || !(remote instanceof IInventoryResourceManager))
				{
					throw new RemoteException(name + " does not implement the required resource interfaces");
				}
				System.out.println("Connected to '" + name + "' resource manager [" + host + ":" + RMI_PORT + "/" + RMI_PREFIX + name + "]");
				return remote;
			}
			catch (NotBoundException | RemoteException e)
			{
				if (first)
				{
					System.out.println("Waiting for '" + name + "' resource manager [" + host + ":" + RMI_PORT + "/" + RMI_PREFIX + name + "]");
					first = false;
				}
				Thread.sleep(500);
			}
		}
	}

	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.err.println("Usage: java Server.RMI.RMIMiddleware <flight-host> <car-host> <room-host>");
			System.exit(1);
		}

		try
		{
			RMIMiddleware middleware = new RMIMiddleware(args[0], args[1], args[2]);
			IResourceManager stub = (IResourceManager)UnicastRemoteObject.exportObject(middleware, 0);

			Registry registry;
			try
			{
				registry = LocateRegistry.createRegistry(RMI_PORT);
			}
			catch (RemoteException e)
			{
				registry = LocateRegistry.getRegistry(RMI_PORT);
			}

			final Registry shutdownRegistry = registry;
			registry.rebind(RMI_PREFIX + MIDDLEWARE_NAME, stub);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run()
				{
					try
					{
						shutdownRegistry.unbind(RMI_PREFIX + MIDDLEWARE_NAME);
						System.out.println("'" + MIDDLEWARE_NAME + "' middleware unbound");
					}
					catch (Exception e)
					{
						System.err.println("Unable to unbind middleware: " + e.getMessage());
					}
				}
			});

			System.out.println("'" + MIDDLEWARE_NAME + "' middleware ready and bound to '" + RMI_PREFIX + MIDDLEWARE_NAME + "'");
		}
		catch (Exception e)
		{
			System.err.println("Middleware exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
