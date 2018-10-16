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

      /* Show all subscriptions */ //maybe test
      else if(user_input.equals("7")){
        server.showAllSubs();
      }
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
    String cover = "\nWhat do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Add flight\n  2 - Add accomodation.\n  3 - Remove plane ticket.\n  4 - Remove accomodation.\n  5 - Show all flights.\n  6 - Show all hotels.\n  7 - Show all subscriptions.\n";
    String options1 = "\n**NEW FLIGHT**\nEnter flight information as below. Or press 0 to go back.\n";
    String options2 = "\n**NEW ACCOMODATION**\nEnter accomodation information as below. Or press 0 to go back.\n";
    String options3 = "Enter the ID of the flight you want to remove.\n";
    String options4 = "Enter the ID of the hotel you want to remove.\n";

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
    System.out.println("Enter origin:\n");
    String origin = System.console().readLine();
    // get destination place
    System.out.println("Enter destination:\n");
    String destination = System.console().readLine();
    // get departure date
    System.out.println("Enter departure date like dd/mm/yyyy:\n");
    String departure = System.console().readLine();
    // get return date
    System.out.println("In case the ticket is ROUND-TRIP, enter return date like dd/mm/yyyy:\n(if ONE-WAY enter 0)\n");
    String returnDate = System.console().readLine();
    // get number of tickets available
    System.out.println("Enter number of tickets:\n");
    String qty = System.console().readLine();
    int quantity = Integer.parseInt(qty);
    // get price of each ticket
    System.out.println("Enter price:\n");
    String p = System.console().readLine();
    float price = Float.parseFloat(p);

    Flight new_flight = new Flight(origin, destination, departure, returnDate, quantity, price);
    server.addToList(0, new_flight, null);

    checkNotifications(0, new_flight, null);
  }

  /* Register new accomodation */
  static void registerAccomodation() throws Exception{
    // get hotel location
    System.out.println("Enter location:\n");
    String place = System.console().readLine();
    // get number of rooms available
    System.out.println("Enter number of rooms:\n");
    String r = System.console().readLine();
    int rooms = Integer.parseInt(r);
    // get price of room per day
    System.out.println("Enter price:\n");
    String p = System.console().readLine();
    float price = Float.parseFloat(p);

    Accomodation new_accomodation = new Accomodation(place, rooms, price);
    server.addToList(1, null, new_accomodation);

    checkNotifications(1, null, new_accomodation);
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

  /* Check for notifications */
  static void checkNotifications(int type, Flight new_flight, Accomodation new_hotel) throws Exception{
    List<Event> notifyFlight = new ArrayList<Event>();
    List<Event> notifyHotel = new ArrayList<Event>();

    for(int i = 0; i < server.all_events.size(); i++){
      //a new flight adding can trigger NEW TICKETS or NEW PACKAGES notifications
      if(type == 0){
        if(server.all_events.get(i).type.equals("NEW TICKETS") && server.all_events.get(i).destination.equals(new_flight.destination) && server.all_events.get(i).max_price >= new_flight.price){
          System.out.println("found a match for ticket."); //test
          //notifyFlight
          server.all_events.get(i).client_id.notify();
        }
        if(server.all_events.get(i).type.equals("NEW PACKAGES") && server.all_events.get(i).destination.equals(new_flight.destination)){
          for(int j = 0; j < server.all_hotels.size(); j++){
            if(server.all_hotels.get(j).place.equals(new_flight.destination) && server.all_events.get(i).max_price >= new_flight.price+server.all_hotels.get(j).price){
              //notify package
              server.all_events.get(i).client_id.notify();
            }
          }
        }
      }
      //a new hotel adding can trigger NEW HOTELS or NEW PACKAGES notifications
      else if(type == 1){
        if(server.all_events.get(i).type.equals("NEW HOTELS") && server.all_events.get(i).destination.equals(new_hotel.place) && server.all_events.get(i).max_price >= new_hotel.price){
          System.out.println("found a match for hotel."); //test
          //notifyHotel
          server.all_events.get(i).client_id.notify();
        }
        if(server.all_events.get(i).type.equals("NEW PACKAGES") && server.all_events.get(i).destination.equals(new_hotel.place)){
          for(j = 0; j < server.all_flights.size(); j++){
            if(server.all_flights.get(j).destination.equals(new_hotel.place) && server.all_events.get(i).max_price >= new_hotel.price+server.all_flights.get(j).price){
              server.all_events.get(i).client_id.notify();
              //notify package
            }
          }
        }
      }

    }
  }
}

interface ServerInterface extends Remote{
  int searchFlights(ClientInterface client) throws Exception;
  int searchHotels(ClientInterface client) throws Exception;
  void subscribe(ClientInterface client, String type, String destination, float price) throws Exception;
  void unsubscribe(ClientInterface client, String id) throws Exception;
  int showSubscriptions(ClientInterface client) throws Exception;
  void buyTicket(ClientInterface client, int ticket, int qty) throws Exception;
  void bookRoom(ClientInterface client, int room, int qty) throws Exception;
  void buyPackage(ClientInterface client, int ticket, int room) throws Exception;
}

interface ClientInterface extends Remote{
  void Notify() throws Exception;
  void echo(String message) throws Exception;
}

class ServerImplement extends UnicastRemoteObject implements ServerInterface{

  static List<Flight> all_flights;
  static List<Accomodation> all_hotels;

  public List<Event> all_events;

  /*constructor*/
  public ServerImplement() throws RemoteException{
    all_flights = new ArrayList<Flight>();
    all_hotels = new ArrayList<Accomodation>();
    all_events = new ArrayList<Event>();
  }

  /* Search function called by user */
  public int searchFlights(ClientInterface client) throws Exception{
    for(int i = 0;i < all_flights.size(); i++){
      client.echo("\t--*--\n");
      client.echo("\t" + all_flights.get(i).type);
      client.echo("\tID: " + i);
      client.echo("\tOrigin: " + all_flights.get(i).origin);
      client.echo("\tDestination: " + all_flights.get(i).destination);
      client.echo("\tDeparture date: " + all_flights.get(i).departure);
      client.echo("\tPrice: " + all_flights.get(i).price);
    }
    client.echo("\t--*--\n");
    if(all_flights.size() == 0)
      return 0;
    else
      return 1;
  }

  /* Search function called by user */
  public int searchHotels(ClientInterface client) throws Exception{
    for(int i = 0;i < all_hotels.size(); i++){
      client.echo("\t--*--\n");
      client.echo("\tID: " + i);
      client.echo("\tLocation: " + all_hotels.get(i).place);
      client.echo("\tPrice: " + all_hotels.get(i).price);
    }
    client.echo("\t--*--\n");
    if(all_hotels.size() == 0)
      return 0;
    else
      return 1;
  }

  /* Buy flight ticket */
  public synchronized void buyTicket(ClientInterface client, int ticket, int qty) throws Exception{
    if(ticket >= all_flights.size()){
      client.echo("Ticket not available.");
      return;
    }
    int av = all_flights.get(ticket).quantity;
    if(qty == 0)
      client.echo("No ticket was bought.");
    else if(qty > av)
      client.echo("That quantity is not available!");
    else{
      all_flights.get(ticket).quantity = av - qty;
      if(all_flights.get(ticket).quantity == 0)
        removeFromList(0, ticket);

      System.out.println("Flight ticket bought.");
      client.echo("Flight ticket purchased sucessfully!");
    }
  }

  /* Book hotel room */
  public synchronized void bookRoom(ClientInterface client, int room, int qty) throws Exception{
    if(room >= all_hotels.size()){
      client.echo("Hotel not available.");
      return;
    }
    int av = all_hotels.get(room).rooms;
    if(qty == 0)
      client.echo("No room was booked.");
    else if(qty > av)
      client.echo("That quantity is not available!");
    else{
      all_hotels.get(room).rooms = av - qty;
      if(all_hotels.get(room).rooms == 0)
        removeFromList(1, room);

      System.out.println("Room booked.");
      client.echo("Hotel room booked sucessfully!");
    }
  }

  /* Buy package (ticket + hotel) */
  public synchronized void buyPackage(ClientInterface client, int ticket, int room) throws Exception{
    if(ticket >= all_flights.size() || room >= all_hotels.size()){
      client.echo("The ticket or the hotel is unavailable.");
      return;
    }
    all_flights.get(ticket).quantity--;
    all_hotels.get(room).rooms--;

    if(all_flights.get(ticket).quantity == 0)
      removeFromList(0, ticket);
    if(all_hotels.get(room).rooms == 0)
      removeFromList(1, room);

    System.out.println("Package bought.");
    client.echo("Package purchased sucessfully!");
  }

  /* Subscribe to mailing list (called by user) */
  public void subscribe(ClientInterface client, String type, String destination, float price) throws Exception{
    Event new_event = new Event(client, type, destination, price);
    all_events.add(new_event);
    System.out.println("Client subscribed.");
    client.echo("You have been subscribed!");
  }

  /* Show client subcriptions */
  public int showSubscriptions(ClientInterface client) throws Exception{
    int count = 1;
    for(int i = 0; i < all_events.size(); i++){
      if(all_events.get(i).client_id.equals(client)){
        client.echo("\t--*--");
        client.echo("\t" + all_events.get(i).type);
        client.echo("\tID: " + count);
        client.echo("\tDestination: " + all_events.get(i).destination);
        client.echo("\tMaximum price: " + all_events.get(i).max_price);
        client.echo("\t--*--\n");
        count++;
      }
    }
    return count-1;
  }

  /* Unsubscribe to mailing list (called by user) */
  public void unsubscribe(ClientInterface client, String id) throws Exception{
    if(id.equals("")){
      client.echo("Invalid value.");
      return;
    }
    int id_event = Integer.parseInt(id);
    int count = 1;
    for(int i = 0; i < all_events.size(); i++){
      if(all_events.get(i).client_id.equals(client)){
        if(count == id_event){
          removeFromList(3, i);
          client.echo("You have been unsubscribed.\n");
          return;
        }
        count++;
      }
    }
    client.echo("Invalid value.");
  }

  /* List manipulation function add */
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

  /* List manipulation function remove */
  public void removeFromList(int list, int id){
    //remove from all_flights
    if(list == 0){
      all_flights.remove(id);
      System.out.println("Flight removed.\n");
    }
    //remove from all_hotels
    else if(list == 1){
      all_hotels.remove(id);
      System.out.println("Accomodation removed.\n");
    }
    //remove from all_events
    else if(list == 3){
      all_events.remove(id);
      System.out.println("Client unsubscribed.\n");
    }
  }

  /* Show all clients subcriptions for server */
  public void showAllSubs() throws Exception{
    for(int i = 0; i < all_events.size(); i++){
      System.out.println("\t--*--");
      System.out.println("\tClient: " + all_events.get(i).client_id);
      System.out.println("\t" + all_events.get(i).type);
      System.out.println("\tID: " + i);
      System.out.println("\tDestination: " + all_events.get(i).destination);
      System.out.println("\tMaximum price: " + all_events.get(i).max_price);
      System.out.println("\t--*--\n");
    }
  }
}

class Flight{
  String type;
  String origin;
  String destination;
  String departure;
  String returnDate;
  int quantity;
  float price;

  public Flight(String ori, String des, String dep, String ret, int qty, float pri) throws Exception{
    this.origin = ori;
    this.destination = des;
    this.departure = dep;
    if(ret.equals("0")){
      this.returnDate = null;
      this.type = "ONE-WAY";
    }
    else{
      this.returnDate = ret;
      this.type = "ROUND-TRIP";
    }
    this.quantity = qty;
    this.price = pri;
  }

  /* Shows flight information */
  public void printInfo(){
    System.out.println("\t" + type);
    System.out.println("\tOrigin: " + origin);
    System.out.println("\tDestination: " + destination);
    System.out.println("\tDeparture date: " + departure);
    if(type == "ROUND-TRIP")
      System.out.println("\tReturn: " + returnDate);
    System.out.println("\tPrice: " + price);
    System.out.println("\tQuantity: " + quantity);
  }
}

class Accomodation{
  String place;
  int rooms; // number of rooms
  float price;

  public Accomodation(String pla, int r, float pri) throws Exception{
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

class Event{
  ClientInterface client_id;
  String type; // NEW FLIGHTS, NEW HOTELS or NEW PACKAGES (both)
  String destination;
  float max_price;

  public Event(ClientInterface id, String type, String dest, float max) throws Exception{
    this.client_id = id;
    this.type = type;
    this.destination = dest;
    this.max_price = max;

    System.out.println("Event registered."); //test
  }
}
