package client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import server.KVStoreInt;

/**
 * The StoreClient class is a client-side class that connects to a remote key-value store.
 * It provides methods to connect to multiple servers, log activities, and execute commands.
 */
public class StoreClient {
  private static final int TIMEOUT_MS = 5000;
  private List<KVStoreInt> remoteStubs;
  private PrintWriter logger;
  private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * Constructs a StoreClient and sets up the logger and stubs for connecting to the server.
   *
   * @param serverAddress The address of the RMI server.
   * @param startPort     The starting port number to connect to.
   * @param portCount     The number of ports to attempt to connect to.
   * @throws IOException If an I/O error occurs.
   */
  public StoreClient(String serverAddress, int startPort, int portCount) throws IOException {
    initializeLogger();
    initializeStubs(serverAddress, startPort, portCount);
  }

  /**
   * Initializes the logger for logging client activities.
   */
  private void initializeLogger() throws IOException {
    this.logger = new PrintWriter(new FileWriter("client_store_log_rmi.txt", true), true);
  }

  /**
   * Initializes the stubs for connecting to multiple RMI servers.
   */
  private void initializeStubs(String serverAddress, int startPort, int portCount) {
    this.remoteStubs = new ArrayList<>();
    for (int i = 0; i < portCount; i++) {
      try {
        int port = startPort + i;
        Registry registry = LocateRegistry.getRegistry(serverAddress, port);
        KVStoreInt stub = (KVStoreInt) registry.lookup("KVStore");
        remoteStubs.add(stub);
        logWithTimestamp("Connected to RMI server at " + serverAddress + ":" + port);
      } catch (Exception e) {
        logWithTimestamp("Failed to connect to RMI server at " + serverAddress + ":" + (startPort + i) + " - " + e.getMessage());
      }
    }
  }

  /**
   * Logs a message with a timestamp to both the log file and the console.
   */
  private void logWithTimestamp(String message) {
    String timestamp = timestampFormat.format(new Date());
    logger.println(timestamp + " - " + message);
    System.out.println(timestamp + " - " + message);
  }

  /**
   * The main method to start the StoreClient.
   */
  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: java client.StoreClient <server-address> <start-port> <port-count>");
      return;
    }
    String serverAddress = args[0];
    int startPort = Integer.parseInt(args[1]);
    int portCount = Integer.parseInt(args[2]);

    StoreClient client;
    try {
      client = new StoreClient(serverAddress, startPort, portCount);
      client.start();
    } catch (IOException e) {
      System.out.println("Error setting up client: " + e.getMessage());
    }
  }

  /**
   * Runs the StoreClient, allowing the user to enter commands to interact with the key-value store.
   */
  private void start() {
    this.prepopulateCommands();
    Scanner scanner = new Scanner(System.in);
    ExecutorService executor = Executors.newSingleThreadExecutor();

    while (true) {
      System.out.print("Enter command: ");
      if (!scanner.hasNextLine()) {
        System.out.println("No input found. Exiting...");
        break;
      }
      String command = scanner.nextLine();
      if (command.equalsIgnoreCase("QUIT")) {
        logWithTimestamp("Client is shutting down. Press Ctrl^C.");
        break;
      }
      logWithTimestamp("Request: " + command);

      Future<String> future = executor.submit(() -> processCommand(command));
      try {
        String response = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        logWithTimestamp("Response: " + response);
      } catch (TimeoutException e) {
        future.cancel(true);
        logWithTimestamp("Request timed out");
      } catch (Exception e) {
        logWithTimestamp("Error executing command: " + e.getMessage());
      }
    }
    scanner.close();
    executor.shutdown();
  }

  /**
   * Prepopulates the key-value store with a set of predefined commands.
   */
  private void prepopulateCommands() {
    String[] commands = {
            "PUT Usa Nyc",
            "PUT India Delhi",
            "PUT Greece Athens",
            "PUT Spain Madrid",
            "PUT Germany Berlin",
            "PUT England London"
    };

    ExecutorService executor = Executors.newFixedThreadPool(5);

    for (String command : commands) {
      executor.submit(() -> {
        try {
          String response = remoteStubs.get(0).handleCommand(command);
        } catch (Exception e) {
          logWithTimestamp("Prepopulate error: " + e.getMessage());
        }
      });
    }

    executor.shutdown();
    try {
      executor.awaitTermination(TIMEOUT_MS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logWithTimestamp("Prepopulate interrupted: " + e.getMessage());
    }
  }

  /**
   * Processes the given command by invoking the corresponding method on the remote key-value store.
   */
  private String processCommand(String command) throws RemoteException {
    String[] parts = command.split("\\s+");
    String action = parts[0];
    String key = parts[1];
    String value = parts.length > 2 ? parts[2] : null;
    String response;

    if (action.equalsIgnoreCase("put")) {
      response = remoteStubs.get(0).put(key, value);
    } else if (action.equalsIgnoreCase("get")) {
      response = remoteStubs.get(0).get(key);
    } else if (action.equalsIgnoreCase("delete")) {
      response = remoteStubs.get(0).delete(key);
    } else {
      response = "Invalid command";
    }
    return response;
  }
}
