import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import java.io.*;
//import java.util.concurrent.*;

class Principal extends JFrame {
  int classe; //1 - Cacador; 2 - Presa;
  // acho que tem que ajustar o tamanho do passo e o da tela
  int wwidth, wheight;
  int passo;
  int tparado;
  int projetil;
  Double projx, projy;
  Desenho d;
  //to pensando em ao invés de fazer uma classe Cacador e outra classe Presa, faz apenas uma classe Posicao
  //nao vai precisar verificar a classe toda vez que mexer com a posição do jogador atual
  //já que o usuário só vai ver a ele mesmo
  Posicao jogador, Tiro, Inimigo, TiroInimigo;
  //Cacador c;
  //Presa p;
  Guizos gu;
  Color bg, ch, gz;
  Random r;
  //talvez agora dê pra usar a queue normal
  //antes tava dando ruim pq executando direto de dentro da thread
  //Queue<Sons> som = new ConcurrentLinkedQueue<Sons>();
  Queue<Sons> som = new LinkedList<Sons>();

  //Parte para o Servidor
  ObjectOutputStream os = null;
  Queue<Sons> SomIn = new LinkedList<Sons>();
  Sons SomOut;

  Principal() {
    super("Paranoia");
    classe = 2;
    wwidth = 800;
    wheight = 500;
    passo = 0;
    tparado = 180;
    projetil = 0;
    r = new Random();
    d = new Desenho();
    if(classe == 1) {
      jogador = new Posicao(0, 0);
    } else {
      jogador = new Posicao(wwidth, wheight);
    }
    if(classe == 1) {
      d.addMouseListener(new ListenerShot());
    } else {
      gu = new Guizos(r.nextInt(wwidth / 10) * 10, r.nextInt(wheight / 10) * 10);
      gz = new Color(255, 255, 0);
    }
    bg = new Color(0, 0, 0);
    ch = new Color(255, 255, 255);
    new ThreadDraw().start();
    new ThreadServer().start();
    addKeyListener(new ListenerMovement());
    add(d);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  class Desenho extends JPanel {
    Sons s;
    Sons s2;
    Desenho() {
      setPreferredSize(new Dimension(wwidth, wheight));
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.setColor(bg);
      g.fillRect(0, 0, wwidth, wheight);

      Iterator<Sons> it = som.iterator();
      Iterator<Sons> IteratorServer = SomIn.iterator();
      while(it.hasNext()) {
        s = it.next();
        s.setRaio(s.getRaio() + 2);
        g.setColor(s.getColor());
        g.drawOval(s.getX() - s.getRaio() / 2, s.getY() - s.getRaio() / 2, s.getRaio(), s.getRaio());
        if(s.getRaio() >= 150) it.remove();
      }
      while (IteratorServer.hasNext()){
        s2 = IteratorServer.next();
        s2.setRaio(s2.getRaio() + 2);
        g.setColor(s2.getColor());
        g.drawOval(s2.getX() - s2.getRaio() / 2, s2.getY() - s2.getRaio() / 2, s2.getRaio(), s2.getRaio());
        if(s2.getRaio() >= 150) IteratorServer.remove();
      }


      if(classe == 2) {
        g.setColor(gz);
        g.drawOval(gu.getX() - 5, gu.getY() - 5, 10, 10);
      }
      if(projetil > 0) {
        projetil--;
        if (classe == 1){
          if(projetil > 120) {
            g.setColor(new Color((projetil - 120) * 255 / 60, 0, 0));
            g.drawLine(jogador.getX(), jogador.getY(), Tiro.getX(), Tiro.getY());
          }
          if(projetil > 30) {
            g.setColor(new Color(255, 255, 255));
          } else {
            g.setColor(new Color(projetil * 255 / 30, projetil * 255 / 30, projetil * 255 / 30));
          }
          if(jogador.getX() > wwidth / 2) {
            g.drawRect(jogador.getX() - 20, jogador.getY() - 5, 10, 10);
            g.fillRect(jogador.getX() - 20, jogador.getY() - 5, 10 - projetil * 10 / 180, 10);
          } else {
            g.drawRect(jogador.getX() + 10, jogador.getY() - 5, 10, 10);
            g.fillRect(jogador.getX() + 10, jogador.getY() - 5, 10 - projetil * 10 / 180, 10);
          }
        }

        else {
          if(projetil > 120) {
            g.setColor(new Color((projetil - 120) * 255 / 60, 0, 0));
            g.drawLine(Inimigo.getX(), Inimigo.getY(), TiroInimigo.getX(), TiroInimigo.getY());
          }
          if(projetil > 30) {
            g.setColor(new Color(255, 255, 255));
          } else {
            g.setColor(new Color(projetil * 255 / 30, projetil * 255 / 30, projetil * 255 / 30));
          }
        }
      }

      g.setColor(ch);
      g.fillOval(jogador.getX() - 5, jogador.getY() - 5, 10, 10);
    }
  }

  class ListenerMovement extends KeyAdapter {
    int movx, movy;
    public void keyPressed(KeyEvent e) {
      movx = 0;
      movy = 0;
      switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
          movx = -10;
          break;
        case KeyEvent.VK_RIGHT:
          movx = 10;
          break;
        case KeyEvent.VK_UP:
          movy = -10;
          break;
        case KeyEvent.VK_DOWN:
          movy = 10;
          break;
      }

      if(jogador.getX() + movx <= wwidth && jogador.getX() + movx >= 0
        && jogador.getY() + movy <= wheight && jogador.getY() + movy >= 0
        && (classe == 1 && projetil == 0 || classe == 2)) {
          passo++;
          tparado = 180;
          jogador.setX(jogador.getX() + movx);
          jogador.setY(jogador.getY() + movy);
          if(classe == 2 && jogador.getX() == gu.getX() && jogador.getY() == gu.getY()) {
            //ponto++
            SomOut = new Sons(gu.getX(), gu.getY(), 255, 255, 0);
            som.add(SomOut);
            //Manda para o servidor o som do guizo
            try{
              os.writeObject(SomOut);
            } catch(Exception excecao){
              excecao.printStackTrace();
            }

            gu = new Guizos(r.nextInt(wwidth / 10) * 10, r.nextInt(wheight / 10) * 10);
          }
        }

      if(passo == 30) {
        SomOut = new Sons(jogador.getX(), jogador.getY(), 255, 255, 255);
        som.add(SomOut);
        //Manda para o servidor o Passo do jogador
        try{
          os.writeObject(SomOut);
        } catch(Exception excecao){
          excecao.printStackTrace();
        }

        passo = 0;
      }
    }
  }

  class ListenerShot extends MouseAdapter {

    public void mouseReleased(MouseEvent e) {
      Point tiro = e.getPoint();
      if(tiro.getX() - jogador.getX() != 0 && projetil == 0) {
        Double m = (tiro.getY() - jogador.getY()) / (tiro.getX() - jogador.getX());
        //y = mx + b; b = y - mx;
        //x = (b - y) / m;
        Double b = jogador.getY() - m * jogador.getX();
        if(tiro.getX() < jogador.getX()) {
          /*projx = 0.0;
          projy = b;*/
          Tiro.setX(0);
          Tiro.setY((int) Math.floor(b));
        } else if (tiro.getX() > jogador.getX()) {
          /*projx = (double)wwidth;
          projy = m * wwidth + b;*/
          Tiro.setX(wwidth);
          Tiro.setY((int)Math.floor(m * wwidth + b));
        } else if (tiro.getY() < jogador.getY()) {
          /*projx = (-b) / m;
          projy = 0.0;*/
          Tiro.setX((int)Math.floor((-b) / m));
          Tiro.setY(0);
        } else if (tiro.getY() > jogador.getY()) {
          /*projx = (wheight - b) / m;
          projy = (double)wheight;*/
          Tiro.setX((int)Math.floor((wheight - b) / m));
          Tiro.setY(wheight);
        }
        projetil = 180;
        try{
          os.writeObject(Tiro);
        } catch(Exception excecao){
          excecao.printStackTrace();
        }
        try{
          os.writeObject(jogador);
        } catch(Exception excecao){
          excecao.printStackTrace();
        }

      }
    }
  }

  class ThreadDraw extends Thread {
    public void run() {
      while(true) {
        try {
          Thread.sleep(1000/60);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        tparado--;
        if(tparado <= 0 && tparado * (-1) % 40 == 0) {
          SomOut = new Sons(jogador.getX(), jogador.getY(), 255, 0, 0);
          som.add(SomOut);
          try{
            os.writeObject(SomOut);
          } catch(Exception e){
            e.printStackTrace();
          }
        }
        repaint();
      }
    }
  }

  class ThreadServer extends Thread {
    public void run(){
      Socket socket = null;
      ObjectInputStream is = null;
      Object obj = null;

      try {
        socket = new Socket("127.0.0.1", 8080);
        os = new ObjectOutputStream(socket.getOutputStream());
        is = new ObjectInputStream(socket.getInputStream());
      } catch (UnknownHostException e) {
        System.err.println("Don't know about host.");
      } catch (IOException e) {
        System.err.println("Couldn't get I/O for the connection to host");
      } catch (Exception e){
        e.printStackTrace();
      }
      //if (socket != null){
        while (true){
          try{
            System.out.println("BAI");
            obj = is.readObject();


            if (obj instanceof Guizos){
              Guizos GuizoIn = (Guizos) obj;
              SomIn.add(new Sons(GuizoIn.getX(), GuizoIn.getY(), 255, 255, 0));
            }
            else if (obj instanceof Sons){
              Sons temp = (Sons) obj;
              SomIn.add(temp);
              System.out.println("AAAA");
            }
            else if (obj instanceof Posicao){
              TiroInimigo = (Posicao) obj;
              try{
                obj = is.readObject();
                Inimigo = (Posicao) obj;
              } catch (Exception e) {
                e.printStackTrace();
              }


            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      //}
    }
  }

  public static void main(String[] args) {
    new Principal();
  }
}
