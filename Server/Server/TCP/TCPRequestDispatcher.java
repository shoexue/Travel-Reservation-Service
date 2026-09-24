package Server.TCP;

import Server.Interface.IInventoryResourceManager;
import Server.Interface.IResourceManager;
import Server.Interface.TCPRequest;
import Server.Interface.TCPResponse;

import java.util.List;
import java.util.Vector;

/** Maps every general TCP request to the corresponding service method. */
public class TCPRequestDispatcher
{
	private final IResourceManager m_manager;
	private final IInventoryResourceManager m_inventory;

	public TCPRequestDispatcher(IResourceManager manager, IInventoryResourceManager inventory)
	{
		m_manager = manager;
		m_inventory = inventory;
	}

	@SuppressWarnings("unchecked")
	public TCPResponse dispatch(TCPRequest request)
	{
		try
		{
			List<Object> args = request.getArguments();
			Object result;
			switch (request.getOperation())
			{
				case ADD_FLIGHT:
					result = m_manager.addFlight(integer(args, 0), integer(args, 1), integer(args, 2));
					break;
				case ADD_CARS:
					result = m_manager.addCars(string(args, 0), integer(args, 1), integer(args, 2));
					break;
				case ADD_ROOMS:
					result = m_manager.addRooms(string(args, 0), integer(args, 1), integer(args, 2));
					break;
				case NEW_CUSTOMER:
					result = m_manager.newCustomer();
					break;
				case NEW_CUSTOMER_ID:
					result = m_manager.newCustomer(integer(args, 0));
					break;
				case DELETE_FLIGHT:
					result = m_manager.deleteFlight(integer(args, 0));
					break;
				case DELETE_CARS:
					result = m_manager.deleteCars(string(args, 0));
					break;
				case DELETE_ROOMS:
					result = m_manager.deleteRooms(string(args, 0));
					break;
				case DELETE_CUSTOMER:
					result = m_manager.deleteCustomer(integer(args, 0));
					break;
				case QUERY_FLIGHT:
					result = m_manager.queryFlight(integer(args, 0));
					break;
				case QUERY_CARS:
					result = m_manager.queryCars(string(args, 0));
					break;
				case QUERY_ROOMS:
					result = m_manager.queryRooms(string(args, 0));
					break;
				case QUERY_CUSTOMER_INFO:
					result = m_manager.queryCustomerInfo(integer(args, 0));
					break;
				case QUERY_FLIGHT_PRICE:
					result = m_manager.queryFlightPrice(integer(args, 0));
					break;
				case QUERY_CARS_PRICE:
					result = m_manager.queryCarsPrice(string(args, 0));
					break;
				case QUERY_ROOMS_PRICE:
					result = m_manager.queryRoomsPrice(string(args, 0));
					break;
				case RESERVE_FLIGHT:
					result = m_manager.reserveFlight(integer(args, 0), integer(args, 1));
					break;
				case RESERVE_CAR:
					result = m_manager.reserveCar(integer(args, 0), string(args, 1));
					break;
				case RESERVE_ROOM:
					result = m_manager.reserveRoom(integer(args, 0), string(args, 1));
					break;
				case BUNDLE:
					if (!(args.get(1) instanceof Vector))
					{
						throw new IllegalArgumentException("Bundle flights must be a Vector");
					}
					result = m_manager.bundle(integer(args, 0), (Vector<String>)args.get(1), string(args, 2), bool(args, 3), bool(args, 4));
					break;
				case GET_NAME:
					result = m_manager.getName();
					break;
				case RESERVE_INVENTORY:
					requireInventory();
					result = m_inventory.reserveInventory(string(args, 0), integer(args, 1));
					break;
				case RELEASE_INVENTORY:
					requireInventory();
					result = m_inventory.releaseInventory(string(args, 0), integer(args, 1));
					break;
				default:
					throw new IllegalArgumentException("Unsupported operation " + request.getOperation());
			}
			return TCPResponse.success(request.getRequestId(), result);
		}
		catch (Exception e)
		{
			String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return TCPResponse.failure(request.getRequestId(), message);
		}
	}

	private void requireInventory()
	{
		if (m_inventory == null)
		{
			throw new IllegalArgumentException("Inventory operation is not available on this server");
		}
	}

	private static int integer(List<Object> args, int index)
	{
		return ((Integer)args.get(index)).intValue();
	}

	private static String string(List<Object> args, int index)
	{
		return (String)args.get(index);
	}

	private static boolean bool(List<Object> args, int index)
	{
		return ((Boolean)args.get(index)).booleanValue();
	}
}
