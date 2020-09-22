import Manager.OrderManager;

import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static void main (String[] args) {
        Scanner sc = new Scanner(System.in);
        LOGGER.info("Enter number of Orders");
        int numberOfOrders = sc.nextInt();
        LOGGER.info("Enter timeIntervalInMillis");
        int timeIntervalInMillis = sc.nextInt();

        OrderManager orderManager = new OrderManager();
        orderManager.startProcessingOrders(numberOfOrders, timeIntervalInMillis);
    }
}
