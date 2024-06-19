import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Main {

  private static final DataStorage dataStorage = new DataStorage();


  private static ServerConfig parseArguments(String[] args) {
    ServerConfig config = new ServerConfig();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--port") && i < args.length - 1) {
        try {
          config.setPort(Integer.parseInt(args[i + 1]));
          System.out.println("Using custom port: " + config.getPort());
        } catch (NumberFormatException e) {
          System.err.println("Invalid port number. Using default port 6379.");
        }
      } else if (args[i].equals("--replicaof") && i < args.length - 1) {
        String replicaOfArg = args[i + 1];
        replicaOfArg = replicaOfArg.substring(1, replicaOfArg.length() - 1);
        String[] parts = replicaOfArg.split(" ");
        if (parts.length == 2) {
          config.setMasterHost(parts[0]);
          try {
            config.setMasterPort(Integer.parseInt(parts[1]));
            System.out.println("Replica of: " + config.getMasterHost() + ":" + config.getMasterPort());
            dataStorage.addServerInfo("role", "slave");
          } catch (NumberFormatException e) {
            System.err.println("Invalid master port number.");
            return config;
          }
        } else {
          System.err.println("Invalid replicaof argument format. Expected: \"host port\"");
          return config;
        }
      }
    }

    return config;
  }

  public static void main(String[] args) {

    ServerConfig config = parseArguments(args);

    System.out.println("Server starting on port " + config.getPort() + "...");

    ServerSocket serverSocket = null;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    try {
      serverSocket = new ServerSocket(config.getPort());
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
        executor.submit(new ClientHandler(clientSocket, dataStorage));
      }
    } catch (IOException e) {
      System.err.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (serverSocket != null) {
          serverSocket.close();
        }
        executor.shutdown();
      } catch (IOException e) {
        System.err.println("IOException closing resources: " + e.getMessage());
      }
    }
  }
}
