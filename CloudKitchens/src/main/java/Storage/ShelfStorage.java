package Storage;

import OrderType.Order;
import Utils.TimeFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private AtomicBoolean mStartAThread = new AtomicBoolean(true);
    private AtomicBoolean mCanShutdownAThread = new AtomicBoolean(false);
    private ScheduledExecutorService mScheduledExecutorService;
    private Map<SHELF, Queue<Order>> mShelfMap = new ConcurrentHashMap<>();
    private AtomicInteger mOrdercount = new AtomicInteger(0);

    @Override
    public void addOrderToStorage(Order order) {
        if (mStartAThread.getAndSet(false)) {
            checkExpiredOrders();
        }
        SHELF shelf = SHELF.valueOf(order.getTemp().toUpperCase());
        order.setCreateTime(System.currentTimeMillis());
        setOrderExpiry(order, shelf);

        if (canAddOrderToShelf(shelf)) {
            addOrderToShelf(order, shelf);
        } else {
            shelf = SHELF.OVERFLOW;
            if (canAddOrderToShelf(shelf)) {
                addOrderToShelf(order, shelf);
            } else {
                removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(shelf);
            }
        }
    }

    @Override
    public void markOrderDelivered(Order order) {

        if (order.getExpiry() - System.currentTimeMillis() < 0.0) {
            LOGGER.info("Order " + order.getName() +
                    " with id " + order.getId() +
                    " with an order value " + order.getValue() +
                    " discarded as order was expired at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
        }
        else {
            SHELF shelf = SHELF.valueOf(order.getTemp());
            if(mShelfMap.get(shelf).contains(order))
            {
                setOrderValue(order, shelf);
                mShelfMap.get(shelf).remove(order);
                mOrdercount.getAndDecrement();
                LOGGER.info("Order " + order.getName() +
                        " with id " + order.getId() +
                        " with an order value " + order.getValue() +
                        " delivered at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                        " from shelf " + shelf +
                        printAllelementsOfQueue(mShelfMap.get(shelf)));
            }
            else {
                setOrderValue(order, shelf);
                shelf = SHELF.OVERFLOW;
                if (mShelfMap.get(shelf).contains(order))
                {
                    mShelfMap.get(SHELF.OVERFLOW).remove(order);
                    mOrdercount.getAndDecrement();
                    LOGGER.info("Order " + order.getName() +
                            " with id " + order.getId() +
                            " with an order value " + order.getValue() +
                            " delivered at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                            " from shelf " + shelf +
                            printAllelementsOfQueue(mShelfMap.get(shelf)));
                }
                else {
                    LOGGER.info("Order " + order.getName() +
                            " with id " + order.getId() +
                            " with an order value " + order.getValue() +
                            " picked up to deliver but has already been discarded");
                }
            }
        }

    }

    private void setOrderExpiry(Order order, SHELF shelf) {
        setOrderValue(order, shelf);
        double orderAgeAllowed = order.getShelfLife() / (1 - order.getDecayRate() * shelf.getShelfDecayModifier());
        order.setExpiry(order.getCreateTime() + orderAgeAllowed*1000);
        LOGGER.info("Order " + order.getName() +
                " with id " + order.getId() +
                " has a order value " + order.getValue());
    }

    private void setOrderValue(Order order, SHELF shelf) {
        double value = (order.getShelfLife() - order.getOrderAge() - order.getDecayRate() * order.getOrderAge() * shelf.getShelfDecayModifier()) / order.getShelfLife();
        order.setValue(value);
    }

    private boolean canAddOrderToShelf(SHELF shelf) {
        return mShelfMap.get(shelf) == null || mShelfMap.get(shelf).size() < shelf.getShelfCapacity();
    }

    private void addOrderToShelf(Order order, SHELF shelf) {
        if (mShelfMap.get(shelf) == null) {
            mShelfMap.put(shelf, new LinkedList<>());
        }
        if (mShelfMap.get(shelf).size() < shelf.getShelfCapacity()) {
            mOrdercount.getAndIncrement();
            LOGGER.info("Order " + order.getName() +
                    " with id " + order.getId() +
                    " with an order value " + order.getValue() +
                    " received at " + TimeFormat.systemToSimpleDateFormat(order.getCreateTime()) +
                    " and added to shelf " + shelf +
                    printAllelementsOfQueue(mShelfMap.get(shelf)));

            mShelfMap.get(shelf).add(order);
            mCanShutdownAThread.set(true);
        }
    }

    private String printAllelementsOfQueue(Queue<Order> queue) {
        int count = 0;
        String str = "has the following orders\n";
        for(Order order : queue) {
            str += count + ": " + order.getId() + "\n";
            count++;
        }
        return str;
    }

    private void removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(SHELF shelf) {
        boolean discardOrder = true;
        Iterator iterator = mShelfMap.get(shelf).iterator();
        while (iterator.hasNext()) {
            Order orderToBeRemoved = (Order) iterator.next();
            SHELF regularShelf = SHELF.valueOf(orderToBeRemoved.getTemp());
            if (mShelfMap.get(regularShelf).size() < regularShelf.getShelfCapacity()) {
                setOrderValue(orderToBeRemoved, shelf);
                LOGGER.info("Order " + orderToBeRemoved.getName() +
                        " with id " + orderToBeRemoved.getId() +
                        " with an order value " + orderToBeRemoved.getValue() +
                        " removed at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                        " from shelf " + shelf +
                        " and put into shelf " + regularShelf +
                        printAllelementsOfQueue(mShelfMap.get(shelf)));

                setOrderExpiry(orderToBeRemoved, regularShelf);
                mShelfMap.get(regularShelf).add(orderToBeRemoved);
                iterator.remove();
                discardOrder = false;
            }
        }
        if (discardOrder) {
            discardOrderFromOverflowShelf();
        }
    }

    private void discardOrderFromOverflowShelf() {
        SHELF shelf = SHELF.OVERFLOW;
        Order orderToBeDiscarded = mShelfMap.get(shelf).peek();
        setOrderValue(orderToBeDiscarded, shelf);

        LOGGER.info("Order " + orderToBeDiscarded.getName() +
                " with id " + orderToBeDiscarded.getId() +
                " with an order value " + orderToBeDiscarded.getValue() +
                " discarded at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                " from shelf " + shelf +
                " as no allowable shelf available"+
                printAllelementsOfQueue(mShelfMap.get(shelf)));

        mShelfMap.get(shelf).remove();
        mOrdercount.getAndDecrement();
    }

    /**
     * Creates a thread which constantly runs every 2 seconds to check if there are any expired orders
     * If yes then removes them from the corresponding shelf
     */
    private void checkExpiredOrders() {
        LOGGER.severe("Started a thread " + Thread.currentThread().getName() +
                " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                " to check if any orders have expired");

        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable myRunnable = () -> discardExpiredOrders();
        mScheduledExecutorService.scheduleWithFixedDelay(myRunnable, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void discardExpiredOrders() {
        int count = 0;
        for (Map.Entry entry : mShelfMap.entrySet()) {
            count += mShelfMap.get(entry.getKey()).size();
            Iterator iterator = ((LinkedList<?>) entry.getValue()).iterator();
            while (iterator.hasNext()) {
                Order orderToBeDiscarded = (Order) iterator.next();
                if (orderToBeDiscarded.getExpiry() - System.currentTimeMillis() < 0.0) {
                    setOrderValue(orderToBeDiscarded, (SHELF) entry.getKey());
                    LOGGER.info("Order " + orderToBeDiscarded.getName() +
                            " with id " + orderToBeDiscarded.getId() +
                            " with an order value " + orderToBeDiscarded.getValue() +
                            " was discarded as order was expired at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()) +
                            " from shelf " + entry.getKey()+
                            printAllelementsOfQueue(mShelfMap.get(entry.getKey())));

                    iterator.remove();
                    mOrdercount.getAndDecrement();
                }
            }
        }
        /**
         * Shut down the thread if there are no orders to be processed in the map
         * and at least one order was added to the map
         */
        if(count == 0 && mCanShutdownAThread.get() == true) {
            LOGGER.severe("Shutting down " + mScheduledExecutorService +
                    " at " + TimeFormat.systemToSimpleDateFormat(System.currentTimeMillis()));
            mScheduledExecutorService.shutdown();
        }
    }
}
