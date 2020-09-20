public class Shelf {
    //I want to make enum of shelf temperature, when order comes then we can add to the enum
    // capacity should be configurable
    String type;
    int capacity;

    public Shelf(String type, int capacity) {
        this.type = type;
        this.capacity = capacity;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }
}
