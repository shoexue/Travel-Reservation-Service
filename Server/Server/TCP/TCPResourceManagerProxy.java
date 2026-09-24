package Server.TCP;

import Server.Interface.IInventoryResourceManager;
import Server.Interface.IResourceManager;
import Server.Interface.Operation;
import Server.Interface.TCPRequest;
import Server.Interface.TCPResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

/** Blocking client-side proxy backed by the general TCP request envelope. */
public class TCPResourceManagerProxy implements IResourceManager, IInventoryResourceManager
{
	private static final int CONNECT_TIMEOUT_MS = 3000;
	private static final int READ_TIMEOUT_MS = 30000;
	private static final AtomicLong NEXT_REQUEST_ID = new AtomicLong(1);

	private final String m_host;
	private final int m_port;

	public TCPResourceManagerProxy(String host, int port)
	{
		m_host = host;
		m_port = port;
	}

	private Object invoke(Operation operation, Object... arguments) throws RemoteException
	{
		long requestId = NEXT_REQUEST_ID.getAndIncrement();
		TCPRequest request = new TCPRequest(requestId, operation, arguments);

		try (Socket socket = new Socket())
		{
			socket.connect(new InetSocketAddress(m_host, m_port), CONNECT_TIMEOUT_MS);
			socket.setSoTimeout(READ_TIMEOUT_MS);

			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			output.writeObject(request);
			output.flush();

			Object object = input.readObject();
			if (!(object instanceof TCPResponse))
			{
				throw new RemoteException("Invalid response from " + m_host + ":" + m_port);
			}

			TCPResponse response = (TCPResponse)object;
			if (response.getRequestId() != requestId)
			{
				throw new RemoteException("Mismatched response ID from " + m_host + ":" + m_port);
			}
			if (!response.isSuccess())
			{
				throw new RemoteException(response.getError());
			}
			return response.getResult();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RemoteException("TCP request to " + m_host + ":" + m_port + " failed", e);
		}
	}

	public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		return (Boolean)invoke(Operation.ADD_FLIGHT, flightNum, flightSeats, flightPrice);
	}

	public boolean addCars(String location, int numCars, int price) throws RemoteException
	{
		return (Boolean)invoke(Operation.ADD_CARS, location, numCars, price);
	}

	public boolean addRooms(String location, int numRooms, int price) throws RemoteException
	{
		return (Boolean)invoke(Operation.ADD_ROOMS, location, numRooms, price);
	}

	public int newCustomer() throws RemoteException
	{
		return (Integer)invoke(Operation.NEW_CUSTOMER);
	}

	public boolean newCustomer(int customerID) throws RemoteException
	{
		return (Boolean)invoke(Operation.NEW_CUSTOMER_ID, customerID);
	}

	public boolean deleteFlight(int flightNum) throws RemoteException
	{
		return (Boolean)invoke(Operation.DELETE_FLIGHT, flightNum);
	}

	public boolean deleteCars(String location) throws RemoteException
	{
		return (Boolean)invoke(Operation.DELETE_CARS, location);
	}

	public boolean deleteRooms(String location) throws RemoteException
	{
		return (Boolean)invoke(Operation.DELETE_ROOMS, location);
	}

	public boolean deleteCustomer(int customerID) throws RemoteException
	{
		return (Boolean)invoke(Operation.DELETE_CUSTOMER, customerID);
	}

	public int queryFlight(int flightNumber) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_FLIGHT, flightNumber);
	}

	public int queryCars(String location) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_CARS, location);
	}

	public int queryRooms(String location) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_ROOMS, location);
	}

	public String queryCustomerInfo(int customerID) throws RemoteException
	{
		return (String)invoke(Operation.QUERY_CUSTOMER_INFO, customerID);
	}

	public int queryFlightPrice(int flightNumber) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_FLIGHT_PRICE, flightNumber);
	}

	public int queryCarsPrice(String location) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_CARS_PRICE, location);
	}

	public int queryRoomsPrice(String location) throws RemoteException
	{
		return (Integer)invoke(Operation.QUERY_ROOMS_PRICE, location);
	}

	public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException
	{
		return (Boolean)invoke(Operation.RESERVE_FLIGHT, customerID, flightNumber);
	}

	public boolean reserveCar(int customerID, String location) throws RemoteException
	{
		return (Boolean)invoke(Operation.RESERVE_CAR, customerID, location);
	}

	public boolean reserveRoom(int customerID, String location) throws RemoteException
	{
		return (Boolean)invoke(Operation.RESERVE_ROOM, customerID, location);
	}

	public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		return (Boolean)invoke(Operation.BUNDLE, customerID, flightNumbers, location, car, room);
	}

	public String getName() throws RemoteException
	{
		return (String)invoke(Operation.GET_NAME);
	}

	public int reserveInventory(String key, int count) throws RemoteException
	{
		return (Integer)invoke(Operation.RESERVE_INVENTORY, key, count);
	}

	public boolean releaseInventory(String key, int count) throws RemoteException
	{
		return (Boolean)invoke(Operation.RELEASE_INVENTORY, key, count);
	}
}
