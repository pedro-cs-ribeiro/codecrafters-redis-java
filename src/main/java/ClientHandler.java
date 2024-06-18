import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
  private final Socket clientSocket;
  private final DataStorage dataStorage;

  private static final String EMPTY_RESPONSE = "$-1\r";

  public ClientHandler(Socket socket, DataStorage dataStorage) {
    this.clientSocket = socket;
    this.dataStorage = dataStorage;
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

      while (true) {
        String messageType = in.readLine();

        if (messageType == null) {
          break;
        }

        /*
         * The Redis client sends a message in the following format:
         * *<number of arguments>\r\n
         * $<length of argument>\r\n
         * <argument>\r\n
         * $<length of argument>\r\n
         * <argument>\r\n
         */
        if (messageType.startsWith("*")) {
          int numArgs = Integer.parseInt(messageType.substring(1));
          String[] args = new String[numArgs];
          for (int i = 0; i < numArgs; i++) {
            int argLength = Integer.parseInt(in.readLine().substring(1));
            char[] argChars = new char[argLength];
            in.read(argChars, 0, argLength);
            in.readLine();
            args[i] = new String(argChars);
          }

          String command = args[0].toUpperCase();
          switch (command) {
            case "PING" -> out.println(createStringResponse("PONG"));
            case "ECHO" -> out.println(createStringResponse(args[1]));
            case "GET" -> {
              String value = dataStorage.get(args[1]);
              if (value == null) {
                out.println(EMPTY_RESPONSE);
              } else {
                out.println("$" + value.length() + "\r\n" + value + "\r");
              }
            }
            case "SET" -> {
              if (args.length == 3) {
                dataStorage.set(args[1], args[2]);
                out.println(createStringResponse("OK"));
              } else if (args.length == 5 && args[3].equalsIgnoreCase("px")) {
                try {
                  long expiryMillis = Long.parseLong(args[4]);
                  dataStorage.set(args[1], args[2], expiryMillis);
                  out.println(createStringResponse("OK"));
                } catch (NumberFormatException e) {
                  out.println(createStringResponse("ERR value is not an integer or out of range"));
                }
              } else {
                out.println(createStringResponse("ERR wrong number of arguments for 'SET' command"));
              }
            }
            case "INFO" -> {
              Map<String, String> serverInfo = dataStorage.getServerInfo();
              StringBuilder info = new StringBuilder();
              for (Map.Entry<String, String> entry : serverInfo.entrySet()) {
                info.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
              }
              out.println(createStringResponse(info.toString()));
            }
          }
        }
      }
    } catch (IOException e) {
      System.err.println("IOException handling client: " + e.getMessage());
    } finally {
      try {
        clientSocket.close();
        System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
      } catch (IOException e) {
        System.err.println("IOException closing client socket: " + e.getMessage());
      }
    }
  }

  private String createStringResponse(String response) {
    return "$" + response.length() + "\r\n" + response + "\r";
  }
}
