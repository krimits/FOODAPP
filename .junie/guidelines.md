# FoodApp Project Guidelines

## Project Overview
FoodApp is a distributed food ordering/processing system built using a MapReduce architecture. The application processes food store data including store information, product details, and potentially customer orders.

## Project Structure
- **Architecture**: The project follows a distributed MapReduce pattern with:
  - **Master**: Central coordinator that accepts client requests and distributes work to Workers
  - **Workers**: Process data in parallel (map phase)
  - **Reducer**: Aggregates results from Workers (reduce phase)
  - **Client**: Entry point for user interaction

- **Key Components**:
  - `Client.java`: Main entry point for user interaction
  - `Master.java`: Server that coordinates Workers and Reducer
  - `Worker.java`: Processes store data in parallel
  - `Reducer.java`: Aggregates results from Workers
  - `Actions.java`, `WorkerActions.java`, `ReducerActions.java`: Thread handlers for connections
  - `Store.java`, `Product.java`, `Purchase.java`: Data models
  - `stores/*.json`: Store and product data

## Development Guidelines
- **Code Style**: Follow standard Java conventions with proper indentation and documentation
- **Error Handling**: All network and I/O operations should include proper exception handling
- **Concurrency**: Use proper synchronization when accessing shared resources
- **Testing**: Run tests to verify functionality after making changes

## Build and Test Instructions
- Build the project before submitting any changes
- Test the distributed system by running Master, Workers, and Reducer in separate processes
- Verify that client requests are properly processed through the entire pipeline

## Running the Application
1. Start the Reducer: `java com.example.myapplication.Reducer`
2. Start Worker nodes: `java com.example.myapplication.Worker <port>`
3. Start the Master with Worker info: `java com.example.myapplication.Master <worker1_ip> <worker1_port> <worker2_ip> <worker2_port> ...`
4. Run the Client: `java com.example.myapplication.Client`
