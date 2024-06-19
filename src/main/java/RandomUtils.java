import java.security.SecureRandom;

public class RandomUtils {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom random = new SecureRandom();

  public static String generateRandomId() {
    StringBuilder sb = new StringBuilder(40);
    for (int i = 0; i < 40; i++) {
      int randomIndex = random.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }

}
