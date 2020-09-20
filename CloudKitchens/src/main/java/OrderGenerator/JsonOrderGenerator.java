package OrderGenerator;

import OrderType.Order;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonOrderGenerator implements IOrderGenerator {

    private static final String FILE_NAME = "orders.json";
    private Gson gson = new Gson();
    private List<Order> mOrdersList = new ArrayList<>();
    private int mCurrentOrder;

    public JsonOrderGenerator() {
        try (Reader reader = new FileReader(FILE_NAME)) {
            Order[] orders = gson.fromJson(reader, Order[].class);
            mOrdersList.addAll(Arrays.asList(orders));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCurrentOrder = 0;
    }

    @Override
    public boolean hasNextOrder() {
        return mOrdersList.size() > mCurrentOrder;
    }

    @Override
    public Order getNextOrder() {
        return mOrdersList.get(mCurrentOrder++);
    }
}

