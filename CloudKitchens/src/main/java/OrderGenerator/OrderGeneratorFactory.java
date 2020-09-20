package OrderGenerator;

public class OrderGeneratorFactory {
    public static IOrderGenerator createOrderInstance() {
        return new JsonOrderGenerator();
    }
}
