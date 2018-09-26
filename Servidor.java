import java.rmi.*;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Servidor{
  public static void main(String[] args) throws Exception{
    // - iniciar o serviço de nomes (usar a classe LocateRegistry e o método
    // createRegistry)
    // - criar instância da classe ServImpl e registrar a referência da sua
    // aplicação (tipo InterfaceServ) no serviço de nomes.
    int port = 1099;
    Registry referenciaServicoNomes = LocateRegistry.createRegistry(port);
    ServImpl servidorImpl = new ServImpl();
    Naming.rebind("Servidor", servidorImpl);
    System.out.println("Servidor pronto!");
  }
}

interface InterfaceServ extends Remote{
  void chamar(String qualquer, InterfaceCli referencia) throws RemoteException;
}
interface InterfaceCli extends Remote{
  void echo(String qualquer) throws RemoteException;
}

/* Classe Servente */
class ServImpl extends UnicastRemoteObject implements InterfaceServ
{
  public ServImpl() throws RemoteException{}
  public void chamar(String qualquer, InterfaceCli referencia) throws RemoteException{
    System.out.println("metodo chamar do servidor");
    referencia.echo(qualquer);
  }
}
