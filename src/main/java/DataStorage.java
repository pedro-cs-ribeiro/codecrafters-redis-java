import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class DataStorage {
  private final ConcurrentHashMap<String, AbstractMap.SimpleEntry<String, Long>> data = new ConcurrentHashMap<>();
  private final Map<String, String> serverInfo = new HashMap<>();
  private final ScheduledExecutorService expiryScheduler = Executors.newSingleThreadScheduledExecutor();
  private final Long MAX_EXPIRY = Long.MAX_VALUE;

  public DataStorage() {
    serverInfo.put("role", "master");
    serverInfo.put("master_replid", RandomUtils.generateRandomId());
    serverInfo.put("master_repl_offset", "0");
  }

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

  public void addServerInfo(String key, String value) {
    serverInfo.put(key, value);
  }

  public String getServerInfoKey(String key) {
    return serverInfo.get(key);
  }

  public Map<String,String> getServerInfo() {
    return serverInfo;
  }
}
