/* Server from the travel agency */
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.util.Vector;
import java.lang.Object;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Server{
  public static void main(String args[]) throws Exception{
    initialize();
    // waiting for travel agency action
    while(true){
      // show initial options
      showOptions(0);
      String userInput = System.console().readLine();
      if(userInput == "1"){
        showOptions(1);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
        writeArchive(userInput, "plane-tickets.txt");
      }
      else if(userInput == "2"){
        showOptions(2);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
        writeArchive(userInput, "hotels.txt");
      }
      else if(userInput == "3"){
        showOptions(3);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
      }
      else if(userInput == "4"){
        showOptions(4);
        userInput = System.console().readLine();
        if( userInput == "0")
          continue;
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
    String options1 = "NEW PLANE TICKET\nType plane ticket information as below. Separate the informations using semicolon (;). Or press 0 to go back.\n<Origin>;<Destination>;<Departure date>;<Quantity available>;<Price>\n";
    String options2 = "NEW ACCOMODATION\nType accomodation information as below. Separate the informations using semicolon (;). Or press 0 to go back.\n<Place>;<Quantity available>;<Price>\n";
    String options3 = "REMOVE PLANE TICKET\nType the matching number. Or press 0 to go back.\n"; //show all tickets
    String options4 = "REMOVE ACCOMODATION\nType the matching number. Or press 0 to go back.\n"; //show all hotels
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
  /* Analize new user addition to files */
  static void checkNewAddition(){
    //check for mispelling, not using commas, number of parameters...
  }
  /* Read and Write functions for files */
  static void readArchive(String file) throws Exception{
    fr = new FileReader("archives/" + file);
    BufferedReader br = new BufferedReader(fr);
    String line;
    while((line = br.readLine()) != null){
      System.out.println(line);
    }
  }
  static void writeArchive(String content, String file) throws Exception{
    FileWriter fw = new FileWriter("archives/" + file, true);
    BufferedReader br = new BufferedReader(fr);
    String line;
    int id = 0;
    do{
      id++;
    }while((line = br.readLine()) != null);

    fw.write(id + content + "\n");
    fw.close();
  }
  static void deleteFromArchive(int id, String file) throws Exception {

  }
}

interface ServerInterface extends Remote{

}

class ServerImplement extends UnicastRemoteObject implements ServerInterface{

  /*constructor*/
  public ServerImplement() throws RemoteException{

  }
}
