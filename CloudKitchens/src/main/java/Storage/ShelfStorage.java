package Storage;

import OrderType.Order;
import OrderType.OrderComparator;
import Utils.TimeFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ShelfStorage implements IStorage {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private enum SHELF {
        HOT(10, 1),
        COLD(10, 1),
        FROZEN(10, 1),
        OVERFLOW(15, 2);

        private final int capacity;
        private final int shelfDecayModifier;

        SHELF(int capacity, int shelfDecayModifier) {
            this.capacity = capacity;
            this.shelfDecayModifier = shelfDecayModifier;
        }

        public int getShelfCapacity() {
            return capacity;
        }

        public int getShelfDecayModifier() {
            return shelfDecayModifier;
        }
    }

    private AtomicBoolean startAThread = new AtomicBoolean(true);
    private ScheduledExecutorService scheduledExecutorService;
    private Map<SHELF, Queue<Order>> mShelfMap = new ConcurrentHashMap<>();
    private Map<String, Order> mGlobalOrdersMap = new ConcurrentHashMap<>();

    private void setOrderExpiry(Order order, SHELF shelf) {
        double value = (order.getShelfLife() - order.getOrderAge() - order.getDecayRate() * order.getOrderAge() * shelf.getShelfDecayModifier())/order.getShelfLife();
        order.setValue(value);
        double orderAgeAllowed = order.getShelfLife()/(1 - order.getDecayRate()* shelf.getShelfDecayModifier());
        order.setExpiry(order.getCreateTime() + orderAgeAllowed);
        LOGGER.info("Order expiry " + order.getExpiry());
        LOGGER.info("Order " + order.getName() +
                " with id " + order.getId() +
                " has a order value " + order.getValue());
    }

    @Override
    public void addOrderToStorage(Order order) {
        if(startAThread.get() == true) {
            startAThread.set(false);
            checkExpiredOrders();
        }
        SHELF shelf = SHELF.valueOf(order.getTemp());
        LOGGER.info("Shelf: " + shelf);
        order.setCreateTime(System.currentTimeMillis());
        setOrderExpiry(order, shelf);
        addOrderToGlobalOrdersMap(order);

        if(canAddOrderToShelf(shelf)) {
            addOrderToShelf(order, shelf);
        }
        else {
            shelf = SHELF.OVERFLOW;
            if(canAddOrderToShelf(shelf)) {
                addOrderToShelf(order, shelf);
            }
            else {
                removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(shelf);
            }
        }
        LOGGER.info("Order " + order.getName() +
                " with id " + order.getId() +
                " with an order value " + order.getValue() +
                " received at " + TimeFormat.systemToSimpleDateFormat(order.getCreateTime()) +
                " and added to shelf " + shelf);
    }

    @Override
    public void markOrderDelivered(Order order) {
        if(order.getExpiry() - System.currentTimeMillis() < 0.0) {
            LOGGER.info("Order " + order.getName() +
                    " with id " + order.getId() +
                    " with an order value " + order.getValue() +
                    " discarded as order was expired at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
        }
        else {
            LOGGER.info("Order " + order.getName() +
                    " with id " + order.getId() +
                    " with an order value " + order.getValue() +
                    " delivered at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                    " from shelf " + SHELF.valueOf(order.getTemp()));
        }

        mGlobalOrdersMap.remove(order);
        order.setExpiry(System.currentTimeMillis());
    }

    private void addOrderToGlobalOrdersMap(Order order) {
        mGlobalOrdersMap.put(order.getId(), order);
    }

    private boolean canAddOrderToShelf(SHELF shelf) {
        return mShelfMap.get(shelf) == null || mShelfMap.get(shelf).size() < shelf.getShelfCapacity();
    }

    private void addOrderToShelf(Order order, SHELF shelf) {
        if(mShelfMap.get(shelf) == null) {
            mShelfMap.put(shelf, new PriorityQueue<>(shelf.getShelfCapacity(), new OrderComparator()));
        }
        if(mShelfMap.get(shelf).size() < shelf.getShelfCapacity()) {
            mShelfMap.get(shelf).add(order);
        }
    }

    private void removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(SHELF shelf) {
        boolean discardOrder = true;
        Iterator iterator = mShelfMap.get(shelf).iterator();
        while (iterator.hasNext()) {
            Order orderToBeRemoved = (Order) iterator.next();
            SHELF regularShelf = SHELF.valueOf(orderToBeRemoved.getTemp());
            if(mShelfMap.get(regularShelf).size() < regularShelf.getShelfCapacity()) {

                LOGGER.info("Order " + orderToBeRemoved.getName() +
                        " with id " + orderToBeRemoved.getId() +
                        " with an order value " + orderToBeRemoved.getValue() +
                        " removed at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                        " from shelf " + shelf +
                        " and put into shelf " + regularShelf);

                mShelfMap.get(regularShelf).add(orderToBeRemoved);
                iterator.remove();
                discardOrder = false;
            }
        }
        if(discardOrder) {
            discardOrderFromOverflowShelf();
        }
    }

    private void discardOrderFromOverflowShelf() {
        SHELF shelf = SHELF.OVERFLOW;
        Order orderToBeDiscarded = mShelfMap.get(shelf).peek();

        LOGGER.info("Order " + orderToBeDiscarded.getName() +
                " with id " + orderToBeDiscarded.getId() +
                " with an order value " + orderToBeDiscarded.getValue() +
                " discarded at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                " from shelf " + shelf +
                " as no allowable shelf available");

        mGlobalOrdersMap.remove(orderToBeDiscarded.getId());
        mShelfMap.get(shelf).poll();
    }

    private void checkExpiredOrders() {
        LOGGER.severe("Started a thread " + Thread.currentThread().getName() +
                " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                " to check if any orders have expired");

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable myRunnable = () -> discardExpiredOrders();
        scheduledExecutorService.scheduleWithFixedDelay(myRunnable, 0,2000, TimeUnit.MILLISECONDS);
    }

    private void discardExpiredOrders() {
        for(Map.Entry entry : mShelfMap.entrySet()) {
            Iterator iterator = ((PriorityQueue<?>) entry.getValue()).iterator();
            while (iterator.hasNext()) {
                Order orderToBeDiscarded = (Order) iterator.next();
                if(orderToBeDiscarded.getExpiry() - System.currentTimeMillis() < 0.0) {

                    LOGGER.info("Order " + orderToBeDiscarded.getName() +
                            " with id " + orderToBeDiscarded.getId() +
                            " with an order value " + orderToBeDiscarded.getValue() +
                            " discarded as order was expired at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                            " from shelf " + entry.getKey());

                    iterator.remove();
                    mGlobalOrdersMap.remove(orderToBeDiscarded.getId());
                }
            }
        }

        if(mGlobalOrdersMap.size() == 0) {
            LOGGER.severe("Closing the thread haha" + Thread.currentThread().getName() +
                    " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
        }
    }
}
