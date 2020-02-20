FROM alpine:latest

RUN apk --no-cache add ca-certificates wget && \
wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk && \
apk add glibc-2.30-r0.apk && \
apk add potrace

#Download all the dictionary files for the dictionary
RUN mkdir -p /app/dict/ && \
wget -q -O /app/dict/en_US.txt https://log3900.blob.core.windows.net/polydraw/dict/en_US.txt && \
wget -q -O /app/dict/en_US_bad.txt https://log3900.blob.core.windows.net/polydraw/badwords/en_US.txt && \
wget -q -O /app/dict/fr_QC.txt https://log3900.blob.core.windows.net/polydraw/dict/fr_QC.txt && \
wget -q -O /app/dict/fr_QC_bad.txt https://log3900.blob.core.windows.net/polydraw/badwords/fr_QC.txt

COPY dist/server-linux /app/server
RUN chmod +x /app/server

HEALTHCHECK --interval=30s --timeout=3s CMD wget --quiet --tries=1 --spider http://localhost:3000/healthcheck || exit 1

EXPOSE 3000/tcp
EXPOSE 3001/tcp
WORKDIR /app/

CMD ["./server"]