# Net Send - Multicast Messaging Application

## Mô tả
Chương trình Net Send cho phép gửi tin nhắn giữa các máy tính trong mạng sử dụng Java Swing và Multicast UDP.

## Tính năng
- ✅ Gửi tin nhắn đến một máy cụ thể (theo IP)
- ✅ Gửi tin nhắn đến một nhóm máy (group)
- ✅ Gửi tin nhắn đến tất cả các máy trên mạng (broadcast)
- ✅ Nhận và hiển thị tin nhắn với địa chỉ IP người gửi
- ✅ Giao diện đồ họa Java Swing thân thiện
- ✅ Hiển thị timestamp cho mỗi tin nhắn
- ✅ Thông báo khi nhận được tin nhắn mới

## Cách sử dụng

### 1. Biên dịch và chạy
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Hoặc chạy trực tiếp từ IDE (IntelliJ IDEA):
- Mở class `Main.java`
- Click nút Run hoặc nhấn Shift+F10

### 2. Gửi tin nhắn

#### Gửi đến một máy cụ thể:
- **Destination:** `192.168.1.100` (nhập IP máy đích)
- **Message:** Nội dung tin nhắn
- Click **Send Message**

#### Gửi đến tất cả các máy:
- **Destination:** `*`
- **Message:** Nội dung tin nhắn
- Click **Send Message**

#### Gửi đến một nhóm:
- **Destination:** `groupA` hoặc `groupB` hoặc `groupC`
- **Message:** Nội dung tin nhắn
- Click **Send Message**

### 3. Nhận tin nhắn
- Chương trình tự động lắng nghe và hiển thị tin nhắn nhận được
- Mỗi tin nhắn sẽ hiển thị:
  - Thời gian nhận
  - Địa chỉ IP người gửi
  - Nội dung tin nhắn

## Cấu trúc dự án

```
src/main/java/org/example/
├── Main.java                 # Entry point
├── NetSendGUI.java          # Giao diện chính
├── MessageSender.java       # Xử lý gửi tin nhắn
├── MessageReceiver.java     # Xử lý nhận tin nhắn
└── MulticastConfig.java     # Cấu hình multicast
```

## Cấu hình Multicast

### Địa chỉ Multicast:
- **Broadcast (*):** 230.0.0.0:4446
- **Group A:** 230.0.0.1:4446
- **Group B:** 230.0.0.2:4446
- **Group C:** 230.0.0.3:4446
- **Unicast:** Port 4447

### Lưu ý khi chạy trên mạng:
1. Đảm bảo firewall cho phép UDP traffic trên port 4446 và 4447
2. Các máy phải cùng mạng LAN
3. Router phải hỗ trợ multicast (IGMP)

### Mở firewall trên Windows:
```cmd
netsh advfirewall firewall add rule name="Net Send UDP 4446" dir=in action=allow protocol=UDP localport=4446
netsh advfirewall firewall add rule name="Net Send UDP 4447" dir=in action=allow protocol=UDP localport=4447
```

## Kiểm tra trên một máy
Bạn có thể mở nhiều cửa sổ ứng dụng trên cùng một máy để test:
1. Chạy ứng dụng lần 1
2. Chạy ứng dụng lần 2 (từ terminal khác hoặc IDE khác)
3. Gửi tin nhắn từ cửa sổ 1 với destination `*`
4. Cửa sổ 2 sẽ nhận được tin nhắn

## Yêu cầu hệ thống
- Java 17 hoặc cao hơn
- Maven 3.6+
- Hỗ trợ multicast trên card mạng

## Troubleshooting

### Không nhận được tin nhắn:
1. Kiểm tra firewall
2. Kiểm tra card mạng có hỗ trợ multicast không
3. Thử chạy với quyền Administrator

### Lỗi "Permission denied":
- Chạy ứng dụng với quyền Administrator (Windows)
- Hoặc thay đổi port thành port > 1024

## Tác giả
Bài tập lập trình mạng - Multicast Messaging

