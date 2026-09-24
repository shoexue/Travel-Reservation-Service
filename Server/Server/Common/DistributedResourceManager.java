package Server.Common;

import Server.Interface.IInventoryResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/** Transport-independent middleware implementation shared by RMI and TCP. */
public class DistributedResourceManager extends ResourceManager
{
	private final ResourceEndpoint m_flights;
	private final ResourceEndpoint m_cars;
	private final ResourceEndpoint m_rooms;

	private static class ResourceEndpoint
	{
		final IResourceManager manager;
		final IInventoryResourceManager inventory;

		ResourceEndpoint(IResourceManager manager, IInventoryResourceManager inventory)
		{
			this.manager = manager;
			this.inventory = inventory;
		}
	}

	private static class PendingReservation
	{
		final ResourceEndpoint endpoint;
		final String key;
		final String location;
		final int count;
		int price = -1;

		PendingReservation(ResourceEndpoint endpoint, String key, String location, int count)
		{
			this.endpoint = endpoint;
			this.key = key;
			this.location = location;
			this.count = count;
		}
	}

	public DistributedResourceManager(
		String name,
		IResourceManager flightManager,
		IInventoryResourceManager flightInventory,
		IResourceManager carManager,
		IInventoryResourceManager carInventory,
		IResourceManager roomManager,
		IInventoryResourceManager roomInventory)
	{
		super(name);
		m_flights = new ResourceEndpoint(flightManager, flightInventory);
		m_cars = new ResourceEndpoint(carManager, carInventory);
		m_rooms = new ResourceEndpoint(roomManager, roomInventory);
	}

	public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		return m_flights.manager.addFlight(flightNum, flightSeats, flightPrice);
	}

	public boolean addCars(String location, int numCars, int price) throws RemoteException
	{
		return m_cars.manager.addCars(location, numCars, price);
	}

	public boolean addRooms(String location, int numRooms, int price) throws RemoteException
	{
		return m_rooms.manager.addRooms(location, numRooms, price);
	}

	public boolean deleteFlight(int flightNum) throws RemoteException
	{
		return m_flights.manager.deleteFlight(flightNum);
	}

	public boolean deleteCars(String location) throws RemoteException
	{
		return m_cars.manager.deleteCars(location);
	}

	public boolean deleteRooms(String location) throws RemoteException
	{
		return m_rooms.manager.deleteRooms(location);
	}

	public int queryFlight(int flightNum) throws RemoteException
	{
		return m_flights.manager.queryFlight(flightNum);
	}

	public int queryCars(String location) throws RemoteException
	{
		return m_cars.manager.queryCars(location);
	}

	public int queryRooms(String location) throws RemoteException
	{
		return m_rooms.manager.queryRooms(location);
	}

	public int queryFlightPrice(int flightNum) throws RemoteException
	{
		return m_flights.manager.queryFlightPrice(flightNum);
	}

	public int queryCarsPrice(String location) throws RemoteException
	{
		return m_cars.manager.queryCarsPrice(location);
	}

	public int queryRoomsPrice(String location) throws RemoteException
	{
		return m_rooms.manager.queryRoomsPrice(location);
	}

	private boolean reserve(ResourceEndpoint endpoint, int customerID, String key, String location) throws RemoteException
	{
		synchronized (m_data)
		{
			Customer customer = (Customer)readData(Customer.getKey(customerID));
			if (customer == null)
			{
				return false;
			}

			int price = endpoint.inventory.reserveInventory(key, 1);
			if (price < 0)
			{
				return false;
			}

			customer.reserve(key, location, price);
			writeData(customer.getKey(), customer);
			return true;
		}
	}

	public boolean reserveFlight(int customerID, int flightNum) throws RemoteException
	{
		return reserve(m_flights, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}

	public boolean reserveCar(int customerID, String location) throws RemoteException
	{
		return reserve(m_cars, customerID, Car.getKey(location), location);
	}

	public boolean reserveRoom(int customerID, String location) throws RemoteException
	{
		return reserve(m_rooms, customerID, Room.getKey(location), location);
	}

	public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		synchronized (m_data)
		{
			Customer customer = (Customer)readData(Customer.getKey(customerID));
			if (customer == null || flightNumbers == null || flightNumbers.isEmpty())
			{
				return false;
			}

			LinkedHashMap<Integer, Integer> flightCounts = new LinkedHashMap<Integer, Integer>();
			try
			{
				for (String flightNumber : flightNumbers)
				{
					int number = Integer.parseInt(flightNumber);
					Integer count = flightCounts.get(number);
					flightCounts.put(number, count == null ? 1 : count + 1);
				}
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			List<PendingReservation> pending = new ArrayList<PendingReservation>();
			for (Map.Entry<Integer, Integer> entry : flightCounts.entrySet())
			{
				pending.add(new PendingReservation(m_flights, Flight.getKey(entry.getKey()), String.valueOf(entry.getKey()), entry.getValue()));
			}
			if (car)
			{
				pending.add(new PendingReservation(m_cars, Car.getKey(location), location, 1));
			}
			if (room)
			{
				pending.add(new PendingReservation(m_rooms, Room.getKey(location), location, 1));
			}

			List<PendingReservation> completed = new ArrayList<PendingReservation>();
			for (PendingReservation reservation : pending)
			{
				reservation.price = reservation.endpoint.inventory.reserveInventory(reservation.key, reservation.count);
				if (reservation.price < 0)
				{
					rollback(completed);
					return false;
				}
				completed.add(reservation);
			}

			for (PendingReservation reservation : completed)
			{
				for (int i = 0; i < reservation.count; ++i)
				{
					customer.reserve(reservation.key, reservation.location, reservation.price);
				}
			}
			writeData(customer.getKey(), customer);
			return true;
		}
	}

	private void rollback(List<PendingReservation> reservations) throws RemoteException
	{
		for (int i = reservations.size() - 1; i >= 0; --i)
		{
			PendingReservation reservation = reservations.get(i);
			if (!reservation.endpoint.inventory.releaseInventory(reservation.key, reservation.count))
			{
				throw new RemoteException("Unable to roll back reservation for " + reservation.key);
			}
		}
	}

	public boolean deleteCustomer(int customerID) throws RemoteException
	{
		synchronized (m_data)
		{
			Customer customer = (Customer)readData(Customer.getKey(customerID));
			if (customer == null)
			{
				return false;
			}

			List<PendingReservation> released = new ArrayList<PendingReservation>();
			RMHashMap reservations = customer.getReservations();
			for (String key : reservations.keySet())
			{
				ReservedItem item = customer.getReservedItem(key);
				PendingReservation reservation = new PendingReservation(endpointForKey(key), key, item.getLocation(), item.getCount());
				if (!reservation.endpoint.inventory.releaseInventory(key, reservation.count))
				{
					restore(released);
					return false;
				}
				released.add(reservation);
			}

			removeData(customer.getKey());
			return true;
		}
	}

	private void restore(List<PendingReservation> reservations) throws RemoteException
	{
		for (PendingReservation reservation : reservations)
		{
			if (reservation.endpoint.inventory.reserveInventory(reservation.key, reservation.count) < 0)
			{
				throw new RemoteException("Unable to restore reservation for " + reservation.key);
			}
		}
	}

	private ResourceEndpoint endpointForKey(String key) throws RemoteException
	{
		if (key.startsWith("flight-"))
		{
			return m_flights;
		}
		if (key.startsWith("car-"))
		{
			return m_cars;
		}
		if (key.startsWith("room-"))
		{
			return m_rooms;
		}
		throw new RemoteException("Unknown reservation key " + key);
	}
}
