import java.util.AbstractMap;
import java.util.concurrent.*;

public class DataStorage {
  private final ConcurrentHashMap<String, AbstractMap.SimpleEntry<String, Long>> data = new ConcurrentHashMap<>();
  private final ScheduledExecutorService expiryScheduler = Executors.newSingleThreadScheduledExecutor();
  private final Long MAX_EXPIRY = Long.MAX_VALUE;

  public String get(String key) {
    AbstractMap.SimpleEntry<String, Long> entry = data.get(key);
    if (entry == null || entry.getValue() < System.currentTimeMillis()) {
      return null;
    } else {
      return entry.getKey();
    }
  }

  public void set(String key, String value, long expiryMillis) {
    long expiryTime = System.currentTimeMillis() + expiryMillis;
    data.put(key, new AbstractMap.SimpleEntry<>(value, expiryTime));
    expiryScheduler.schedule(() -> data.remove(key), expiryMillis, TimeUnit.MILLISECONDS);
  }

  public void set(String key, String value) {
    data.put(key, new AbstractMap.SimpleEntry<>(value, MAX_EXPIRY));
  }

  public void delete(String key) {
    data.remove(key);
  }
}
