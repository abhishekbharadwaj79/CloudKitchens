It's a maven project using Java 8, import the project in Intellij Idea IDE for seamless experience.

Input:
Enter number of Orders
Enter timeIntervalInMillis

type 2 and 1000 if you want 2 orders per 1 second, 5 and 2000 if you want 5 orders per 2 second

/**
IOrdergenerator
**/
IOrdergenerator is the interface which reads the input.
JsonOrdergenerator reads the Json file and stores it in a List. Gson library is used for this
I have created Ordergenerator factory which gives us flexibility to have different types of orders.
It can Json or any other format also.

/**
IStorage
**/
IStorage is the interface is implemented by concrete classes which store the data.
It provides the public functions to add any type of Order to Storage and mark when order is delivered.
Shelf storage stores a map of enum type SHELF and Queue of orders Map<SHELF, Queue<Order>>,
which is a CocncurrentHashMap to keep concurrency in mind.
I have provided StorageFactory so that if we have some other type of storage than Shelf Storage that can also be implemented

Main class creates OrderManager and starts the processing of order

/**
OrderManager
**/
OrderManager is the Orchestrator here. It creates the OrderGenerator("Json") and ShelfStorage("Shelf") and starts processing the orders.
It creates a thread which runs every "timeIntervalInMillis" given as input. This thread reads the orders and adds them to Storage.
When the first order is picked up, a thread is created in ShelfStorage which keeps going through the map every 2 seconds to check
if there are any expired orders, if so it removes them. It helps us get rid of unnecessary orders preemptively and helps us
deliver the best orders. When orders are received the expiry time is calculated based on the formula provided in the problem,
this helps us not re-evaluate ordervalue frequently. ShelfStorage uses this expiry time to calculate if order is expired or not
instead of the order value.
The thread which receives order creates a random number between 2000 - 6000 (2 - 6 seconds) and schedules to execute a thread
at that amount of time. This newly spawned thread checks if the order is still available in storage and marks it delivered
if it's not expired.

/**
Order
**/
Maintains the order retrieved from orders.json file
