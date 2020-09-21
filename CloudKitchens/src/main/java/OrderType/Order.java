package OrderType;

import java.sql.Timestamp;

public class Order {
    String id;
    String name;
    String temp;
    int shelfLife;
    double decayRate;
    // How to track it across threads.
    long createTime;
    double lifeValue;

    public Order(String id, String name, String temp, int shelfLife, double decayRate) {
        this.id = id;
        this.name = name;
        this.temp = temp;
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
    }

    public double getLifeValue() { return lifeValue; }

    public void setLifeValue(double lifeValue) { this.lifeValue = lifeValue; }

    public double getOrderAge() {
        return System.currentTimeMillis() - getCreateTime();
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