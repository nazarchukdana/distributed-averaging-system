
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DAS {
    private static final int BUFFER_SIZE = 256;
    private static InetAddress broadcastAddress;
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect arguments");
            System.exit(1);
        }
        int port  = 0;
        int number = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number. Please provide a valid integer for the port.");
            System.exit(1);
        }
        try {
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number. Please provide a valid integer as the second argument.");
            System.exit(1);
        }
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
    private static void runMaster(DatagramSocket socket, int masterNumber) throws SocketException {
        List<Integer> nums = new ArrayList<>();
        if(masterNumber != 0) nums.add(masterNumber);
        int slaveNumber;
        String received;
        byte[] buf = new byte[BUFFER_SIZE];
        System.out.println("Waiting for slaves...");
        DatagramPacket packet = null;
        try {
            broadcastAddress = getBroadcastAddress();
            if (broadcastAddress == null){
                System.err.println("Broadcast address not found");
                return;
            }
            while (true) {
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                if (Inet4Address.getLocalHost().equals(packet.getAddress())) {
                    continue;
                }
                received = new String(packet.getData(), 0, packet.getLength()).trim();
                slaveNumber = Integer.parseInt(received);
                if (slaveNumber != 0 && slaveNumber != -1) {
                    nums.add(slaveNumber);
                    System.out.println("Received " + slaveNumber + " from " + packet.getAddress());
                }
                else if (slaveNumber == -1) {
                    System.out.println("Received -1 from " + packet.getAddress());
                    sendSignalAndTerminate(socket);
                    return;
                }
                else{
                    broadcastAverage(socket, nums);
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
    private static void sendSignalAndTerminate(DatagramSocket socket) throws IOException {
        broadcastMessage(socket, "-1");
        System.out.println("Broadcasted -1 to all machines.");
        System.out.println("Terminating");
    }
    private static void broadcastAverage(DatagramSocket socket, List<Integer> nums) throws IOException {
        int average = calculateAverage(nums);
        System.out.println("Average: "+average);
        String averageMessage = "" + average;
        broadcastMessage(socket, averageMessage);
        System.out.println("Broadcasted average: "+average);
    }
    private static void broadcastMessage(DatagramSocket socket, String message) throws IOException {
        byte[] responseData = message.getBytes();
        DatagramPacket broadcastPacket = new DatagramPacket(
                responseData, responseData.length,
                broadcastAddress,
                socket.getLocalPort()
        );
        socket.send(broadcastPacket);
    }
    private static int calculateAverage(List<Integer> nums){
        int sum = 0;
        for (int num : nums) sum += num;
        return sum / nums.size();
    }
    private static void runSlave(int port, int number){
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
    private static InetAddress getBroadcastAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Local address not found");
            return null;
        }
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByInetAddress(localAddress);
        } catch (SocketException e) {
            System.err.println("Network interface not found");
            return null;
        }
        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
                InetAddress broadcast = address.getBroadcast();
                if (broadcast != null) return broadcast;
            }
        }
        return null;
    }
}
