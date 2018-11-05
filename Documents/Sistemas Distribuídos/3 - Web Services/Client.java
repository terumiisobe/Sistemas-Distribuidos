/* Client written in Java */
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

/*
 - Tickets and rooms can be bought in quantity, but packages are just for one person.
 - You don't get unsubscribed automatically when event happens.
 -
*/
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
        bookRoom();
      }

      /* Buy package */
      else if(user_input.equals("3")){
        showOptions(3);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        buyPackage();
      }

      /* Subscribe */
      else if(user_input.equals("4")){
        showOptions(4);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        subscribe(user_input);
      }

      /* Unsubscribe */
      else if(user_input.equals("5")){
        showOptions(5);
        unsubscribe();
      }
    }
  }

  /* Initializes basic variables */
  static void initialize() throws Exception{
    int port = 1099;
    NameServiceRef = LocateRegistry.getRegistry(port);
    client = new ClientImplement(NameServiceRef);

  }

  /* Show optins for client */
  static void showOptions(int option) throws Exception{
    String cover = "\nWhat do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Buy flight.\n  2 - Book hotel.\n  3 - Buy package.\n  4 - Subscribe for event.\n  5 - Unsubscribe.\n";
    String options1 = "\n**BUY FLIGHT**\nEnter the flight ID to buy. Or press 0 to go back.\n";
    String options2 = "\n**BOOK HOTEL**\nEnter the accomodation ID to buy. Or press 0 to go back.\n";
    String options3 = "\n**BUY PACKAGE**\nEnter the flight ID and the accomodation ID you want buy. Or press 0 to go back.\n";
    String options4 = "\n**SUBSCRIBE**\nWhat are you interested in?\nEnter 1 for new flights, 2 for new hotels or 3 for both. Enter 0 to go back\n";
    String options5 = "\n**UNSUBSCRIBE**\n";

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

  /* Buy flight ticket (calls server function) */
  static void buyTicket() throws Exception{
    if(client.serverReference.searchFlights(client) == 0){
      System.out.println("No tickets found!");
      return;
    }
    String id_t = System.console().readLine();
    if(id_t.equals("0"))
      return;
    int id_ticket = Integer.parseInt(id_t);
    System.out.println("How many tickets?");
    String q = System.console().readLine();
    int qty = Integer.parseInt(q);
    client.serverReference.buyTicket(client, id_ticket, qty);
  }

  /* Book hotel room (calls server function) */
  static void bookRoom() throws Exception{
    if(client.serverReference.searchHotels(client) == 0){
      System.out.println("No hotels found!");
      return;
    }
    String id_h = System.console().readLine();
    if(id_h.equals("0"))
      return;
    int id_hotel = Integer.parseInt(id_h);

    System.out.println("How many rooms?");
    String q = System.console().readLine();
    int qty = Integer.parseInt(q);

    System.out.println("How many people?");
    String ppl = System.console().readLine();

    System.out.println("When is the checkin date?");
    String cin = System.console().readLine();

    System.out.println("When is the checkout date?");
    String cout = System.console().readLine();

    client.serverReference.bookRoom(client, id_hotel, qty);
  }

  /* Buy package (calls server function) */
  static void buyPackage() throws Exception{
    System.out.println("\t--FLIGHT TICKETS--\n");
    int tn = client.serverReference.searchFlights(client);
    System.out.println("\n\t--HOTEL OPENINGS--\n");
    int hn = client.serverReference.searchHotels(client);

    if(tn == 0 || hn == 0){
      System.out.println("No packages found!");
      return;
    }

    System.out.println("Enter the ID of flight ticket you wish to purchase.");
    String id_t = System.console().readLine();
    if(id_t.equals("0"))
      return;
    int id_ticket = Integer.parseInt(id_t);

    System.out.println("Enter the ID of hotel you wish to book.");
    String id_h = System.console().readLine();
    int id_hotel = Integer.parseInt(id_h);

    client.serverReference.buyPackage(client, id_ticket, id_hotel);
  }

  /* Subscribe to event (calls server function) */
  static void subscribe(String t) throws Exception{
    if(!t.equals("1") && !t.equals("2") && !t.equals("3")){
      System.out.println("Invalid value.");
      return;
    }
    String type;
    switch(t){
      case "1":
        type = "NEW FLIGHTS";
        break;
      case "2":
        type = "NEW HOTELS";
        break;
      case "3":
        type = "NEW PACKAGES";
        break;
      default:
        type = "";
    }
    System.out.println("What is the destination of interest?");
    String destination = System.console().readLine();
    System.out.println("What is the maximum price?");
    String p = System.console().readLine();
    float price = Float.parseFloat(p);

    client.serverReference.subscribe(client, type, destination, price);
  }

  /* Unubscribe to event (calls server function) */
  static void unsubscribe() throws Exception{
    if(client.serverReference.showSubscriptions(client) == 0){
      System.out.println("You are not subscribed!\n");
      return;
    }

    System.out.println("Enter de ID of the subcription you want to unsubscribe. Or enter 0 to go back.");
    String user_input = System.console().readLine();
    if(user_input.equals("0"))
      return;

    client.serverReference.unsubscribe(client, user_input);
  }
}

interface ClientInterface extends Remote{
  void echo(String message) throws Exception;
  void notifyEvent(String type, String destination, float price) throws Exception;
}

interface ServerInterface extends Remote{
  void buyTicket(ClientInterface client, int ticket, int qty) throws Exception;
  void bookRoom(ClientInterface client, int room, int qty) throws Exception;
  void buyPackage(ClientInterface client, int ticket, int room) throws Exception;
  int searchFlights(ClientInterface client) throws Exception;
  int searchHotels(ClientInterface client) throws Exception;
  void subscribe(ClientInterface client, String type, String destination, float price) throws Exception;
  void unsubscribe(ClientInterface client, String id) throws Exception;
  int showSubscriptions(ClientInterface client) throws Exception;
}

class ClientImplement extends UnicastRemoteObject implements ClientInterface{
  Registry NameServiceReference;
  public ServerInterface serverReference;

  /*constructor*/
  public ClientImplement(Registry reference) throws Exception{
    this.NameServiceReference = reference;
    this.serverReference = (ServerInterface)reference.lookup("Server");
  }

  /* Called by server to print message */
  public void echo(String message) throws Exception{
    System.out.println(message);
  }

  /* Called by server when subscribed event happens */
  public void notifyEvent(String type, String destination, float price) throws Exception{
    System.out.println("\nLucky! We found a " + type + " matching one of your subscriptions!");
    System.out.println("|Destination: " + destination + "\n|Price: " + price);
    System.out.println("Use menu to purchase this offer. If you do not want to receive more notifications for this event unsubscribe using the menu.");
  }
}
