package Manager;

import OrderGenerator.IOrderGenerator;
import OrderGenerator.OrderGeneratorFactory;
import OrderType.Order;
import Storage.IStorage;
import Storage.ShelfStorage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderManager {
    private IOrderGenerator orderGenerator;
    IStorage storage;
    private void createOrderGenerator() {
        orderGenerator = OrderGeneratorFactory.createOrderInstance();
    }

    public void StartProcessingOrders(int ordersPerSecond) {
        createOrderGenerator();
        if(orderGenerator == null) {
            System.out.println("OrderGenerator is null");
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        IStorage storage = new ShelfStorage();
        ProcessOrders processOrders = new ProcessOrders(storage, orderGenerator, ordersPerSecond);
        scheduledExecutorService.scheduleAtFixedRate(processOrders, 0,1, TimeUnit.SECONDS);

        UpdateOrders updateOrders = new UpdateOrders(storage);
        scheduledExecutorService.scheduleAtFixedRate(
                updateOrders,
                1
                ,calculateUpdateOrdersTimer(ordersPerSecond),
                TimeUnit.SECONDS);
    }

    //Calculate how frequently the orders must be updated
    public long calculateUpdateOrdersTimer(int ordersPerSecond) {
        long interval = 10/ordersPerSecond;
        return interval;
    }
    public void DispatchCourierForAnOrder(Order order) {

    }
}
