/* Client from the travel agency */
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Client{
  public static void main(String args[]){

  }
}

interface ClientInterface extends Remote{

}

interface ServerInterface extends Remote{
  void searchFlights() throws Exception;
  void searchHotels() throws Exception;
  void subscribe();
  void unsubscribe();
}

class ClientImplement extends UnicastRemoteObject implements ClientInterface{

  /*constructor*/
  public ClientImplement(){

  }
}
