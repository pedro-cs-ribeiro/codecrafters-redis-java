import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) {
    int port = 6379;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--port") && i < args.length - 1) {
        try {
          port = Integer.parseInt(args[i + 1]);
          System.out.println("Using custom port: " + port);
          break;
        } catch (NumberFormatException e) {
          System.err.println("Invalid port number. Using default port 6379.");
        }
      }
    }

    System.out.println("Server starting on port " + port + "...");

    ServerSocket serverSocket = null;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    DataStorage dataStorage = new DataStorage();
    dataStorage.addServerInfo("role", "master");
    try {
      serverSocket = new ServerSocket(port);
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
