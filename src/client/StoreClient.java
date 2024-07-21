package client;

import server.KVStoreInt;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * The StoreClient class is a client-side application that connects to a remote key-value store server via RMI (Remote Method Invocation).
 * It provides methods to connect to multiple servers, log activities, and execute commands such as put, get, and delete on the key-value store.
 */
public class StoreClient {
  private static final int TIMEOUT = 5000;
  private List<KVStoreInt> stubs;
  private PrintWriter logWriter;
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * Constructs an StoreClient and sets up the log writer and stubs for connecting to the server.
   *
   * @param serverAddress The address of the RMI server.
   * @param rmiPort     The starting port number to connect to.
   * @throws IOException If an I/O error occurs.
   */
  public StoreClient(String serverAddress, int rmiPort) throws IOException {
    setupLogWriter();
    setupStubs(serverAddress, rmiPort);
  }

  /**
   * Sets up the log writer for logging client activities.
   *
   * @throws IOException If an I/O error occurs while setting up the log writer.
   */
  private void setupLogWriter() throws IOException {
    this.logWriter = new PrintWriter(new FileWriter("client_store_log_rmi.txt", true), true);
  }

  /**
   * Sets up the stubs for connecting to multiple RMI servers.
   *
   * @param serverAddress The address of the RMI server.
   * @param rmiPort     The starting port number to connect to.
   */
  private void setupStubs(String serverAddress, int rmiPort) {
    this.stubs = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      try {
        int port = rmiPort + i;
        Registry registry = LocateRegistry.getRegistry(serverAddress, port);
        KVStoreInt stub = (KVStoreInt) registry.lookup("KVStore");
        stubs.add(stub);
        logWithTimestamp("Connected to RMI server at " + serverAddress + ":" + port);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Logs a message with a timestamp to both the log file and the console.
   *
   * @param message The message to log.
   */
  private void logWithTimestamp(String message) {
    String timestamp = sdf.format(new Date());
    logWriter.println(timestamp + " - " + message);
    System.out.println(timestamp + " - " + message);
  }

  /**
   * Runs the StoreClient, allowing the user to enter commands to interact with the key-value store.
   */
  private void run() {
    prepopulateCommands();
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

      Future<String> future = executor.submit(() -> stubs.get(0).handleCommand(command));
      try {
        String response = future.get(TIMEOUT, TimeUnit.SECONDS);
        logWithTimestamp("Response: " + response);
      } catch (TimeoutException e) {
        future.cancel(true);
        logWithTimestamp("Request timed out");
      } catch (Exception e) {
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
          String response = stubs.get(0).handleCommand(command);
          logWithTimestamp("Prepopulate " + command + " - Response: " + response);
        } catch (Exception e) {
          logWithTimestamp("Prepopulate error: " + e.getMessage());
        }
      });
    }

    executor.shutdown();
    try {
      executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logWithTimestamp("Prepopulate interrupted: " + e.getMessage());
    }
  }

  /**
   * The main method to start the StoreClient.
   *
   * @param args The command line arguments. The first argument is the server address, and the second argument is the starting port number.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: java client.StoreClient <server-address> <start-port>");
      return;
    }
    String hostname = args[0];
    int rmiPort = Integer.parseInt(args[1]);

    StoreClient client;
    try {
      client = new StoreClient(hostname, rmiPort);
      client.run();
    } catch (IOException e) {
      System.out.println("Client Exception!");
    }
  }
}
