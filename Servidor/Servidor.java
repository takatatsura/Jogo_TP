import java.net.*;
import java.io.*;
import java.util.*;

class Servidor {
  public static void main(String[] args) {
    ServerSocket serverSocket=null;

    try {
      serverSocket = new ServerSocket(8080);
    } catch (IOException e) {
      System.out.println("Nao foi possivel usar o porto: " + 8080 + ", " + e);
      System.exit(1);
    }

    for (int i=0; i<2; i++) {
      Socket clientSocket = null;
      try {
        clientSocket = serverSocket.accept();
      } catch (IOException e) {
        System.out.println("Falha na aceitacao: " + 8080 + ", " + e);
        System.exit(1);
      }

      System.out.println("Funcionou!");

      new Servindo(clientSocket).start();

    }

    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


class Servindo extends Thread {
  Socket clientSocket;
  static ObjectOutputStream os[] = new ObjectOutputStream[2];
  static int cont=0;

  Servindo(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
      os[cont++] = new ObjectOutputStream(clientSocket.getOutputStream());
      Object inputLine, outputLine;
      try{
        do {

            inputLine = is.readObject();

            for (int i=0; i<cont; i++) {
              os[i].writeObject(inputLine);
              os[i].flush();
            }

        } while (true);
      } catch(Exception e){
        e.printStackTrace();
      }
      for (int i=0; i<cont; i++)
        os[i].close();
      is.close();
      clientSocket.close();

    } catch (IOException e) {
      e.printStackTrace();
    } catch (NoSuchElementException e) {
      System.out.println("Conexao terminada pelo cliente");
    }
  }
};
