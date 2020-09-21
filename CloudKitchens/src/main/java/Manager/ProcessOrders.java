package Manager;

import OrderGenerator.IOrderGenerator;
import OrderType.Order;
import Storage.IStorage;
import Storage.ShelfStorage;

public class ProcessOrders implements Runnable {
    IStorage storage;
    IOrderGenerator orderGenerator;
    int ordersPerSecond;

    public ProcessOrders(IStorage storage, IOrderGenerator orderGenerator, int ordersPerSecond) {
        this.storage = new ShelfStorage();
        this.orderGenerator = orderGenerator;
        this.ordersPerSecond = ordersPerSecond;
    }

    @Override
    public void run() {
        int num = 0;
        while(orderGenerator.hasNextOrder() && num != ordersPerSecond) {
            Order order = orderGenerator.getNextOrder();
            order.setCreateTime(System.currentTimeMillis());
            storage.addOrderToStorage(order);
            num++;
        }
    }
}
