# FOODAPP Codebase Index

## Overview
This document provides a comprehensive index of the FOODAPP codebase, cataloging all classes, methods, data structures, and their relationships. The FOODAPP is a distributed food delivery application using a MapReduce architecture pattern.

## Table of Contents
- [File Structure](#file-structure)
- [Core Classes](#core-classes)
- [Data Model Classes](#data-model-classes)
- [Thread Handler Classes](#thread-handler-classes)
- [Utility Classes](#utility-classes)
- [Data Files](#data-files)
- [Communication Patterns](#communication-patterns)
- [Method Index](#method-index)
- [Dependencies](#dependencies)

---

## File Structure

```
FOODAPP/
├── src/
│   ├── com/example/myapplication/
│   │   ├── Actions.java                 # Master request handler
│   │   ├── Client.java                  # Client application
│   │   ├── DistanceCalculator.java      # Distance calculation utility
│   │   ├── Manager.java                 # Administrative interface
│   │   ├── MapReduceRequest.java        # Request data structure
│   │   ├── Master.java                  # Master coordinator
│   │   ├── Product.java                 # Product data model
│   │   ├── Purchase.java                # Purchase data model
│   │   ├── Reducer.java                 # Reducer coordinator
│   │   ├── ReducerActions.java          # Reducer request handler
│   │   ├── Store.java                   # Store data model
│   │   ├── TestHashDistribution.java    # Hash distribution test
│   │   ├── Worker.java                  # Worker node
│   │   └── WorkerActions.java           # Worker request handler
│   └── stores/
│       ├── store.json                   # Sample store data
│       ├── store2.json                  # Additional store data
│       └── *.png                        # Store logo images
├── lib/
│   ├── json-20250107.jar               # JSON processing library
│   └── json-simple-1.1.1.jar           # JSON simple library
├── food_app_architecture_overview.md    # Architecture documentation
└── .junie/guidelines.md                 # Development guidelines
```

---

## Core Classes

### 1. Master.java
**Purpose**: Central coordinator that manages worker nodes and routes requests
**Key Responsibilities**:
- Accepts client and manager connections
- Distributes work to worker nodes
- Manages worker registry
- Coordinates MapReduce operations

**Key Methods**:
- `main(String[] args)` - Entry point, parses worker addresses
- `openServer(String[][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut)` - Starts server on port 4321

**Network Configuration**:
- Listens on port **4321**
- Accepts unlimited connections
- Creates new `Actions` thread for each connection

### 2. Worker.java
**Purpose**: Distributed data processing node that stores and processes store data
**Key Responsibilities**:
- Stores subset of store data
- Processes search and filter requests
- Handles data modifications
- Participates in MapReduce operations

**Key Methods**:
- `main(String[] args)` - Entry point, takes port as argument
- `openServer(int port, ArrayList<Store> stores, Object lock)` - Starts worker server

**Data Management**:
- Maintains `ArrayList<Store>` for local store data
- Uses `Object lock` for thread synchronization
- Stores assigned via hash distribution

### 3. Reducer.java
**Purpose**: Aggregates partial results from multiple workers
**Key Responsibilities**:
- Merges store lists from workers
- Aggregates sales data
- Eliminates duplicates
- Sends final results to master

**Key Methods**:
- `main(String[] args)` - Entry point
- `openServer()` - Starts reducer server on port 4325

**Network Configuration**:
- Listens on port **4325**
- Creates new `ReducerActions` thread for each connection

### 4. Client.java
**Purpose**: User interface for interacting with the food delivery system
**Key Responsibilities**:
- Provides command-line interface
- Handles user input and display
- Communicates with master node
- Manages client sessions

**Key Features**:
- Generates unique `clientId` (UUID)
- Supports location-based search
- Handles filtering by category, stars, price
- Manages product browsing and purchasing
- Allows store rating

### 5. Manager.java
**Purpose**: Administrative interface for managing store and product data
**Key Responsibilities**:
- Loads store data from JSON files
- Manages product inventory
- Handles store operations
- Generates sales reports

**Key Features**:
- JSON file parsing and loading
- Product quantity management
- Store and product addition/removal
- Sales analytics and reporting

---

## Data Model Classes

### 1. Store.java
**Purpose**: Represents a food store with all its attributes
**Implements**: `Serializable`

**Fields**:
- `String storeName` - Unique store identifier
- `double latitude` - Geographic latitude
- `double longitude` - Geographic longitude
- `String category` - Food category (e.g., "pizzeria", "burger")
- `double stars` - Average rating (0-5)
- `int noOfReviews` - Number of reviews
- `String storeLogoPath` - Path to store logo image
- `ArrayList<Product> products` - List of available products
- `ArrayList<Purchase> purchases` - Purchase history

**Key Methods**:
- `calculatePriceCategory()` - Returns "$", "$$", or "$$$" based on average product price
- `toString()` - Formatted string representation

### 2. Product.java
**Purpose**: Represents a product available in a store
**Implements**: `Serializable`

**Fields**:
- `String name` - Product name
- `String category` - Product category (e.g., "pizza", "burger")
- `double price` - Product price
- `int quantity` - Available quantity
- `String status` - "visible" or "hidden"

**Key Methods**:
- Complete getter/setter methods
- `toString()` - Formatted product information

### 3. Purchase.java
**Purpose**: Represents a customer purchase transaction
**Implements**: `Serializable`

**Fields**:
- `String customerName` - Customer name
- `String customerEmail` - Customer email
- `ArrayList<Product> purchasedProducts` - List of purchased products
- `double totalPrice` - Total purchase amount

**Key Methods**:
- `calculateTotalPrice()` - Calculates total based on products and quantities
- `toString()` - Formatted purchase receipt

### 4. MapReduceRequest.java
**Purpose**: Encapsulates search and filter criteria for MapReduce operations
**Implements**: `Serializable`

**Fields**:
- `double clientLatitude` - Client's latitude
- `double clientLongitude` - Client's longitude
- `ArrayList<String> foodCategories` - Categories to filter by
- `double minStars` - Minimum rating threshold
- `String priceCategory` - Price range filter
- `double radius` - Search radius in km
- `String requestId` - Unique request identifier

---

## Thread Handler Classes

### 1. Actions.java
**Purpose**: Handles requests from clients and managers on master node
**Extends**: `Thread`

**Key Responsibilities**:
- Processes different request types based on "role"
- Coordinates with worker nodes
- Manages MapReduce operations
- Handles data distribution

**Request Types Handled**:
- `"manager"` - Store data loading
- `"findStore"` - Store lookup
- `"addProduct"` - Product addition
- `"increaseQuantity"` - Inventory increase
- `"removeProduct"` - Product removal
- `"client"` - Client search requests
- `"filter"` - Advanced filtering
- `"products"` - Product listing
- `"purchase"` - Purchase processing
- `"rate"` - Store rating
- `"storeType"` - Sales by store type
- `"productCategory"` - Sales by product category

### 2. WorkerActions.java
**Purpose**: Handles requests on worker nodes
**Extends**: `Thread`

**Key Responsibilities**:
- Processes requests from master
- Manages local store data
- Handles synchronization with locks
- Performs local filtering and processing

**Request Types Handled**:
- `"manager"` - Store addition
- `"findStore"` - Local store search
- `"addProduct"` - Add product to store
- `"increaseQuantity"` - Increase product quantity
- `"removeProduct"` - Hide/remove product
- `"client"` - Process client search
- `"filter"` - Apply filters to local data
- `"products"` - Get store products
- `"purchase"` - Process purchase
- `"rate"` - Update store rating
- `"storeType"` - Calculate sales by store type
- `"productCategory"` - Calculate sales by product category

### 3. ReducerActions.java
**Purpose**: Handles aggregation requests on reducer node
**Extends**: `Thread`

**Key Responsibilities**:
- Merges results from multiple workers
- Eliminates duplicates
- Aggregates sales data
- Sends final results to master

**Request Types Handled**:
- `"client"` / `"filter"` - Merge store lists
- `"storeType"` - Aggregate sales by store type
- `"productCategory"` - Aggregate sales by product category

---

## Utility Classes

### 1. DistanceCalculator.java
**Purpose**: Utility for calculating distances between coordinates
**Key Methods**:
- `calculateDistance(double lat1, double lon1, double lat2, double lon2)` - Haversine formula implementation
- `main(String[] args)` - Test distance calculations

### 2. TestHashDistribution.java
**Purpose**: Test utility for hash distribution of stores to workers
**Key Methods**:
- `main(String[] args)` - Tests store name hashing to worker assignment

---

## Data Files

### Store JSON Structure
```json
{
  "StoreName": "pizza hut",
  "Latitude": 37.9932963,
  "Longitude": 23.733413,
  "FoodCategory": "pizzeria",
  "Stars": 5,
  "NoOfVotes": 2,
  "StoreLogo": "stores/pizzahut.png",
  "Products": [
    {
      "ProductName": "margarita",
      "ProductType": "pizza",
      "AvailableAmount": 5000,
      "Price": 9.2
    }
  ]
}
```

**Available Store Data**:
- `store.json` - Pizza Hut data
- `store2.json` - Multiple stores (Burger House, Sushi Zen, Healthy Bites, Street Gyros)
- Logo images: `pizzahut.png`, `burgerhouse.png`, `sushi.png`, `healthy.png`, `greeksouvlaki.png`

---

## Communication Patterns

### Network Ports
- **Master**: Port 4321
- **Reducer**: Port 4325
- **Workers**: Custom ports (specified as command-line arguments)

### Communication Flow
1. **Client/Manager → Master**: Initial requests
2. **Master → Workers**: Distribute work (Map phase)
3. **Workers → Master**: Return partial results
4. **Master → Reducer**: Send partial results for aggregation
5. **Reducer → Master**: Return final aggregated results
6. **Master → Client/Manager**: Return final results

### Data Serialization
- Uses `ObjectOutputStream` and `ObjectInputStream`
- All data classes implement `Serializable`
- Supports complex object transmission (Store, Product, Purchase, ArrayList, HashMap)

---

## Method Index

### Core Business Logic Methods

#### Distance Calculation
- `DistanceCalculator.calculateDistance()` - Haversine formula for geographic distance

#### Store Distribution
- Hash-based distribution: `Math.abs(storeName.hashCode()) % workers.length`

#### Price Categorization
- `Store.calculatePriceCategory()` - Returns "$", "$$", "$$$" based on average product price

#### Search and Filtering
- Location-based filtering using distance calculation
- Category filtering by store food category
- Rating filtering by minimum stars
- Price filtering by calculated price category

### Data Management Methods

#### Store Management
- `Store` constructor and all getters/setters
- `Store.toString()` for formatted display

#### Product Management
- `Product` constructor and all getters/setters
- `Product.toString()` for formatted display

#### Purchase Management
- `Purchase` constructor and getters/setters
- `Purchase.calculateTotalPrice()` for total calculation
- `Purchase.toString()` for receipt formatting

### Network Communication Methods

#### Server Setup
- `Master.openServer()` - Master server initialization
- `Worker.openServer()` - Worker server initialization  
- `Reducer.openServer()` - Reducer server initialization

#### Thread Handling
- `Actions.run()` - Master request processing
- `WorkerActions.run()` - Worker request processing
- `ReducerActions.run()` - Reducer request processing

---

## Dependencies

### External Libraries
- **json-20250107.jar** - JSON processing
- **json-simple-1.1.1.jar** - Simplified JSON handling

### Java Standard Library Dependencies
- `java.io.*` - Input/output operations
- `java.net.*` - Network communication
- `java.util.*` - Collections and utilities
- `java.text.ParseException` - Exception handling

### Internal Dependencies
```
Master.java
├── Actions.java
├── Store.java
├── Product.java
├── Purchase.java
└── MapReduceRequest.java

Worker.java
├── WorkerActions.java
├── Store.java
├── Product.java
└── Purchase.java

Reducer.java
├── ReducerActions.java
├── Store.java
└── Product.java

Client.java
├── MapReduceRequest.java
├── Store.java
├── Product.java
└── Purchase.java

Manager.java
├── Store.java
├── Product.java
└── JSON Libraries
```

---

## Build and Run Instructions

### Compilation
```bash
javac -cp "lib/*:src" -d out src/com/example/myapplication/*.java
```

### Running the Application
1. **Start Reducer**: `java -cp "lib/*:out" com.example.myapplication.Reducer`
2. **Start Workers**: `java -cp "lib/*:out" com.example.myapplication.Worker <port>`
3. **Start Master**: `java -cp "lib/*:out" com.example.myapplication.Master <worker1_ip> <worker1_port> <worker2_ip> <worker2_port> ...`
4. **Run Client**: `java -cp "lib/*:out" com.example.myapplication.Client`
5. **Run Manager**: `java -cp "lib/*:out" com.example.myapplication.Manager`

### Example Startup Sequence
```bash
# Terminal 1 - Start Reducer
java -cp "lib/*:out" com.example.myapplication.Reducer

# Terminal 2 - Start Worker 1
java -cp "lib/*:out" com.example.myapplication.Worker 4322

# Terminal 3 - Start Worker 2  
java -cp "lib/*:out" com.example.myapplication.Worker 4323

# Terminal 4 - Start Master
java -cp "lib/*:out" com.example.myapplication.Master localhost 4322 localhost 4323

# Terminal 5 - Run Client
java -cp "lib/*:out" com.example.myapplication.Client
```

---

## Summary

This codebase index provides a comprehensive reference for the FOODAPP distributed food delivery system. The application demonstrates a clean separation of concerns with distinct roles for each component:

- **Master**: Central coordination
- **Workers**: Data storage and processing
- **Reducer**: Result aggregation
- **Client**: User interface
- **Manager**: Administrative interface

The system efficiently handles concurrent requests using multi-threading and employs a MapReduce pattern for scalable data processing across distributed nodes.