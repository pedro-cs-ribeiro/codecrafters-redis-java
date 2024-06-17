import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) {
    System.out.println("Server starting on port 6379...");

    ServerSocket serverSocket = null;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    try {
      serverSocket = new ServerSocket(6379);
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
        executor.submit(new ClientHandler(clientSocket));
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

  static class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
      this.clientSocket = socket;
    }

    @Override
    public void run() {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

        String line;
        while ((line = in.readLine()) != null) {
          if (line.equalsIgnoreCase("ping")) {
            out.println("+PONG\r");
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
  }
}
