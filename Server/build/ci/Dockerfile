# This file is used for the image used in the CI of gitlab
FROM golang:latest

# Install golint
ENV GOPATH /go
ENV PATH ${GOPATH}/bin:$PATH
RUN go get -u golang.org/x/lint/golint && mkdir /workdir
WORKDIR /workdir/
