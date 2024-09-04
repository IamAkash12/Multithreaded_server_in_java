import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

public class MultitThreadedServer {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int MAX_CACHE_SIZE = 100;
    private static final long CACHE_EXPIRY_TIME = 60000; // 1 minute

    private static final Semaphore semaphore = new Semaphore(5); // Limit concurrent operations
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final ReentrantLock cacheLock = new ReentrantLock();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                executor.execute(new ClientHandler(socket));
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from client: " + inputLine);
                    
                    try {
                        semaphore.acquire();
                        String response = processRequest(inputLine);
                        out.println(response);
                    } finally {
                        semaphore.release();
                    }

                    if ("exit".equalsIgnoreCase(inputLine)) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("Client handler exception: " + ex.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("Error closing client socket: " + ex.getMessage());
                }
                System.out.println("Client disconnected");
            }
        }

        private String processRequest(String request) {
            String cachedResult = getCachedResult(request);
            if (cachedResult != null) {
                return "Cached: " + cachedResult;
            }

            // Simulate processing time
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String result = "Processed: " + request.toUpperCase();
            cacheResult(request, result);
            return result;
        }

        private String getCachedResult(String key) {
            cacheLock.lock();
            try {
                CacheEntry entry = cache.get(key);
                if (entry != null && !entry.isExpired()) {
                    return entry.getValue();
                }
            } finally {
                cacheLock.unlock();
            }
            return null;
        }

        private void cacheResult(String key, String value) {
            cacheLock.lock();
            try {
                if (cache.size() >= MAX_CACHE_SIZE) {
                    String oldestKey = cache.entrySet().stream()
                        .min(Comparator.comparingLong(e -> e.getValue().getTimestamp()))
                        .map(Map.Entry::getKey)
                        .orElse(null);
                    if (oldestKey != null) {
                        cache.remove(oldestKey);
                    }
                }
                cache.put(key, new CacheEntry(value));
            } finally {
                cacheLock.unlock();
            }
        }
    }

    static class CacheEntry {
        private final String value;
        private final long timestamp;

        public CacheEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME;
        }
    }
}
