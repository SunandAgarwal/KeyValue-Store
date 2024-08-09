package server;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The server class for RMI connections.
 */
public class StoreServer {
  private PrintWriter logWriter;
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * This method sets up the configuration before start of the server sockets.
   */
  private void setupConfig(int rmiPort) throws IOException {
    // Setup log writer
    this.logWriter = new PrintWriter(new FileWriter("server_store_log_rmi.txt", true), true);

    try {
      KVStore kvStore = new KVStore();
      KVStoreInt stub;
      Registry reg;

      try {
        stub = (KVStoreInt) UnicastRemoteObject.exportObject(kvStore, 0);
      } catch (ExportException e) {
        stub = kvStore;
      }

      try {
        reg = LocateRegistry.createRegistry(rmiPort);
      } catch (ExportException e) {
        reg = LocateRegistry.getRegistry(rmiPort);
      }
      reg.rebind("KVStore", stub);

      logWithTimestamp("RMI Server is running on port " + rmiPort + "...");
    } catch (RemoteException e) {
      logWriter.println("Failed to export KVStore object " + e);
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
   * The main method.
   *
   * @param args The RMI port.
   */
  public static void main(String[] args) throws IOException {
    // Check number of arguments
    if (args.length != 1) {
      throw new IOException("Invalid syntax! Usage: java server.StoreServer <rmi-port>");
    }

    // Assign port
    int rmiPort = Integer.parseInt(args[0]);

    ExecutorService exServ = Executors.newFixedThreadPool(5);

    for (int i = 0; i < 5; i++) {
      int finalI = i;
      exServ.submit(() -> {
        StoreServer serverObj = new StoreServer();
        try {
          serverObj.setupConfig(rmiPort + finalI);
        } catch (Exception e) {
          serverObj.logWithTimestamp("Server Exception!");
        }
      });
    }

    exServ.shutdown();
  }
}
