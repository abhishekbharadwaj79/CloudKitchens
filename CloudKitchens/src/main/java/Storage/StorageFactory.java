package Storage;


import java.util.logging.Logger;

public class StorageFactory {
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static IStorage createStorage(String storage) {
        switch (storage) {
            case "Shelf":
                LOGGER.info("Storage created for Storage Type " + storage);
                return new ShelfStorage();
        }
        LOGGER.info("Wrong storage entered: " + storage);
        return null;
    }
}
