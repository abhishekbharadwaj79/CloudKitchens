package OrderType;

import OrderType.Order;

import java.util.Comparator;

public class OrderComparator implements Comparator<Order> {
    @Override
    public int compare(Order order1, Order order2) {
        if(order1.getLifeValue() < order2.getLifeValue())
            return 1;
        if(order1.getLifeValue() > order2.getLifeValue())
            return -1;
        return 0;
    }
}
