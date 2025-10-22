package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetSendGUI extends JFrame {
    private JTextArea receivedMessagesArea;
    private JTextField destinationField;
    private JTextArea messageField;
    private JLabel statusLabel;
    private JLabel localIPLabel;

    private MessageReceiver receiver;
    private MessageSender sender;

    public NetSendGUI() {
        sender = new MessageSender();
        initializeUI();
        startReceiver();
    }

    private void initializeUI() {
        setTitle("Net Send - Multicast Messaging");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - Local IP info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            localIPLabel = new JLabel("Your IP: " + localIP);
            localIPLabel.setFont(new Font("Arial", Font.BOLD, 12));
            localIPLabel.setForeground(new Color(0, 100, 0));
            topPanel.add(localIPLabel);
        } catch (Exception e) {
            localIPLabel = new JLabel("Your IP: Unable to detect");
            topPanel.add(localIPLabel);
        }
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Received messages
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(new TitledBorder("Received Messages"));

        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(receivedMessagesArea);
        scrollPane.setPreferredSize(new Dimension(650, 250));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Send message
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new TitledBorder("Send Message"));

        // Destination panel
        JPanel destPanel = new JPanel(new BorderLayout(5, 5));
        JLabel destLabel = new JLabel("Destination:");
        destLabel.setPreferredSize(new Dimension(80, 25));
        destinationField = new JTextField();
        destinationField.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel destHelpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel helpLabel = new JLabel("<html><small>Enter: IP address | * (all) | groupA | groupB | groupC</small></html>");
        helpLabel.setForeground(Color.GRAY);
        destHelpPanel.add(helpLabel);

        destPanel.add(destLabel, BorderLayout.WEST);
        destPanel.add(destinationField, BorderLayout.CENTER);
        destPanel.add(destHelpPanel, BorderLayout.SOUTH);

        // Message panel
        JPanel msgPanel = new JPanel(new BorderLayout(5, 5));
        JLabel msgLabel = new JLabel("Message:");
        msgLabel.setPreferredSize(new Dimension(80, 25));
        msgLabel.setVerticalAlignment(SwingConstants.TOP);

        messageField = new JTextArea(4, 30);
        messageField.setFont(new Font("Arial", Font.PLAIN, 12));
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        JScrollPane msgScrollPane = new JScrollPane(messageField);

        msgPanel.add(msgLabel, BorderLayout.WEST);
        msgPanel.add(msgScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send Message");
        sendButton.setPreferredSize(new Dimension(120, 30));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(80, 30));
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);

        // Assemble bottom panel
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        sendPanel.add(destPanel, BorderLayout.NORTH);
        sendPanel.add(msgPanel, BorderLayout.CENTER);
        sendPanel.add(buttonPanel, BorderLayout.SOUTH);

        bottomPanel.add(sendPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready to send/receive messages");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(statusLabel);

        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }

    private void startReceiver() {
        receiver = new MessageReceiver((senderIP, message) -> {
            SwingUtilities.invokeLater(() -> {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String displayMessage = String.format("[%s] From %s:\n%s\n%s\n",
                    timestamp, senderIP, message, "─".repeat(60));
                receivedMessagesArea.append(displayMessage);
                receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());

                // Show notification
                showNotification("New Message", "From: " + senderIP);
            });
        });
        receiver.start();
        updateStatus("Receiver started. Listening for messages...", Color.BLUE);
    }

    private void sendMessage() {
        String destination = destinationField.getText().trim();
        String message = messageField.getText().trim();

        if (destination.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a destination!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a message!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            sender.sendMessage(destination, message);
            updateStatus("Message sent successfully to: " + destination, new Color(0, 128, 0));
            messageField.setText("");

            // Log sent message
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String logMessage = String.format("[%s] SENT to %s: %s\n%s\n",
                timestamp, destination, message, "─".repeat(60));
            receivedMessagesArea.append(logMessage);
            receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());

        } catch (Exception ex) {
            updateStatus("Failed to send message: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this,
                "Error sending message:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        destinationField.setText("");
        messageField.setText("");
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("");
                TrayIcon trayIcon = new TrayIcon(image, "Net Send");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

                // Remove tray icon after showing notification
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        tray.remove(trayIcon);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                // Silently fail if system tray is not available
            }
        }
    }

    private void shutdown() {
        if (receiver != null) {
            receiver.shutdown();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            NetSendGUI gui = new NetSendGUI();
            gui.setVisible(true);
        });
    }
}
