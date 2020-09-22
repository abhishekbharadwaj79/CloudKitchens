package Storage;

import OrderType.Order;


public interface IStorage {
    /**
     *
     * Checks if orders can be added to the storage, if yes then
     * adds them to storage or else discards them
     */
    void addOrderToStorage(Order order);

    /**
     *
     * Checks if orders can be delivered. If yes then removes it from the storage
     * or else discards them
     */
    void markOrderDelivered(Order order);
}
