package server;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The server class for rmi connections.
 */
public class StoreServer {
  private PrintWriter logWriter;
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * This method sets up the configuration before start of the server sockets.
   */
  private void setupConfig(String[] args) throws IOException {
    // Check number of arguments
    if (args.length != 1) {
      throw new IOException("Invalid syntax! Usage: java server.StoreServer <rmi-port>");
    }

    KVStoreInt kvStore = new KVStore();

    // Assign ports
    int rmiPort = Integer.parseInt(args[0]);

    // Setup log writer
    this.logWriter = new PrintWriter(new FileWriter("server_store_log_rmi.txt", true), true);

    Registry reg = LocateRegistry.createRegistry(rmiPort);
    reg.rebind("KVStore", kvStore);
    logWithTimestamp("RMI Server is running on port " + rmiPort + "...");
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
   * @param args The rmi port
   */
  public static void main(String[] args) {
    StoreServer serverObj = new StoreServer();
    try {
      serverObj.setupConfig(args);
    } catch (Exception e) {
      serverObj.logWithTimestamp("Server Exception!");
    }
  }
}
