# FOODAPP Class-Method Matrix

## Overview
This matrix provides a comprehensive cross-reference of all classes and their methods in the FOODAPP system.

## Class-Method Cross-Reference

### Core Server Classes

#### Master.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Entry point, parses worker addresses |
| `openServer` | `void` | `String[][] workers`, `HashMap<Integer, ObjectOutputStream> connectionsOut` | Starts master server on port 4321 |

#### Worker.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Entry point, takes port as argument |
| `openServer` | `void` | `int port`, `ArrayList<Store> stores`, `Object lock` | Starts worker server on specified port |

#### Reducer.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Entry point |
| `openServer` | `void` | - | Starts reducer server on port 4325 |

### Client Interface Classes

#### Client.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Entry point, provides CLI interface |

#### Manager.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Entry point, provides admin interface |

### Thread Handler Classes

#### Actions.java (Master Request Handler)
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `Actions` | - | `Socket connection`, `String[][] workers`, `int counterID` | Constructor |
| `run` | `void` | - | Processes requests based on role |

**Handles These Roles:**
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

#### WorkerActions.java (Worker Request Handler)
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `WorkerActions` | - | `Socket connection`, `ArrayList<Store> stores`, `Object lock` | Constructor |
| `run` | `void` | - | Processes requests from master |

**Handles These Roles:**
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

#### ReducerActions.java (Reducer Request Handler)
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `ReducerActions` | - | `Socket connection` | Constructor |
| `run` | `void` | - | Processes aggregation requests |

**Handles These Roles:**
- `"client"` / `"filter"` - Merge store lists
- `"storeType"` - Aggregate sales by store type
- `"productCategory"` - Aggregate sales by product category

### Data Model Classes

#### Store.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `Store` | - | `String storeName`, `double latitude`, `double longitude`, `String category`, `double stars`, `int noOfReviews`, `String storeLogoPath`, `ArrayList<Product> products` | Constructor |
| `getStoreName` | `String` | - | Returns store name |
| `setStoreName` | `void` | `String storeName` | Sets store name |
| `getLatitude` | `double` | - | Returns latitude |
| `setLatitude` | `void` | `double latitude` | Sets latitude |
| `getLongitude` | `double` | - | Returns longitude |
| `setLongitude` | `void` | `double longitude` | Sets longitude |
| `getCategory` | `String` | - | Returns category |
| `setCategory` | `void` | `String category` | Sets category |
| `getStars` | `double` | - | Returns rating |
| `setStars` | `void` | `double stars` | Sets rating |
| `getNoOfReviews` | `int` | - | Returns review count |
| `setNoOfReviews` | `void` | `int noOfReviews` | Sets review count |
| `getStoreLogoPath` | `String` | - | Returns logo path |
| `setStoreLogoPath` | `void` | `String storeLogoPath` | Sets logo path |
| `getProducts` | `ArrayList<Product>` | - | Returns product list |
| `setProducts` | `void` | `ArrayList<Product> products` | Sets product list |
| `getPurchases` | `ArrayList<Purchase>` | - | Returns purchase list |
| `setPurchases` | `void` | `ArrayList<Purchase> purchases` | Sets purchase list |
| `calculatePriceCategory` | `String` | - | Returns "$", "$$", or "$$$" |
| `toString` | `String` | - | Returns formatted string |

#### Product.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `Product` | - | `String name`, `String category`, `int quantity`, `double price` | Constructor |
| `getName` | `String` | - | Returns product name |
| `setName` | `void` | `String name` | Sets product name |
| `getCategory` | `String` | - | Returns product category |
| `setCategory` | `void` | `String category` | Sets product category |
| `getPrice` | `double` | - | Returns price |
| `setPrice` | `void` | `double price` | Sets price |
| `getQuantity` | `int` | - | Returns quantity |
| `setQuantity` | `void` | `int quantity` | Sets quantity |
| `getStatus` | `String` | - | Returns status ("visible"/"hidden") |
| `setStatus` | `void` | `String status` | Sets status |
| `toString` | `String` | - | Returns formatted string |

#### Purchase.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `Purchase` | - | `String customerName`, `String customerEmail`, `ArrayList<Product> purchasedProducts` | Constructor |
| `getCustomerName` | `String` | - | Returns customer name |
| `setCustomerName` | `void` | `String customerName` | Sets customer name |
| `getCustomerEmail` | `String` | - | Returns customer email |
| `setCustomerEmail` | `void` | `String customerEmail` | Sets customer email |
| `getPurchasedProducts` | `ArrayList<Product>` | - | Returns purchased products |
| `setPurchasedProducts` | `void` | `ArrayList<Product> purchasedProducts` | Sets purchased products |
| `getTotalPrice` | `double` | - | Returns total price |
| `calculateTotalPrice` | `double` | - | Calculates total price (private) |
| `toString` | `String` | - | Returns formatted receipt |

#### MapReduceRequest.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `MapReduceRequest` | - | `double clientLatitude`, `double clientLongitude`, `ArrayList<String> foodCategories`, `double minStars`, `String priceCategory`, `double radius`, `String requestId` | Constructor |
| `getClientLatitude` | `double` | - | Returns client latitude |
| `setClientLatitude` | `void` | `double clientLatitude` | Sets client latitude |
| `getClientLongitude` | `double` | - | Returns client longitude |
| `setClientLongitude` | `void` | `double clientLongitude` | Sets client longitude |
| `getFoodCategories` | `List<String>` | - | Returns food categories |
| `setFoodCategories` | `void` | `ArrayList<String> foodCategories` | Sets food categories |
| `getMinStars` | `double` | - | Returns minimum stars |
| `setMinStars` | `void` | `double minStars` | Sets minimum stars |
| `getPriceCategory` | `String` | - | Returns price category |
| `setPriceCategory` | `void` | `String priceCategory` | Sets price category |
| `getRadius` | `double` | - | Returns search radius |
| `setRadius` | `void` | `double radius` | Sets search radius |
| `getRequestId` | `String` | - | Returns request ID |
| `setRequestId` | `void` | `String requestId` | Sets request ID |
| `toString` | `String` | - | Returns formatted string |

### Utility Classes

#### DistanceCalculator.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Test distance calculations |
| `calculateDistance` | `double` | `double lat1`, `double lon1`, `double lat2`, `double lon2` | Haversine formula implementation |

**Inner Class: Store**
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `Store` | - | `String name`, `double latitude`, `double longitude` | Constructor |

#### TestHashDistribution.java
| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `main` | `void` | `String[] args` | Test hash distribution |

## Method Categories

### Network Communication Methods
- `Master.openServer()` - Master server setup
- `Worker.openServer()` - Worker server setup
- `Reducer.openServer()` - Reducer server setup
- `Actions.run()` - Master request handling
- `WorkerActions.run()` - Worker request handling
- `ReducerActions.run()` - Reducer request handling

### Data Access Methods (Getters/Setters)
- All model classes implement complete getter/setter pairs
- Follow JavaBean conventions
- Type-safe parameter handling

### Business Logic Methods
- `Store.calculatePriceCategory()` - Price categorization
- `Purchase.calculateTotalPrice()` - Purchase total calculation
- `DistanceCalculator.calculateDistance()` - Geographic distance

### String Representation Methods
- `Store.toString()` - Store information display
- `Product.toString()` - Product information display
- `Purchase.toString()` - Purchase receipt display
- `MapReduceRequest.toString()` - Request debugging

### Main Entry Points
- `Master.main()` - Master server startup
- `Worker.main()` - Worker server startup
- `Reducer.main()` - Reducer server startup
- `Client.main()` - Client application startup
- `Manager.main()` - Manager application startup

## Role-Based Request Handling Matrix

| Role | Actions.java | WorkerActions.java | ReducerActions.java |
|------|--------------|-------------------|---------------------|
| `"manager"` | ✅ Route to workers | ✅ Store addition | ❌ |
| `"findStore"` | ✅ Route to workers | ✅ Local search | ❌ |
| `"addProduct"` | ✅ Route to worker | ✅ Add to store | ❌ |
| `"increaseQuantity"` | ✅ Route to worker | ✅ Increase qty | ❌ |
| `"removeProduct"` | ✅ Route to worker | ✅ Hide product | ❌ |
| `"client"` | ✅ MapReduce coord | ✅ Local filtering | ✅ Result merging |
| `"filter"` | ✅ MapReduce coord | ✅ Advanced filtering | ✅ Result merging |
| `"products"` | ✅ Route to worker | ✅ Get products | ❌ |
| `"purchase"` | ✅ Route to worker | ✅ Process purchase | ❌ |
| `"rate"` | ✅ Route to worker | ✅ Update rating | ❌ |
| `"storeType"` | ✅ MapReduce coord | ✅ Calculate sales | ✅ Aggregate sales |
| `"productCategory"` | ✅ MapReduce coord | ✅ Calculate sales | ✅ Aggregate sales |

## Data Flow Patterns

### Simple Operations (Direct Worker Access)
```
Client/Manager → Master → Worker → Master → Client/Manager
```
**Used for**: Store management, product operations, purchases, ratings

### MapReduce Operations (Multi-Worker + Reducer)
```
Client/Manager → Master → All Workers → Master → Reducer → Master → Client/Manager
```
**Used for**: Search, filtering, sales analytics

### Hash-Based Distribution
```
storeName → hash(storeName) % workerCount → specific worker
```
**Used for**: Store assignment, targeted operations

This matrix provides a complete reference for understanding the method structure and request handling patterns in the FOODAPP system.