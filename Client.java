import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * Client program sends and recieves messages to the chat server.
 *
 * @author: Liang: Ex. 28.2
 * @author: Sean Strout
 * @author: Edward Mead (robopt)
 */
public class Client extends JFrame implements ActionListener {
    private JTextField jtf;
    private JTextArea jta = new JTextArea();
    private JButton jbtStart, jbtStop;

    private DataOutputStream output;
    private DataInputStream input; 
    private String host;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Client hostname port\nExample: java Client localhost 8080");
            System.exit(-1);
        }
        new Client(args[0], args[1]);
    }

    /**
     * Client Constructor.
     * Create a client which connects to a host on a certain port.
     * TODO Argument validation.
     * @param host Host/IP of the server
     * @param port Port to bind/connect to
     */
    public Client(String host, String port) {
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        p1.add(new Label("Enter message"), BorderLayout.WEST);
        p1.add(jtf = new JTextField(10), BorderLayout.CENTER);
        jtf.setHorizontalAlignment(JTextField.RIGHT);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(p1, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(jta), BorderLayout.CENTER);

        jtf.addActionListener(this);
        
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        jta.append("use the \"/nick <name>\" command to change your name\n");
        try {
            setTitle("Client: " + InetAddress.getLocalHost().getHostName());

            // Create a socket to connect to the server
            Socket socket = new Socket(host, Integer.parseInt(port));
            
            // Create an input thread to listen on.
            ClientThread t = new ClientThread(socket,jta);
            t.start();

            // Create an output stream to send data to the server
            output = new DataOutputStream(
                    socket.getOutputStream());
        } catch (IOException ex) {
            jta.append(ex.toString()+'\n');
        }
    } /* Client Constructor */

    /**
     * Keyboard event!
     * @param e The action event that happened.
     */
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (e.getSource() == jtf) {
            try {
                
                // /quit -> quit
                if (jtf.getText().startsWith("/quit"))
                    System.exit(0);
                
                // send to server
                output.writeUTF(jtf.getText());
                output.flush();
                jtf.setText("");
            } catch (IOException ex) {
                System.err.println(ex);
            }
        } 
    } /* actionPerformed */

    /**
     * Client Thread.
     * Handles the recieving of characters.
     */
    class ClientThread extends Thread {
        private Socket socket;
        private JTextArea jta;

        public ClientThread(Socket s, JTextArea jta) {
            this.socket = s;
            this.jta = jta;
        }

        /**
         * Run. Waits for messages then displays them.
         */
        public void run() {

            try {
                //input stream
                DataInputStream input = new DataInputStream(socket.getInputStream());

                //wait forever for messages
                while (true) {
                    String data = input.readUTF();
                    /*System.out.println(data); //debug */

                    //if server is confirming our username
                    if (data.startsWith("$name "))
                       setTitle("<"+data.substring(6)+">: " + InetAddress.getLocalHost().getHostName());
                    else
                        jta.append(data+"\n");
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        } /* run */

    } /* ClientThread */

}
