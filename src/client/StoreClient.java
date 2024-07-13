package client;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import server.KVStoreInt;

/**
 * The client class for rmi connections.
 */
@SuppressWarnings("ALL")
public class StoreClient {
  private static final int TIMEOUT = 4000;
  private KVStoreInt stub;
  private PrintWriter logWriter;
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * This method sets up the configuration before start of the server sockets.
   */
  private void setupConfig(String[] args) throws IOException, NotBoundException {
    // Check number of arguments
    if (args.length != 2) {
      throw new IOException("Invalid syntax! Usage: java client.StoreClient <server-address> <port>");
    }

    // Assign port and hostname
    String hostname = args[0];
    int port = Integer.parseInt(args[1]);

    // Setup log writer
    this.logWriter = new PrintWriter(new FileWriter("client_store_log_rmi.txt", true), true);

    Registry reg = LocateRegistry.getRegistry(hostname, port);
    this.stub = (KVStoreInt) reg.lookup("KVStore");
  }

  /**
   * Prepopulate the map with some initial data.
   */
  private void prepopulate() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    String[] cmds = {
            "PUT Usa Nyc",
            "PUT India Delhi",
            "PUT Greece Athens",
            "PUT Spain Madrid",
            "PUT Germany Berlin",
            "PUT England London"
    };
    try {
      for (String command : cmds) {
        Future<String> future = executor.submit(() -> this.stub.handleCommand(command));

        try {
          String response = future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          future.cancel(true);
        }
      }
    } catch (Exception e) {
      logWithTimestamp("RMI Client error while prepopulating: " + e.getMessage());
    }
  }

  /**
   * This method sends commands to server via rmi protocol.
   */
  private void handleRmiConnection() {
    this.prepopulate();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Scanner scanner = new Scanner(System.in);
    try {
      while (true) {
        System.out.print("Enter command: ");
        String command = scanner.nextLine();

        // Quit gracefully when prompted
        if (command.equalsIgnoreCase("QUIT")) {
          logWithTimestamp("Client is shutting down. Press Ctrl^C.");
          break;
        }

        logWithTimestamp("Request RMI: " + command);
        Future<String> future = executor.submit(() -> this.stub.handleCommand(command));

        try {
          String response = future.get(TIMEOUT, TimeUnit.SECONDS);
          logWithTimestamp("Response RMI: " + response);
        } catch (TimeoutException e) {
          future.cancel(true);
        }
      }
    } catch (Exception e) {
      logWithTimestamp("RMI Client error");
    }
  }

  /**
   * Logs a message with the current timestamp.
   *
   * @param message The message to log.
   */
  private void logWithTimestamp(String message) {
    String timestamp = sdf.format(new Date());
    logWriter.println(timestamp + " - " + message);
    System.out.println(timestamp + " - " + message);
  }

  /**
   * The main method
   *
   * @param args The hostname and port
   */
  public static void main(String[] args) {
    StoreClient clientObj = new StoreClient();
    try {
      clientObj.setupConfig(args);
      clientObj.handleRmiConnection();
    } catch (Exception e) {
      clientObj.logWithTimestamp("Client Exception!");
    }
  }
}
