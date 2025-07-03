# FOODAPP - Distributed Food Delivery System

## 📋 Documentation Index

This repository contains a comprehensive food delivery application built with a distributed MapReduce architecture. The codebase has been fully indexed with multiple reference documents:

### 🗂️ Index Documents
1. **[CODEBASE_INDEX.md](CODEBASE_INDEX.md)** - Complete codebase catalog
   - Detailed file structure
   - All classes and methods
   - Data structures and relationships
   - Communication patterns
   - Build instructions

2. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Developer quick reference
   - Quick start guide
   - API reference
   - Common patterns
   - Troubleshooting

3. **[CLASS_METHOD_MATRIX.md](CLASS_METHOD_MATRIX.md)** - Complete class-method cross-reference
   - All classes with their methods
   - Parameter and return type details
   - Role-based request handling matrix
   - Data flow patterns

4. **[food_app_architecture_overview.md](food_app_architecture_overview.md)** - System architecture
   - High-level system design
   - Component interactions
   - Use cases and workflows

## 🚀 Quick Start

### Build & Run
```bash
# Build the project
javac -cp "lib/*:src" -d out src/com/example/myapplication/*.java

# Start components (in separate terminals)
java -cp "lib/*:out" com.example.myapplication.Reducer
java -cp "lib/*:out" com.example.myapplication.Worker 4322
java -cp "lib/*:out" com.example.myapplication.Master localhost 4322
java -cp "lib/*:out" com.example.myapplication.Client
```

## 🏗️ Architecture Overview

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

## 📊 Codebase Statistics

- **Total Java Files**: 14
- **Core Classes**: 5 (Master, Worker, Reducer, Client, Manager)
- **Data Models**: 4 (Store, Product, Purchase, MapReduceRequest)
- **Thread Handlers**: 3 (Actions, WorkerActions, ReducerActions)
- **Utility Classes**: 2 (DistanceCalculator, TestHashDistribution)
- **Documentation Lines**: 1,496 (across all index documents)

## 🎯 Key Features

### For Users (Client)
- Location-based store search
- Advanced filtering (category, rating, price)
- Product browsing and purchasing
- Store rating system

### For Administrators (Manager)
- Store data management
- Product inventory control
- Sales analytics and reporting
- JSON data import

### For Developers
- Distributed architecture with MapReduce
- Thread-safe data handling
- Hash-based load distribution
- Comprehensive error handling

## 🔍 Navigation Guide

**New to the codebase?** Start with [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

**Need architectural understanding?** Read [food_app_architecture_overview.md](food_app_architecture_overview.md)

**Looking for specific classes/methods?** Check [CLASS_METHOD_MATRIX.md](CLASS_METHOD_MATRIX.md)

**Want comprehensive details?** Explore [CODEBASE_INDEX.md](CODEBASE_INDEX.md)

## 📁 Directory Structure

```
FOODAPP/
├── src/com/example/myapplication/    # Source code
├── lib/                             # External libraries
├── stores/                          # Store data and images
├── out/                            # Compiled classes
├── CODEBASE_INDEX.md               # Complete codebase index
├── QUICK_REFERENCE.md              # Developer quick reference
├── CLASS_METHOD_MATRIX.md          # Class-method matrix
├── food_app_architecture_overview.md  # Architecture documentation
└── README.md                       # This file
```

## 🧪 Testing Utilities

- **TestHashDistribution.java** - Test store distribution across workers
- **DistanceCalculator.java** - Test geographic distance calculations
- **Sample data** - Available in `src/stores/`

## 🔧 Development Tips

1. **Build frequently** - Use the provided build command
2. **Start components in order** - Reducer → Workers → Master → Client/Manager
3. **Check logs** - Monitor console output for debugging
4. **Test with sample data** - Use provided JSON files
5. **Verify connectivity** - Ensure all ports are available

## 📄 License

This project is part of an educational system demonstrating distributed computing concepts.

---

*Complete codebase indexing completed - all classes, methods, and relationships documented.*