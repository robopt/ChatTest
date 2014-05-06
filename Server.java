/*
 * Server.java
 *
 * $Id$
 *
 * $Log$
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * A server which listens for multiple clients connecting on
 * port 8000.  It spawns a thread for each client to calculate
 * the areas of the radii that are sent to it.
 *
 * @author: Liang, Ex 28.1
 */
public class Server /*extends JFrame*/ {
    private JTextArea jta = new JTextArea();

    public static void main(String[] args) {
        if (args.length != 1)
            System.out.println("Usage Server <port>");
        else
            new Server(Integer.parseInt(args[0]));
    }

    public Server(int port) {
        // Place text area on the frame
        /*getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(jta), BorderLayout.CENTER);

        // It is necessary to show the frame here!
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);*/
        try {
            //setTitle("Server: " + InetAddress.getLocalHost().getHostName());

            int clientNo = 1;

            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(port);
            //jta.append("Server started at " + new Date() + '\n');
	    System.out.println("Server started at " + new Date() + '\n');
            
            ClientNetwork network = new ClientNetwork();
            while (true) {
    
                // Listen for a connection request
                Socket socket = serverSocket.accept();
    
                // Display the client info  
                InetAddress inetAddress = socket.getInetAddress();
                System.out.print("Client " + clientNo + " at " + new Date() + "\n");
                System.out.print("Client " + clientNo + " host name is " + 
                    inetAddress.getHostName() + "\n");
                System.out.print("Client " + clientNo + " IP address is " + 
                    inetAddress.getHostAddress() + "\n");
                /*jta.append("Client " + clientNo + " at " + new Date() + "\n");
                jta.append("Client " + clientNo + " host name is " + 
                    inetAddress.getHostName() + "\n");
                jta.append("Client " + clientNo + " IP address is " + 
                    inetAddress.getHostAddress() + "\n");*/
                // Create a new thread for the connection and start it
                ClientThread thread = new ClientThread(network, socket, inetAddress.getHostName());
                thread.start();
    
                clientNo++;
            }
        }
        catch(IOException ex) {
            System.err.println(ex);
        }
    }


    class ClientNetwork {
        Map<String, DataOutputStream> ostreams;
        public ClientNetwork(){
            ostreams = new HashMap<String, DataOutputStream>();
        }
        
        public String addOutput(String s, DataOutputStream o) {
	    while (ostreams.containsKey(s))
		s = s + "_";
            ostreams.put(s,o);
	    try {
		o.writeUTF("$name " + s);
	    }
	    catch (IOException ioerr) {
		System.err.println(ioerr);
	    }
	    return s;
        }

        public void send(String id, String s) {
            //for (Enumeration<DataOutputStream> e = ostreams.elements(); e.hasMoreElements();)
            for (Map.Entry<String, DataOutputStream> e : ostreams.entrySet())
            {
                try {
                    DataOutputStream out = e.getValue();
                    Calendar now = Calendar.getInstance();
                    String min = "0" + now.get(Calendar.MINUTE);
                    min = min.substring(min.length()-2);
                    String date = "<"+now.get(Calendar.HOUR_OF_DAY) + ":"+ min +">";
                    out.writeUTF("[" + id + "]"+date+": " + s);
                    out.flush();
                } catch (IOException ioerr)
                {

                }

            }
        }

        public boolean updateKey(String oldS, String newS) {
            boolean result = false;
            if (ostreams.containsKey(oldS))
            {
                DataOutputStream temp = ostreams.get(oldS);
                if (!ostreams.containsKey(newS)) {
                    ostreams.remove(oldS);
                    ostreams.put(newS,temp);
                    try {
                        temp.writeUTF("$name "+newS);
                    } catch (IOException ioerr)
                    {
                        System.err.println(ioerr);
                    }
                    result = true;
                }
                else
                {
                    Calendar now = Calendar.getInstance();
                    String min = "0" + now.get(Calendar.MINUTE);
                    min = min.substring(min.length()-2);
                    String date = "<"+now.get(Calendar.HOUR_OF_DAY) + ":"+ min +">";
                    try {
                        temp.writeUTF("[Server]"+ date +" : Username in use. Try another username.");
                    } catch (IOException ioerr)
                    {
                        System.err.println(ioerr);
                    }
                }
            }
            return result;
        }

        public void remove(String s)
        {
            ostreams.remove(s);
        }
    }

    // inner thread class
    class ClientThread extends Thread {
        private Socket socket;  // A connected socket
        private ClientNetwork network;
        private String id;

        public ClientThread(ClientNetwork n, Socket s, String id) {
            this.network = n;
            this.socket = s;
            this.id = id;
        }

        public void run() {
        
            try {
                // Create data input and output streams
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                
                //add output stream to network
                id = network.addOutput(id,out);

                while (true) {
                    String inData = input.readUTF();
                    if (inData.startsWith("/nick ")){
                       String newid = inData.substring(6);
                       if (network.updateKey(id,newid))
                       {
                           network.send("Server", id + " is now known as " + newid);
                           id = newid;
                       }
                    }
                    else
                    {
                        network.send(id, inData);
                    }
                }
            } catch (EOFException eof)
	    {
		network.send("Server", id + " has disconnected.");
	    }
            catch (IOException e) {
                System.err.println(e);
            } finally {
                network.remove(id);
            }
        } // run

    } // ClientThread
} // Server
