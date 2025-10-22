package org.example;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MessageReceiver extends Thread {
    private MulticastSocket multicastSocket;
    private DatagramSocket unicastSocket;
    private MessageListener listener;
    private boolean running = true;
    private String currentGroup = "None";
    private List<InetSocketAddress> joinedGroups = new ArrayList<>();
    private NetworkInterface networkInterface;

    public interface MessageListener {
        void onMessageReceived(String senderIP, String message, String destination);
    }

    public MessageReceiver(MessageListener listener) {
        this.listener = listener;
    }

    public void joinGroup(String group) throws IOException {
        if (group == null || group.equals("None")) {
            leaveAllGroups();
            currentGroup = "None";
            return;
        }

        // Leave previous groups
        leaveAllGroups();

        currentGroup = group;
        String groupAddress = getGroupAddress(group);

        if (groupAddress != null && multicastSocket != null) {
            InetAddress inetGroup = InetAddress.getByName(groupAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(inetGroup, MulticastConfig.MULTICAST_PORT);

            try {
                multicastSocket.joinGroup(socketAddress, networkInterface);
                joinedGroups.add(socketAddress);
                System.out.println("Joined group: " + group + " at " + groupAddress);
            } catch (IOException e) {
                System.err.println("Failed to join group: " + e.getMessage());
                throw e;
            }
        }
    }

    private void leaveAllGroups() {
        for (InetSocketAddress groupAddress : joinedGroups) {
            try {
                if (multicastSocket != null && !multicastSocket.isClosed()) {
                    multicastSocket.leaveGroup(groupAddress, networkInterface);
                    System.out.println("Left group: " + groupAddress);
                }
            } catch (IOException e) {
                System.err.println("Error leaving group: " + e.getMessage());
            }
        }
        joinedGroups.clear();
    }

    private String getGroupAddress(String groupName) {
        switch (groupName.toLowerCase()) {
            case "groupa":
            case "group_a":
                return MulticastConfig.GROUP_A;
            case "groupb":
            case "group_b":
                return MulticastConfig.GROUP_B;
            case "groupc":
            case "group_c":
                return MulticastConfig.GROUP_C;
            default:
                return null;
        }
    }

    @Override
    public void run() {
        try {
            // Setup multicast socket for broadcast messages
            multicastSocket = new MulticastSocket(MulticastConfig.MULTICAST_PORT);
            multicastSocket.setReuseAddress(true);

            try {
                networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            } catch (Exception e) {
                networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();
            }

            // Join the broadcast group by default
            InetAddress broadcastGroup = InetAddress.getByName(MulticastConfig.MULTICAST_ADDRESS);
            InetSocketAddress broadcastAddress = new InetSocketAddress(broadcastGroup, MulticastConfig.MULTICAST_PORT);
            multicastSocket.joinGroup(broadcastAddress, networkInterface);
            joinedGroups.add(broadcastAddress);

            // Setup unicast socket for direct messages
            unicastSocket = new DatagramSocket(MulticastConfig.UNICAST_PORT);
            unicastSocket.setReuseAddress(true);

            // Start threads for both sockets
            Thread multicastThread = new Thread(() -> receiveMessages(multicastSocket, "Multicast"));
            Thread unicastThread = new Thread(() -> receiveMessages(unicastSocket, "Unicast"));

            multicastThread.start();
            unicastThread.start();

            multicastThread.join();
            unicastThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages(DatagramSocket socket, String type) {
        byte[] buffer = new byte[2048];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String fullMessage = new String(packet.getData(), 0, packet.getLength());
                String senderIP = packet.getAddress().getHostAddress();

                // Parse message format: DESTINATION|MESSAGE
                String[] parts = fullMessage.split("\\|", 2);
                String destination = "*";
                String message = fullMessage;

                if (parts.length == 2) {
                    destination = parts[0];
                    message = parts[1];
                }

                // Filter messages based on destination
                boolean shouldReceive = false;

                if (destination.equals("*")) {
                    // Broadcast - everyone receives
                    shouldReceive = true;
                } else if (destination.startsWith("group")) {
                    // Group message - only if we're in that group
                    shouldReceive = destination.equalsIgnoreCase(currentGroup);
                } else {
                    // Direct message - check if it's for our IP
                    try {
                        String localIP = InetAddress.getLocalHost().getHostAddress();
                        shouldReceive = destination.equals(localIP);
                    } catch (Exception e) {
                        shouldReceive = false;
                    }
                }

                if (shouldReceive && listener != null) {
                    listener.onMessageReceived(senderIP, message, destination);
                }

            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shutdown() {
        running = false;
        leaveAllGroups();
        try {
            if (multicastSocket != null && !multicastSocket.isClosed()) {
                multicastSocket.close();
            }
            if (unicastSocket != null && !unicastSocket.isClosed()) {
                unicastSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
