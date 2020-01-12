.PHONY: build clean run

GOPATH ?= $(shell go env GOPATH)
GOFLAGS ?= $(GOFLAGS:) -mod=vendor
GO=go
BUILD_HASH = $(shell git rev-parse HEAD)

DIST_ROOT=dist/
PWD = $(shell pwd)

build:
	GOBIN=$(PWD)/$(DIST_ROOT) \
	$(GO) install ./... 

clean:
	go clean ./...
	rm -rf $(DIST_ROOT)

run: build
	./$(DIST_ROOT)server