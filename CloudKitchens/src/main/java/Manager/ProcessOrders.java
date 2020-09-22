package Manager;

import courier.IDispatchCouriers;
import OrderGenerator.IOrderGenerator;
import OrderType.Order;
import Storage.IStorage;
import Utils.TimeFormat;

import java.util.logging.Logger;

class ProcessOrders implements Runnable {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    IStorage storage;
    IOrderGenerator orderGenerator;
    int ordersToBeProcessed;
    IDispatchCouriers dispatchCouriers;
    OrderManager.IProcessOrdersThread processOrdersThread;


    public ProcessOrders(
            IStorage storage,
            IOrderGenerator orderGenerator,
            int ordersToBeProcessed,
            IDispatchCouriers dispatchCouriers,
            OrderManager.IProcessOrdersThread processOrdersThread) {
        this.storage = storage;
        this.orderGenerator = orderGenerator;
        this.ordersToBeProcessed = ordersToBeProcessed;
        this.dispatchCouriers = dispatchCouriers;
        this.processOrdersThread = processOrdersThread;
    }

    @Override
    public void run() {
        int num = 0;
        while (orderGenerator.hasNextOrder() && num != ordersToBeProcessed) {
            Order order = orderGenerator.getNextOrder();
            storage.addOrderToStorage(order);
            num++;
            dispatchCouriers.dispatchCouriers(order);
        }
        if(!orderGenerator.hasNextOrder()) {
            processOrdersThread.killProcessOrderThread();
        }
    }
}
