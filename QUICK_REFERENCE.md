# FOODAPP Quick Reference Guide

## Quick Navigation Index

### 🏃 Getting Started
- [Build & Run](#build--run)
- [Architecture Overview](#architecture-overview)
- [Key Components](#key-components)

### 🔍 Developer Reference
- [Class Quick Reference](#class-quick-reference)
- [API Reference](#api-reference)
- [Data Structures](#data-structures)
- [Network Protocol](#network-protocol)

---

## Build & Run

### Prerequisites
- Java 8 or higher
- JSON libraries (included in `/lib/`)

### Quick Start
```bash
# 1. Build
javac -cp "lib/*:src" -d out src/com/example/myapplication/*.java

# 2. Start components (in separate terminals)
java -cp "lib/*:out" com.example.myapplication.Reducer
java -cp "lib/*:out" com.example.myapplication.Worker 4322
java -cp "lib/*:out" com.example.myapplication.Master localhost 4322
java -cp "lib/*:out" com.example.myapplication.Client
```

---

## Architecture Overview

```
┌─────────┐    ┌─────────┐    ┌─────────┐
│ Client  │───▶│ Master  │───▶│ Worker  │
│Manager  │    │ :4321   │    │ :4322+  │
└─────────┘    └─────────┘    └─────────┘
                      │
                      ▼
                ┌─────────┐
                │ Reducer │
                │ :4325   │
                └─────────┘
```

**Data Flow**: Client/Manager → Master → Workers → Reducer → Master → Client/Manager

---

## Key Components

| Component | Purpose | Port | Main Class |
|-----------|---------|------|------------|
| **Master** | Coordinates requests | 4321 | `Master.java` |
| **Worker** | Stores & processes data | Custom | `Worker.java` |
| **Reducer** | Aggregates results | 4325 | `Reducer.java` |
| **Client** | User interface | - | `Client.java` |
| **Manager** | Admin interface | - | `Manager.java` |

---

## Class Quick Reference

### Core Classes
```java
// Master - Central coordinator
public class Master {
    void openServer(String[][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut)
}

// Worker - Data processor
public class Worker {
    void openServer(int port, ArrayList<Store> stores, Object lock)
}

// Reducer - Result aggregator
public class Reducer {
    void openServer()
}
```

### Data Models
```java
// Store - Food store representation
public class Store implements Serializable {
    String storeName;
    double latitude, longitude;
    String category;
    double stars;
    int noOfReviews;
    ArrayList<Product> products;
    ArrayList<Purchase> purchases;
}

// Product - Store product
public class Product implements Serializable {
    String name, category;
    double price;
    int quantity;
    String status; // "visible" or "hidden"
}

// Purchase - Customer transaction
public class Purchase implements Serializable {
    String customerName, customerEmail;
    ArrayList<Product> purchasedProducts;
    double totalPrice;
}
```

### Request Models
```java
// MapReduceRequest - Search/filter criteria
public class MapReduceRequest implements Serializable {
    double clientLatitude, clientLongitude;
    ArrayList<String> foodCategories;
    double minStars;
    String priceCategory;
    double radius;
    String requestId;
}
```

---

## API Reference

### Client Operations
| Operation | Role | Parameters | Returns |
|-----------|------|------------|---------|
| **Find Nearby Stores** | `"client"` | `MapReduceRequest` | `ArrayList<Store>` |
| **Filter Stores** | `"filter"` | `MapReduceRequest` | `ArrayList<Store>` |
| **Get Products** | `"products"` | `storeName` | `ArrayList<Product>` |
| **Make Purchase** | `"purchase"` | `storeName`, `Purchase` | `String` |
| **Rate Store** | `"rate"` | `storeName`, `rating` | `String` |

### Manager Operations
| Operation | Role | Parameters | Returns |
|-----------|------|------------|---------|
| **Add Stores** | `"manager"` | `ArrayList<Store>` | `int` (success count) |
| **Find Store** | `"findStore"` | `storeName` | `String` (status) |
| **Add Product** | `"addProduct"` | `storeName`, `Product` | `String` |
| **Increase Quantity** | `"increaseQuantity"` | `storeName`, `productName`, `quantity` | `String` |
| **Remove Product** | `"removeProduct"` | `storeName`, `productName` | `String` |
| **Sales by Store Type** | `"storeType"` | `category` | `Map<String, Integer>` |
| **Sales by Product Category** | `"productCategory"` | `category` | `Map<String, Integer>` |

---

## Data Structures

### Store Distribution
```java
// Hash-based distribution to workers
int workerId = Math.abs(storeName.hashCode()) % workers.length;
```

### Price Categories
```java
// Store.calculatePriceCategory()
if (avgPrice <= 5) return "$";
if (avgPrice <= 15) return "$$";
return "$$$";
```

### Distance Calculation
```java
// Haversine formula for geographic distance
public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371; // Earth's radius in km
    // ... haversine calculation
}
```

---

## Network Protocol

### Message Format
All communication uses Java Object Serialization:
1. **Role String** - Identifies operation type
2. **Request Data** - Operation-specific parameters
3. **Response Data** - Operation results

### Communication Patterns

#### Client Search Flow
```
Client → Master: "client", MapReduceRequest
Master → Workers: "client", MapReduceRequest
Workers → Master: ArrayList<Store>
Master → Reducer: "client", clientId, workerCount, clientId, ArrayList<Store>
Reducer → Master: clientId, ArrayList<Store>
Master → Client: clientId, ArrayList<Store>
```

#### Manager Store Addition Flow
```
Manager → Master: "manager", ArrayList<Store>
Master → Workers: "manager", Store (hash-distributed)
Workers → Master: "Store added successfully"
Master → Manager: successCount
```

### Error Handling
- **IOException**: Network communication errors
- **ClassNotFoundException**: Serialization errors
- **Client ID Mismatch**: Results validation errors

---

## Development Tips

### Adding New Operations
1. Add role string to `Actions.java`, `WorkerActions.java`, `ReducerActions.java`
2. Implement request/response handling
3. Update client/manager interfaces
4. Test with distributed setup

### Debugging
- Check client ID matching in responses
- Verify worker connectivity
- Monitor thread synchronization
- Test hash distribution balance

### Performance Considerations
- Worker load balancing via hash distribution
- Thread synchronization with locks
- Network connection pooling
- Result caching potential

---

## File Extensions and Formats

### JSON Store Format
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

### Configuration Files
- `FoodApp.iml` - IntelliJ IDEA project configuration
- `.gitignore` - Git ignore patterns
- `lib/` - External JAR dependencies

---

## Testing Utilities

### TestHashDistribution.java
Tests store distribution across workers:
```java
String[] storeNames = {"Burger House", "Sushi Zen", "Healthy Bites", "Street Gyros"};
int workerId = Math.abs(storeName.hashCode()) % numWorkers;
```

### DistanceCalculator.java
Tests distance calculations:
```java
double distance = calculateDistance(userLat, userLon, store.latitude, store.longitude);
String status = distance <= 5.0 ? "✅ SHOULD APPEAR" : "❌ TOO FAR";
```

---

## Common Issues & Solutions

### Build Issues
- **ClassNotFoundException**: Ensure all JAR files are in classpath
- **Compilation Errors**: Check Java version compatibility

### Runtime Issues
- **Connection Refused**: Verify component startup order
- **Port Already in Use**: Change port numbers or kill existing processes
- **Client ID Mismatch**: Check UUID generation and request handling

### Data Issues
- **Empty Results**: Verify worker data loading
- **Incorrect Filtering**: Check distance calculation and filter logic
- **Missing Products**: Verify JSON parsing and product visibility status

---

*This quick reference complements the detailed [CODEBASE_INDEX.md](CODEBASE_INDEX.md) for comprehensive codebase understanding.*