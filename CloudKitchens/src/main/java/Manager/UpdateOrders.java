package Manager;

import Storage.IStorage;
import Storage.ShelfStorage;

import java.util.Map;

public class UpdateOrders implements Runnable {
    IStorage storage;
    public UpdateOrders(IStorage storage) {
        this.storage = new ShelfStorage();
    }

    @Override
    public void run() {
        storage.updateStorage();
    }
}
