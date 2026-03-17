# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.8-amazoncorretto-21 AS build

# Tạo và nhảy vào thư mục /app bên trong máy ảo. Mọi lệnh sau đó sẽ chạy ở đây.
# Chép file khai báo thư viện pom.xml, src từ máy Mac của bạn vào máy ảo.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
RUN mvn package -DskipTests

#Stage 2: Create Image (Thành phẩm gọn nhẹ hơn)
# Lúc này ta không cần Maven nữa, chỉ cần Java (JDK) để chạy file.
FROM amazoncorretto:21.0.4

# Set working folder to App and copy complied file from above step
WORKDIR /app
# Nó sang "nhà máy" ở Giai đoạn 1 (--from=build), lấy file .jar đã build xong và chép sang máy ảo mới này, đổi tên thành app.jar.
COPY --from=build /app/target/*.jar app.jar

# Lệnh mặc định khi khởi động Docker container
ENTRYPOINT ["java", "-jar", "app.jar"]