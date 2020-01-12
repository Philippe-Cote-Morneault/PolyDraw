.PHONY: build clean run lint fix-lint

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