package OrderGenerator;

import OrderType.Order;

//What if there are multiple json orders
public interface IOrderGenerator {
    boolean hasNextOrder();
    Order getNextOrder();
}
