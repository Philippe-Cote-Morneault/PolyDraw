# This file is used for the image used in the CI of gitlab
FROM golang:latest

# Install golint
ENV GOPATH /go
ENV PATH ${GOPATH}/bin:$PATH
RUN go get -u github.com/golang/lint/golint

