import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.sum;

public class Master {
    private final int BUFFER_SIZE = 256;
    private List<Integer> nums;
    public Master(DatagramSocket socket, int masterNumber){
        nums = new ArrayList<>();
        nums.add(masterNumber);
        int slaveNumber;
        int sum;
        String received;
        byte[] buf = new byte[BUFFER_SIZE];
        System.out.println("Waiting for slaves...");
        DatagramPacket packet = null;
        try {
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
                            InetAddress.getByName("255.255.255.255"),
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
                    DatagramPacket broadcastPacket = new DatagramPacket(
                            responseData, responseData.length,
                            InetAddress.getByName("255.255.255.255"),
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
}
