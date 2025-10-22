package org.example;

import java.io.IOException;
import java.net.*;

public class MessageReceiver extends Thread {
    private MulticastSocket multicastSocket;
    private DatagramSocket unicastSocket;
    private MessageListener listener;
    private boolean running = true;

    public interface MessageListener {
        void onMessageReceived(String senderIP, String message);
    }

    public MessageReceiver(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            // Setup multicast socket for broadcast messages
            multicastSocket = new MulticastSocket(MulticastConfig.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MulticastConfig.MULTICAST_ADDRESS);

            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(
                InetAddress.getLocalHost()
            );

            if (networkInterface == null) {
                networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();
            }

            InetSocketAddress groupAddress = new InetSocketAddress(group, MulticastConfig.MULTICAST_PORT);
            multicastSocket.joinGroup(groupAddress, networkInterface);

            // Setup unicast socket for direct messages
            unicastSocket = new DatagramSocket(MulticastConfig.UNICAST_PORT);

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
        byte[] buffer = new byte[1024];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                String senderIP = packet.getAddress().getHostAddress();

                if (listener != null) {
                    listener.onMessageReceived(senderIP, message);
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

