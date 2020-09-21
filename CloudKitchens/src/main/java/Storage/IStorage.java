package Storage;

import OrderType.Order;

import java.util.Map;
import java.util.PriorityQueue;

public interface IStorage {
    public void updateStorage();
    public void addOrderToStorage(Order order);
}
