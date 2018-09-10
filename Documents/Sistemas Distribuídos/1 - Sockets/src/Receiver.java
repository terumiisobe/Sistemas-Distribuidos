import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.security.spec.X509EncodedKeySpec;
//import sun.misc.BASE64Encoder;

/*------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------Receiver----------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------*/
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
  // as mensagens recebidas pelo grupo sao identificadas com **
  public static void main(String args[]) throws Exception {

    iniciar(args[0]);
    publish.enviarInfo();  // anuncia entrada no grupo, enviando sua chave publica

    byte[] buffer;
    DatagramPacket messageIn;
    while(!publish.sair)
    {
        buffer = new byte[500];
        messageIn = new DatagramPacket(buffer, buffer.length);
        socket.receive(messageIn);

        String mensagem = new String(messageIn.getData());

        if(mensagem.contains("entrou")) //ok
          novosUsers(messageIn.getData());

        if(mensagem.contains("saiu")) //ok
          retirarUsers(messageIn.getData());

        if(mensagem.contains("pede acesso"))
          publish.enviarResposta(new String(messageIn.getData()));

        if(mensagem.contains("respondeu"))
        {
          //caso vc tenha feito o pedido -> verificar respostas que estao codificadas
          //caso vc n tenha feito o pedido -> faz nada
        }
        if(publish.esperando_resposta)
        {
          // timer de 1000ms = 1s
          timer = new Timer();
          timer.schedule(new TimerTask(){
            @Override
            public void run(){
              System.out.println("estouro do timer");
              timer.cancel(); //talvez de problema**
            }

          }, 1000);
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

    users = new ArrayList<String>();
    users_chave = new ArrayList<PublicKey>();

    socket = new MulticastSocket(6789);
    grupo = InetAddress.getByName("224.0.0.1");
    socket.joinGroup(grupo);

    publish = new Publisher(meu_nome, chave_publica, chave_privada, socket, grupo);
    t = new Thread(publish);

  }
  /* atualiza ambas as listas de usuários online e chaves publicas, quando n>=3 atualiza variavel requisito_n3*/
  public static void novosUsers(byte[] m) throws Exception{
      byte[] pubb = new byte[94];  //a chave publica tem 94 bytes
      byte[] restb = new byte[500]; //mesmo tamanho do buffer de dados
      int n_espaco = 0;
      char espaco = ' ';
      int i;

      for(i = 0; i < m.length; i++){
        if(m[i] == (byte)espaco)
          n_espaco++;
        if(n_espaco==2) //espaco depois da palavra chave "entrou"
          break;
      }
      i++;             //posicao inicial da chave publica

      System.arraycopy(m, i, pubb, 0, 94);
      System.arraycopy(m, 0, restb, 0, i);

      String rest = new String(restb);

      String[] s = rest.split(" ");
      String novo_processo = s[0];

      //se a lista ainda não contem o novo processo
      if(!(users.contains(novo_processo))){
        System.out.println("**" + rest);  //imprime mensagem recebida

        PublicKey pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubb)); //ok
        users_chave.add(pub);     //adiciona chave publica na lista
        users.add(novo_processo); //adiciona novo usuário a lista

        if(!novo_processo.equals(meu_nome))
          publish.enviarInfo();

        imprimeLista();

      }

      //inicia apenas quando existem 3 processos ou mais
      if(users.size() > 2){
        if(!publish.requisito_n3)
          t.start();
        publish.requisito_n3 = true;
      }
      else
        System.out.println("Esperando novos processos...");

  }
  /* atualiza lista de processos online quando um deles sai*/
  public static void retirarUsers(byte[] m){
    String mensagem = new String(m);
    System.out.println("**" + mensagem);  //imprime mensagem recebida
    String[] s = mensagem.split(" ");
    String processo_fora = s[0];

    if(processo_fora.equals(meu_nome)) //nao sei se conserta nao imprimir a propria saida**
      return;

    int index  = users.indexOf(processo_fora);
    users.remove(index);
    users_chave.remove(index);

    imprimeLista();
    //chamar atualizaWanted
  }

  public static void imprimeLista(){
    System.out.println("Lista de processos atualizada: ");
    for(int i = 0; i < users.size(); i++){
      System.out.println(" -" + users.get(i));
    }
  }

  /* atualiza filas de WANTED (apenas quando tem o HELD). Atualiza quando alguem sai*/
  public void atualizaWanted(){

  }
  /* decodificar uma resposta (autenticidade) usando chave pública */
  public int decode(String remetente, byte[] m_assinada) throws Exception{ //nao ta pronto
    String ok = new String("OK");
    String no = new String("NO");
    int index = users.indexOf(remetente);

    Signature assinatura = Signature.getInstance("DSA");
    assinatura.initVerify(users_chave.get(index));        //initialize this object for verifing using public key
    assinatura.update(ok.getBytes());                 //updates the data to be verified by a byte
    boolean verificadoOK = assinatura.verify(m_assinada); //verifies the passed-in signature.
    if(verificadoOK)
      return 2;
    else{
      assinatura.update(no.getBytes());
      boolean verificadoNO = assinatura.verify(m_assinada);
      if(verificadoNO)
        return 1;
      else //a assinatura não foi verificada
        return 0;
    }
  }
}

/*------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------Publisher---------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------*/
/*envio de mensagens*/
class Publisher extends Thread{

  MulticastSocket socket;
  InetAddress grupo;

  public String nome;
  public PublicKey chave_publica;
  public PrivateKey chave_privada;

  // RELEASED, WANTED, HELD
  String estadoSC1;
  String estadoSC2;

  public boolean requisito_n3;
  public boolean esperando_resposta;
  public boolean sair;

  /* definir as variáveis de id e chave pública*/
  public Publisher(String meu_nome, PublicKey pub, PrivateKey priv, MulticastSocket socket, InetAddress grupo){
    this.nome = meu_nome;
    this.chave_publica = pub;
    this.chave_privada = priv;
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
    //nao pode querer (WANTED) acessar as duas SC ao mesmo tempo
    //nao pode digitar qualquer coisa
    String menu1 = "Digite o número correspondente à sua escolha:\n1-Acessar SC1\n2-Acessar SC2\n3-Sair";
    String menu2 = "Digite o número correspondente à sua escolha:\n1-Liberar SC\n2-Sair";
    String invalido = "Este valor digitado é inválido!";

    try{
      while(!sair){

          //caso estadoSC for HELD: (liberar SC, sair)
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
            }
          }

          //caso estadoSC for RELEASED: (acessar SC1, acessar SC2, sair)
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
              estadoSC2 = "WANTED";
              enviarPedido(2);
              esperando_resposta = true;
            }
            else if (userInput.equals("3")){
              enviarSaida();
              sair = true;
            }
            else{
              System.out.println(invalido);
            }
          }
      }
    }
  catch(IOException e){System.out.println("IO: " + e.getMessage());}
  catch(Exception e){System.out.println("IO: " + e.getMessage());}

  }
  /* enviar id e chave pública na forma "X entrou {chave_publica}", chave publica tem o tamanha de 94 bytes */
  public void enviarInfo() throws Exception{
    String m = nome + " entrou ";
    byte[] b_m = m.getBytes();
    byte[] b_pub = chave_publica.getEncoded();
    byte[] mensagem = new byte[b_m.length + b_pub.length];

    System.arraycopy(b_m, 0, mensagem, 0, b_m.length);
    System.arraycopy(b_pub, 0, mensagem, b_m.length, b_pub.length);
    enviarDatagrama(mensagem);
  }
  /* enviar pedido à seção crítica na forma <Tempo,Id> MUDAR*/
  public void enviarPedido(int sc) throws IOException{
    String m = nome + " pede acesso a SC" + sc;
    byte[] b_m = m.getBytes();
    enviarDatagrama(b_m);
  }
  /* enviar resposta a pedido de SC */
  public void enviarResposta(String mensagem) throws Exception{
    //precisa ser encoded, pode ser só uma parte da mensagem encoded
    System.out.println(mensagem);
    String[] s = mensagem.split(" ");
    String nome_respondeu = s[0];
    String sc = s[4];

    // determina qual sera a resposta baseado nas variaveis estadoSC
    String resposta = new String();
    if(sc.equals("SC1")){
      if(estadoSC1.equals("RELEASED") || estadoSC1.equals("WANTED"))
        resposta = "OK";
      else  //processo possui o acesso à SC1
        resposta = "NO";
    }
    else if(sc.equals("SC2")){
      if(estadoSC1.equals("RELEASED") || estadoSC1.equals("WANTED"))
        resposta = "OK";
      else  //processo possui o acesso à SC2
        resposta = "NO";
    }

    // unir o nome à resposta codificada
    byte[] nb = nome.getBytes();  //nome proprio em bytes
    byte[] rb = encode(resposta); //resposta em bytes m_assinada
    byte[] resultado = new byte[nb.length + rb.length];

    System.arraycopy(nb, 0, resultado, 0, nb.length);
    System.arraycopy(rb, 0, resultado, nb.length, rb.length);

    enviarDatagrama(resultado);

    //coloca na lista WANTED
  }

  /* enviar lista de WANTED daquela SC */
  public void transferirSC(){

  }

  /* envia anuncio de saida na forma "X saiu" */
  public void enviarSaida() throws Exception{
    String m = nome + " saiu";
    byte[] b_m = m.getBytes();
    sair = true;
    enviarDatagrama(b_m);
  }

  /* envio a nível de socket qualquer datagrama */
  public void enviarDatagrama(byte[] m) throws IOException{
    DatagramPacket messageOut = new DatagramPacket(m, m.length, grupo, 6789);
    socket.send(messageOut);
  }

  /* encodificar resposta usando chave privada */
  public byte[] encode(String mensagem) throws Exception{
    Signature assinatura = Signature.getInstance("RSA");
    assinatura.initSign(chave_privada);
    assinatura.update(mensagem.getBytes());
    byte[] m_assinada = assinatura.sign();
    System.out.println(new String(m_assinada));
    return(m_assinada);
  }

}
