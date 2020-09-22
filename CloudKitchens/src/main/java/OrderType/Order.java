package OrderType;

import java.sql.Timestamp;

public class Order {
    private String id;
    private String name;
    private String temp;
    private int shelfLife;
    private double decayRate;
    private long createTime;
    private double expiry;
    private double value;


    public Order(String id, String name, String temp, int shelfLife, double decayRate) {
        this.id = id;
        this.name = name;
        this.temp = temp;
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
    }

    public double getOrderAge() {
        return getCreateTime() - System.currentTimeMillis();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setExpiry(double expiry) {
        this.expiry = expiry;
    }

    public double getExpiry() {
        return expiry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public String getTemp() {
        return temp;
    }

    public int getShelfLife() { return shelfLife; }

    public double getDecayRate() {
        return decayRate;
    }

    public long getCreateTime() { return createTime; }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}