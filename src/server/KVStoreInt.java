package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The KVStoreInt interface defines the remote methods that can be invoked by a client on the key-value store server.
 * These methods include operations for storing, retrieving, and deleting key-value pairs, as well as handling commands and transactions.
 */
public interface KVStoreInt extends Remote {

  /**
   * Puts a key-value pair into the store.
   */
  String put(String key, String value) throws RemoteException;

  /**
   * Gets the value associated with a key from the store.
   */
  String get(String key) throws RemoteException;

  /**
   * Deletes a key-value pair from the store.
   */
  String delete(String key) throws RemoteException;

  /**
   * Handles a command by parsing it and invoking the corresponding method.
   */
  String handleCommand(String command) throws RemoteException;

  /**
   * Prepares a transaction by logging its state and handling the command.
   */
  boolean prepare(String id, String command) throws RemoteException;

  /**
   * Commits a transaction by changing its state to "Committed".
   */
  void commit(String id) throws RemoteException;

  /**
   * Aborts a transaction by changing its state to "Aborted".
   */
  void abort(String id) throws RemoteException;
}
