package Storage;

import OrderType.Order;
import OrderType.OrderComparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShelfStorage implements IStorage {
    private enum SHELF {
        hot(10, 1),
        cold(10, 1),
        frozen(10, 1),
        miscellaneous(15, 2);

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

    private Map<SHELF, PriorityQueue<Order>> shelfMap = new ConcurrentHashMap<>();
    private Queue<Order> staleOrders = new LinkedList<>();

    public double updateShelfValue(Order order, SHELF shelf) {
        double orderAge = order.getOrderAge()/1000;
        double shelfOrderValue = (order.getShelfLife() - orderAge - order.getDecayRate() * orderAge * shelf.getShelfDecayModifier())/order.getShelfLife();
        order.setLifeValue(shelfOrderValue);
        System.out.println("shelfOrderValue " + shelfOrderValue);
        return shelfOrderValue;
    }

    @Override
    public void addOrderToStorage(Order order) {
        SHELF shelf = SHELF.valueOf(order.getTemp());
        if(canAddOrderToShelf(shelf)) {
            addOrderToShelf(order, shelf);
        }
        else {
            shelf = SHELF.miscellaneous;
            if(canAddOrderToShelf(shelf)) {
                addOrderToShelf(order, shelf);
            }
            else {
                removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(shelf);
            }
        }
    }

    private void removeOrdersFromOverFlowShelfAndInsertIntoRegularShelf(SHELF shelf) {
        Iterator iterator = shelfMap.get(shelf).iterator();
        while (iterator.hasNext()) {
            Order orderToBeRemoved = (Order) iterator.next();
            if(shelfMap.get(shelf).size() < shelf.getShelfCapacity()) {
                System.out.println("Order removed " + orderToBeRemoved.getId());
                updateShelfValue(orderToBeRemoved, shelf);
                shelfMap.get(shelf).add(orderToBeRemoved);
                iterator.remove();
            }
        }
    }

    private boolean canAddOrderToShelf(SHELF shelf) {
        return shelfMap.get(shelf) == null || shelfMap.get(shelf).size() < shelf.getShelfCapacity();
    }

    private void addOrderToShelf(Order order, SHELF shelf) {
        if(shelfMap.get(shelf) == null) {
            shelfMap.put(shelf, new PriorityQueue<>(shelf.getShelfCapacity(), new OrderComparator()));
        }
        if(shelfMap.get(shelf).size() < shelf.getShelfCapacity()) {
            updateShelfValue(order, shelf);
            shelfMap.get(shelf).add(order);
        }
    }

    @Override
    public void updateStorage() {
        for(Map.Entry entry : shelfMap.entrySet()) {
            Iterator iterator = ((PriorityQueue<?>) entry.getValue()).iterator();
            while (iterator.hasNext()) {
                Order orderToBeRemoved = (Order) iterator.next();
                double orderShelfValue = updateShelfValue(orderToBeRemoved, (SHELF) entry.getKey());
                if(orderShelfValue <= 0.0) {
                    //Adding expired orders to stale order queue
                    System.out.println("Order removed " + orderToBeRemoved.getId());
                    staleOrders.add(orderToBeRemoved);
                    iterator.remove();
                }
            }
        }
    }
}
