# Food Delivery Application: System Architecture and Operation

This document provides a comprehensive overview of the Food Delivery Application, detailing its architecture, data management practices, component functionalities, and an example execution flow.

## 1. Application Architecture Summary

The application follows a distributed, client-server architecture incorporating a MapReduce pattern for certain operations.

**Main Components and Roles:**

1.  **Client (`Client.java`):**
    *   Acts as the user interface, allowing users to interact with the system.
    *   Initiates requests for finding stores, filtering stores based on criteria (location, category, rating, price), fetching products, purchasing products, and rating stores.
    *   Communicates directly with the `Master` node.

2.  **Manager (`Manager.java`):**
    *   Acts as an administrative interface for managing store and product data.
    *   Handles operations like adding new stores (from JSON files), adding new products to existing stores, increasing product quantities, and removing/hiding products.
    *   Also initiates requests for aggregated sales data (total sales by store type, total sales by product category).
    *   Communicates directly with the `Master` node for these operations.

3.  **Master (`Master.java`, `Actions.java`):**
    *   Acts as the central coordinator and entry point for both `Client` and `Manager` requests.
    *   Receives requests and determines how to process them.
    *   For data management tasks initiated by the `Manager` (e.g., adding a store, adding a product), it identifies the appropriate `Worker` (based on a hash of the store name) and forwards the request to that specific `Worker`.
    *   For client requests that involve searching/filtering across multiple stores (e.g., finding nearby stores, filtering stores) or aggregating data (e.g., sales by store type), it distributes the task to all registered `Worker` nodes (Map phase).
    *   Collects partial results from `Worker` nodes and sends them to the `Reducer` for aggregation.
    *   Forwards the final aggregated results from the `Reducer` back to the `Client` or `Manager`.
    *   Manages a list of available `Worker` nodes (IPs and ports provided as command-line arguments).
    *   `Actions.java` contains the logic for handling different types of requests received by the `Master`.

4.  **Worker (`Worker.java`, `WorkerActions.java`):**
    *   Acts as a distributed data store and processing unit. Each `Worker` is responsible for a subset of the store data.
    *   Stores information about specific stores, including their products, location, ratings, and purchase history.
    *   Handles requests from the `Master` to:
        *   Add new store data.
        *   Find a specific store or product.
        *   Update product quantities (increment/decrement).
        *   Add new products to a store.
        *   Mark products as "hidden" (effectively removing them from client view).
        *   Update store ratings.
        *   Process client search/filter requests (Map phase): It filters its local store data based on the criteria (location, category, stars, price) provided in the `MapReduceRequest` and returns the matching stores.
        *   Process manager aggregation requests (Map phase): It calculates partial sales data (e.g., sales for a specific store type or product category) from its local stores and returns this to the `Master`.
        *   Fetch available products for a given store.
        *   Process purchase requests, updating product quantities and recording the purchase.
    *   `WorkerActions.java` contains the logic for the operations performed by the `Worker`. Data consistency for store and product information within a worker is managed using `synchronized` blocks on a shared `lock` object.

5.  **Reducer (`Reducer.java`, `ReducerActions.java`):**
    *   Responsible for aggregating partial results received from multiple `Worker` nodes via the `Master`.
    *   Receives lists of stores (from client search/filter operations) or maps of sales data (from manager aggregation requests).
    *   Merges these partial results into a single, consolidated result. For store lists, it ensures no duplicate stores are included. For sales data, it sums up the values for each key (e.g., store name).
    *   Sends the final aggregated result back to the `Master`.
    *   `ReducerActions.java` contains the logic for these aggregation tasks.

**Communication Mechanism:**

*   Communication between all components (`Client` <-> `Master`, `Manager` <-> `Master`, `Master` <-> `Worker`, `Master` <-> `Reducer`) is primarily done using **Java Sockets**.
*   Data serialization and deserialization for transmission over sockets are handled using **`ObjectOutputStream`** and **`ObjectInputStream`**. This allows complex Java objects (like `Store`, `Product`, `MapReduceRequest`, `Purchase`, `ArrayList`, `Map`) to be sent and received between the components.
*   The `Master` listens on port 4321 for `Client` and `Manager` connections.
*   `Worker` nodes listen on ports specified as command-line arguments when they are started.
*   The `Reducer` listens on port 4325 for connections from the `Master`.
*   Requests are typically identified by a "role" string (e.g., "client", "manager", "findStore", "storeType") sent as the first object in the stream, followed by the necessary data objects.

This architecture allows for distributing data and processing load across multiple `Worker` nodes, with the `Master` coordinating tasks and the `Reducer` handling final aggregation, making it suitable for handling queries across a potentially large dataset of stores and products.

## 2. Data Management Explanation

This section details how data is represented, loaded, distributed, managed at runtime, and persisted (or not persisted) within the application.

**2.1. Data Representation**

The core data entities of the application are `Store`, `Product`, and `Purchase`. All three are implemented as Java classes and are `Serializable` to allow them to be passed between different components (Master, Worker, Client, Manager) via ObjectInput/OutputStreams.

*   **`Store.java`**:
    *   **Purpose:** Represents a retail store in the application.
    *   **Main Fields:**
        *   `storeName` (String): The unique name of the store.
        *   `latitude` (double): Geographical latitude of the store.
        *   `longitude` (double): Geographical longitude of the store.
        *   `category` (String): The type of store (e.g., "pizzeria", "healthy").
        *   `stars` (double): The average customer rating of the store.
        *   `noOfReviews` (int): The total number of reviews the store has received.
        *   `storeLogoPath` (String): Path or identifier for the store's logo image.
        *   `products` (ArrayList<Product>): A list of products available at this store.
        *   `purchases` (ArrayList<Purchase>): A list of all purchases made at this store. This is initialized as an empty list when a store is created.

*   **`Product.java`**:
    *   **Purpose:** Represents an item that can be sold by a store.
    *   **Main Fields:**
        *   `name` (String): The name of the product.
        *   `category` (String): The category of the product (e.g., "pizza", "salad").
        *   `price` (double): The price of one unit of the product.
        *   `quantity` (int): The available stock of the product. A quantity of -1 is used internally by the `Manager` and `Worker` to signify that a product has been "removed" or hidden from the client view.
        *   `status` (String): Indicates the visibility of the product (e.g., "visible", "hidden"). Defaults to "visible".

*   **`Purchase.java`**:
    *   **Purpose:** Represents a transaction made by a customer at a store.
    *   **Main Fields:**
        *   `customerName` (String): The name of the customer who made the purchase.
        *   `customerEmail` (String): The email of the customer.
        *   `purchasedProducts` (ArrayList<Product>): A list of products included in this purchase, along with the quantities purchased for each. Note that for products in this list, the `quantity` field represents the *amount purchased*, not the remaining stock.
        *   `totalPrice` (double): The total calculated price for all items in the purchase. This is calculated when a `Purchase` object is instantiated.

**2.2. Initial Data Loading**

*   Initial store and product data is introduced into the system exclusively through the **`Manager` client**.
*   The `Manager` prompts the user for a path to a JSON file (e.g., `src/stores/store.json` or `src/stores/store2.json`).
*   The `Manager.java` code then reads this JSON file. The JSON structure is expected to be an array of store objects. Each store object contains details like `StoreName`, `Latitude`, `Longitude`, `FoodCategory`, `Stars`, `NoOfVotes`, `StoreLogo`, and a nested array of `Products`. Each product object within this array contains `ProductName`, `ProductType`, `AvailableAmount`, and `Price`.
*   The `Manager` parses this JSON data and creates a list of `Store` objects (which in turn contain `Product` objects).
*   This list of `Store` objects is then sent to the `Master` node with the role "manager".

**2.3. Data Distribution**

*   Once the `Master` receives the list of `Store` objects from the `Manager`, it is responsible for distributing this data to the available `Worker` nodes.
*   The distribution strategy is based on the **hash of the store's name**.
*   In `Actions.java` (within the `Master`), for each `Store` object received from the `Manager`:
    *   It calculates `workerId = Math.abs(store.getStoreName().hashCode()) % workers.length`. The `workers` array holds the IP and port information of all registered worker nodes.
    *   The `Master` then establishes a connection with the selected `Worker` and sends the `Store` object to it.
*   This ensures that each store (and its associated product data) is assigned to a specific `Worker`. The same hashing logic is used by the `Master` when it needs to route other store-specific requests (like finding a store, adding a product to a store, etc.) to the correct `Worker`.

**2.4. Runtime Data Management**

*   Each `Worker` node stores its assigned data **in-memory**.
*   In `Worker.java`, a shared `ArrayList<Store> stores` is maintained. This list holds all the `Store` objects that have been assigned to that particular `Worker` by the `Master`.
*   All operations within a `Worker` that access or modify this `stores` list (or the data within the `Store` objects, such as products or purchases) are handled by `WorkerActions.java`.
*   To manage concurrent access to the shared `stores` list (as multiple `Master` requests could potentially be handled by different `WorkerActions` threads), a `synchronized (lock)` block is used in `WorkerActions.java` around critical sections where the `stores` list or its elements are read or modified. This `lock` is an `Object` instance shared among all `WorkerActions` threads for a given `Worker`.
*   Examples of runtime data management include:
    *   Adding new `Store` objects received from the `Master`.
    *   Updating `Product` quantities when purchases are made or when the `Manager` updates stock.
    *   Adding new `Product` objects to a `Store`'s product list.
    *   Changing a `Product`'s status to "hidden" (effectively a soft delete).
    *   Adding `Purchase` objects to a `Store`'s purchase list after a successful transaction.
    *   Updating a `Store`'s `stars` and `noOfReviews` when a client rates a store.

**2.5. Data Persistence**

*   The application **lacks a persistent storage mechanism** for runtime data changes on the `Worker` nodes.
*   All store data, product information, purchase records, and updated ratings are held in the in-memory `ArrayList<Store>` on each `Worker`.
*   **If a `Worker` node shuts down or crashes, all the data it held, including any changes made since it was last initialized (e.g., new purchases, stock updates, new ratings, products added by the Manager), will be lost.**
*   The only way data is "persisted" in a more permanent form is through the initial JSON files (`store.json`, `store2.json`). If the system needs to be reset or restarted, the `Manager` would re-read these JSON files, and the data would be redistributed to the `Worker`s. However, any transactional data (like purchases or ratings) or modifications made via the `Manager` after the initial load (like adding new products not in the original JSON) would not be present unless they were manually added back to the source JSON files.

This approach simplifies the application's design by avoiding the complexity of database integration or file-based persistence on the workers but comes at the cost of data durability for runtime changes.

## 3. Core Client (`Client.java`) Functionalities

The `Client.java` application provides a command-line interface for users to interact with the food delivery and store information system. It communicates with the `Master` node to perform various operations. A unique `clientId` (a UUID) is generated for each client session to help in tracking requests and responses.

The `MapReduceRequest.java` class is a key data structure used by the Client to send search and filter criteria to the Master. It encapsulates:
*   `clientLatitude`, `clientLongitude` (double): User's location.
*   `foodCategories` (ArrayList<String>): Categories to filter by.
*   `minStars` (double): Minimum star rating.
*   `priceCategory` (String): Price range (e.g., "$", "$$", "$$$").
*   `radius` (double): Search radius.
*   `requestId` (String): An identifier for the request.

Here's a breakdown of each key functionality:

**3.1. Stores Near You (Option 1)**

*   **User Interaction:** Prompts for latitude and longitude.
*   **Communication Flow:**
    *   A `MapReduceRequest` is created with the user's location, default filters (no category, 0 stars, no price filter), and a 5.0 km radius.
    *   Client sends "client" role, its `clientId`, and the `MapReduceRequest` to the Master.
    *   Master broadcasts this request to all Workers.
    *   Workers filter their local stores based on distance from the client's location and the radius.
    *   Workers send their partial lists of matching stores back to the Master.
    *   Master forwards these partial lists to the Reducer.
    *   Reducer merges the lists, removing duplicates (based on store name).
    *   Reducer sends the final list to the Master, which forwards it to the Client.
    *   Client displays the list of nearby stores or a "no stores found" message.

**3.2. Filtering Stores (Option 2)**

*   **User Interaction:** Prompts for latitude, longitude, food categories (comma-separated), minimum stars, and price category.
*   **Communication Flow:**
    *   A `MapReduceRequest` is created with all user-provided filter criteria and a 5.0 km radius.
    *   Client sends "filter" role (though Master's `Actions.java` might process this under the "client" role logic), its `clientId`, and the `MapReduceRequest` to the Master.
    *   Master broadcasts to all Workers.
    *   Workers filter their local stores based on all criteria in the `MapReduceRequest` (distance, category, stars, price).
    *   Workers send partial lists to the Master.
    *   Master forwards to the Reducer.
    *   Reducer merges lists, removing duplicates.
    *   Reducer sends the final list to the Master, which forwards it to the Client.
    *   Client displays the filtered list of stores or a "no stores found" message.

**3.3. Purchase Products (Option 3)**

This is a two-step process:

*   **3.3.1. Fetch Products:**
    *   **User Interaction:** Prompts for the store name.
    *   **Communication Flow:**
        *   Client sends "fetchProducts" role, `clientId`, and `storeName` to Master.
        *   Master (currently, inefficiently) broadcasts to all Workers. *Ideally, it should hash the storeName to target the specific worker.*
        *   The relevant Worker (or the last one in the Master's loop) finds the store and returns a list of its "visible" products.
        *   Master sends this product list back to the Client.
        *   Client displays available products or a "no products available" message.

*   **3.3.2. Make Purchase:**
    *   **User Interaction:** If products are available, prompts for product names and quantities, then customer name and email.
    *   **Communication Flow:**
        *   Client creates a `Purchase` object (with product names and desired quantities, customer details).
        *   Client sends "purchase" role, `clientId`, the `Purchase` object, and the `storeName` to Master.
        *   Master (currently, inefficiently) broadcasts to all Workers. *Ideally, it should hash the storeName.*
        *   The relevant Worker:
            *   Validates product availability and quantity.
            *   If valid, updates product stock, fills in full product details in the `Purchase` object, and records the purchase.
            *   Sends a success or error message string back to the Master.
        *   Master sends this message to the Client.
        *   Client displays the server's response.

**3.4. Rate Store (Option 4)**

*   **User Interaction:** Prompts for store name and a rating (1-5).
*   **Communication Flow:**
    *   Client sends "rate" role, `clientId`, `storeName`, and `rating` (int) to Master.
    *   Master (currently, inefficiently) broadcasts to all Workers. *Ideally, it should hash the storeName.*
    *   The relevant Worker finds the store, updates its average star rating and review count.
    *   Worker sends a success ("Rating submitted successfully.") or error ("Store not found.") message to Master.
    *   Master sends this message to the Client.
    *   Client displays the server's response.

*(Note: Inefficiencies in Master's routing for `fetchProducts`, `purchase`, and `rate` are detailed in Section 5.)*

## 4. Core Manager (`Manager.java`) Functionalities

The `Manager.java` application serves as an administrative client for managing store and product data, as well as for retrieving aggregated sales reports. It interacts with the user via a command-line interface and communicates with the `Master` node to execute its operations.

**4.1. Add Store (Option 1)**

*   **User Interaction:** Prompts for a JSON file path.
*   **Communication Flow:**
    *   Manager parses the JSON file into an `ArrayList<Store>`.
    *   Manager sends "manager" role and the `ArrayList<Store>` to Master.
    *   Master iterates through the list. For each store, it calculates the target Worker based on `storeName.hashCode()` and sends the store object (with role "manager") to that Worker.
    *   Worker adds the store to its local in-memory list and confirms to Master.
    *   Master sends an overall success/failure message to the Manager.

**4.2. Add Product (Option 2)**

Allows adding a new product or increasing an existing one's quantity. This involves several steps:

*   **Step 1: Find Store:**
    *   Manager prompts for `storeName`. Sends "findStore" role and `storeName` to Master.
    *   Master hashes `storeName` to find the target Worker, forwards the request.
    *   Worker checks local data, returns `storeName` or `null` to Master, which forwards to Manager. If `null`, Manager prints "Store not found."
*   **Step 2: Find Product (if store found):**
    *   Manager prompts for `productName`. Sends "findProduct" role, `storeName`, `productName` to Master.
    *   Master routes to the target Worker.
    *   Worker checks if product exists, returns "exists" or "doesnt exist" to Master, then to Manager.
*   **Step 3a: Increase Quantity (if product "exists"):**
    *   Manager prompts for additional quantity. Sends "AmountInc" role, `storeName`, `productName`, quantity to Master.
    *   Master routes to Worker. Worker increases product quantity locally, confirms to Master, then to Manager.
*   **Step 3b: Add New Product (if product "doesnt exist"):**
    *   Manager prompts for product type, amount, price. Creates a `Product` object.
    *   Sends "NewProduct" role, `storeName`, `Product` object to Master.
    *   Master routes to Worker. Worker adds product to store's list locally, confirms to Master, then to Manager.

**4.3. Remove Product (Option 3)**

Allows marking a product as "hidden" or decreasing its quantity.

*   **Step 1: Find Store (Same as 4.2 Step 1).**
*   **Step 2: Find Product (Special - "findProduct2"):**
    *   Manager prompts for `productName`. Sends "findProduct2" role, `storeName`, `productName` to Master.
    *   Master routes to Worker. Worker checks product status: returns `productName` if visible, "hidden" if already hidden, or `null` if not found. Master forwards to Manager.
    *   Manager prints appropriate message if product is already hidden or not found.
*   **Step 3: Perform Action (if product exists and is visible):**
    *   Manager presents options: "1. Remove" or "2. Decrease quantity".
    *   If "Remove": Sends "remove" role, `storeName`, `productName` to Master. Master routes to Worker. Worker sets product quantity to -1 and status to "hidden". Confirms.
    *   If "Decrease quantity": Manager prompts for amount. Sends "AmountDec" role, `storeName`, `productName`, amount to Master. Master routes to Worker. Worker decreases quantity if sufficient. Confirms or sends error.

**4.4. Total sales by store type (Option 4)**

*   **User Interaction:** Prompts for store type (e.g., "pizzeria").
*   **Communication Flow:**
    *   Manager sends "storeType" role and the `storeType` string to Master.
    *   Master broadcasts to all Workers.
    *   Each Worker calculates total units sold for stores matching `storeType` in its local data, creating a `Map<String, Integer>` (store name -> sales quantity). Workers send these partial maps to Master.
    *   Master sends all partial maps to the Reducer (with role "storeType" and worker count).
    *   Reducer merges these maps, summing quantities for any identical store names using `merged.merge(key, value, Integer::sum)`.
    *   Reducer sends the final aggregated `Map<String, Integer>` to Master.
    *   Master forwards to Manager. Manager displays per-store sales and a grand total.

**4.5. Total sales by product category (Option 5)**

*   **User Interaction:** Prompts for product category (e.g., "pizza").
*   **Communication Flow:**
    *   Manager sends "productCategory" role and the `productCategory` string to Master.
    *   Master broadcasts to all Workers.
    *   Each Worker calculates total units sold for products matching `productCategory` across all its stores, creating a `Map<String, Integer>` (store name -> sales quantity for that product category). Workers send partial maps to Master.
    *   Master sends all partial maps to the Reducer (with role "productCategory" and worker count).
    *   Reducer merges maps, summing quantities.
    *   Reducer sends the final aggregated map to Master.
    *   Master forwards to Manager. Manager displays per-store sales and a grand total.

**4.6. Exit (Option 6)**
*   Terminates the `Manager` application.

## 5. Master Node (`Master.java`, `Actions.java`) Logic

The `Master` node is the central coordinator. It listens for connections from `Client` and `Manager` applications, then delegates tasks to `Worker` nodes and uses the `Reducer` for aggregation.

**5.1. Initialization & Connection Handling (`Master.java`)**

*   **Startup:** `Master.main` parses worker IP/port pairs from command-line arguments.
*   **Listening:** `openServer` creates a `ServerSocket` on port `4321`.
*   **Concurrency:** For each incoming connection, a new `Actions` thread is created and started. This thread handles all further communication for that specific client/manager session. The `Socket`, worker information array, and a unique connection ID are passed to the `Actions` thread.

**5.2. Request Handling in `Actions.java`**

The `Actions.run()` method processes requests based on a "role" string.

*   **Manager-Initiated Store/Product Operations:**
    *   **`manager` (add stores):** Receives `ArrayList<Store>`. For each store, hashes `storeName` to determine the target Worker. Sends the store to that Worker. Collects success confirmations. Responds to Manager with overall status.
    *   **`findStore`, `findProduct`, `findProduct2`, `AmountInc`, `NewProduct`, `remove`, `AmountDec`:** These roles receive necessary identifiers (store name, product name, etc.) from the Manager. The Master hashes the `storeName` to identify the specific Worker responsible for that data and forwards the request and data to only that Worker. It then forwards the Worker's response back to the Manager. No Reducer interaction.

*   **Manager-Initiated Analytical Queries (MapReduce):**
    *   **`storeType`, `productCategory`:** Receives the type/category from Manager.
        1.  **Map Phase:** Broadcasts the request to *all* Workers. Each Worker processes its local data and returns a partial result (a `Map<String, Integer>` of sales).
        2.  **Reduce Phase:** Master collects all partial maps and sends them to the Reducer (with the role, worker count, and then each map).
        3.  Master receives the final aggregated map from the Reducer and forwards it to the Manager.

*   **Client-Initiated Store Searching/Filtering (MapReduce):**
    *   **`client` (and `filter` implicitly through `MapReduceRequest`):** Receives `clientId` and `MapReduceRequest` from Client.
        1.  **Map Phase:** Broadcasts the `clientId` and `MapReduceRequest` to *all* Workers. Each Worker filters its local stores based on the request criteria and returns a partial `ArrayList<Store>`.
        2.  **Reduce Phase:** Master collects all partial store lists. Sends them to the Reducer (with role, `clientId`, worker count, and then each partial list).
        3.  Master receives the final, merged, and deduplicated `ArrayList<Store>` from the Reducer and forwards it to the Client.
    *   *(Note: The `Actions.java` file contains a duplicate `else if (role.equals("client"))` block, which appears to be a copy-paste error.)*

*   **Client-Initiated Direct Store Operations (Inefficient Routing):**
    *   **`fetchProducts`:** Receives `clientId`, `storeName`.
    *   **`purchase`:** Receives `clientId`, `Purchase` object, `storeName`.
    *   **`rate`:** Receives `clientId`, `storeName`, `rating`.
    *   **Processing for these three roles:** The Master iterates through *all* Worker nodes, sending the request to each. However, it only uses the response from the *last Worker* in the loop. This is inefficient and potentially incorrect if the target data isn't on the last worker. No Reducer interaction.

**5.3. Highlighted Inefficiencies in Master Routing**

The following client-initiated roles in `Actions.java` demonstrate an inefficient routing strategy by querying all workers instead of targeting the specific worker responsible for the `storeName`:
*   **`fetchProducts`**
*   **`purchase`**
*   **`rate`**

**Problem:** The Master sends the request to every worker, but the result variable in the Master's code is overwritten in each iteration. Thus, only the response from the *last worker contacted* is returned to the client.
**Contrast with Efficient Strategy:** For most Manager-initiated store-specific operations (e.g., adding a store, adding a product), the Master correctly uses `storeName.hashCode() % workers.length` to route the request to a single, specific worker. The `fetchProducts`, `purchase`, and `rate` roles should ideally adopt this targeted hashing approach.

## 6. Worker Node (`Worker.java`, `WorkerActions.java`) Logic

The `Worker` node stores and manages a subset of the application's data and executes tasks delegated by the `Master`.

**6.1. Initialization & Connection Handling (`Worker.java`)**

*   **Startup:** `Worker.main` accepts a port number as a command-line argument.
*   **Data Storage:**
    *   `ArrayList<Store> stores = new ArrayList<>();`: Main in-memory data structure for all store-related information assigned to this worker.
    *   `Object lock = new Object();`: Used for `synchronized` blocks to ensure thread-safe access to the `stores` list.
*   **Listening:** `openServer` creates a `ServerSocket` on the specified port.
*   **Concurrency:** For each incoming connection from the Master, a new `WorkerActions` thread is created and started. This thread handles the specific request, operating on the shared `stores` list (using the `lock`).

**6.2. Request Handling in `WorkerActions.java`**

The `WorkerActions.run()` method processes requests based on a "role" string from the Master. All operations are performed on the worker's local `stores` data, typically within `synchronized(lock)` blocks.

*   **`manager` (add store):** Receives a `Store` object, adds it to the local `stores` list. Responds "Store added successfully".
*   **`findStore`:** Receives `storeName`. Checks local `stores`. Returns the `storeName` if found, else `null`.
*   **`findProduct`:** Receives `storeName`, `ProductName`. Checks locally. Returns "exists" or "doesnt exist".
*   **`findProduct2`:** Receives `storeName`, `ProductName`. Checks locally. Returns `ProductName` if visible, "hidden" if quantity is -1, or `null` if not found. Response is sent immediately upon finding the product.
*   **`AmountInc`:** Receives `storeName`, `ProductName`, `amount`. Increases quantity of the specified product. Responds "Amount changed successfully".
*   **`NewProduct`:** Receives `storeName`, `Product` object. Adds the product to the specified store's list. Responds "Product added successfully".
*   **`remove`:** Receives `storeName`, `productName`. Sets the product's quantity to -1 and status to "hidden". Responds "Product removed or updated successfully." or "Product not found."
*   **`AmountDec`:** Receives `storeName`, `ProductName`, `amount`. Decreases product quantity if sufficient stock. Responds with success or "Amount is greater than the quantity".
*   **`storeType` (Map phase):** Receives `requestedType`. Iterates local stores. If category matches, calculates total items sold from all purchases in that store. Returns a `Map<String, Integer>` (storeName -> total sales for type) to Master.
*   **`productCategory` (Map phase):** Receives `requestedCategory`. Iterates local stores. For each store, calculates total items sold for products matching `requestedCategory`. Returns a `Map<String, Integer>` (storeName -> total sales for product category) to Master.
*   **`client` (Map phase - nearby stores):** Receives `clientId`, `MapReduceRequest`. Filters its local stores based on distance to client's location (from `MapReduceRequest`). Returns `clientId` and an `ArrayList<Store>` of matching stores to Master.
*   **`filter` (Map phase - filtered search):** Receives `clientId`, `MapReduceRequest`. Filters its local stores based on all criteria in `MapReduceRequest` (distance, category, stars, price). Returns `clientId` and an `ArrayList<Store>` of matching stores to Master.
*   **`fetchProducts`:** Receives `responseId` (client's ID), `storeName`. Finds the store locally, creates a list of products with `status == "visible"`. Returns `responseId` and the `ArrayList<Product>`.
*   **`purchase`:** Receives `responseId`, `Purchase` object, `storeName`.
    *   Validates if products in `Purchase` exist, are visible, and have sufficient quantity in the named store.
    *   If valid: updates local product quantities, fills in product category/price in the `Purchase` object, adds `Purchase` to the store's purchase list.
    *   Returns `responseId` and a success/error message string.
*   **`rate`:** Receives `responseId`, `storeName`, `rating`. Finds the store locally, updates its average star rating and review count. Returns `responseId` and a success/error message.

## 7. Reducer Node (`Reducer.java`, `ReducerActions.java`) Logic

The `Reducer` node aggregates partial results received from `Worker` nodes (via the `Master`).

**7.1. Initialization & Connection Handling (`Reducer.java`)**

*   **Startup:** `Reducer.main` starts `openServer`.
*   **Listening:** `openServer` creates a `ServerSocket` on port `4325`.
*   **Concurrency:** For each incoming connection from the Master, a new `ReducerActions` thread is created and started to handle the aggregation task.

**7.2. Request Handling in `ReducerActions.java`**

The `ReducerActions.run()` method processes aggregation tasks based on a "role" string.

*   **`client` (Aggregate store lists for "nearby stores"):**
    *   **Expects:** `clientId`, `totalWorkers` (count of partial lists), then `totalWorkers` pairs of (`requestId`, `ArrayList<Store>`).
    *   **Aggregation:** Merges all received `ArrayList<Store>` into a single list. Uses a `HashSet<String>` (based on store names) to ensure each store appears only once in the final list. Only processes partial lists where `requestId` matches the overall `clientId`.
    *   **Response:** Sends back the original `clientId` and the final, merged, deduplicated `ArrayList<Store>` to the Master.

*   **`filter` (Aggregate store lists for "filtered stores"):**
    *   **Expects:** Similar to "client" role: `clientId`, `totalWorkers`, then `totalWorkers` pairs of (`requestId`, `ArrayList<Store>`).
    *   **Aggregation:** Same logic as "client" role: merges lists and uses a `HashSet<String>` for deduplication, checking `requestId` against `clientId`.
    *   **Response:** Sends back `clientId` and the final `ArrayList<Store>` to the Master.

*   **`storeType` (Aggregate sales data by store type):**
    *   **Expects:** `totalWorkers` (count of partial maps), then `totalWorkers` instances of `Map<String, Integer>` (where key is store name, value is sales quantity for that type).
    *   **Aggregation:** Merges all partial maps. If multiple maps contain the same store name (key), their sales quantities (values) are summed using `merged.merge(entry.getKey(), entry.getValue(), Integer::sum)`.
    *   **Response:** Sends back the final aggregated `Map<String, Integer>` to the Master.

*   **`productCategory` (Aggregate sales data by product category):**
    *   **Expects:** `totalWorkers` (count of partial maps), then `totalWorkers` instances of `Map<String, Integer>` (key is store name, value is sales quantity for that product category).
    *   **Aggregation:** Same logic as "storeType": merges maps and sums values for identical keys.
    *   **Response:** Sends back the final aggregated `Map<String, Integer>` to the Master.

## 8. High-Level Execution Flow Example: Client Searching for Nearby Stores

This flow outlines the sequence of events when a user on the `Client` application searches for stores within a certain radius of their location.

1.  **User Initiates Search (Client):**
    *   User selects "1. Stores near you" and enters latitude/longitude.
    *   `Client` generates a unique `clientId`.

2.  **Client Prepares and Sends Request (Client to Master):**
    *   `Client` creates `MapReduceRequest` with location, default filters (empty category, 0 stars, empty price), 5.0 km radius, and a request ID.
    *   `Client` connects to `Master` (port 4321), sends role "client", its `clientId`, and the `MapReduceRequest`.

3.  **Master Receives and Broadcasts Request (Master to Workers):**
    *   `Master` (`Actions` thread) receives the request.
    *   `Master` iterates through all registered `Worker` nodes, forwarding role "client", `clientId`, and `MapReduceRequest` to each.

4.  **Workers Process Request (Map Phase):**
    *   Each `Worker` (`WorkerActions` thread) receives the request.
    *   It filters its local `Store` objects based on distance (calculated using client's location and store's location from `MapReduceRequest` vs. radius).
    *   Matching stores are added to a partial `ArrayList<Store>`.

5.  **Workers Send Partial Results (Workers to Master):**
    *   Each `Worker` sends its `clientId` and its partial `ArrayList<Store>` (which might be empty) back to the `Master`.

6.  **Master Collects and Forwards to Reducer (Master to Reducer):**
    *   `Master` collects all partial lists from Workers.
    *   `Master` connects to `Reducer` (port 4325).
    *   Sends role "client", original `clientId`, number of partial results, and then each (worker's `clientId`, partial `ArrayList<Store>`) pair.

7.  **Reducer Aggregates and Deduplicates (Reduce Phase):**
    *   `Reducer` (`ReducerActions` thread) receives data.
    *   Initializes an empty `ArrayList<Store>` (merged list) and a `HashSet<String>` (for store name deduplication).
    *   For each partial result:
        *   Checks if the partial result's `requestId` matches the overall `clientId`.
        *   If yes, iterates through stores in the partial list. If a store name is not in the `HashSet`, adds the store to merged list and name to `HashSet`.

8.  **Reducer Sends Final List (Reducer to Master):**
    *   `Reducer` sends original `clientId` and the final merged, deduplicated `ArrayList<Store>` to `Master`.

9.  **Master Sends Final List to Client (Master to Client):**
    *   `Master` forwards the `clientId` and final `ArrayList<Store>` to the originating `Client`.

10. **Client Displays Results (Client):**
    *   `Client` receives results, verifies `clientId`.
    *   Displays store details or "No nearby stores found."
```
