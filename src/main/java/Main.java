import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Random;

public class Main
{
    public static String[]word = {"a","about","all","also","and","as","at","be","because","but","by","can","come","could","day","do","even","find","first","for","from","get","give","go","have","he","her","here","him","his","how","I","if","in","into","it","its","just","know","like","look","make","man","many","me","more","my","new","no","not","now","of","on","one","only","or","other","our","out","people","say","see","she","so","some","take","tell","than","that","the","their","them","then","there","these","they","thing","think","this","those","time","to","two","up","use","very","want","way","we","well","what","when","which","who","will","with","would","year","you","your"};
    private static Windows windows = new Windows();
    public static int serverClient = 0;
    public static int port = 0;
    public static String adres = "";
    public static String wiadomosc = "";
    public static void main(String[]args)
    {
        windows.initialize();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    windows.firstScreen();
                    while (Main.getServerClient() == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Signer signer = new Signer();
                    signer.setup();
                    if (Main.getServerClient() == 1) {
                        windows.thirdScreen();
                        while (Main.getPort() == 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        windows.secondScreen("", "Oczekiwanie na połączenie...");
                        ServerSocket welcomeSocket = new ServerSocket(Main.getPort());
                        Socket connectionSocket = welcomeSocket.accept();
                        DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(signer.getKeys().getPublic());
                        byte[] serializedPublicBytes = publicKeyInfo.toASN1Primitive().getEncoded();
                        outToClient.write(serializedPublicBytes);
                        windows.secondScreen("", "Oczekiwanie na wiadomosc i jej podpisywanie...");
                        ArrayList<byte[]> lista = new ArrayList<>();
                        Random generator = new Random();
                        //int z =generator.nextInt(100)+1;
                        int z =97;
                        int i = 0;
                        for (String string : word) {
                            i++;
                            while (inFromClient.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            byte[] tmpBytes = new byte[inFromClient.available()];
                            inFromClient.read(tmpBytes, 0, inFromClient.available());
                            RequestForSign requestForSign = new RequestForSign();
                            requestForSign.setup(tmpBytes);
                            byte[] signature = null;
                            if (i==z)
                            {
                                System.out.println("Podpis " + i);
                                signature = signer.sign(requestForSign);
                            }
                            else
                            {
                                System.out.println("Brak podpisu " + i);
                                signature = requestForSign.getMessage();
                            }
                            lista.add(signature);
                            outToClient.write("OK".getBytes());
                        }

                        windows.secondScreen("", "Wysyłanie podpisu...");
                        i = 0;
                        for (byte[]signature : lista)
                        {
                            i++;
                            outToClient.write(signature);
                            System.out.println("Wyslano podpis " + i + " signature: " + signature);
                            while (inFromClient.available()==0)
                            {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            byte[]tmpBytes = new byte[inFromClient.available()];
                            inFromClient.read(tmpBytes, 0, inFromClient.available());
                        }
                        windows.secondScreen("", "Oczekiwanie na odciemnienie...");
                        boolean flag = false;
                        for (String string : word) {
                            i--;
                            while (inFromClient.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            byte[] tmpBytes = new byte[inFromClient.available()];
                            inFromClient.read(tmpBytes, 0, inFromClient.available());
                            outToClient.write("ok".getBytes());
                            byte[]id = tmpBytes;
                            while (inFromClient.available()==0)
                            {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            tmpBytes = new byte[inFromClient.available()];
                            inFromClient.read(tmpBytes, 0, inFromClient.available());
                            outToClient.write("ok".getBytes());
                            System.out.println("Wysłano odciemniony "+id+" "+tmpBytes);
                            byte[]sig = tmpBytes;
                            SignedPackage signedPackage = new SignedPackage();
                            signedPackage.setup(sig, id);
                            boolean effect = signer.verify(signedPackage);
                            System.out.println(effect);
                            if (effect==true) flag = true;
                        }
                        if (flag)
                        {
                            windows.secondScreen("", "Podpis OK");
                        }
                        else
                        {
                            windows.secondScreen("", "Podpis nieOK");
                        }
                    } else {
                        windows.fourthScreen();
                        while (Main.getPort() == 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //windows.fifthScreen();
                        Socket connectionSocket = new Socket(Main.getAdres(), Main.getPort());
                        DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream());
                        DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                        while (inFromServer.available()==0)
                        {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        byte[]tmpBytes = new byte[inFromServer.available()];
                        inFromServer.read(tmpBytes, 0, inFromServer.available());
                        RSAKeyParameters publicKey = (RSAKeyParameters) PublicKeyFactory.createKey(tmpBytes);
                        windows.secondScreen("", "Zaciemnianie i wysyłanie wiadomości...");
                        ArrayList<byte[]> lista2 = new ArrayList<>();
                        for (String string : word) {
                            Blinder blinder = new Blinder();
                            blinder.setup(string.getBytes(), (RSAKeyParameters) publicKey);
                            RequestForSign requestForSign = blinder.generateRequest();
                            System.out.println("Zaciemnione "+string+" "+requestForSign.getMessage());
                            tmpBytes = requestForSign.getMessage();
                            lista2.add(tmpBytes);
                        }
                        for(byte[]message : lista2)
                        {
                            outToServer.write(message);
                            while (inFromServer.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            tmpBytes = new byte[inFromServer.available()];
                            inFromServer.read(tmpBytes, 0, inFromServer.available());
                        }
                        windows.secondScreen("", "Odciemnianie wiadomości i wysyłanie do weryfikacji...");
                        ArrayList<byte[]> lista = new ArrayList<>();
                        for (String string : word) {
                            while (inFromServer.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            tmpBytes = new byte[inFromServer.available()];
                            inFromServer.read(tmpBytes, 0, inFromServer.available());
                            outToServer.write("ok".getBytes());
                            Blinder blinder = new Blinder();
                            blinder.setup(string.getBytes(), (RSAKeyParameters) publicKey);
                            SignedPackage signedPackage = blinder.createSignedPackage(tmpBytes);
                            lista.add(signedPackage.getId());
                            lista.add(signedPackage.getSignature());
                            System.out.println("Odciemnianie "+signedPackage.getId()+" "+signedPackage.getSignature());
                            System.out.println(signer.verify(signedPackage));
                        }
                        for(byte[]signature : lista)
                        {
                            outToServer.write(signature);
                            System.out.println("Wysłano "+signature);
                            while (inFromServer.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            byte[]tmpBytes2 = new byte[inFromServer.available()];
                            inFromServer.read(tmpBytes2, 0, inFromServer.available());
                        }
                        for(byte[]signature : lista)
                        {
                            outToServer.write(signature);
                            System.out.println("Wysłano "+signature);
                            while (inFromServer.available() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            byte[]tmpBytes2 = new byte[inFromServer.available()];
                            inFromServer.read(tmpBytes2, 0, inFromServer.available());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static int getServerClient() {
        return serverClient;
    }
    public static void setServerClient(int serverClient) {
        Main.serverClient = serverClient;
    }
    public static int getPort() {
        return port;
    }
    public static void setPort(int port) {
        Main.port = port;
    }
    public static String getAdres() {
        return adres;
    }
    public static void setAdres(String adres) {
        Main.adres = adres;
    }
    public static String getWiadomosc() {
        return wiadomosc;
    }
    public static void setWiadomosc(String wiadomosc) {
        Main.wiadomosc = wiadomosc;
    }
}
