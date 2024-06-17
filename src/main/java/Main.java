import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) {
    System.out.println("Server starting on port 6379...");

    ServerSocket serverSocket = null;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    DataStorage dataStorage = new DataStorage();
    try {
      serverSocket = new ServerSocket(6379);
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
