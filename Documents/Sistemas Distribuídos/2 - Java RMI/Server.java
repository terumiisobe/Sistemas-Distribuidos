/* ALTERNATIVE Server from the travel agency  - USING CLASSES FOR TICKETS, HOTELS AND PACKAGES*/
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.*;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Server{

  static ServerImplement server;
  static Registry NameServiceRef;

  public static void main(String args[]) throws Exception{
    initialize();

    // waiting for travel agency action
    while(true){
      // show initial options
      showOptions(0);
      String user_input = System.console().readLine();

      /* Add new flight */
      if(user_input.equals("1")){
        showOptions(1);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        registerFlight();
      }

      /* Add new accomodation */
      else if(user_input.equals("2")){
        showOptions(2);
        user_input = System.console().readLine();
        if( user_input.equals("0"))
          continue;
        registerAccomodation();
      }

      /* Remove flight */
      else if(user_input.equals("3")){
        showOptions(3);
        user_input = System.console().readLine();
        removeFlight(user_input);
      }

      /* Remove hotel */
      else if(user_input.equals("4")){
        showOptions(4);
        user_input = System.console().readLine();
        removeAccomodation(user_input);
      }

      /* Show all flights */
      else if(user_input.equals("5"))
        checkList(0);

      /* Show all accomodation */
      else if(user_input.equals("6"))
        checkList(1);

  }
}

  /* Initializes basic variables */
  static void initialize() throws Exception{
    int port = 1099;
    NameServiceRef = LocateRegistry.createRegistry(port);
    server = new ServerImplement();
    Naming.rebind("Server", server);

    System.out.println("Server set up."); //test
  }

  /* Show optins for travel agency */
  static void showOptions(int option) throws Exception{
    String cover = "What do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Add flight\n  2 - Add accomodation.\n  3 - Remove plane ticket.\n  4 - Remove accomodation.\n  5 - Show all flights.\n  6 - Show all hotels.\n";
    String options1 = "**NEW FLIGHT**\nType flight information as below. Or press 0 to go back.\n";
    String options2 = "**NEW ACCOMODATION**\nType accomodation information as below. Or press 0 to go back.\n";
    String options3 = "Type the ID of the flight you want to remove.\n";
    String options4 = "Type the ID of the hotel you want to remove.\n";

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

    else
      System.out.println("error in function showOptions"); //test
  }

  /* Prints everything in lists (0 - flights, 1 - hotels)*/
  static void checkList(int list){
    System.out.println("\t--*--");
    if(list == 0){
      for(int i = 0; i < server.all_flights.size(); i++){
        System.out.println("\tID:" + i);
        server.all_flights.get(i).printInfo();
        System.out.println("\t--*--\n");
      }
    }
    else if(list == 1){
      for(int i = 0; i < server.all_hotels.size(); i++){
        System.out.println("\tID: " + i);
        server.all_hotels.get(i).printInfo();
        System.out.println("\t--*--\n");
      }
    }
  }

  /* Register new flight */
  static void registerFlight() throws Exception{
    // get origin place
    System.out.println("Type origin:\n");
    String origin = System.console().readLine();
    // get destination place
    System.out.println("Type destination:\n");
    String destination = System.console().readLine();
    // get departure date
    System.out.println("Type departure date like dd/mm/yyyy:\n");
    String departure = System.console().readLine();
    // get number of tickets available
    System.out.println("Type number of tickets:\n");
    String qty = System.console().readLine();
    int quantity = Integer.parseInt(qty);
    // get price of each ticket
    System.out.println("Type price:\n");
    String price = System.console().readLine();

    Flight new_flight = new Flight(origin, destination, departure, quantity, price);
    server.addToList(0, new_flight, null);
  }

  /* Register new accomodation */
  static void registerAccomodation() throws Exception{
    // get hotel location
    System.out.println("Type location:\n");
    String place = System.console().readLine();
    // get number of rooms available
    System.out.println("Type number of rooms:\n");
    String r = System.console().readLine();
    int rooms = Integer.parseInt(r);
    // get price of room per day
    System.out.println("Type price:\n");
    String price = System.console().readLine();

    Accomodation new_accomodation = new Accomodation(place, rooms, price);
    server.addToList(1, null, new_accomodation);
  }

  /* Remove flight */
  static void removeFlight(String ID){
    int id = Integer.parseInt(ID);
    if(id >= server.all_flights.size()){
      System.out.println("This flight does not exist!");
      return;
    }
    server.removeFromList(0, id);
  }

  /* Remove accomodation */
  static void removeAccomodation(String ID){
    int id = Integer.parseInt(ID);
    if(id >= server.all_hotels.size()){
      System.out.println("This accomodation does not exist!");
      return;
    }
    server.removeFromList(1, id);
  }
}

interface ServerInterface extends Remote{
  void searchFlights(ClientInterface client) throws Exception;
  void searchHotels(ClientInterface client) throws Exception;
  //void subscribe();
  //void unsubscribe();
  void buyTicket(ClientInterface client, int ticket, int qty) throws Exception;
  void bookRoom(ClientInterface client, int room, int qty) throws Exception;
  void buyPackage() throws RemoteException;
}

interface ClientInterface extends Remote{
  void Notify() throws Exception;
  void echo(String message) throws Exception;
}

class ServerImplement extends UnicastRemoteObject implements ServerInterface{

  static List<Flight> all_flights;
  static List<Accomodation> all_hotels;

  /*constructor*/
  public ServerImplement() throws RemoteException{
    all_flights = new ArrayList<Flight>();
    all_hotels = new ArrayList<Accomodation>();
  }

  /* Search function called by user */
  public void searchFlights(ClientInterface client) throws Exception{
    for(int i = 0;i < all_flights.size(); i++){
      client.echo("\t--*--");
      client.echo("\tID: " + i);
      client.echo("\tOrigin: " + all_flights.get(i).origin);
      client.echo("\tDestination: " + all_flights.get(i).destination);
      client.echo("\tDeparture date: " + all_flights.get(i).departure);
      client.echo("\tPrice: " + all_flights.get(i).price);
    }
    client.echo("\t--*--\n");
  }

  /* Search function called by user */
  public void searchHotels(ClientInterface client) throws Exception{
    for(int i = 0;i < all_hotels.size(); i++){
      client.echo("\t--*--");
      client.echo("\tID: " + i);
      client.echo("\tLocation: " + all_hotels.get(i).place);
      client.echo("\tPrice: " + all_hotels.get(i).price);
    }
    client.echo("\t--*--\n");
  }

  /* Buy flight ticket */
  public void buyTicket(ClientInterface client, int ticket, int qty) throws Exception{
    int av = all_flights.get(ticket).quantity;
    all_flights.get(ticket).quantity = av - qty;
    if(qty == 0)
      client.echo("No ticket was bought.");
    else if(qty > av)
      client.echo("That quantity is not available!");
    else{
      if(all_flights.get(ticket).quantity == 0)
        removeFromList(0, ticket);
      System.out.println("Flight ticket bought.");
      client.echo("Flight ticket bought sucessfully!");
    }
  }

  /* Book hotel room */
  public void bookRoom(ClientInterface client, int room, int qty) throws Exception{
    int av = all_hotels.get(room).rooms;
    all_hotels.get(room).rooms = av - qty;
    if(qty == 0)
      client.echo("No room was booked.");
    else if(qty > av)
      client.echo("That quantity is not available!");
    else{
      if(all_hotels.get(room).rooms == 0)
        removeFromList(1, room);
      System.out.println("Room booked.");
      client.echo("Hotel room booked sucessfully!");
    }
  }

  public void buyPackage() throws RemoteException{
    
  }

  /* Subscribe to mailing list (called by user)*/
  public void subscribe(){
    System.out.println("You have been subscribed!");
  }

  /* Unsubscribe to mailing list (called by user)*/
  public void unsubscribe(){
    System.out.println("You have been unsubscribed!");
  }

  /* List manipulation function add*/
  public void addToList(int list, Flight new_flight, Accomodation new_accomodation){
    //add to all_flights
    if(list == 0){
      all_flights.add(new_flight);
      System.out.println("Flight added sucessfuly!\n");
    }
    //add to all_hotels
    else if(list == 1){
      all_hotels.add(new_accomodation);
      System.out.println("Accomodation added sucessfuly!\n");
    }
  }

  /* List manipulation function remove*/
  public void removeFromList(int list, int id){
    //remove from all_flights
    if(list == 0){
      all_flights.remove(id);
      System.out.println("Flight sucessfuly removed!\n");
    }
    else if(list == 1){
      all_hotels.remove(id);
      System.out.println("Accomodation sucessfuly removed!\n");
    }
  }
}

class Flight{
  String origin;
  String destination;
  String departure;
  int quantity;
  String price;

  public Flight(String ori, String des, String dep, int qty, String pri) throws Exception{
    this.origin = ori;
    this.destination = des;
    this.departure = dep;
    this.quantity = qty;
    this.price = pri;
  }

  /* Shows flight information */
  public void printInfo(){
    System.out.println("\tOrigin: " + origin);
    System.out.println("\tDestination: " + destination);
    System.out.println("\tDeparture date: " + departure);
    System.out.println("\tPrice: " + price);
    System.out.println("\tQuantity: " + quantity);
  }
}

class Accomodation{
  String place;
  int rooms; // number of rooms
  String price;

  public Accomodation(String pla, int r, String pri) throws Exception{
    this.place = pla;
    this.rooms = r;
    this.price = pri;
  }

  /* Shows accomodation information */
  public void printInfo(){
    System.out.println("\tLocation: " + place);
    System.out.println("\tPrice: " + price);
    System.out.println("\tNumber of rooms: " + rooms);
  }
}

class Packages{
  int id;
  Flight planeTicket;
  Accomodation accomodation;
  public Packages() throws Exception{

  }
}
