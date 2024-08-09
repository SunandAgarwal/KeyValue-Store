package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Acceptor implements Runnable {
  private final ConcurrentHashMap<String, Message> receivedPromises = new ConcurrentHashMap<>();
  private final AtomicReference<Message> currentAcceptedValue = new AtomicReference<>();
  private boolean isActive = true;

  public void processMessage(Message message) {
    if (!isActive) return;
    switch (message.type) {
      case PREPARE:
        processPrepare(message);
        break;
      case ACCEPT:
        processAccept(message);
        break;
      default:
        break;
    }
  }

  private void processPrepare(Message message) {
    Message storedPromise = receivedPromises.get(message.key);
    if (storedPromise == null || storedPromise.proposalId.compareTo(message.proposalId) < 0) {
      receivedPromises.put(message.key, message);
    }
  }

  private void processAccept(Message message) {
    Message storedPromise = receivedPromises.get(message.key);
    if (storedPromise != null && storedPromise.proposalId.equals(message.proposalId)) {
      currentAcceptedValue.set(message);
    }
  }

  @Override
  public void run() {
    Random random = new Random();
    while (true) {
      try {
        TimeUnit.SECONDS.sleep(random.nextInt(10));
        isActive = false;
        TimeUnit.SECONDS.sleep(random.nextInt(10));
        isActive = true;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
