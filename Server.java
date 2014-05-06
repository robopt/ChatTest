import java.io.*;
import java.net.*;
import java.util.*;

/*
 * A server which listens for multiple clients connecting on a specified port.
 * It spawns a thread for each client to accept messages from users,
 * and then passes the messages and all connected clients.
 *
 * @author: Liang, Ex 28.1
 * @author: Edward Mead (robopt)
 */
public class Server {
    private JTextArea jta = new JTextArea();

    public static void main(String[] args) {
        if (args.length != 1)
            System.out.println("Usage Server <port>\nExample: java Server 8080");
        else
            new Server(Integer.parseInt(args[0]));
    }

    /**
     * Server constructor
     * @param port Port number to listen on.
     */
    public Server(int port) {
        try {
            int clientNo = 1;

            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(port);
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
                
                // Create a new thread for the connection and start it
                ClientThread thread = new ClientThread(network, socket, inetAddress.getHostName());
                thread.start();

                clientNo++;
            }
        }
        catch(IOException ex) {
            System.err.println(ex);
        }
    } /* Server */


    /**
     * Client Network.
     * Manages the broadcast of messages to all connected clients.
     * Also manages the change of nicknames.
     */
    class ClientNetwork {

        //hashmap of usernames and output streams
        Map<String, DataOutputStream> ostreams;

        /**
         * Default constructor
         */
        public ClientNetwork(){
            ostreams = new HashMap<String, DataOutputStream>();
        }

        /**
         * addOutput (aka add a client)
         * Adds an output stream and a nickname to the broadcast map.
         * If nick is duplicative, Appends '_' until it isnt.
         * Responds with $name <nick> to confirm and to set client ui.
         * @param s Nickname... intuitive i know.
         * @param o Output stream to write to.
         * @return The new nickname (incase '_' was added)
         */
        public String addOutput(String s, DataOutputStream o) {
            while (ostreams.containsKey(s))
                s = s + "_";
            //add to map
            ostreams.put(s,o);

            //respond with result.
            try {
                o.writeUTF("$name " + s);
            } catch (IOException ioerr) {
                System.err.println(ioerr);
            }
            return s;
        }/* addOutput */

        /**
         * send (message)
         * Broadcasts a message from a specified user.
         * @param id Username of sender.
         * @param s Message.
         */
        public void send(String id, String s) {
            //for (Enumeration<DataOutputStream> e = ostreams.elements(); e.hasMoreElements();)
            for (Map.Entry<String, DataOutputStream> e : ostreams.entrySet())
            {
                try {                    
                    //date formatting
                    DataOutputStream out = e.getValue();
                    Calendar now = Calendar.getInstance();
                    String min = "0" + now.get(Calendar.MINUTE);
                    min = min.substring(min.length()-2);
                    String date = "<"+now.get(Calendar.HOUR_OF_DAY) + ":"+ min +">";
                    
                    //write to output stream
                    out.writeUTF("[" + id + "]"+date+": " + s);
                    out.flush(); 
                } catch (IOException ioerr) {
                    System.err.println(ioerr);
                }

            }
        } /* send */


        /**
         * updateKey
         * Update a username and let me know if it succeeded.
         * @param oldS old username.
         * @param newS new username.
         * @return Whether or not update was sucessful.
         */
        public boolean updateKey(String oldS, String newS) {
            boolean result = false;
            if (ostreams.containsKey(oldS)) { //does contain old name
                DataOutputStream temp = ostreams.get(oldS);
                if (!ostreams.containsKey(newS)) { //doesnt contain new name

                    //replace old with new
                    ostreams.remove(oldS);
                    ostreams.put(newS,temp);

                    //respond with new name
                    try {
                        temp.writeUTF("$name "+newS);
                    } catch (IOException ioerr) {
                        System.err.println(ioerr);
                    }
                    result = true;
                } else { //new username in use
                    //date formatting
                    Calendar now = Calendar.getInstance();
                    String min = "0" + now.get(Calendar.MINUTE);
                    min = min.substring(min.length()-2);
                    String date = "<"+now.get(Calendar.HOUR_OF_DAY) + ":"+ min +">";
                    
                    //new username in use response
                    try {
                        temp.writeUTF("[Server]"+ date +" : Username in use. Try another username.");
                    } catch (IOException ioerr) {
                        System.err.println(ioerr);
                    }
                }
            }
            return result;
        } /* updateKey */

        public void remove(String s)
        {
            ostreams.remove(s);
        }
    }

    /**
     * Client Thread.
     * Class which runs on a separate thread to handle a single client connection.
     * Works with ClientNetwork to broadcast.
     */
    class ClientThread extends Thread {
        private Socket socket; 
        private ClientNetwork network;
        private String id;

        /**
         * Construct a client thread.
         * @param n Client network to communicate on.
         * @param s Socket to use.
         * @param id Initial username to use.
         */
        public ClientThread(ClientNetwork n, Socket s, String id) {
            this.network = n;
            this.socket = s;
            this.id = id;
        }

        /**
         * Run thread handles incoming data.
         */
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
                        if (network.updateKey(id,newid)) {
                            network.send("Server", id + " is now known as " + newid);
                            id = newid;
                        }
                    } else {
                        network.send(id, inData);
                    }
                }
            } catch (EOFException eof) {
                network.send("Server", id + " has disconnected.");
            } catch (IOException e) {
                System.err.println(e);
            } finally {
                network.remove(id);
            }
        } /* run */

    } // ClientThread
} // Server
