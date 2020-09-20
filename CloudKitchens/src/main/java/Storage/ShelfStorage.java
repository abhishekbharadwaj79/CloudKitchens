package Storage;

import OrderType.Order;
import Utils.OrderComparator;

import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
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

    public void calculateShelfValue(Order order, SHELF shelf) {
        double orderAge = order.getOrderAge()/1000;
        System.out.println(orderAge);
        order.setLifeValue((order.getShelfLife() - orderAge - order.getDecayRate() * orderAge * shelf.getShelfDecayModifier())/order.getShelfLife());
        System.out.println(order.getLifeValue());
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
                removeFromOverFlowShelfAndInsertIntoRegularShelf(shelf);
            }
        }
    }

    private void removeFromOverFlowShelfAndInsertIntoRegularShelf(SHELF shelf) {
        Iterator iterator = shelfMap.get(shelf).iterator();
        while (iterator.hasNext()) {
            Order orderToBeRemoved = (Order) iterator.next();
            if(shelfMap.get(shelf).size() < shelf.getShelfCapacity()) {
                calculateShelfValue(orderToBeRemoved, shelf);
                shelfMap.get(shelf).add(orderToBeRemoved);
                iterator.remove();
                break;
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
            calculateShelfValue(order, shelf);
            shelfMap.get(shelf).add(order);
        }
    }

    @Override
    public Order getOrderFromStorage(Order order) {
        return shelfMap.get(order.getTemp()).peek();
    }
}
