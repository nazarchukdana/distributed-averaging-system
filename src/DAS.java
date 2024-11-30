import java.net.*;

public class DAS {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect arguments");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        int number = Integer.parseInt(args[1]);
        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Master mode activated on port " + port);
            socket.setBroadcast(true);
            Master master = new Master(socket, number);
        } catch (SocketException e) {
            System.out.println("Slave mode activated. Connecting to master on port " + port);
            Slave slave = new Slave(port, number);
        }
    }
}
