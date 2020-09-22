package Courier;

import Courier.DispatchCouriers;
import OrderGenerator.IOrderGenerator;
import OrderType.Order;
import Storage.IStorage;
import Utils.TimeFormat;

import java.util.logging.Logger;

public class ProcessCouriers implements Runnable {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    IStorage storage;
    Order order;

    public ProcessCouriers(IStorage storage, Order order) {
        this.storage = storage;
        this.order = order;
    }

    @Override
    public void run() {
        LOGGER.info("Order " + order.getName() +
                " with id " + order.getId() +
                " with an order value " + order.getValue() +
                " picked up at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));

        storage.markOrderDelivered(order);
    }
}
