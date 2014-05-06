/*
 * Client.java
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
 * Client program sends radii to the server and then receives
 * the resulting area back.
 *
 * @author: Liang: Ex. 28.2
 * @author: Sean Strout
 */
public class Client extends JFrame implements ActionListener {
    private JTextField jtf, name;
    private JTextArea jta = new JTextArea();
    private JButton jbtStart, jbtStop;

    private DataOutputStream output;
    private DataInputStream input; 
    private String host;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client hostname port");
            System.exit(-1);
        }
        new Client(args[0], args[1]);
    }

    public Client(String host, String port) {
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        p1.add(new Label("Enter message"), BorderLayout.WEST);
        p1.add(jtf = new JTextField(10), BorderLayout.CENTER);
        //p1.add(name = new JTextField(10), BorderLayout.NORTH);
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
            
            // Create an input stream to receive data from the server
            //input = new DataInputStream(
              //      socket.getInputStream());
            ClientThread t = new ClientThread(socket,jta);
            t.start();
            // Create an output stream to send data to the server
            output = new DataOutputStream(
                    socket.getOutputStream());
            //output.writeUTF("\\nick <nameless>");
        }
        catch (IOException ex) {
            jta.append(ex.toString()+'\n');
        }
    }

    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (e.getSource() == jtf) {
            try {
                // Read the radius from the text field
                //double radius = Double.parseDouble(jtf.getText().trim());
                //jta.append("Radius is " + radius + "\n");
                if (jtf.getText().startsWith("/quit"))
                    System.exit(0);
                // Send radius to the server
                output.writeUTF(jtf.getText());
                output.flush();
                jtf.setText("");
                // Get area from the server
                //double area = input.readDouble();

                // Print area on
                //jta.append("Area received from the server is "
                        //+ area + "\n");
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        } 
    }
    class ClientThread extends Thread {
        private Socket socket;
        private JTextArea jta;

        public ClientThread(Socket s, JTextArea jta) {
            this.socket = s;
            this.jta = jta;
        }

        public void run() {

            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                //DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                //network.addOutput(id,out);

                while (true) {
                    String data = input.readUTF();
                    System.out.println(data);
                    if (data.startsWith("$name "))
                       setTitle("<"+data.substring(6)+">: " + InetAddress.getLocalHost().getHostName());
                    else
                        jta.append(data+"\n");
                }
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }

    }

}
