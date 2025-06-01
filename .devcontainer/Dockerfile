FROM mcr.microsoft.com/devcontainers/java:1-17-bullseye

# Install dependencies to run graphical applications with Java Swing
RUN apt-get update && apt-get install -y \
  libx11-6 libxtst6 libxrender1 libxi6 x11-apps \
  && rm -rf /var/lib/apt/lists/*

# Configure DISPLAY to use the graphical interface from Windows
ENV DISPLAY=:0
