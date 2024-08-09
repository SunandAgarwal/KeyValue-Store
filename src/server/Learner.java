package server;

import java.util.concurrent.ConcurrentHashMap;

public class Learner {
  private final ConcurrentHashMap<String, String> learnedValues = new ConcurrentHashMap<>();

  public void learn(Message message) {
    if (message.type == Message.Type.ACCEPTED) {
      learnedValues.put(message.key, message.value);
    }
  }
}
