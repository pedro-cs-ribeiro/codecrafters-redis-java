import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
  private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

  public String get(String key) {
    return data.get(key);
  }

  public void set(String key, String value) {
    data.put(key, value);
  }

  public void delete(String key) {
    data.remove(key);
  }
}