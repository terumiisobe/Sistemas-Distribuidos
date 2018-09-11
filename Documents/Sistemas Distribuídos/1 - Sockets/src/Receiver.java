import java.net.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.security.spec.X509EncodedKeySpec;
import java.lang.Object.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

/* o que precisa ser feito:
    - Listas WANTED e compartilhamento delas
    - Liberação das SC
    - Correção (reset) das variaveis flags
    - Conferir requisitos do PDF
    - Testes diversos
    */
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

  static boolean falhou;

  static List<String> users;          // nome dos usuários online
  static List<PublicKey> users_chave; // chave pública dos usuários online
  static List<String> respostas;      //pares que responderam OK ou NO

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

        if(publish.inicia_timer)
        {
          // timer de 1000ms = 1s
          publish.inicia_timer = false;
          publish.esperando_resposta = true;
          timer = new Timer();
          timer.schedule(new TimerTask(){
            @Override
            public void run() {
              timer.cancel(); //talvez de problema**
              publish.esperando_resposta = false;
              System.out.println("estouro timer");                              //teste
              if(publish.acesso_negado){
                System.out.println("Seu acesso foi negado!");
                publish.acesso_negado = false;
              }
              else if(respostas.size() == users.size()){ // condição de acesso
                if(publish.estadoSC1.equals("WANTED")){
                  publish.estadoSC1 = "HELD";
                }
                else if(publish.estadoSC2.equals("WANTED")){
                  publish.estadoSC2 = "HELD";
                }
                System.out.println("Sucesso!");
              }
              else{ // acesso nao foi negado, mas alguem não respondeu, significa falha do par
                for(int i = 0; i < users.size(); i++) {
                  if(!respostas.contains(users.get(i))){
                    System.out.println("Houve falha no par " + users.get(i) + "! Pedido cancelado.");
                    users.remove(i);
                    users_chave.remove(i);

                    imprimeLista();
                    //o processo deverá refazer a requisição de acesso em caso de falha do par
                    publish.estadoSC1 = "RELEASED";
                    publish.estadoSC2 = "RELEASED";

                    }
                  }
                }
                respostas.clear();        // remove todos os elementos da lista
              }
          }, 500);
        }

        if(mensagem.contains("entrou")) //ok
          novosUsers(messageIn.getData());

        else if(mensagem.contains("saiu")) //ok
          retirarUsers(messageIn.getData());

        else if(mensagem.contains("pede acesso"))
          publish.enviarResposta(new String(messageIn.getData()));

        else if(mensagem.contains("respondeu"))
        {
          analisaResposta(messageIn.getData());
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

    falhou = false;

    users = new ArrayList<String>();
    users_chave = new ArrayList<PublicKey>();
    respostas = new ArrayList<String>();

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

        KeyFactory fact = KeyFactory.getInstance("RSA");
        PublicKey pub = fact.generatePublic(new X509EncodedKeySpec(pubb));

        users_chave.add(pub);     //adiciona chave publica na lista
        users.add(novo_processo); //adiciona novo usuário a lista

        if(!novo_processo.equals(meu_nome))
          publish.enviarInfo();

        imprimeLista();

      }

      //inicia apenas quando existem 3 processos ou mais
      if(users.size() > 1){                                                     //mudar para 2
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

  /* testar autenticidade do par */
  public static int testarAutenticidade(byte[] m) throws Exception{
    byte[] m_assinada = new byte[64];  //a mensagem assinada tem 64 bytes
    byte[] restb = new byte[500];      //mesmo tamanho do buffer de dados
    int n_espaco = 0;
    char espaco = ' ';
    int i;

    for(i = 0; i < m.length; i++){
      if(m[i] == (byte)espaco)
        n_espaco++;
      if(n_espaco==2) //espaco depois da palavra chave "respondeu"
        break;
    }
    i++;             //posicao inicial da mensagem assinada                     //(ok)

    System.arraycopy(m, 0, restb, 0, i);
    System.arraycopy(m, i, m_assinada, 0, 64);

    String rest = new String(restb);
    String[] s = rest.split(" ");
    String processo_respondeu = s[0];

    if(decode(processo_respondeu, m_assinada) == 2){
      System.out.println("**" + rest + " OK (verified)");
      respostas.add(processo_respondeu);
      return 2;
    }
    else if(decode(processo_respondeu, m_assinada) == 1){
      System.out.println("**" + rest + " NO (verified)");
      respostas.add(processo_respondeu);
      return 1;
    }
    else{
      System.out.println("(message not verified)");
      return 0;
    }

  }
  /* analisa respostas recebidas, apenas quando pede acesso */
  public static void analisaResposta(byte[] m) throws Exception{
    //caso o próprio processo tenha feito o pedido
    if((publish.estadoSC1.equals("WANTED") || publish.estadoSC2.equals("WANTED")) && publish.esperando_resposta){ //caracteriza pedido
      if(testarAutenticidade(m) == 1){      //assinatura verificada com resposta NO
        publish.acesso_negado = true;       //uma resposta negativa é suficiente para negar o acesso
      }
    }
    //caso não tenha feito o pedido
    else{
      testarAutenticidade(m);
    }
  }

  /* decodificar uma resposta (autenticidade) usando chave pública */
  public static int decode(String remetente, byte[] m_assinada) throws Exception{
    String ok = new String("OK");
    String no = new String("NO");
    int index = users.indexOf(remetente);
    Signature assinatura = Signature.getInstance("SHA256withRSA");
    assinatura.initVerify(users_chave.get(index));        //initialize this object for verifing using public key
    assinatura.update(ok.getBytes());                     //updates the data to be verified by a byte
    boolean verificadoOK = assinatura.verify(m_assinada); //verifies the passed-in signature.
    if(verificadoOK)
      return 2;

    else{
      assinatura = Signature.getInstance("SHA256withRSA");
      assinatura.initVerify(users_chave.get(index));
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

  // O processo poderá ter acesso a apenas uma SC de cada vez
  // portanto, é necessário apenas uma fila de espera.
  // No caso do processo ser HELD ele irá inserir os elementos que querem acessar o recurso.

  LinkedList<String> SC_espera;           // usuários na fila de SC1
  LinkedList<Long> timestamp_espera; // timestamp de pedido daqueles usuários

  public boolean inicia_timer;
  public boolean acesso_negado;
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

    SC_espera = new LinkedList<String>();
    timestamp_espera = new LinkedList<Long>();

    this.acesso_negado = false;
    this.inicia_timer = false;
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

          System.out.println("SC1: " + estadoSC1 + " SC2: " + estadoSC2);
          //caso estadoSC for HELD: (liberar SC, sair)
          if(estadoSC1.equals("HELD") || estadoSC2.equals("HELD"))
          {
            System.out.println(menu2);
            String userInput = System.console().readLine();
            /* Liberar */
            if(userInput.equals("1")){
              transferirSC();
            }
            /* Sair */
            else if (userInput.equals("2")){
              if(estadoSC1.equals("HELD") || estadoSC2.equals("HELD")){
                transferirSC();
                enviarSaida();
                sair = true;
            }
              else{
                System.out.println(invalido);
              }
            }
          }

          //caso estadoSC for RELEASED: (acessar SC1, acessar SC2, sair)
          else
          {
            System.out.println(menu1);
            String userInput = System.console().readLine();
            /* Acessar SC1 */
            if(userInput.equals("1")){
              //caso já esteja esperando outra SC
              if(estadoSC2.equals("WANTED")){
                System.out.println("Você já está na fila!");
                continue;
              }

              estadoSC1 = "WANTED";
              enviarPedido(1);
              inicia_timer = true;
            }
            /* Acessar SC2 */
            else if (userInput.equals("2")){
              //caso já esteja esperando outra SC
              if(estadoSC1.equals("WANTED")){
                System.out.println("Você já está na fila!");
                continue;
              }

              estadoSC2 = "WANTED";
              enviarPedido(2);
              inicia_timer = true;
            }
            /* Sair */
            else if (userInput.equals("3")){
              enviarSaida();
              sair = true;
            }
            else{
              System.out.println(invalido);
            }
          }
          //delay necessário para impedir o aparecimento do menu antes da atualização das variavel de estadoSC
          sleep(700);

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
    LocalTime agora = LocalTime.now();
    DateTimeFormatter form = DateTimeFormatter.ofPattern("HH:mm:AAAAA");
    //DateFormat format = new SimpleDateFormat("HH:mm:ss.AAAAAA");
    //Instant instante = Instant.now();
    //long agora = instante.toEpochMilli();
    String m = nome + " pede acesso a SC" + sc + " em " + agora.format(form);
    byte[] b_m = m.getBytes();
    enviarDatagrama(b_m);
  }

  /* enviar resposta a pedido de SC */
  public void enviarResposta(String mensagem) throws Exception{
    // mensagem é da forma " X pede acesso a Y em Z"
    System.out.println("**" + mensagem);
    String[] s = mensagem.split(" ");
    String nome_respondeu = s[0]; //usuario que está pedindo acesso

    // determina qual sera a resposta baseado nas variaveis estadoSC
    int resposta = 2;
    if(mensagem.contains("SC1")){
      if(estadoSC1.equals("RELEASED") || estadoSC1.equals("WANTED"))
        resposta = 1;
      else  //processo possui o acesso à SC1
        resposta = 0;
    }
    else if(mensagem.contains("SC2")){
      if(estadoSC1.equals("RELEASED") || estadoSC1.equals("WANTED"))
        resposta = 1;
      else  //processo possui o acesso à SC2
        resposta = 0;
    }

    // unir o nome à resposta codificada
    String m = nome + " respondeu ";
    byte[] nb = m.getBytes();
    byte[] rb = encode(resposta); //resposta em bytes m_assinada sempre com 64 bytes
    byte[] resultado = new byte[nb.length + rb.length];

    System.arraycopy(nb, 0, resultado, 0, nb.length);
    System.arraycopy(rb, 0, resultado, nb.length, rb.length);

    enviarDatagrama(resultado);
    System.out.println(" parse s[6]: " + s[6]);  //tempo em HH:mm:AAAAA
    //coloca na lista WANTED processo que pediram(condição: não colocar o próprio, não incluir se você não é HELD)
    if(!nome_respondeu.equals(nome) &&                           //não deve incluir o próprio nome na listas
      (estadoSC1.equals("HELD") && mensagem.contains("SC1")||    //incluir se pedido é para SC1 e você tem a SC1
       estadoSC2.equals("HELD") && mensagem.contains("SC2")))    //inlcuir se pedido é para SC2 e você tem a SC2
    {
      //s[6] tem a hora
      DateTimeFormatter form = DateTimeFormatter.ofPattern("HH:mm:AAAAA");
      LocalTime tempo = LocalTime.parse(s[6], form);
      //Long tempo = Long.parseLong(s[6]);

      //caso algum processo tenha sido adicionado fora de ordem
      if(tempo < timestamp_espera.get(0)){
       SC_espera.addFirst(nome_respondeu);
      timestamp_espera.addFirst(tempo);
      }
      else{
       SC_espera.add(nome_respondeu);
       timestamp_espera.add(tempo);
      }
    }

    for(int i = 0; i < SC_espera.size(); i++){                                  //teste
      System.out.println("Lista de espera para " + s[4] + ": ");
      System.out.println(" -" + SC_espera.get(i) + " em " + timestamp_espera.get(i));
    }

  }

  /* enviar lista de WANTED com assinatura digital */
  public void transferirSC(){
    if(estadoSC1.equals("HELD")){
      estadoSC1 = "RELEASED";

    }

    else if(estadoSC2.equals("HELD")){
      estadoSC2 = "RELEASED";
    }

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
  public byte[] encode(int resposta) throws Exception{
    Signature assinatura = Signature.getInstance("SHA256withRSA");
    assinatura.initSign(chave_privada);

    String mensagem;
    if(resposta == 1){mensagem = "OK";}
    else{mensagem = "NO";}

    assinatura.update(mensagem.getBytes());
    byte[] m_assinada = assinatura.sign();
    //System.out.println(new String(m_assinada)); teste
    return(m_assinada);
  }

  /* converte hora "HH:mm:AAAAA" para milisegundos*/
  public int converteTempo(String tempo){
    String[] s = tempo.split(":"); //sendo que s[0] é hora, s[1] é minuto e s[2] é milisegundos
    int hora = Integer.parseInteger(s[0]);
    int minuto = Integer.parseInteger(s[1]);
    int mili = Integer.parseInteger(s[2]);
    int tempo_mili = hora*60*60000 + minuto*60000 + mili;
    return tempo_mili;
  }
}
