package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The KeyValueStore class is a remote object that implements the KeyValueStoreRemote interface.
 * It provides methods for storing, retrieving, and deleting key-value pairs, as well as handling commands and transactions.
 */
public class KVStore extends UnicastRemoteObject implements KVStoreInt {
  private static final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
  private final ExecutorService exService;
  private static final ConcurrentHashMap<String, AtomicReference<String>> logs = new ConcurrentHashMap<>();

  /**
   * Constructs a KeyValueStore and initializes the executor service.
   *
   * @throws RemoteException If a remote invocation error occurs.
   */
  public KVStore() throws RemoteException {
    exService = Executors.newCachedThreadPool();
  }

  /**
   * Puts a key-value pair into the store.
   *
   * @param key   The key to store.
   * @param value The value to store.
   * @return A message indicating the result of the operation.
   * @throws RemoteException If a remote invocation error occurs.
   */
  @Override
  public synchronized String put(String key, String value) throws RemoteException {
    exService.submit(() -> store.put(key, value));
    return "Put successful";
  }

  /**
   * Gets the value associated with a key from the store.
   *
   * @param key The key to retrieve.
   * @return The value associated with the key, or "Key not found!" if the key does not exist.
   * @throws RemoteException If a remote invocation error occurs.
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
   * Deletes a key-value pair from the store.
   *
   * @param key The key to delete.
   * @return A message indicating the result of the operation.
   * @throws RemoteException If a remote invocation error occurs.
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
   * Handles a command by parsing it and invoking the corresponding method.
   *
   * @param command The command to handle.
   * @return The result of the command.
   * @throws RemoteException If a remote invocation error occurs.
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
        return "Invalid command!";
    }
  }

  /**
   * Prepares a transaction by logging its state and handling the command.
   *
   * @param id      The transaction ID.
   * @param command The command to handle.
   * @return True if the command was successful, false otherwise.
   * @throws RemoteException If a remote invocation error occurs.
   */
  @Override
  public boolean prepare(String id, String command) throws RemoteException {
    AtomicReference<String> state = new AtomicReference<>("Prepared");
    logs.put(id, state);
    String result = this.handleCommand(command);

    if (result.contains("successful")) {
      return true;
    } else {
      state.set("Aborted");
      return false;
    }
  }

  /**
   * Commits a transaction by changing its state to "Committed".
   *
   * @param id The transaction ID.
   * @throws RemoteException If a remote invocation error occurs.
   */
  @Override
  public void commit(String id) throws RemoteException {
    AtomicReference<String> state = logs.get(id);
    if (state != null && state.get().equals("Prepared")) {
      state.set("Committed");
    }
  }

  /**
   * Aborts a transaction by changing its state to "Aborted".
   *
   * @param id The transaction ID.
   * @throws RemoteException If a remote invocation error occurs.
   */
  @Override
  public void abort(String id) throws RemoteException {
    AtomicReference<String> state = logs.get(id);
    if (state != null && state.get().equals("Prepared")) {
      state.set("Aborted");
    }
  }
}
