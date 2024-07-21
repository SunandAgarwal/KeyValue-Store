package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVStoreInt extends Remote {
  /**
   * Store a key value pair in the map.
   *
   * @param key   The key of the map
   * @param value The value of the key
   * @return Output message
   */
  String put(String key, String value) throws RemoteException;

  /**
   * Get the value corresponding to a key in the map
   *
   * @param key The key in the map
   * @return The value of the key
   */
  String get(String key) throws RemoteException;

  /**
   * Delete the key from the map.
   *
   * @param key The key in the map
   * @return Output message
   */
  String delete(String key) throws RemoteException;

  /**
   * Generate response based on input commands.
   *
   * @param command The input command
   * @return The response
   */
  String handleCommand(String command) throws RemoteException;

  /**
   * Prepare the transaction.
   *
   * @param id      The transaction id
   * @param command The input command
   * @return Whether transaction is prepared or not
   */
  boolean prepare(String id, String command) throws RemoteException;

  /**
   * Commit the transaction.
   *
   * @param id The transaction id
   */
  void commit(String id) throws RemoteException;

  /**
   * Abort the transaction.
   *
   * @param id The transaction id
   */
  void abort(String id) throws RemoteException;
}
