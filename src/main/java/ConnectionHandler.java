import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ConnectionHandler implements Runnable {
  private final String host;
  private final int port;

  public ConnectionHandler(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void run() {
    System.out.println("Connecting to " + host + ":" + port + "...");
    try (Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

      out.println(createRedisArrayCommand("PING"));

      String response;
      while ((response = in.readLine()) != null) {
        System.out.println("Received from " + host + ":" + port + ": " + response);
      }
    } catch (IOException e) {
      System.err.println("Connection error: " + e.getMessage());
    }
  }

  //*1\r\n$4\r\nPING\r\n
  private String createRedisArrayCommand(String command, String... args) {
    StringBuilder builder = new StringBuilder();

    builder.append("*").append(args.length + 1).append("\r\n");

    builder.append("$").append(command.length()).append("\r\n");
    builder.append(command).append("\r");

    for (String arg : args) {
      builder.append("$").append(arg.length()).append("\r\n");
      builder.append(arg).append("\r\n");
    }

    return builder.toString();
  }
}
