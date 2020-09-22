package Manager;

import Courier.DispatchCouriers;
import OrderGenerator.IOrderGenerator;
import OrderType.Order;
import Storage.IStorage;
import Utils.TimeFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

class ProcessOrders implements Runnable {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    IStorage storage;
    IOrderGenerator orderGenerator;
    int ordersToBeProcessed;
    DispatchCouriers dispatchCouriers;


    public ProcessOrders(IStorage storage, IOrderGenerator orderGenerator, int ordersToBeProcessed, DispatchCouriers dispatchCouriers) {
        this.storage = storage;
        this.orderGenerator = orderGenerator;
        this.ordersToBeProcessed = ordersToBeProcessed;
        this.dispatchCouriers = dispatchCouriers;
    }

    @Override
    public void run() {
        LOGGER.info("Started a thread to process orders " + Thread.currentThread().getName() +
                " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
        int num = 0;
        Instant start = Instant.now();
        while(orderGenerator.hasNextOrder() && num != ordersToBeProcessed) {
            Order order = orderGenerator.getNextOrder();
            storage.addOrderToStorage(order);
            num++;
            dispatchCouriers.dispatchCouriers(order);
        }
        Instant end = Instant.now();
        LOGGER.info("Time to process " + ordersToBeProcessed + " orders in nanoseconds" + Duration.between(start, end).getNano());
    }
}
