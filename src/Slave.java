import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Slave {
    public Slave(int port, int number){
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        try{
            socket = new DatagramSocket();

        } catch (IOException e) {
            System.err.println("Error in creating a socket: "+e.getMessage());
            System.exit(1);
        }
        InetAddress address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.err.println("Error in creating address: "+e.getMessage());
            System.exit(1);
        }
        byte[] buf = String.valueOf(number).getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error in sending the packet: "+e.getMessage());
            System.exit(1);
        }
        System.out.println("Sent " + number + " to master on port " + port);
        socket.close();
    }
}
