package OrderGenerator;

import Storage.ShelfStorage;

import java.util.logging.Logger;

public class OrderGeneratorFactory {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static IOrderGenerator createOrderInstance(String orderType) {
        switch (orderType) {
            case "Json":
                LOGGER.info("OrderGenerator created for OrderType " + orderType);
                return new JsonOrderGenerator();
        }
        LOGGER.info("Wrong OrderType entered: " + orderType);
        return null;
    }
}
