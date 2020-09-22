package OrderGenerator;

import OrderType.Order;

//What if there are multiple json orders
public interface IOrderGenerator {
    /**
     *
     * Checks if next order is available
     */
    boolean hasNextOrder();

    /**
     *
     * If next order is available then returns it
     */
    Order getNextOrder();
}
