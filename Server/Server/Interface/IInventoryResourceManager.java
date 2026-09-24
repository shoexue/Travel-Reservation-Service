package Server.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Internal interface used by the middleware to update inventory atomically.
 * Clients continue to use IResourceManager.
 */
public interface IInventoryResourceManager extends Remote
{
	/**
	 * Reserve count units of an inventory item.
	 *
	 * @return the unit price, or -1 when the item is unavailable
	 */
	public int reserveInventory(String key, int count) throws RemoteException;

	/**
	 * Return count previously reserved units to inventory.
	 *
	 * @return true when the inventory was restored
	 */
	public boolean releaseInventory(String key, int count) throws RemoteException;
}
