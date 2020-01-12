.PHONY: build clean run lint fix-lint package

GOPATH ?= $(shell go env GOPATH)
GOOS ?= $(shell go env GOOS)
GOARCH ?= $(shell go env GOARCH)
GOFLAGS ?= $(GOFLAGS:) -mod=vendor
LDFLAGS = -s -w
GO = go
DOCKERNAME = log3900-server
BUILD_HASH = $(shell git rev-parse HEAD)

DIST_ROOT=dist
PWD = $(shell pwd)

build:
	$(GO) build -i -o $(DIST_ROOT)/server ./cmd/server

build-linux: build
ifneq ($(GOOS)/$(GOARCH), linux/amd64)
	GOOS=linux \
	GOARCH=amd64 \
	$(GO) build -i -ldflags="$(LDFLAGS)" -o $(DIST_ROOT)/server-linux ./cmd/server
else
	cp $(DIST_ROOT)/server $(DIST_ROOT)/server-linux
endif

package: build-linux build/Dockerfile
	docker build -t $(DOCKERNAME) -f build/Dockerfile .

clean:
	go clean ./...
	rm -rf $(DIST_ROOT)/

run: build
	./$(DIST_ROOT)/server

lint:
	@for package in $$(go list ./...); do \
		echo "Checking "$$package; \
		files=$$(go list -f '{{range .GoFiles}}{{$$.Dir}}/{{.}} {{end}}' $$package); \
		if [ "$$files" ]; then \
			gofmt_output=$$(gofmt -d -s $$files 2>&1); \
			if [ "$$gofmt_output" ]; then \
				echo "$$gofmt_output"; \
				echo "Gofmt failure"; \
				exit 1; \
			fi; \
		fi; \
	done
	@echo Gofmt success

fix-lint:
	go fmt ./...