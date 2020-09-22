package Manager;

import Courier.DispatchCouriers;
import Courier.ProcessCouriers;
import OrderGenerator.IOrderGenerator;
import OrderGenerator.OrderGeneratorFactory;
import OrderType.Order;
import Storage.IStorage;
import Storage.StorageFactory;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OrderManager {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IOrderGenerator mOrderGenerator;
    private IStorage mStorage;

    private void createOrderGenerator() {
        mOrderGenerator = OrderGeneratorFactory.createOrderInstance("Json");
    }

    private void createStorage() {
        mStorage = StorageFactory.createStorage("Shelf");
    }

    public void startProcessingOrders(int numberOfOrders, int timeIntervalInMillis) {
        LOGGER.info("Start processing orders");
        createOrderGenerator();
        if(mOrderGenerator == null)
            return;;
        createStorage();
        if(mStorage == null)
            return;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

        DispatchCouriers dispatchCouriers = (Order order) -> {
            Random random = new Random();
            int lower = 2000, upper = 6000;
            int pickupOrderTimeInMillis = random.nextInt(upper) + lower;
            LOGGER.info("Dispatch Couriers");
            ProcessCouriers processCouriers = new ProcessCouriers(mStorage, order);
            scheduledExecutorService.schedule(processCouriers, pickupOrderTimeInMillis, TimeUnit.MILLISECONDS);
        };

        ProcessOrders processOrders = new ProcessOrders(mStorage,
                mOrderGenerator,
                numberOfOrders, dispatchCouriers);

        scheduledExecutorService.scheduleAtFixedRate(processOrders, 0, timeIntervalInMillis, TimeUnit.MILLISECONDS);
    }
}
