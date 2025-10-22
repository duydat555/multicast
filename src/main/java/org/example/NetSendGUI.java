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
    private JLabel currentGroupLabel;
    private JComboBox<String> groupSelector;

    private MessageReceiver receiver;
    private MessageSender sender;
    private String currentGroup = "None";

    public NetSendGUI() {
        sender = new MessageSender();
        initializeUI();
        startReceiver();
    }

    private void initializeUI() {
        setTitle("Gửi Tin Nhắn - Multicast");
        setSize(900, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top panel - Local IP info and Group selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        
        // IP Panel
        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            localIPLabel = new JLabel("Địa chỉ IP của bạn: " + localIP);
            localIPLabel.setFont(new Font("Arial", Font.BOLD, 16));
            localIPLabel.setForeground(new Color(0, 100, 0));
            ipPanel.add(localIPLabel);
        } catch (Exception e) {
            localIPLabel = new JLabel("Địa chỉ IP của bạn: Không xác định được");
            localIPLabel.setFont(new Font("Arial", Font.BOLD, 16));
            ipPanel.add(localIPLabel);
        }
        
        // Group Panel
        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JLabel groupLabel = new JLabel("Chọn nhóm của bạn:");
        groupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        groupPanel.add(groupLabel);
        
        String[] groups = {"Không có", "Nhóm A", "Nhóm B", "Nhóm C"};
        groupSelector = new JComboBox<>(groups);
        groupSelector.setFont(new Font("Arial", Font.PLAIN, 15));
        groupSelector.setPreferredSize(new Dimension(150, 35));
        groupSelector.addActionListener(e -> {
            String selected = (String) groupSelector.getSelectedItem();
            String groupName = "None";
            if (selected.equals("Nhóm A")) groupName = "groupA";
            else if (selected.equals("Nhóm B")) groupName = "groupB";
            else if (selected.equals("Nhóm C")) groupName = "groupC";
            joinGroup(groupName);
        });
        groupPanel.add(groupSelector);
        
        currentGroupLabel = new JLabel("Nhóm hiện tại: Không có");
        currentGroupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentGroupLabel.setForeground(new Color(0, 0, 200));
        groupPanel.add(currentGroupLabel);
        
        topPanel.add(ipPanel, BorderLayout.NORTH);
        topPanel.add(groupPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Received messages
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        TitledBorder receivedBorder = new TitledBorder("Tin nhắn đã nhận");
        receivedBorder.setTitleFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.setBorder(receivedBorder);

        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(receivedMessagesArea);
        scrollPane.setPreferredSize(new Dimension(850, 300));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Send message
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        TitledBorder sendBorder = new TitledBorder("Gửi tin nhắn");
        sendBorder.setTitleFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.setBorder(sendBorder);

        // Destination panel
        JPanel destPanel = new JPanel(new BorderLayout(5, 5));
        JLabel destLabel = new JLabel("Đích đến:");
        destLabel.setFont(new Font("Arial", Font.BOLD, 15));
        destLabel.setPreferredSize(new Dimension(100, 35));
        destinationField = new JTextField();
        destinationField.setFont(new Font("Arial", Font.PLAIN, 15));
        destinationField.setPreferredSize(new Dimension(0, 35));

        JPanel destHelpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel helpLabel = new JLabel("<html><small style='font-size:12px;'>Nhập: Địa chỉ IP | * (gửi tất cả) | groupA | groupB | groupC</small></html>");
        helpLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        helpLabel.setForeground(Color.GRAY);
        destHelpPanel.add(helpLabel);

        destPanel.add(destLabel, BorderLayout.WEST);
        destPanel.add(destinationField, BorderLayout.CENTER);
        destPanel.add(destHelpPanel, BorderLayout.SOUTH);

        // Message panel
        JPanel msgPanel = new JPanel(new BorderLayout(5, 5));
        JLabel msgLabel = new JLabel("Tin nhắn:");
        msgLabel.setFont(new Font("Arial", Font.BOLD, 15));
        msgLabel.setPreferredSize(new Dimension(100, 35));
        msgLabel.setVerticalAlignment(SwingConstants.TOP);

        messageField = new JTextArea(4, 30);
        messageField.setFont(new Font("Arial Unicode MS", Font.PLAIN, 15));
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        JScrollPane msgScrollPane = new JScrollPane(messageField);

        msgPanel.add(msgLabel, BorderLayout.WEST);
        msgPanel.add(msgScrollPane, BorderLayout.CENTER);

        // Button panel with quick send buttons
        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));

        // Quick send buttons
        JPanel quickSendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JLabel quickLabel = new JLabel("Gửi nhanh:");
        quickLabel.setFont(new Font("Arial", Font.BOLD, 14));
        quickSendPanel.add(quickLabel);

        JButton broadcastBtn = new JButton("Gửi tất cả (*)");
        broadcastBtn.setFont(new Font("Arial", Font.BOLD, 13));
        broadcastBtn.setPreferredSize(new Dimension(140, 32));
        broadcastBtn.addActionListener(e -> {
            destinationField.setText("*");
            messageField.requestFocus();
        });
        quickSendPanel.add(broadcastBtn);

        JButton groupABtn = new JButton("Nhóm A");
        groupABtn.setFont(new Font("Arial", Font.BOLD, 13));
        groupABtn.setPreferredSize(new Dimension(100, 32));
        groupABtn.addActionListener(e -> {
            destinationField.setText("groupA");
            messageField.requestFocus();
        });
        quickSendPanel.add(groupABtn);

        JButton groupBBtn = new JButton("Nhóm B");
        groupBBtn.setFont(new Font("Arial", Font.BOLD, 13));
        groupBBtn.setPreferredSize(new Dimension(100, 32));
        groupBBtn.addActionListener(e -> {
            destinationField.setText("groupB");
            messageField.requestFocus();
        });
        quickSendPanel.add(groupBBtn);

        JButton groupCBtn = new JButton("Nhóm C");
        groupCBtn.setFont(new Font("Arial", Font.BOLD, 13));
        groupCBtn.setPreferredSize(new Dimension(100, 32));
        groupCBtn.addActionListener(e -> {
            destinationField.setText("groupC");
            messageField.requestFocus();
        });
        quickSendPanel.add(groupCBtn);

        // Main action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton sendButton = new JButton("Gửi tin nhắn");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(150, 38));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        JButton clearButton = new JButton("Xóa");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setPreferredSize(new Dimension(100, 38));
        clearButton.addActionListener(e -> clearFields());

        actionPanel.add(clearButton);
        actionPanel.add(sendButton);

        buttonPanel.add(quickSendPanel, BorderLayout.NORTH);
        buttonPanel.add(actionPanel, BorderLayout.SOUTH);

        // Assemble bottom panel
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        sendPanel.add(destPanel, BorderLayout.NORTH);
        sendPanel.add(msgPanel, BorderLayout.CENTER);
        sendPanel.add(buttonPanel, BorderLayout.SOUTH);

        bottomPanel.add(sendPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Sẵn sàng gửi/nhận tin nhắn");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
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

    private void joinGroup(String group) {
        if (receiver != null) {
            try {
                currentGroup = group;
                receiver.joinGroup(group);

                String displayGroup = "Không có";
                if (group.equals("groupA")) displayGroup = "Nhóm A";
                else if (group.equals("groupB")) displayGroup = "Nhóm B";
                else if (group.equals("groupC")) displayGroup = "Nhóm C";

                currentGroupLabel.setText("Nhóm hiện tại: " + displayGroup);

                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String systemMessage = String.format("[%s] HỆ THỐNG: Đã tham gia nhóm '%s'\n%s\n",
                    timestamp, displayGroup, "─".repeat(60));
                receivedMessagesArea.append(systemMessage);
                receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());

                updateStatus("Đã tham gia nhóm thành công: " + displayGroup, new Color(0, 100, 200));
            } catch (Exception e) {
                updateStatus("Không thể tham gia nhóm: " + e.getMessage(), Color.RED);
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tham gia nhóm:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startReceiver() {
        receiver = new MessageReceiver((senderIP, message, destination) -> {
            SwingUtilities.invokeLater(() -> {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String destInfo = "";
                if (destination.equals("*")) {
                    destInfo = " [BROADCAST - GỬI TẤT CẢ]";
                } else if (destination.startsWith("group")) {
                    String groupName = destination;
                    if (destination.equals("groupA")) groupName = "Nhóm A";
                    else if (destination.equals("groupB")) groupName = "Nhóm B";
                    else if (destination.equals("groupC")) groupName = "Nhóm C";
                    destInfo = " [ĐẾN: " + groupName + "]";
                } else {
                    destInfo = " [TRỰC TIẾP]";
                }

                String displayMessage = String.format("[%s]%s Từ %s:\n%s\n%s\n",
                    timestamp, destInfo, senderIP, message, "─".repeat(60));
                receivedMessagesArea.append(displayMessage);
                receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());

                // Show notification
                showNotification("Tin nhắn mới" + destInfo, "Từ: " + senderIP);
            });
        });
        receiver.start();
        updateStatus("Đã bắt đầu nhận tin nhắn. Đang lắng nghe...", Color.BLUE);
    }

    private void sendMessage() {
        String destination = destinationField.getText().trim();
        String message = messageField.getText().trim();

        if (destination.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập địa chỉ đích!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tin nhắn!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            sender.sendMessage(destination, message);

            String destType = "";
            if (destination.equals("*")) {
                destType = " [BROADCAST - GỬI TẤT CẢ]";
            } else if (destination.startsWith("group")) {
                String groupName = destination;
                if (destination.equals("groupA")) groupName = "Nhóm A";
                else if (destination.equals("groupB")) groupName = "Nhóm B";
                else if (destination.equals("groupC")) groupName = "Nhóm C";
                destType = " [NHÓM: " + groupName + "]";
            } else {
                destType = " [TRỰC TIẾP]";
            }

            updateStatus("Đã gửi tin nhắn thành công đến: " + destination + destType, new Color(0, 128, 0));
            messageField.setText("");

            // Log sent message
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String logMessage = String.format("[%s] ĐÃ GỬI%s đến %s: %s\n%s\n",
                timestamp, destType, destination, message, "─".repeat(60));
            receivedMessagesArea.append(logMessage);
            receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());

        } catch (Exception ex) {
            updateStatus("Không thể gửi tin nhắn: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this,
                "Lỗi khi gửi tin nhắn:\n" + ex.getMessage(),
                "Lỗi",
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
