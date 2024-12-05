
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

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
    private static void runMaster(DatagramSocket socket, int masterNumber) throws SocketException {
        List<Integer> nums = new ArrayList<>();
        nums.add(masterNumber);
        int slaveNumber;
        int sum;
        String received;
        byte[] buf = new byte[BUFFER_SIZE];
        System.out.println("Waiting for slaves...");
        DatagramPacket packet = null;
        try {
            InetAddress broadcastAddress = getBroadcastAddress();
            System.out.println(broadcastAddress);
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
                    nums.add(slaveNumber);
                    System.out.println("Received " + slaveNumber + " from " + packet.getAddress());
                }
                else if (slaveNumber == -1) {
                    System.out.println("Received -1 from " + packet.getAddress());
                    String broadcastMessage = "-1";
                    byte[] broadcastData = broadcastMessage.getBytes();
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            broadcastData, broadcastData.length,
                            broadcastAddress,
                            socket.getLocalPort()
                    );
                    socket.send(broadcastPacket);
                    System.out.println("Broadcasted -1 to all machines.");
                    System.out.println("Terminating");
                    return;
                }
                else{
                    sum = 0;
                    for (int num : nums) sum += num;
                    double average = (double) sum / nums.size();
                    String averageMessage = "Average: " + average;
                    System.out.println(averageMessage);
                    byte[] responseData = averageMessage.getBytes();
                    System.out.println(broadcastAddress);
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            responseData, responseData.length,
                            broadcastAddress,
                            socket.getLocalPort()
                    );
                    socket.send(broadcastPacket);
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
        //finding IP address
        InetAddress localIPAddress;
        try {
            localIPAddress = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("IP address not found");
            return null;
        }

        //taking network interface according to the IP address we retrieved
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByInetAddress(localIPAddress);
            System.out.println(networkInterface);
        } catch (SocketException e) {
            System.out.println("Network interface not found");
            return null;
        }
        //finding proper prefix length for IPv4
        //short prefix = -1;
        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
               InetAddress broadcast = address.getBroadcast();
                if(broadcast != null) return broadcast;
            }
        } return null;/*
                prefix = address.getNetworkPrefixLength();
            }
        }
        if (prefix == -1) {
            System.out.println("Prefix not found");
            return null;
        }

        return calculation(localIPAddress, prefix);
        */
    }
    private static InetAddress calculation(InetAddress IPAddress, short prefix) {
        //taking byte representation of the ip and subnet mask
        byte[] IPBytes = IPAddress.getAddress();
        int subnetMask = -(1 << (32 - prefix));
        byte[] maskBytes = {
                (byte) ((subnetMask >> 24) & 0xFF),
                (byte) ((subnetMask >> 16) & 0xFF),
                (byte) ((subnetMask >> 8) & 0xFF),
                (byte) (subnetMask & 0xFF)
        };

        //computing broadcast address
        byte[] broadcastAddress = new byte[4];
        for (int i = 0; i < 4; i++) {
            broadcastAddress[i] = (byte) (IPBytes[i] | ~maskBytes[i]);
        }

        try {
            return InetAddress.getByAddress(broadcastAddress);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
