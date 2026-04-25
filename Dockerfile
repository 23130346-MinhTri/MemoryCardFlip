# ── Stage 1: Builder ────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS builder
LABEL stage=builder
WORKDIR /app
# Cache Maven dependencies trước (layer riêng để tái sử dụng)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q
# Copy source và build
COPY src/ src/
RUN ./mvnw clean package -B -DskipTests -q
# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime
LABEL maintainer="memory-card-flip"
LABEL version="1.0.0"
LABEL description="Memory Card Flip — JavaFX 21 Desktop Game"
# Cài JavaFX runtime dependencies + VNC server cho GUI trong container
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgtk-3-0 \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libxtst6 \
    libxrender1 \
    libxi6 \
    libxext6 \
    libx11-6 \
    xvfb \
    x11vnc \
    fontconfig \
    fonts-liberation \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean
# Tạo user không phải root (security best practice)
RUN groupadd -r gameuser && useradd -r -g gameuser -m gameuser
WORKDIR /app
# Copy JAR từ builder stage
COPY --from=builder /app/target/MemoryCardFlip-*.jar app.jar
# Copy JavaFX SDK modules (nếu không bundle trong fat JAR)
# COPY --from=builder /app/target/lib/ lib/
# Thư mục lưu data người dùng (high scores, settings)
RUN mkdir -p /home/gameuser/.memorycardflip && \
    chown -R gameuser:gameuser /home/gameuser/.memorycardflip

USER gameuser
# Port VNC
EXPOSE 5900
# Entrypoint script
COPY --chown=gameuser:gameuser docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh
ENTRYPOINT ["./docker-entrypoint.sh"]