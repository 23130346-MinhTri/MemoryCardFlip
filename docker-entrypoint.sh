#!/bin/bash
# ============================================================
# docker-entrypoint.sh
# Khởi động Xvfb (virtual display) + x11vnc + JavaFX app
# ============================================================
set -e
DISPLAY_NUM=${DISPLAY_NUM:-99}
VNC_PORT=${VNC_PORT:-5900}
VNC_PASSWORD=${VNC_PASSWORD:-""}
SCREEN_RESOLUTION=${SCREEN_RESOLUTION:-"1280x720x24"}
echo "[entrypoint] Starting virtual display :${DISPLAY_NUM} (${SCREEN_RESOLUTION})"
Xvfb :${DISPLAY_NUM} -screen 0 ${SCREEN_RESOLUTION} -ac +extension GLX &
XVFB_PID=$!
# Đợi Xvfb sẵn sàng
sleep 1
export DISPLAY=:${DISPLAY_NUM}
# Khởi động VNC server
if [ -n "$VNC_PASSWORD" ]; then
    x11vnc -display :${DISPLAY_NUM} -rfbport ${VNC_PORT} \
           -passwd "${VNC_PASSWORD}" -forever -shared -bg -quiet
else
    x11vnc -display :${DISPLAY_NUM} -rfbport ${VNC_PORT} \
           -nopw -forever -shared -bg -quiet
fi
echo "[entrypoint] VNC server started on port ${VNC_PORT}"
echo "[entrypoint] Connect via VNC viewer: localhost:${VNC_PORT}"
# Khởi động JavaFX application
echo "[entrypoint] Launching Memory Card Flip..."
exec java \
    -Djava.awt.headless=false \
    -Dprism.order=sw \
    -Djavafx.verbose=false \
    -Duser.home=/home/gameuser \
    -jar app.jar "$@"
# Cleanup khi app kết thúc
kill $XVFB_PID 2>/dev/null || true