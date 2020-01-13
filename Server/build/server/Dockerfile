FROM alpine:latest

COPY dist/server-linux /app/server
RUN chmod +x /app/server
EXPOSE 3000/tcp
CMD ["/app/server"]