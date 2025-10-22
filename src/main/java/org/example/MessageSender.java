package org.example;

import java.io.IOException;
import java.net.*;

public class MessageSender {

    public void sendMessage(String destination, String message) throws IOException {
        if (destination.equals("*")) {
            // Broadcast to all machines
            sendBroadcast(message);
        } else if (destination.startsWith("group")) {
            // Send to a group
            sendToGroup(destination, message);
        } else {
            // Send to specific IP
            sendToIP(destination, message);
        }
    }

    private void sendBroadcast(String message) throws IOException {
        MulticastSocket socket = new MulticastSocket();
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
                throw new IllegalArgumentException("Unknown group: " + groupName);
        }

        MulticastSocket socket = new MulticastSocket();
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
    }
}

