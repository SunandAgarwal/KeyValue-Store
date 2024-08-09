package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class KVStore extends UnicastRemoteObject implements KVStoreInt {
  private static final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
  private final Proposer proposer;
  private final List<Acceptor> acceptors;
  private final ExecutorService executorService;
  private static final ConcurrentHashMap<String, AtomicReference<String>> logs = new ConcurrentHashMap<>();

  public KVStore() throws RemoteException {
    this.acceptors = new ArrayList<>();
    this.executorService = Executors.newCachedThreadPool();

    for (int i = 0; i < 5; i++) {
      Acceptor acceptor = new Acceptor();
      this.acceptors.add(acceptor);
      this.executorService.submit(acceptor);
    }

    this.proposer = new Proposer("proposer-1", acceptors);
  }

  @Override
  public synchronized String put(String key, String value) throws RemoteException {
    proposer.propose(key, value);
    // Assuming consensus is reached and value is stored
    executorService.submit(() -> store.put(key, value));
    return "Put successful";
  }

  @Override
  public synchronized String delete(String key) throws RemoteException {
    try {
      if (!store.containsKey(key)) {
        return "Key not found!";
      }

      // Propose to delete the key
      proposer.propose(key, null);

      executorService.submit(() -> {
        store.remove(key);
      }).get();

      return "Delete successful";
    } catch (Exception e) {
      throw new RemoteException("Delete failed", e);
    }
  }

  @Override
  public String get(String key) throws RemoteException {
    try {
      return executorService.submit(() -> {
        String value = store.get(key);
        if (value == null) {
          value = "Key not found!";
        }
        return value;
      }).get();
    } catch (Exception e) {
      throw new RemoteException("Key not found!", e);
    }
  }


  @Override
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

  @Override
  public void commit(String id) throws RemoteException {
    AtomicReference<String> state = logs.get(id);
    if (state != null && state.get().equals("Prepared")) {
      state.set("Committed");
    }
  }

  @Override
  public void abort(String id) throws RemoteException {
    AtomicReference<String> state = logs.get(id);
    if (state != null && state.get().equals("Prepared")) {
      state.set("Aborted");
    }
  }

  public static void main(String[] args) throws RemoteException {
    System.out.println("KVStore server started...");
  }
}
