import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

  public static List<ClientHandler> chatRoomClients = new ArrayList<>();
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private String clientUsername;

  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.clientUsername = this.bufferedReader.readLine();
      chatRoomClients.add(this);
      broadcastMessage("Server: " + clientUsername + " has entered this chat room!");
    } catch (IOException e) {
      stopCurrentClient(socket, bufferedReader, bufferedWriter);
    }
  }



  @Override
  public void run() {
    String msgFromClient;

    while (socket.isConnected()) {
      try {
        msgFromClient = bufferedReader.readLine();
        broadcastMessage(msgFromClient);
      } catch (IOException e) {
        stopCurrentClient(socket, bufferedReader, bufferedWriter);
        break;
      }
    }
  }

  public void broadcastMessage(String msg) {
    for (ClientHandler otherClient : chatRoomClients) {
      try {
        if (!otherClient.clientUsername.equals(clientUsername)) {
          otherClient.bufferedWriter.write(msg);
          otherClient.bufferedWriter.newLine();
          otherClient.bufferedWriter.flush();
        }
      } catch (IOException e) {
        stopCurrentClient(socket, bufferedReader, bufferedWriter);
        break;
      }
    }
  }

  public void removeClient() {
    chatRoomClients.remove(this);
    broadcastMessage("Server: " + clientUsername + " has left this chat room!");
  }

  public void stopCurrentClient(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    removeClient();
    Client.stopConnect(socket, bufferedReader, bufferedWriter);
  }
}
