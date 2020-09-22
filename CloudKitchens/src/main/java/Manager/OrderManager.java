package Manager;

import Utils.TimeFormat;
import courier.IDispatchCouriers;
import courier.ProcessCouriers;
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
        if (mOrderGenerator == null)
            return;
        ;
        createStorage();
        if (mStorage == null)
            return;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

        IDispatchCouriers dispatchCouriers = (Order order) -> {
            Random random = new Random();
            int lower = 2000, upper = 6000;
            int pickupOrderTimeInMillis = random.nextInt(upper) + lower;
            ProcessCouriers processCouriers = new ProcessCouriers(mStorage, order);
            scheduledExecutorService.schedule(processCouriers, pickupOrderTimeInMillis, TimeUnit.MILLISECONDS);
        };

        IProcessOrdersThread processOrdersThread = () -> {
            LOGGER.severe("Shutting down " + scheduledExecutorService +
                    " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
            scheduledExecutorService.shutdown();
        };

        ProcessOrders processOrders = new ProcessOrders(mStorage,
                mOrderGenerator,
                numberOfOrders,
                dispatchCouriers,
                processOrdersThread);

        scheduledExecutorService.scheduleWithFixedDelay(processOrders, 0, timeIntervalInMillis, TimeUnit.MILLISECONDS);
    }

    interface IProcessOrdersThread {
        void killProcessOrderThread();
    }
}
