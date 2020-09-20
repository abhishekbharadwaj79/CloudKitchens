package Storage;

import OrderType.Order;

public interface IStorage {
    public Order getOrderFromStorage(Order order);
    public void addOrderToStorage(Order order);
}
