FROM alpine:latest

RUN apk --no-cache add ca-certificates wget && \
wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk && \
apk add glibc-2.30-r0.apk
COPY dist/server-linux /app/server
RUN chmod +x /app/server

HEALTHCHECK --interval=30s --timeout=3s CMD wget --quiet --tries=1 --spider http://localhost:3000/healthcheck || exit 1

EXPOSE 3000/tcp
EXPOSE 3001/tcp

CMD ["/app/server"]