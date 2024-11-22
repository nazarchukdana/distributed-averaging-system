import java.io.*;
import java.net.*;

public class DAS {
    private static final int BUFFER_SIZE = 256;
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
            runMaster(socket, number);
        } catch (SocketException e) {
            System.out.println("Slave mode activated. Connecting to master on port " + port);
            runSlave(port, number);
        }
    }
    private static void runMaster(DatagramSocket socket, int masterNumber){
        int totalSum = masterNumber;
        int countNums = 1;
        int slaveNumber;
        String received;
        byte[] buf = new byte[BUFFER_SIZE];
        System.out.println("Waiting for slaves...");
        DatagramPacket packet = null;
        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            while (true) {
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                received = new String(packet.getData(), 0, packet.getLength()).trim();
                try {
                    slaveNumber = Integer.parseInt(received);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (slaveNumber != 0 && slaveNumber != -1) {
                    totalSum += slaveNumber;
                    countNums++;
                    System.out.println("Received " + slaveNumber + " from " + packet.getAddress());
                }
                else if (slaveNumber == -1) {
                    System.out.println("Received value -1");
                    String broadcastMessage = "-1";
                    byte[] broadcastData = broadcastMessage.getBytes();
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            broadcastData, broadcastData.length,
                            broadcastAddress,
                            socket.getLocalPort()
                    );
                    socket.send(broadcastPacket);
                    System.out.println("Broadcasted value -1 to all machines.");
                    return;
                }
                else{
                    double average = (double) totalSum / countNums;
                    String averageMessage = "Average: " + average;
                    System.out.println(averageMessage);
                    byte[] responseData = averageMessage.getBytes();
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            responseData, responseData.length,
                            broadcastAddress,
                            socket.getLocalPort()
                    );
                    socket.send(broadcastPacket);
                    System.out.println("Broadcasted average: " + average);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Error in address: "+e.getMessage());
        } catch (IOException e) {
            System.err.println("Error in receiving/sending messages: "+e.getMessage());
        }
        finally{
            socket.close();
        }
    }
    private static void runSlave(int port, int number) {
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
