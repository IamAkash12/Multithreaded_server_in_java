# **Multithreaded Java Server**

This project implements a multithreaded server in Java that can handle multiple client connections concurrently. It includes features such as a thread pool, connection limiting, request caching, and cache management.

## **How It Works**

### **Server Initialization:**

- The server listens on port `8080`.
- It uses a fixed thread pool of 10 threads to handle client connections.

### **Connection Handling:**

- When a client connects, the server assigns a `ClientHandler` to manage the connection.
- The `ClientHandler` runs in a separate thread from the thread pool.

### **Request Processing:**

- The server can handle multiple requests concurrently.
- A semaphore limits the number of concurrent operations to 5.
- Each request is processed by converting the input to uppercase.
- Processed results are cached for 1 minute to improve performance.

### **Caching Mechanism:**

- The server maintains a cache with a maximum size of 100 entries.
- When the cache is full, the oldest entry is removed to make space for new entries.
- Cache entries expire after 1 minute.

### **Client Communication:**

- Clients can send multiple requests in a single connection.
- The server responds to each request individually.
- Clients can disconnect by sending an `"exit"` command.

## **How to Execute**

### **Compile the Server:**

```bash
javac MultiThreadedServer.java
```

### **Run the Server:**

```bash
javac MultiThreadedServer.java
```
### **Testing:**
A test client (TestClient.java) is provided to demonstrate the server's functionality. To run the test:
### **Compile the Test Client:**

```bash
javac TestClient.java
```

### **Run the Test Client (make sure the server is running):**

```bash
java TestClient
```

The test client performs the following actions:

- Sends a basic request.
- Sends a cached request.
- Sends multiple concurrent requests.
- Sends an exit command.
