# Tài Liệu Yêu Cầu Nghiệp Vụ (BRD)
# Game: Memory Card Flip

---

| Thông tin | Chi tiết |
|---|---|
| **Mã tài liệu** | MCF-BRD-001 |
| **Phiên bản** | 1.0 |
| **Ngày tạo** | 2025 |
| **Người viết** | Product Owner |
| **Trạng thái** | Draft |
| **Phân loại** | Nội bộ – Bảo mật |

> Tài liệu này chứa thông tin mật. Không sao chép hoặc phân phối khi chưa được sự đồng ý.

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Mục tiêu kinh doanh](#2-mục-tiêu-kinh-doanh)
3. [Stakeholders](#3-stakeholders)
4. [Mô tả tổng quan hệ thống](#4-mô-tả-tổng-quan-hệ-thống)
5. [Yêu cầu nghiệp vụ](#5-yêu-cầu-nghiệp-vụ)
6. [Yêu cầu giao diện](#6-yêu-cầu-giao-diện)
7. [Yêu cầu phi chức năng](#7-yêu-cầu-phi-chức-năng)
8. [Use Cases](#8-use-cases)
9. [KPI và Tiêu chí thành công](#9-kpi-và-tiêu-chí-thành-công)
10. [Định hướng phát triển](#10-định-hướng-phát-triển)
11. [Ràng buộc](#11-ràng-buộc)
12. [Giả định](#12-giả-định)
13. [Ma trận truy xuất yêu cầu](#13-ma-trận-truy-xuất-yêu-cầu)

---

## 1. Giới thiệu

### 1.1 Mục đích tài liệu

Tài liệu Business Requirements Document (BRD) này mô tả đầy đủ các yêu cầu nghiệp vụ cho việc phát triển sản phẩm game **Memory Card Flip**. Tài liệu được sử dụng làm cơ sở cho quá trình phân tích, thiết kế, phát triển và kiểm thử sản phẩm.

**Mục tiêu cụ thể của tài liệu:**

- Xác định rõ ràng các yêu cầu nghiệp vụ và chức năng hệ thống
- Thống nhất nhận thức giữa các bên liên quan (Stakeholders)
- Làm nền tảng cho tài liệu System Requirements Specification (SRS) và thiết kế kỹ thuật
- Cung cấp cơ sở để kiểm thử và nghiệm thu sản phẩm

---

### 1.2 Phạm vi dự án

Game **Memory Card Flip** là một ứng dụng game trí nhớ chạy trên nền tảng desktop (Windows). Người chơi sẽ lật các thẻ bài ẩn để tìm các cặp thẻ có hình ảnh trùng nhau trong thời gian ngắn nhất có thể.

**Phạm vi bao gồm:**

- Giao diện người dùng (UI) đầy đủ cho toàn bộ luồng chơi game
- Hệ thống logic trò chơi (game engine): lật thẻ, kiểm tra cặp, tính điểm, đếm giờ
- Hệ thống lưu trữ điểm cao cục bộ (Local High Score)
- Cấu hình 3 mức độ khó: Easy (4×4), Medium (6×6), Hard (8×8)
- Hệ thống tính điểm có bonus streak

**Phạm vi KHÔNG bao gồm (ngoài phạm vi v1.0):**

- Chức năng multiplayer online
- Lưu trữ dữ liệu trên cloud / server
- Hệ thống thanh toán hoặc in-app purchase
- Ứng dụng mobile (iOS / Android) – sẽ xem xét ở phiên bản sau

---

### 1.3 Định nghĩa và viết tắt

| Thuật ngữ / Viết tắt | Giải thích |
|---|---|
| **BRD** | Business Requirements Document – Tài liệu yêu cầu nghiệp vụ |
| **SRS** | System Requirements Specification – Tài liệu đặc tả hệ thống |
| **BR** | Business Requirement – Yêu cầu nghiệp vụ |
| **UC** | Use Case – Kịch bản sử dụng |
| **UI** | User Interface – Giao diện người dùng |
| **NFR** | Non-Functional Requirement – Yêu cầu phi chức năng |
| **FPS** | Frames Per Second – Số khung hình mỗi giây |
| **KPI** | Key Performance Indicator – Chỉ số hiệu suất chính |
| **High Score** | Điểm cao nhất đạt được và lưu lại |
| **Streak** | Chuỗi lật thẻ đúng liên tiếp không sai |
| **Flip** | Hành động lật mở một thẻ bài |

---

## 2. Mục tiêu kinh doanh

### 2.1 Mục tiêu tổng quát

**Memory Card Flip** được phát triển nhằm mang lại một sản phẩm game đơn giản nhưng gây nghiện, kết hợp giải trí với rèn luyện trí nhớ, phù hợp với mọi lứa tuổi.

| STT | Mục tiêu | Tiêu chí thành công |
|---|---|---|
| **BO-01** | Tạo trải nghiệm chơi game mượt mà | Người chơi chơi lại ≥ 3 lần/ngày, tỷ lệ giữ chân ≥ 40% |
| **BO-02** | Rèn luyện trí nhớ người dùng | Điểm trung bình tăng ≥ 10% sau 10 lần chơi |
| **BO-03** | Xây dựng nền tảng để mở rộng | Có thể thêm chủ đề, mode mới trong < 1 tuần dev |
| **BO-04** | Demo quy trình DevOps / Java | CI/CD pipeline hoàn chỉnh, có unit test ≥ 80% coverage |
| **BO-05** | Ra mắt sản phẩm đúng hạn | Release v1.0 đúng deadline dự án học tập |

---

## 3. Stakeholders

Danh sách các bên liên quan trực tiếp và gián tiếp đến dự án:

| Vai trò | Mô tả | Quyền lợi / Mong đợi | Mức độ ảnh hưởng |
|---|---|---|---|
| **Người chơi** | Người dùng cuối sử dụng game | Game vui, mượt mà, không lỗi, điểm số chính xác | Cao |
| **Product Owner** | Định hướng tính năng và độ ưu tiên | Sản phẩm đúng ý tưởng, đúng hạn, đủ chất lượng | Rất cao |
| **Developer** | Xây dựng và phát triển hệ thống | Yêu cầu rõ ràng, không thay đổi đột ngột | Cao |
| **Tester / QA** | Kiểm thử chất lượng sản phẩm | Tiêu chí chấp nhận rõ ràng, có test case đầy đủ | Trung bình |
| **Giảng viên / Mentor** | Đánh giá dự án học tập | Demo được các kỹ năng kỹ thuật và quy trình | Trung bình |

---

## 4. Mô tả tổng quan hệ thống

### 4.1 Tổng quan kiến trúc

**Memory Card Flip** là một single-player offline desktop game viết bằng Java. Hệ thống gồm 3 tầng chính:

- **Presentation Layer (UI):** Hiển thị giao diện game, xử lý sự kiện người chơi
- **Business Logic Layer:** Xử lý luật chơi, tính điểm, quản lý trạng thái game
- **Data Layer:** Lưu trữ điểm cao cục bộ (file / local DB)

---

### 4.2 Chức năng chính

| STT | Chức năng | Mô tả ngắn |
|---|---|---|
| **F-01** | Bắt đầu game mới | Khởi tạo bàn game, trộn xáo thẻ, bật timer |
| **F-02** | Lật thẻ | Người chơi click/tap thẻ để lật mặt hiển thị |
| **F-03** | Kiểm tra cặp thẻ | So sánh 2 thẻ vừa lật, xử lý trùng / sai |
| **F-04** | Tính điểm và streak | Cập nhật điểm theo luật, xử lý bonus ×2 |
| **F-05** | Đếm ngược thời gian | Hiển thị và quản lý timer theo cấp độ |
| **F-06** | Kết thúc game | Phát hiện điều kiện win/lose, hiển thị kết quả |
| **F-07** | Lưu điểm cao | So sánh và lưu high score cục bộ |
| **F-08** | Chọn cấp độ khó | Người chơi lựa chọn Easy / Medium / Hard |
| **F-09** | Chơi lại | Reset game ở cùng cấp độ mà không cần restart ứng dụng |
| **F-10** | Xem bảng xếp hạng | Hiển thị top điểm cao nhất đã lưu |

---

### 4.3 Người dùng mục tiêu

- **Học sinh, sinh viên (10–25 tuổi):** Nhóm chính, sử dụng game giải trí và luyện trí nhớ
- **Người đi làm muốn thư giãn (25–40 tuổi):** Chơi trong giờ nghỉ ngắn, yêu cầu game load nhanh
- **Người cao tuổi muốn rèn luyện trí nhớ:** Giao diện đơn giản, chữ to, không cần hướng dẫn phức tạp

---

## 5. Yêu cầu nghiệp vụ

Mỗi yêu cầu nghiệp vụ được mô tả đầy đủ theo mẫu chuẩn bao gồm: mô tả, điều kiện tiên quyết, luồng xử lý chính, điều kiện sau và trường hợp ngoại lệ.

---

### BR-01 – Bắt đầu game mới

| Trường | Nội dung |
|---|---|
| **Mô tả** | Hệ thống cho phép người chơi khởi tạo một ván chơi mới. Hệ thống sẽ xáo trộn và hiển thị bộ thẻ theo cấp độ được chọn, bắt đầu đếm giờ và đặt trạng thái sẵn sàng. |
| **Actor** | Người chơi |
| **Điều kiện trước** | Ứng dụng đang ở màn hình chính (Main Menu) hoặc màn hình kết thúc game |
| **Luồng chính** | 1. Người chơi chọn cấp độ khó (Easy / Medium / Hard) <br> 2. Hệ thống tạo bộ 16 / 36 / 64 thẻ (8 / 18 / 32 cặp) <br> 3. Hệ thống xáo trộn ngẫu nhiên vị trí các thẻ <br> 4. Hệ thống hiển thị tất cả thẻ ở trạng thái úp (mặt sau) <br> 5. Hệ thống bắt đầu đếm giờ ngược (30 / 60 / 90 giây tương ứng) <br> 6. Hệ thống đặt điểm = 0, streak = 0 <br> 7. Người chơi bắt đầu chơi |
| **Điều kiện sau** | Ván chơi mới được khởi tạo đầy đủ, người chơi có thể lật thẻ |
| **Trường hợp ngoại lệ** | • Người chơi chưa chọn cấp độ: Hệ thống mặc định là Easy <br> • Đang giữa ván chơi chọn "New Game": Hiển thị xác nhận trước khi reset |

---

### BR-02 – Lật thẻ (Card Flip)

| Trường | Nội dung |
|---|---|
| **Mô tả** | Người chơi có thể click/tap vào thẻ để lật mặt trước. Mỗi lần chỉ được lật tối đa 2 thẻ. Thẻ đang được giữ nguyên sẽ không thể lật lại. |
| **Actor** | Người chơi |
| **Điều kiện trước** | Ván chơi đang hoạt động, còn ít nhất 2 thẻ chưa tìm được cặp |
| **Luồng chính** | 1. Người chơi click/tap vào một thẻ đang úp <br> 2. Hệ thống hiển thị mặt trước của thẻ với animation lật mượt mà (0.3 giây) <br> 3. Hệ thống đánh dấu thẻ là "đang lật" <br> 4. Nếu đã có 1 thẻ đang lật: Hệ thống cho phép lật thêm 1 thẻ thứ 2 <br> 5. Nếu đã có 2 thẻ đang lật: Hệ thống khóa không cho lật thêm thẻ khác cho đến khi kết quả được xử lý |
| **Điều kiện sau** | Thẻ được lật và hiển thị mặt trước, hệ thống chờ kết quả kiểm tra cặp |
| **Trường hợp ngoại lệ** | • Click vào thẻ đã tìm được cặp (biến mất): Không có tác dụng <br> • Click vào thẻ đang lật (chưa có kết quả): Không có tác dụng <br> • Click khi đã có 2 thẻ đang chờ kết quả: Hệ thống bỏ qua input |

---

### BR-03 – Kiểm tra cặp thẻ

| Trường | Nội dung |
|---|---|
| **Mô tả** | Sau khi người chơi lật 2 thẻ, hệ thống tự động so sánh và xử lý kết quả: nếu trùng khớp thì xóa thẻ, nếu khác nhau thì lật lại. Quá trình này xảy ra tự động, không cần input từ người chơi. |
| **Actor** | Hệ thống (tự động) |
| **Điều kiện trước** | Người chơi đã lật chính xác 2 thẻ |
| **Luồng chính** | 1. Hệ thống đợi 1 giây để người chơi nhìn rõ cả 2 thẻ <br> 2. Hệ thống so sánh giá trị / hình ảnh của 2 thẻ <br> 3a. **[Trường hợp TRÙNG]:** Hệ thống chạy animation biến mất (0.5 giây), thêm +1 vào số cặp đã tìm <br> 3b. **[Trường hợp KHÁC]:** Hệ thống chạy animation lật lại (0.3 giây), cả 2 thẻ trở về trạng thái úp <br> 4. Hệ thống cập nhật streak và tính điểm theo BR-04 <br> 5. Hệ thống mở khóa input, cho phép người chơi tiếp tục |
| **Điều kiện sau** | Hai thẻ trả về trạng thái phù hợp, điểm và streak được cập nhật |
| **Trường hợp ngoại lệ** | • Hệ thống bị lag hoặc timer hết trước khi có kết quả: Xử lý theo kết quả đã có, sau đó kết thúc game |

---

### BR-04 – Hệ thống tính điểm và Streak Bonus

| Trường | Nội dung |
|---|---|
| **Mô tả** | Điểm số phản ánh hiệu quả chơi game của người chơi, tính toán dựa trên số cặp tìm đúng và thời gian còn lại. Có cơ chế bonus ×2 khi đạt streak liên tiếp trong window thời gian. |
| **Actor** | Hệ thống (tự động) |
| **Điều kiện trước** | Người chơi tìm được một cặp thẻ đúng |
| **Luồng chính** | 1. Hệ thống cộng điểm cơ bản: **+100 điểm / cặp đúng** <br> 2. Hệ thống tính bonus thời gian: **+(thời gian còn lại × 2) điểm** <br> 3. Hệ thống theo dõi streak: Đếm số lần lật đúng liên tiếp KHÔNG có lần sai <br> 4. Streak ≥ 3 trong vòng 5 giây: Hệ thống kích hoạt **"Streak Bonus Mode" ×2** <br> 5. Trong Streak Bonus Mode: Mỗi cặp đúng tiếp theo nhân điểm ×2 <br> 6. Streak Bonus Mode kết thúc khi người chơi sai 1 cặp hoặc 5 giây trôi qua <br> 7. Nếu sai: streak = 0, Streak Bonus Mode tắt <br> 8. Cuối game: Điểm tổng = tổng tất cả điểm tích lũy |
| **Điều kiện sau** | Điểm được cập nhật đúng công thức, hiển thị chính xác trên UI |
| **Trường hợp ngoại lệ** | • Điểm âm: Không cho phép, điểm tối thiểu là 0 <br> • Bonus Mode còn hiệu lực khi hết giờ: Điểm được tính như bình thường rồi kết thúc game |

---

### BR-05 – Hệ thống đếm ngược thời gian

| Trường | Nội dung |
|---|---|
| **Mô tả** | Mỗi ván chơi có một bộ đếm ngược tổng thời gian. Khi hết giờ, game tự động kết thúc. Thời gian được cài đặt theo cấp độ khó và hiển thị rõ ràng trên giao diện. |
| **Actor** | Hệ thống (tự động) |
| **Điều kiện trước** | Ván chơi được khởi tạo thành công |
| **Luồng chính** | 1. Hệ thống bắt đầu đếm ngược từ thời gian tương ứng cấp độ (90 / 60 / 30 giây) <br> 2. Timer giảm 1 giây mỗi giây <br> 3. Khi còn ≤ 10 giây: Hệ thống đổi màu timer sang đỏ và chạy hiệu ứng nhấp nháy <br> 4. Khi timer = 0: Hệ thống gọi BR-06 để xử lý kết thúc game (thua) <br> 5. Người chơi có thể tạm dừng (Pause): Timer dừng lại <br> 6. Resume: Timer tiếp tục từ nơi dừng lại |
| **Điều kiện sau** | Timer chạy đúng, hiệu ứng cảnh báo hiển thị đúng, game kết thúc đúng hạn |
| **Trường hợp ngoại lệ** | • Màn hình bị ẩn / minimize: Nên tạm dừng timer tự động để tránh mất điểm vô lý |

---

### BR-06 – Kết thúc game và màn hình kết quả

| Trường | Nội dung |
|---|---|
| **Mô tả** | Game kết thúc khi 1 trong 2 điều kiện: (1) Tìm được tất cả cặp thẻ (thắng), hoặc (2) Hết giờ (thua). Màn hình kết quả hiển thị toàn bộ thông số ván chơi và cho phép người chơi tiếp tục. |
| **Actor** | Hệ thống (tự động) |
| **Điều kiện trước** | Điều kiện kết thúc game được kích hoạt |
| **Luồng chính** | 1. Hệ thống dừng timer <br> 2. Hệ thống xác định kết quả: **WIN** (tìm hết cặp) hoặc **LOSE** (hết giờ) <br> 3. Hệ thống tính toán điểm cuối cùng (có thêm bonus kết thúc nếu WIN) <br> 4. Hệ thống hiển thị màn hình kết quả với: Kết quả WIN/LOSE, Thời gian, Số lượt lật, Điểm số, Số cặp tìm được / tổng cặp <br> 5. Hệ thống so sánh với high score hiện tại <br> 6. Nếu là điểm cao mới: Hiển thị thông báo **"New High Score!"** và lưu <br> 7. Hiển thị 2 nút: **"Chơi lại"** (cùng cấp độ) và **"Menu chính"** |
| **Điều kiện sau** | Màn hình kết quả hiển thị đầy đủ, high score được cập nhật nếu cần |
| **Trường hợp ngoại lệ** | • Người chơi đóng cửa sổ đột ngột: Tự động lưu trạng thái, sẽ hỏi khi mở lại |

---

### BR-07 – Lưu trữ và hiển thị High Score

| Trường | Nội dung |
|---|---|
| **Mô tả** | Hệ thống lưu điểm cao nhất cho từng cấp độ một cách bền vững. Bảng xếp hạng hiển thị top 10 kết quả tốt nhất của người chơi trên thiết bị này. |
| **Actor** | Hệ thống + Người chơi |
| **Điều kiện trước** | Người chơi đã hoàn thành ít nhất 1 ván chơi |
| **Luồng chính** | 1. Hệ thống lưu bộ tứ dữ liệu: Cấp độ, Điểm, Thời gian, Số lượt, Ngày giờ <br> 2. Dữ liệu được lưu vào file cục bộ (JSON hoặc SQLite) <br> 3. Màn hình Leaderboard hiển thị top 10 kết quả, phân loại theo cấp độ <br> 4. Người chơi có thể xem leaderboard bất kỳ lúc nào từ menu chính <br> 5. Người chơi có thể xóa lịch sử (có xác nhận) |
| **Điều kiện sau** | Dữ liệu được lưu bền vững, hiển thị chính xác trong leaderboard |
| **Trường hợp ngoại lệ** | • File dữ liệu bị hỏng: Hệ thống tự reset về rỗng và báo lỗi cho người dùng <br> • Điểm bằng nhau: Ưu tiên kết quả có thời gian nhanh hơn |

---

## 6. Yêu cầu giao diện

### 6.1 Màn hình chính (Main Menu)

- Logo và tên game: **Memory Card Flip** – Hiển thị rõ ràng, bắt mắt
- Nút **"Chơi ngay"** (Play): Chuyển thẳng sang màn hình chọn cấp độ
- Nút **"Leaderboard"**: Hiển thị bảng điểm cao
- Nút **"Cài đặt"** (Settings): Âm thanh, ngôn ngữ (nếu có)
- Nút **"Thoát"**: Đóng ứng dụng (có xác nhận)

---

### 6.2 Màn hình chơi game (Game Screen)

**Thanh trạng thái (Status Bar) – Hiển thị liên tục trong khi chơi:**

- **Timer:** Hiển thị thời gian còn lại, đổi màu đỏ khi ≤ 10 giây
- **Điểm số:** Cập nhật ngay lập tức khi tìm được cặp
- **Số cặp:** `X / Y cặp` (đã tìm / tổng số)
- **Streak indicator:** Hiển thị chuỗi streak hiện tại (0 nếu chưa có)
- **Nút Pause:** Có thể tạm dừng bất kỳ lúc nào

**Khu vực bàn game (Game Board):**

| Cấp độ | Grid | Số thẻ | Thời gian |
|---|---|---|---|
| Easy | 4×4 | 16 thẻ | 90 giây |
| Medium | 6×6 | 36 thẻ | 60 giây |
| Hard | 8×8 | 64 thẻ | 30 giây |

- Mỗi thẻ hiển thị mặt sau thống nhất khi chưa lật
- **Animation lật thẻ:** Flip 3D mượt mà trong 0.3 giây
- **Animation biến mất:** Fade out sau 1 giây khi tìm đúng cặp
- **Animation lắc (shake):** Khi lật sai cặp trong 0.5 giây

---

### 6.3 Yêu cầu về hình ảnh và typography

- Phông chữ: Rõ ràng, dễ đọc, hỗ trợ Unicode tiếng Việt
- Màu sắc: Có thể tùy chỉnh qua theme, default là pastel/soft
- Kích thước cửa sổ tối thiểu: **800×600 pixels**
- Hỗ trợ fullscreen mode
- Toàn bộ UI phải accessible với chuột (không bắt buộc bàn phím)

---

## 7. Yêu cầu phi chức năng

| Mã | Loại | Yêu cầu | Tiêu chí đo lường |
|---|---|---|---|
| **NFR-01** | Hiệu năng | Game phải khởi động trong vòng 3 giây trên máy chuẩn | Startup time < 3s |
| **NFR-02** | Hiệu năng | Animation phải mượt, không giật lag | FPS ≥ 30 trong mọi thao tác |
| **NFR-03** | Hiệu năng | Phản hồi input < 100ms | Từ click đến lật thẻ < 100ms |
| **NFR-04** | Usability | Người chơi mới hiểu cách chơi chỉ trong 2 phút | User test: 8/10 chơi được ngay |
| **NFR-05** | Usability | Không cần đọc hướng dẫn, có thể tự hiểu giao diện | UI tự giải thích (self-explanatory) |
| **NFR-06** | Tương thích | Chạy tốt trên Windows 10 và Windows 11 | Kiểm thử trên cả 2 phiên bản |
| **NFR-07** | Tương thích | Hỗ trợ JRE 11 trở lên | Không dùng tính năng Java 17+ |
| **NFR-08** | Độ tin cậy | Không crash trong suốt ván chơi bình thường | 0 crash trong 100 lần chơi thử |
| **NFR-09** | Độ tin cậy | Lưu dữ liệu không bị mất khi đóng đột ngột | Test force-quit, dữ liệu giữ nguyên |
| **NFR-10** | Bảo mật | Dữ liệu người dùng chỉ lưu cục bộ, không gửi mạng | Kiểm tra network traffic = 0 |
| **NFR-11** | Bảo trì | Code có comment đầy đủ, có unit test | Test coverage ≥ 70% |

---

## 8. Use Cases

### 8.1 Tổng quan Use Case

| Mã UC | Tên Use Case | Actor | Mô tả ngắn |
|---|---|---|---|
| **UC-01** | Chọn cấp độ và bắt đầu game | Người chơi | Chọn Easy/Medium/Hard và khởi động ván mới |
| **UC-02** | Lật và kiểm tra thẻ | Người chơi | Click thẻ, xem kết quả, tiếp tục chơi |
| **UC-03** | Tạm dừng và tiếp tục | Người chơi | Pause game, Resume game |
| **UC-04** | Xem kết quả và chơi lại | Người chơi | Xem màn hình kết quả, quyết định tiếp theo |
| **UC-05** | Xem bảng xếp hạng | Người chơi | Xem top điểm cao theo cấp độ |
| **UC-06** | Xóa lịch sử điểm | Người chơi | Reset toàn bộ lịch sử leaderboard |

---

### 8.2 Chi tiết Use Case: UC-02 – Lật và kiểm tra thẻ

Đây là Use Case trung tâm của game, xảy ra nhiều lần nhất trong mỗi ván chơi.

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC-02 |
| **Tên** | Lật và kiểm tra cặp thẻ |
| **Actor chính** | Người chơi |
| **Mô tả** | Người chơi lật 2 thẻ, hệ thống kiểm tra và xử lý kết quả |
| **Điều kiện tiên quyết** | Ván chơi đang chạy, còn thẻ chưa tìm được cặp, đang có < 2 thẻ mở |
| **Luồng chính** | 1. Người chơi click thẻ 1 → Hệ thống lật thẻ 1 (animation 0.3s) <br> 2. Người chơi click thẻ 2 → Hệ thống lật thẻ 2 (animation 0.3s) <br> 3. Hệ thống đợi 1s để người chơi nhìn rõ <br> 4a. **[Trùng]:** Biến mất + cộng điểm + tăng streak <br> 4b. **[Sai]:** Lật lại + reset streak <br> 5. Hệ thống mở khóa input |
| **Điều kiện sau** | 2 thẻ được xử lý, điểm cập nhật, người chơi tiếp tục |
| **Luồng ngoại lệ** | E1: Thẻ 2 = Thẻ 1 → Không cho lật, đợi thẻ 2 khác <br> E2: Hết giờ khi đang chờ kết quả → Kết thúc game với điểm hiện tại |
| **Tần suất** | 8–32 lần / ván chơi (tùy cấp độ) |

---

## 9. KPI và Tiêu chí thành công

| Mã | KPI | Mục tiêu | Cách đo |
|---|---|---|---|
| **K-01** | Thời gian chơi trung bình / ván | ≥ 2 phút | Log thời gian mỗi ván |
| **K-02** | Số lượt chơi lại sau khi kết thúc | ≥ 50% người chơi lại ngay | Đếm sự kiện "Play Again" |
| **K-03** | Tỷ lệ hoàn thành (Win Rate) – Easy | ≥ 70% | Số ván WIN / tổng ván Easy |
| **K-04** | Tỷ lệ hoàn thành – Medium | ≥ 40% | Số ván WIN / tổng ván Medium |
| **K-05** | Tỷ lệ hoàn thành – Hard | ≥ 20% | Số ván WIN / tổng ván Hard |
| **K-06** | Điểm trung bình người chơi | Tăng 10% sau 10 ván | So sánh điểm ván 1–5 vs 6–10 |
| **K-07** | Không có lỗi crash | 0 crash report | Kiểm thử 100 ván liên tục |
| **K-08** | Startup time | < 3 giây | Đo thời gian trên máy test |

---

## 10. Định hướng phát triển

| Mã | Tính năng | Mô tả | Phiên bản dự kiến |
|---|---|---|---|
| **FS-01** | Online Multiplayer | 2 người chơi cạnh tranh cùng một bộ thẻ, ai tìm nhiều cặp hơn trong thời gian giới hạn thì thắng | v2.0 |
| **FS-02** | Theme đa dạng | Hỗ trợ nhiều bộ thẻ: Pokemon, Animals, Flags, Numbers, Letters... | v1.1 |
| **FS-03** | Âm thanh & hiệu ứng | Nhạc nền, âm thanh khi lật thẻ, âm thanh khi tìm đúng, nhạc win/lose | v1.1 |
| **FS-04** | Đăng nhập tài khoản | Người chơi tạo tài khoản để lưu điểm trên nhiều thiết bị | v2.0 |
| **FS-05** | Cloud Leaderboard | Bảng xếp hạng toàn cầu, so sánh với cộng đồng | v2.0 |
| **FS-06** | Chế độ luyện tập | Không giới hạn thời gian, không tính điểm, để người mới học cách chơi | v1.2 |
| **FS-07** | Daily Challenge | Mỗi ngày 1 thách thức đặc biệt với bộ thẻ và luật chơi riêng | v2.0 |
| **FS-08** | iOS / Android | Chuyển game sang nền tảng mobile | v3.0 |

---

## 11. Ràng buộc

### 11.1 Ràng buộc kỹ thuật

- Ngôn ngữ phát triển: **Java** (bắt buộc, để phục vụ mục tiêu demo DevOps Java)
- Nền tảng: Desktop Windows (không phát triển mobile ở giai đoạn này)
- Không sử dụng backend server, database hoặc bất kỳ dịch vụ cloud nào
- Dữ liệu chỉ lưu cục bộ trên máy người chơi

### 11.2 Ràng buộc dự án

- Thời gian phát triển hạn chế (dự án học tập, không phải sản phẩm thương mại)
- Nguồn lực team nhỏ (1–3 developer)
- Ngân sách: Không có (sử dụng toàn bộ open-source / free tools)
- Không có chuyên gia UX/Design chuyên nghiệp trong team

### 11.3 Ràng buộc pháp lý và tác quyền

- Các hình ảnh sử dụng trên thẻ phải là free-to-use hoặc tự vẽ
- Âm thanh (nếu có ở phiên bản sau) phải là copyright-free
- Không sử dụng logo hoặc hình ảnh của các thương hiệu như Pokemon, Disney... trong phiên bản này

---

## 12. Giả định

- Người dùng có máy tính Windows 10/11 với cấu hình tối thiểu: RAM 4GB, CPU dual-core
- Người dùng đã cài JRE 11 hoặc mới hơn (hoặc ứng dụng đóng gói kèm JRE)
- Người chơi biết cách sử dụng chuột cơ bản (click, pointer)
- Người chơi hiểu quy tắc game memory card flip (nhớ thẻ đúng vị trí)
- Game chơi offline, không cần kết nối internet
- Mỗi thiết bị chỉ có một người chơi, không có tính năng nhiều hồ sơ (profile)
- Màn hình người dùng có độ phân giải tối thiểu **1024×768 pixels**

---

## 13. Ma trận truy xuất yêu cầu

Bảng dưới thể hiện mối liên hệ giữa các yêu cầu nghiệp vụ, Use Case và chức năng hệ thống:

| BR ID | Yêu cầu nghiệp vụ | Use Case liên quan | Tính năng | Độ ưu tiên |
|---|---|---|---|---|
| **BR-01** | Bắt đầu game mới | UC-01: Chọn cấp độ | F-01, F-08 | 🔴 Cao |
| **BR-02** | Lật thẻ | UC-02: Lật kiểm tra thẻ | F-02 | 🔴 Cao |
| **BR-03** | Kiểm tra cặp thẻ | UC-02: Lật kiểm tra thẻ | F-03 | 🔴 Cao |
| **BR-04** | Tính điểm và streak | UC-02, UC-04 | F-04 | 🔴 Cao |
| **BR-05** | Đếm ngược thời gian | UC-02, UC-03, UC-04 | F-05 | 🔴 Cao |
| **BR-06** | Kết thúc game | UC-04: Kết quả, chơi lại | F-06 | 🔴 Cao |
| **BR-07** | Lưu và hiển thị High Score | UC-05, UC-06 | F-07, F-10 | 🟡 Trung bình |

---

## Kết luận

Tài liệu Business Requirements Document này đã mô tả đầy đủ và chi tiết toàn bộ yêu cầu nghiệp vụ cho game **Memory Card Flip** phiên bản 1.0. Các yêu cầu được tổ chức theo chuẩn BRD chuyên nghiệp, bao gồm:

- **07 yêu cầu nghiệp vụ chính** (BR-01 đến BR-07) với đầy đủ điều kiện và luồng xử lý
- **10 chức năng hệ thống** (F-01 đến F-10) phản ánh đầy đủ scope của sản phẩm
- **11 yêu cầu phi chức năng** (NFR-01 đến NFR-11) đảm bảo chất lượng
- **06 Use Case chính** với chi tiết UC-02 là use case trung tâm
- **08 KPI** đo lường thành công rõ ràng
- **08 định hướng phát triển** cho các phiên bản tương lai

Game **Memory Card Flip** là dự án học tập lý tưởng để:

- Luyện tập quy trình phát triển phần mềm theo chuẩn (Requirements → Design → Implement → Test)
- Demo kỹ năng DevOps và CI/CD với Java
- Xây dựng portfolio cá nhân cho developer

---


*MCF-BRD-001 | Memory Card Flip – Business Requirements Document | v1.0 | 2025*
