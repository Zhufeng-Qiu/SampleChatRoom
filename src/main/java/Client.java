import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private String username;

  public Client(Socket socket, String username) {
    try {
      this.socket = socket;
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.username = username;
    } catch (IOException e) {
      stopConnect(socket, bufferedReader, bufferedWriter);
    }
  }

  public void sendMessage() {
    try {
      bufferedWriter.write(username);
      bufferedWriter.newLine();
      bufferedWriter.flush();

      Scanner scanner = new Scanner(System.in);
      while (socket.isConnected()) {
        String msg = scanner.nextLine();
        bufferedWriter.write(username + ": " + msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
      }
    } catch (IOException e) {
      stopConnect(socket, bufferedReader, bufferedWriter);
    }
  }

  public void listenForMsg() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String msgFromChatRoom;
        while (socket.isConnected()) {
          try {
            msgFromChatRoom = bufferedReader.readLine();
            System.out.println(msgFromChatRoom);
          } catch (IOException e) {
            stopConnect(socket, bufferedReader, bufferedWriter);
          }
        }
      }
    }).start();
  }

  public static void stopConnect(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    try {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your username for this chat room: ");
    String username = scanner.nextLine();
    Socket socket = new Socket("localhost", 1234);
    Client client = new Client(socket, username);
    client.listenForMsg();
    client.sendMessage();
  }
}
