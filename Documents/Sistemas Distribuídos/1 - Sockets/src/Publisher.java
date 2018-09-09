import java.net.*;
import java.io.*;
import java.security.*;

/* This thread is responsible for the sending of messages */
public class Publisher {

    public static void main(String args[]){

        MulticastSocket sock = null;
        InetAddress group;
        Thread r = new Thread(new Receiver());
        r.start();

        try
        {
            sock = new MulticastSocket(6789);  // port 6789
            group = InetAddress.getByName("224.0.0.1"); // obtem o endereço ip

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(512); // size of key in bits
            KeyPair pair = generator.generateKeyPair(); // generates public and private key

            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            byte[] join_message = (args[0] + " joined group ").getBytes();
            DatagramPacket join_packet = new DatagramPacket(join_message, join_message.length, group, 6789);
            sock.send(join_packet);
            /*send public key to group*/
            byte[] pubb = pub.getBytes();
            join_packet = new DatagramPacket(pubb, pubb.length, group, 6789);
            sock.send(join_packet);

            boolean exit = false; // indicates if user is in group

            while(!exit)
            {
                System.out.println( "1 - Access critic session 1 \n2 - Access critic session 2\n3 - Exit");
                String userInput = System.console().readLine(); // gets input message
                byte[] m = userInput.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
                sock.send(messageOut);
                System.out.println("Sent: " + userInput);
            }
            sock.leaveGroup(group);

        }
        catch (SocketException e){System.out.println("Socket: " + e.getMessage());}
        catch (IOException e){System.out.println("IO: " + e.getMessage());}
        catch (NoSuchAlgorithmException e) {System.out.println("Generator: " + e.getMessage());}
        //catch (InterruptedException e) {System.out.println("Interruption: " + e.getMessage());}
        finally
        {
            if(sock != null)
                sock.close();
        }
    }
}

/* This thread is responsible for the receiving and treatment of messages */
class Receiver extends Thread{

    MulticastSocket sock;
    byte[] buffer;

    public void run(){
        try{
            sock = new MulticastSocket(6789);
            InetAddress group = InetAddress.getByName("224.0.0.1");
            sock.joinGroup(group);

            while(true){
                buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer,buffer.length);
                sock.receive(messageIn);
                System.out.println("Received:" + new String(messageIn.getData()));
            }
        }
        catch (IOException e){System.out.println("IO: " + e.getMessage());}
    }
}
