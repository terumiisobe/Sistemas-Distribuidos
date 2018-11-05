/* ALTERNATIVE Server from the travel agency  - USING CLASSES FOR TICKETS, HOTELS AND PACKAGES*/
//https://jersey.github.io/documentation/latest/getting-started.html
//https://www.youtube.com/watch?v=EfEUDAHgrGQ
package com.example;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Object;
//import java.rmi.registry.*;
//import java.rmi.server.UnicastRemoteObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Server{

  static ServerImplement server;

  static int flight_id_control;
  static int hotel_id_control;

  public static final String BASE_URI = "http://localhost:8080/airplane/";
  /**
   * Main method.
   * @param args
   * @throws IOException
   */
  public static void main(String args[]) throws Exception{
    initialize();
    HttpServer server = startServer();
    System.out.println(String.format("Jersey app started with WADL available at "
            + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
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
/**
 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
 * @return Grizzly HTTP server.
 */
 public static HttpServer startServer() {
    final ResourceConfig rc = new ResourceConfig().packages("com.example");
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
  }

  /* Initializes basic variables */
  static void initialize() throws Exception{
    int port = 1099;
    //NameServiceRef = LocateRegistry.createRegistry(port);
    //server = new ServerImplement();
    //Naming.rebind("Server", server);
    flight_id_control = 1;
    hotel_id_control = 1;
  }

  /* Show optins for travel agency */
  static void showOptions(int option) throws Exception{
    String cover = "\nWhat do you want to do? Press the number matching your choice.\n";
    String options = "  1 - Add flight\n  2 - Add accomodation.\n  3 - Remove plane ticket.\n  4 - Remove accomodation.\n  5 - Show all flights.\n  6 - Show all hotels.\n";
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
        server.all_flights.get(i).printInfo();
        System.out.println("\t--*--\n");
      }
    }
    else if(list == 1){
      for(int i = 0; i < server.all_hotels.size(); i++){
        server.all_hotels.get(i).printInfo();
        System.out.println("\t--*--\n");
      }
    }
  }

  /* Register new flight */
  static void registerFlight() throws Exception{
    // get origin place
    System.out.println("Enter origin:");
    String origin = System.console().readLine();
    // get destination place
    System.out.println("Enter destination:");
    String destination = System.console().readLine();
    // get departure date
    System.out.println("Enter departure date like dd/mm/yyyy:");
    String departure = System.console().readLine();
    // get return date
    System.out.println("In case the ticket is ROUND-TRIP, enter return date like dd/mm/yyyy:\n(if ONE-WAY enter 0)");
    String returnDate = System.console().readLine();
    // get number of tickets available
    System.out.println("Enter number of tickets:");
    String qty = System.console().readLine();
    int quantity = Integer.parseInt(qty);
    // get price of each ticket
    System.out.println("Enter price:");
    String p = System.console().readLine();
    float price = Float.parseFloat(p);

    Flight new_flight = new Flight(flight_id_control, origin, destination, departure, returnDate, quantity, price);
    server.addToList(0, new_flight, null);
    flight_id_control++;
  }

  /* Register new accomodation */
  static void registerAccomodation() throws Exception{
    // get hotel location
    System.out.println("Enter location:");
    String place = System.console().readLine();
    // get number of rooms available
    System.out.println("Enter number of rooms:");
    String r = System.console().readLine();
    int rooms = Integer.parseInt(r);
    // get price of room per day
    System.out.println("Enter price:");
    String p = System.console().readLine();
    float price = Float.parseFloat(p);

    Accomodation new_accomodation = new Accomodation(hotel_id_control, place, rooms, price);
    server.addToList(1, null, new_accomodation);
    hotel_id_control++;
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

/*interface ServerInterface extends Remote{
  int searchFlights(ClientInterface client) throws Exception;
  int searchHotels(ClientInterface client) throws Exception;
  void subscribe(ClientInterface client, String type, String destination, float price) throws Exception;
  void unsubscribe(ClientInterface client, String id) throws Exception;
  int showSubscriptions(ClientInterface client) throws Exception;
  void buyTicket(ClientInterface client, int ticket, int qty) throws Exception;
  void bookRoom(ClientInterface client, int room, int qty) throws Exception;
  void buyPackage(ClientInterface client, int ticket, int room) throws Exception;
}*/

/*interface ClientInterface extends Remote{
  void notifyEvent(String type, String destination, float price) throws Exception;
  void echo(String message) throws Exception;
}*/

class ServerImplement{

  static List<Flight> all_flights;
  static List<Accomodation> all_hotels;

  /*constructor*/
  public ServerImplement(){
    all_flights = new ArrayList<Flight>();
    all_hotels = new ArrayList<Accomodation>();
  }

  /* Search function called by user */
  @Path("searchFlights")
  public int searchFlights() throws Exception{
    for(int i = 0;i < all_flights.size(); i++){
      client.echo("\t--*--");
      client.echo("\t" + all_flights.get(i).type);
      client.echo("\tID: " + all_flights.get(i).id);
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
  @Path("searchHotels")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public int searchHotels() throws Exception{
    /*for(int i = 0;i < all_hotels.size(); i++){
      client.echo("\t--*--");
      client.echo("\tID: " + all_hotels.get(i).id);
      client.echo("\tLocation: " + all_hotels.get(i).place);
      client.echo("\tPrice: " + all_hotels.get(i).price);
    }
    client.echo("\t--*--\n");
    if(all_hotels.size() == 0)
      return 0;
    else
      return 1;*/
    return "searchHotels func called!\n";
  }

  /* Buy flight ticket */
  public synchronized void buyTicket(int ticket, int qty) throws Exception{
    // index keeps selected ticket position
    int index = 12345;
    for(int i = 0; i < all_flights.size(); i++){
      if(all_flights.get(i).id == ticket){
        index = i;
        break;
      }
    }
    // no ticket was found
    if(index == 12345){
      //client.echo("Ticket unavailable.");
      return;
    }

    int av = all_flights.get(index).quantity;
    /*if(qty == 0){
      //client.echo("No ticket was bought.");
    }
    else if(qty > av)
      //client.echo("That quantity is not available!");
    else{
      all_flights.get(index).quantity = av - qty;
      if(all_flights.get(index).quantity == 0)
        removeFromList(0, index);

      System.out.println("Flight ticket bought.");
      //client.echo("Flight ticket purchased successfully!");
    }*/
  }

  /* Book hotel room */
  public synchronized void bookRoom(int room, int qty) throws Exception{
    // index keeps selected hotel position
    int index = 12345;
    for(int i = 0; i < all_hotels.size(); i++){
      if(all_hotels.get(i).id == room){
        index = i;
        break;
      }
    }
    // no hotel was found
    if(index == 12345){
      //client.echo("Hotel unavailable.");
      return;
    }

    int av = all_hotels.get(index).rooms;
    /*if(qty == 0)
      //client.echo("No room was booked.");
    else if(qty > av)
      //client.echo("That quantity is not available!");
    else{
      all_hotels.get(index).rooms = av - qty;
      if(all_hotels.get(index).rooms == 0)
        removeFromList(1, index);

      System.out.println("Room booked.");
      //client.echo("Hotel room booked successfully!");
    }*/
  }

  /* Buy package (ticket + hotel) */
  public synchronized void buyPackage(int ticket, int room) throws Exception{
    if(ticket >= all_flights.size() || room >= all_hotels.size()){
      //client.echo("The ticket or the hotel is unavailable.");
      return;
    }
    all_flights.get(ticket).quantity--;
    all_hotels.get(room).rooms--;

    if(all_flights.get(ticket).quantity == 0)
      removeFromList(0, ticket);
    if(all_hotels.get(room).rooms == 0)
      removeFromList(1, room);

    System.out.println("Package bought.");
    //client.echo("Package purchased successfully!");
  }

  /* List manipulation function add */
  public void addToList(int list, Flight new_flight, Accomodation new_accomodation){
    //add to all_flights
    if(list == 0){
      all_flights.add(new_flight);
      System.out.println("Flight added successfully!");
    }
    //add to all_hotels
    else if(list == 1){
      all_hotels.add(new_accomodation);
      System.out.println("Accomodation added successfully!");
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
}

class Flight{
  String type;
  String origin;
  String destination;
  String departure;
  String returnDate;
  int quantity;
  float price;

  int id;

  public Flight(int id, String ori, String des, String dep, String ret, int qty, float pri) throws Exception{
    this.id = id;
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
    System.out.println("\tID: " + id);
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

  int id;

  public Accomodation(int id, String pla, int r, float pri) throws Exception{
    this.id = id;
    this.place = pla;
    this.rooms = r;
    this.price = pri;
  }

  /* Shows accomodation information */
  public void printInfo(){
    System.out.println("\tID: " + id);
    System.out.println("\tLocation: " + place);
    System.out.println("\tPrice: " + price);
    System.out.println("\tNumber of rooms: " + rooms);
  }
}
