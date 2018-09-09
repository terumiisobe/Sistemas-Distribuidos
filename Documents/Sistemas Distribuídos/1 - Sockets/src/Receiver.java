import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;

/* receber e tratar mensagens*/
public class Receiver{

  static MulticastSocket socket;
  static InetAddress grupo;
  static Publisher publish;
  static Thread t;

  static String meu_nome;
  static PrivateKey chave_privada;
  static PublicKey chave_publica;

  static List<String> users;        // nome dos usuários online
  static List<PublicKey> users_chave; // chave pública dos usuários online

  static Queue<String> SC1_wanted; // usuários na fila de SC1
  static Queue<String> SC2_wanted; // usuários na fila de SC2

  static Timer timer;

  /* monitorando mensagens recebidas do grupo */
  public static void main(String args[]) throws Exception {

    iniciar(args[0]);
    publish.enviarInfo();

    byte[] buffer;
    DatagramPacket messageIn;
    while(!publish.sair)
    {
        buffer = new byte[5000];
        messageIn = new DatagramPacket(buffer, buffer.length);
        socket.receive(messageIn);

        String mensagem = new String(messageIn.getData());
        System.out.println("Received: ");
        System.out.println(mensagem);

        if(mensagem.contains("entrou") || mensagem.contains("saiu"))
          atualizaUsers(messageIn.getData());

        if(mensagem.contains("pede acesso"))
          publish.enviarResposta();

        if(mensagem.contains("respondeu"))
        {
          // faz algo somente se você queria acessar a SC
        }
        if(publish.esperando_resposta)
        {
          timer = new Timer();
          timer.schedule(new TimerTask(){
            @Override
            public void run(){

            }

          }, 1000);
          //   public void run(){
          //   }
          // }, 1000);
        }
    }
    socket.leaveGroup(grupo);
  }
  /* gera as chaves publica e privada*/
  public static void iniciar(String nome) throws Exception{

    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(512);   // tamanho das chaves em bits
    KeyPair pair = generator.generateKeyPair();

    chave_privada = pair.getPrivate();
    chave_publica = pair.getPublic();
    meu_nome = nome;

    socket = new MulticastSocket(6789);
    grupo = InetAddress.getByName("224.0.0.1");
    socket.joinGroup(grupo);

    publish = new Publisher(meu_nome, chave_publica, socket, grupo);
    t = new Thread(publish);
    t.start();

  }
  /* atualiza ambas as listas de usuários online e chaves publicas, quando n>=3 atualiza variavel requisitos_n3*/
  public static void atualizaUsers(byte[] m){

  }
  /* atualiza filas de WANTED (apenas quando tem o HELD). Atualiza quando alguem sai*/
  public void atualizaWanted(){

  }
  /* decodificar uma resposta (autenticidade) usando chave pública */
  public void decode(){

  }
}

/* envio de mensagens */
class Publisher extends Thread{

  MulticastSocket socket;
  InetAddress grupo;

  public String nome;
  public PublicKey pub;

  // RELEASED, WANTED, HELD
  String estadoSC1;
  String estadoSC2;

  public boolean requisito_n3;
  public boolean esperando_resposta;
  public boolean sair;

  /* definir as variáveis de id e chave pública*/
  public Publisher(String meu_nome, PublicKey chave_publica, MulticastSocket socket, InetAddress grupo){
    this.nome = meu_nome;
    this.pub = chave_publica;
    this.socket = socket;
    this.grupo = grupo;

    this.estadoSC1 = "RELEASED";
    this.estadoSC2 = "RELEASED";

    this.requisito_n3 = false;
    this.esperando_resposta = false;
    this.sair = false;

  }
  /* gerencia ação do usuário */
  public void run(){
    //caso estadoSC1 for RELEASED: (acessar SC1, acessar SC2, sair)
    //caso estadoSC2 for HELD: (liberar SC, sair)
    //nao pode querer (WANTED) acessar as duas SC ao mesmo tempo
    //nao pode digitar qualquer coisa
    String menu1 = "Digite o número correspondente à sua escolha:\n1-Acessar SC1\n2-Acessar SC2\n3-Sair";
    String menu2 = "Digite o número correspondente à sua escolha:\n1-Liberar SC\n2-Sair";
    String invalido = "Este valor digitado é inválido!";
    try{
    if(requisito_n3)
    {
      while(true){
          if(estadoSC1 == "HELD" || estadoSC2 == "HELD")
          {
            System.out.println(menu2);
            String userInput = System.console().readLine();
            if(userInput == "1"){

            }
            else if (userInput == "2"){
              if(estadoSC1 == "HELD" || estadoSC2 =="HELD")
                transferirSC();
              enviarSaida();
              sair = true;
            }
            else{
              System.out.println(invalido);
              continue;
            }
          }
          else
          {
            System.out.println(menu1);
            String userInput = System.console().readLine();
            if(userInput == "1"){
              estadoSC1 = "WANTED";
              enviarPedido(1);
              esperando_resposta = true;
            }
            else if (userInput == "2"){

            }
            else if (userInput =="3"){
              enviarSaida();
              sair = true;
            }
            else{
              System.out.println(invalido);
              continue;
            }
          }

      }
    }
  }
  catch(IOException e){System.out.println("IO: " + e.getMessage());}
  catch(Exception e){System.out.println("IO: " + e.getMessage());}
  }
  /* enviar id e chave pública */
  public void enviarInfo() throws Exception{
    String m = nome + " entrou ";
    byte[] b_m = m.getBytes();
    byte[] b_pub = pub.getEncoded();
    byte[] mensagem = new byte[b_m.length + b_pub.length];

    System.arraycopy(b_m, 0, mensagem, 0, b_m.length);
    System.arraycopy(b_pub, 0, mensagem, b_m.length, b_pub.length);
    enviarDatagrama(mensagem);
  }
  /* enviar pedido à seção crítica */
  public void enviarPedido(int sc) throws IOException{
    String m = nome + "pede acesso à SC" + sc;
    byte[] b_m = m.getBytes();
    enviarDatagrama(b_m);
  }
  /* enviar resposta a pedido de SC */
  public void enviarResposta(){
  //precisa ser encoded

  }
  /* enviar token à próximo da lista, enviar lista de WANTED daquela SC*/
  public void transferirSC(){

  }
  public void enviarSaida() throws Exception{
    String m = nome + "saiu";
    byte[] b_m = m.getBytes();
    enviarDatagrama(b_m);
  }
  /* envio a nível de socket qualquer datagrama */
  public void enviarDatagrama(byte[] m) throws IOException{
    DatagramPacket messageOut = new DatagramPacket(m, m.length, grupo, 6789);
    socket.send(messageOut);
  }
  /* encodificar resposta usando chave privada */
  public void encode(){

  }

}
