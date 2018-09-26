import java.security.*;
import java.lang.Object;

public class assinatura{

  public static void main(String args[]) throws Exception{
    KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
    generator.initialize(512);   // tamanho das chaves em bits
    KeyPair pair = generator.generateKeyPair();

    PrivateKey chave_privada;
    PublicKey chave_publica;

    chave_privada = pair.getPrivate();
    chave_publica = pair.getPublic();

    String mensagem = "OK";

    Signature assinatura = Signature.getInstance("DSA");

    assinatura.initSign(chave_privada);     //initialize this object for signing using private key
    assinatura.update(mensagem.getBytes()); //updates the data to be signed by a byte
    byte[] m_assinada = assinatura.sign();  //returns the signature bytes of all the data updated.

    System.out.println(mensagem);
    System.out.println(new String(m_assinada));

    Signature assinatura2 = Signature.getInstance("DSA");
    assinatura2.initVerify(chave_publica);//initialize this object for verifing using public key
    assinatura2.update(mensagem.getBytes());       //updates the data to be verified by a byte
    boolean verifies = assinatura2.verify(m_assinada); //verifies the passed-in signature.

    System.out.println(verifies);
  }

}
