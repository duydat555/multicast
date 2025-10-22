package org.example;

import java.io.IOException;
import java.net.*;

public class MessageSender {

    public void sendMessage(String destination, String message) throws IOException {
        // Format: DESTINATION|MESSAGE
        String fullMessage = destination + "|" + message;

        if (destination.equals("*")) {
            // Broadcast to all machines
            sendBroadcast(fullMessage);
        } else if (destination.toLowerCase().startsWith("group")) {
            // Send to a group
            sendToGroup(destination, fullMessage);
        } else {
            // Send to specific IP
            sendToIP(destination, fullMessage);
        }
    }

    private void sendBroadcast(String message) throws IOException {
        MulticastSocket socket = new MulticastSocket();
        socket.setTimeToLive(255);
        InetAddress group = InetAddress.getByName(MulticastConfig.MULTICAST_ADDRESS);

        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(
            buffer,
            buffer.length,
            group,
            MulticastConfig.MULTICAST_PORT
        );

        socket.send(packet);
        socket.close();
        System.out.println("Broadcast message sent: " + message);
    }

    private void sendToGroup(String groupName, String message) throws IOException {
        String groupAddress;

        switch (groupName.toLowerCase()) {
            case "groupa":
            case "group_a":
                groupAddress = MulticastConfig.GROUP_A;
                break;
            case "groupb":
            case "group_b":
                groupAddress = MulticastConfig.GROUP_B;
                break;
            case "groupc":
            case "group_c":
                groupAddress = MulticastConfig.GROUP_C;
                break;
            default:
                throw new IllegalArgumentException("Unknown group: " + groupName + ". Use groupA, groupB, or groupC");
        }

        MulticastSocket socket = new MulticastSocket();
        socket.setTimeToLive(255);
        InetAddress group = InetAddress.getByName(groupAddress);

        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(
            buffer,
            buffer.length,
            group,
            MulticastConfig.MULTICAST_PORT
        );

        socket.send(packet);
        socket.close();
        System.out.println("Group message sent to " + groupName + ": " + message);
    }

    private void sendToIP(String ip, String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(ip);

        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(
            buffer,
            buffer.length,
            address,
            MulticastConfig.UNICAST_PORT
        );

        socket.send(packet);
        socket.close();
        System.out.println("Direct message sent to " + ip + ": " + message);
    }
}
