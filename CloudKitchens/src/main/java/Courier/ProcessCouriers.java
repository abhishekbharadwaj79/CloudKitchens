package courier;

import OrderType.Order;
import Storage.IStorage;
import Utils.TimeFormat;

import java.util.logging.Logger;

public class ProcessCouriers implements Runnable {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    IStorage mStorage;
    Order mOrder;

    public ProcessCouriers(IStorage storage, Order order) {
        this.mStorage = storage;
        this.mOrder = order;
    }

    @Override
    public void run() {
        LOGGER.info("Order " + mOrder.getName() +
                " with id " + mOrder.getId() +
                " with an order value " + mOrder.getValue() +
                " picked up at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));

        mStorage.markOrderDelivered(mOrder);
    }
}
