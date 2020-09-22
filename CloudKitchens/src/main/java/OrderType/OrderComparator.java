package OrderType;

import java.util.Comparator;

public class OrderComparator implements Comparator<Order> {

    @Override
    public int compare(Order order1, Order order2) {
        if(order1.getExpiry() < order2.getExpiry())
            return 1;
        else if(order1.getExpiry() > order2.getExpiry())
            return -1;
        return 0;
    }
}
