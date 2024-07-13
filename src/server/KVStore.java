package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KVStore extends UnicastRemoteObject implements KVStoreInt {
  private final ConcurrentHashMap<String, String> store;
  private final ExecutorService exService;

  /**
   * Constructor to the KVStore class.
   */
  public KVStore() throws RemoteException {
    this.store = new ConcurrentHashMap<>();
    exService = Executors.newCachedThreadPool();
  }

  /**
   * Store a key value pair in the map.
   * @param key The key of the map
   * @param value The value of the key
   * @return Output message
   */
  @Override
  public synchronized String put(String key, String value) throws RemoteException {
    exService.submit(() -> store.put(key, value));
    return "Put successful";
  }

  /**
   * Get the value corresponding to a key in the map
   * @param key The key in the map
   * @return The value of the key
   */
  @Override
  public String get(String key) throws RemoteException {
    try {
      return exService.submit(() -> store.getOrDefault(key, "Key not found!")).get();
    } catch (Exception e) {
      throw new RemoteException("Key not found!");
    }
  }

  /**
   * Delete the key from the map.
   * @param key The key in the map
   * @return Output message
   */
  @Override
  public synchronized String delete(String key) throws RemoteException {
    if (!store.containsKey(key)) {
      return "Key not found!";
    }

    exService.submit(() -> store.remove(key));
    return "Delete successful";
  }

  /**
   * Generate response based on input commands.
   * @param command The input command
   * @return The response
   */
  public String handleCommand(String command) throws RemoteException {
    String[] commandParts = command.split(" ");
    switch (commandParts[0].toUpperCase()) {
      case "PUT":
        return put(commandParts[1], commandParts[2]);
      case "GET":
        return get(commandParts[1]);
      case "DELETE":
        return delete(commandParts[1]);
      default:
        return  "Invalid command!";
    }
  }
}
