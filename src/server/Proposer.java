package server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Proposer {
  private final String id;
  private final List<Acceptor> acceptors;
  private final ConcurrentHashMap<String, String> proposedValues;

  public Proposer(String id, List<Acceptor> acceptors) {
    this.id = id;
    this.acceptors = acceptors;
    this.proposedValues = new ConcurrentHashMap<>();
  }

  public void propose(String key, String value) {
    try {
      if (key == null) {
        throw new IllegalArgumentException("Key cannot be null");
      }
      if (value == null) {
        System.out.println("Proposing to delete key: " + key);
      } else {
        System.out.println("Proposing value: " + value + " for key: " + key);
      }

      // Propose to all acceptors
      for (Acceptor acceptor : acceptors) {
        acceptor.processMessage(new Message(Message.Type.ACCEPT, id, key, value));
      }

      proposedValues.put(key, value);
    } catch (Exception e) {
      System.err.println("Propose failed: " + e.getMessage());
    }
  }
}
