import java.security.*;
import java.lang.Object;

public class test_signature{

  public static void main(String args[]) throws Exception{
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(512);   // tamanho das chaves em bits
    KeyPair pair = generator.generateKeyPair();

    PrivateKey chave_privada;
    PublicKey chave_publica;

    chave_privada = pair.getPrivate();
    chave_publica = pair.getPublic();

    String ok = "OK";
    String no = "NO";

    Signature assinatura = Signature.getInstance("SHA256withRSA");

    assinatura.initSign(chave_privada);     //initialize this object for signing using private key
    assinatura.update(ok.getBytes()); //updates the data to be signed by a byte
    byte[] m_assinada = assinatura.sign();  //returns the signature bytes of all the data updated.

    Signature assinatura2 = Signature.getInstance("SHA256withRSA");
    assinatura2.initVerify(chave_publica);//initialize this object for verifing using public key
    assinatura2.update(no.getBytes());       //updates the data to be verified by a byte
    boolean verifies = assinatura2.verify(m_assinada); //verifies the passed-in signature.

    System.out.println(verifies);
  }

}
