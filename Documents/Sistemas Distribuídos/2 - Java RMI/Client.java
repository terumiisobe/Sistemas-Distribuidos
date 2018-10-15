/* Client from the travel agency */
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Client{

  static Registry NameServiceRef;
  static ClientImplement client;

  public static void main(String args[]) throws Exception{
    initialize();

    // waiting for client action
    while(true){
      // show initial options
      showOptions(0);
      String user_input = System.console().readLine();

      /* Buy flight ticket */
      if(user_input.equals("1")){
        showOptions(1);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        buyTicket();
      }

      /* Book accomodation */
      else if(user_input.equals("2")){
        showOptions(2);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        client.serverReference.searchHotels(client);
      }

      /* Buy package */
      else if(user_input.equals("3")){
        showOptions(3);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        System.out.println("\t--FLIGHT TICKETS--");
        client.serverReference.searchFlights(client);
        System.out.println("\n\t--HOTEL OPENINGS--");
        client.serverReference.searchHotels(client);
      }

      /* Subscribe */
      else if(user_input.equals("4")){
        showOptions(4);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
      }

      /* Unsubscribe */
      else if(user_input.equals("5")){
        showOptions(5);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
      }
    }
  }

  /* Initializes basic variables */
  static void initialize() throws Exception{
    int port = 1099;
    NameServiceRef = LocateRegistry.getRegistry(port);
    client = new ClientImplement(NameServiceRef);

    System.out.println("Client set up."); //test
  }

  /* Show optins for client */
  static void showOptions(int option) throws Exception{
    String cover = "What do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Buy flight.\n  2 - Book hotel.\n  3 - Buy package.\n  4 - Subscribe for event.\n  5 - Unsubscribe.\n";
    String options1 = "**BUY FLIGHT**\nType the flight ID to buy. Or press 0 to go back.\n";
    String options2 = "**BOOK HOTEL**\nType the accomodation ID to buy. Or press 0 to go back.\n";
    String options3 = "**BUY PACKAGE**\nType the flight ID and the accomodation ID you want buy. Or press 0 to go back.\n";
    String options4 = "What are you interested in? Type 1 for new flights, 2 for new hotels or 3 for both.\n";
    String options5 = "You have been unsubscribed.\n";

    if(option == 0){
      System.out.print(cover);
      System.out.print(options);
    }
    else if(option == 1)
      System.out.print(options1);

    else if(option == 2)
      System.out.print(options2);

    else if(option == 3)
      System.out.print(options3);

    else if(option == 4)
      System.out.print(options4);

    else if(option == 5)
      System.out.print(options5);

    else
      System.out.println("error in function showOptions"); //test
  }

  /* Buy flight ticket */
  static void buyTicket() throws Exception{
    client.serverReference.searchFlights(client);
    String id_t = System.console().readLine();
    int id_ticket = Integer.parseInt(id_t);
    System.out.println("How many tickets?");
    String q = System.console().readLine();
    int qty = Integer.parseInt(q);
    client.serverReference.buyTicket(client, id_ticket, qty);
  }

  /* Buy flight ticket */
  static void bookRoom() throws Exception{
    client.serverReference.searchHotels(client);
    String id_h = System.console().readLine();
    int id_hotel = Integer.parseInt(id_h);
    System.out.println("How many rooms?");
    String q = System.console().readLine();
    int qty = Integer.parseInt(q);
    client.serverReference.bookRoom(client, id_hotel, qty);
  }

}

interface ClientInterface extends Remote{
  void Notify() throws Exception;
  void echo(String message) throws Exception;
}

interface ServerInterface extends Remote{
  void buyTicket(ClientInterface client, int ticket, int qty) throws Exception;
  void bookRoom(ClientInterface client, int room, int qty) throws Exception;
  //void buyPackage() throws RemoteException;
  void searchFlights(ClientInterface client) throws Exception;
  void searchHotels(ClientInterface client) throws Exception;
}

class ClientImplement extends UnicastRemoteObject implements ClientInterface{
  Registry NameServiceReference;
  public ServerInterface serverReference;

  /*constructor*/
  public ClientImplement(Registry reference) throws Exception{
    this.NameServiceReference = reference;
    this.serverReference = (ServerInterface)reference.lookup("Server");
  }

  /* Called by user when subscribed event happens */
  public void Notify() throws Exception{
    System.out.println("Event happened!"); //test
  }

  public void echo(String message) throws Exception{
    System.out.println(message);
  }
}
