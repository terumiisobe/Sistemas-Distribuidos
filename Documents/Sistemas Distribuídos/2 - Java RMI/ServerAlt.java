/* ALTERNATIVE Server from the travel agency  - USING CLASSES FOR TICKETS, HOTELS AND PACKAGES*/
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.Date;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class ServerAlt{
  public static void main(String args[]) throws Exception{
    initialize();
    // waiting for travel agency action
    while(true){
      // show initial options
      showOptions(0);
      String userInput = System.console().readLine();

      /* Add new plane ticket */
      if(userInput == "1"){
        showOptions(1);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
        registerTicket();
      }

      /* Add new accomodation */
      else if(userInput == "2"){
        showOptions(2);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
        registerAccomodation();
      }
    }
  }
  /* Initializes basic variables */
  static void initialize() throws Exception{
    int port = 1099;
    Registry NameServiceRef = LocateRegistry.createRegistry(port);
    ServerImplement server = new ServerImplement();
    Naming.rebind("Server", server);

    System.out.println("Server set up."); //test
  }
  /* Show optins for travel agency */
  static void showOptions(int option) throws Exception{
    String cover = "What do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Add plane ticket\n  2 - Add accomodation.\n  3 - Remove plane ticket.\n  4 - Remove accomodation.\n";
    String options1 = "NEW PLANE TICKET\nType plane ticket information as below. Or press 0 to go back.\n";
    String options2 = "NEW ACCOMODATION\nType accomodation information as below. Or press 0 to go back.\n";
    if(option == 0){
      System.out.print(cover);
      System.out.print(options);
    }
    else if(option == 1)
      System.out.print(options1);
    else if(option == 2)
      System.out.print(options2);
    else
      System.out.println("error in function showOptions"); //test
  }
  /* Analize new user addition to files */
  static void checkNewAddition(){
    //check for mispelling, not using commas, number of parameters...
  }

  /* Register new ticket */
  static void registerTicket(){
    System.out.println("Type origin:\n");
    String origin = System.console().readLine();
    System.out.println("Type destination:\n");
    String destination = System.console().readLine();
    System.out.println("Type departure date:\n");
    Date departure = System.console().readLine();
    System.out.println("Type number of tickets:\n");
    int quantity = System.console().readLine();
    System.out.println("Type price:\n");
    float price = System.console().readLine();
    
  }

  /* Register new accomodation */
  static void registerAccomodation(){
    System.out.println("Type place:\n");
    String place = System.console().readLine();
    System.out.println("Type checkin date:\n");
    Date checkin = System.console().readLine();
    System.out.println("Type checkout date:\n");
    Date checkout = System.console().readLine();
    System.out.println("Type number of rooms:\n");
    int availability = System.console().readLine();
    System.out.println("Type price:\n");
    float price = System.console().readLine();
  }
}

interface ServerInterface extends Remote{

}

class ServerImplement extends UnicastRemoteObject implements ServerInterface{
  /*constructor*/
  public ServerImplement() throws RemoteException{

  }
}
class PlaneTicket{
  int id;
  String origin;
  String destination;
  Date departure;
  int quantity;
  float price;
  public PlaneTicket() throws Exception{

  }
  public int buyTicket() throws Exception{

  }
}

class Accomodation{
  int id;
  String place;
  Date checkin;
  Date checkout;
  int availability; // number of rooms
  int quantity; // number of people(2*rooms)
  float price;
  public Accomodation() throws Exception{

  }
  public int buyRoom() throws Exception{

  }
}

class Packages{
  int id;
  PlaneTicket planeTicket;
  Accomodation accomodation;
  public Packages() throws Exception{

  }
  public int buyPackage() throws Exception{

  }
}
