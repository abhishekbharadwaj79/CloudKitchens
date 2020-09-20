package Manager;

import OrderGenerator.IOrderGenerator;
import OrderGenerator.OrderGeneratorFactory;
import OrderType.Order;
import Storage.IStorage;
import Storage.ShelfStorage;


public class OrderManager {
    private IOrderGenerator orderGenerator;
    IStorage storage;
    private void createOrderGenerator() {
        orderGenerator = OrderGeneratorFactory.createOrderInstance();
    }

    public void StartProcessingOrders() {
        createOrderGenerator();
        if(orderGenerator == null) {
            System.out.println("OrderGenerator is null");
        }
        ShelfStorage shelfStorage = new ShelfStorage();
        while(orderGenerator.hasNextOrder()) {
            Order order = orderGenerator.getNextOrder();
            order.setCreateTime(System.currentTimeMillis());
            shelfStorage.addOrderToStorage(order);
        }
    }

    public void DispatchCourierForAOrder(Order order) {

    }
}
