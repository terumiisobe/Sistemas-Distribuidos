import java.rmi.*;
import java.util.*;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Cliente{
  public static void main(String[] args) throws Exception{
    // - obter referencia do serviço de nomes que será executado no servidor(usar
    // a classe LocateRegistry e o método getRegistry).
    // - instanciar classe CliImpl passando como argumento a referência do SN
    int port = 1099;
    Registry referenciaServicoNomes = LocateRegistry.getRegistry();
    CliImpl clienteImpl = new CliImpl(referenciaServicoNomes);
  }
}

interface InterfaceCli extends Remote{
  void echo(String qualquer) throws RemoteException;
}
interface InterfaceServ extends Remote{
  void chamar(String qualquer, InterfaceCli referencia) throws RemoteException;
}

/* Classe Servente */
class CliImpl extends UnicastRemoteObject implements InterfaceCli{

  Registry referenciaRN;
  InterfaceServ servidorRef;
  /* construtor de CliImpl */
  public CliImpl(Registry referencia) throws Exception{
    this.referenciaRN = referencia;
    this.servidorRef = (InterfaceServ)referencia.lookup("Servidor");
    servidorRef.chamar("echo!", this);
  }
  public void echo(String qualquer) throws RemoteException{
    System.out.println("metodo echo do servidor");
    System.out.println(qualquer);
  }
}
