services:
  api-escalonador:
    image: raphaelmuniz/escalonador-api:1.0.0
    build:
      context: ./LEA01
      dockerfile: Dockerfile
    container_name: escalonador-api
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m
    networks:
      - escalonador-network
    restart: unless-stopped

  web-interface:
    image: raphaelmuniz/escalonador-web:1.0.0
    build:
      context: ./frontend            
      dockerfile: frontend.Dockerfile 
    container_name: escalonador-web
    ports:
      - "8081:80"
    depends_on:
      - api-escalonador
    networks:
      - escalonador-network
    restart: unless-stopped

networks:
  escalonador-network:
    driver: bridge
