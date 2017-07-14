import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClienteFrame extends JFrame implements Runnable, ActionListener {
  ObjectOutputStream os = null;
  JTextField textField;
  JTextArea textArea;

  Posicao pos = new Posicao(2, 5);

  ClienteFrame() {
    super("Cliente do chat");
    add(textField = new JTextField(20), BorderLayout.NORTH);
    add(textArea = new JTextArea(5, 20), BorderLayout.CENTER);
    textField.addActionListener(this);
    textArea.setEditable(false);
    pack();
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    /*try{
      os.writeObject(pos);
    } catch (Exception excecao) {System.out.println("AAA");}*/
    textField.setText("");
  }

  public static void main(String[] args) {
    new Thread(new ClienteFrame()).start();
  }

  public void run() {
    Socket socket = null;
    ObjectInputStream is = null;

    try {
      socket = new Socket("127.0.0.1", 8080);
      os = new ObjectOutputStream(socket.getOutputStream());
      is = new ObjectInputStream(socket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host.");
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to host");
    } catch (Exception e){}

    try {
      String inputLine;
      Posicao pas;
      os.writeObject(pos);
      do {
        pas = (Posicao)is.readObject();
        inputLine = Integer.toString(pas.getX());
        textArea.append((inputLine+"\n"));
      } while (!inputLine.equals(""));

      os.close();
      is.close();
      socket.close();
    } catch (UnknownHostException e) {
      System.err.println("Trying to connect to unknown host: " + e);
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    } catch (Exception e){}
  }
}
