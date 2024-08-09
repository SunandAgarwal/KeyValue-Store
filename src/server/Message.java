package server;

import java.io.Serializable;

public class Message implements Serializable {
  public enum Type {
    PREPARE, ACCEPT, ACCEPTED
  }

  public final Type type;
  public final String proposalId;
  public final String key;
  public final String value;

  public Message(Type type, String proposalId, String key, String value) {
    this.type = type;
    this.proposalId = proposalId;
    this.key = key;
    this.value = value;
  }
}
