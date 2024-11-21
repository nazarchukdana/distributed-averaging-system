import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

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

            runMaster(socket, number);
        } catch (SocketException e) {
            System.out.println("Slave mode activated. Connecting to master on port " + port);
            runSlave(port, number);
        }
    }
    private static void runMaster(DatagramSocket socket, int masterNumber) throws SocketException {
        socket.setBroadcast(true);
        int totalSum = masterNumber;
        int countNums = 1;

        byte[] buffer = new byte[BUFFER_SIZE];
        System.out.println("Waiting for slaves...");

        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength()).trim();
                int slaveNumber = 0;
                try {
                    slaveNumber = Integer.parseInt(received);
                } catch (NumberFormatException e) {
                    continue;  // Skip non-numeric messages
                }
                if (slaveNumber == -1) {
                    System.out.println("Received value -1. Terminating...");

                    String broadcastMessage = "-1";
                    byte[] broadcastData = broadcastMessage.getBytes();
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            broadcastData, broadcastData.length,
                            InetAddress.getByName("255.255.255.255"),
                            socket.getLocalPort()
                    );

                    socket.setBroadcast(true);
                    socket.send(broadcastPacket);
                    System.out.println("Broadcasted value -1 to all machines.");

                    socket.close();
                    System.exit(0);
                }
                if(slaveNumber != 0) {
                    totalSum += slaveNumber;
                    countNums++;
                    System.out.println("Received "+slaveNumber+" from "+packet.getAddress());
                }
                if (slaveNumber == 0){
                    double average = (double) totalSum / countNums;
                    System.out.println("Average: "+average);
                    String averageMessage = "Average: " + average;
                    byte[] responseData = averageMessage.getBytes();
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            responseData, responseData.length,
                            InetAddress.getByName("255.255.255.255"),
                            socket.getLocalPort()
                    );
                    //if (!socket.getLocalAddress().equals(packet.getAddress())) {
                        socket.send(broadcastPacket);
                        System.out.println("Broadcasted average: " + average);
                    //}
                }
            }
        } catch (IOException e) {
            System.err.println("Error in master mode: " + e.getMessage());
        } finally {
            socket.close();
        }
    }
    private static void runSlave(int port, int number) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName("localhost");
            socket.setBroadcast(true);
            byte[] data = String.valueOf(number).getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            System.out.println("Sent " + number + " to master on port " + port);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket broadcastPacket = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(broadcastPacket);
                String broadcastMessage = new String(broadcastPacket.getData(), 0, broadcastPacket.getLength());
                if ("-1".equals(broadcastMessage.trim())) {
                    System.out.println("Received termination broadcast. Exiting...");
                    System.exit(0);
                }
                System.out.println("Broadcast from master: " + broadcastMessage);
            }
        } catch (IOException e) {
            System.err.println("Error in slave mode: " + e.getMessage());
        }
    }
}
