package Storage;

import OrderType.Order;


public interface IStorage {
    void addOrderToStorage(Order order);
    void markOrderDelivered(Order order);
}
