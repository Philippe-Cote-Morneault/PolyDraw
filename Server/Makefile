.PHONY: build clean run lint fix-lint package

GOPATH ?= $(shell go env GOPATH)
GOOS ?= $(shell go env GOOS)
GOARCH ?= $(shell go env GOARCH)
GOLINT ?= $(shell go list -f {{.Target}} golang.org/x/lint/golint)
GOFLAGS ?= $(GOFLAGS:) -mod=vendor
LDFLAGS = -s -w
GO = go
BUILD_HASH = $(shell git rev-parse HEAD)

DIST_ROOT=dist
PWD = $(shell pwd)

ifeq ($(RELEASE), True)
DOCKERNAME = log3900.azurecr.io/server
else
DOCKERNAME = log3900.azurecr.io/server-dev
endif

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

deploy: package
	docker push $(DOCKERNAME)

clean:
	go clean ./...
	rm -rf $(DIST_ROOT)/

run: build
	./$(DIST_ROOT)/server

lint:
	@echo • Checking gofmt
	@for package in $$(go list ./...); do \
		echo "  - Checking "$$package; \
		files=$$(go list -f '{{range .GoFiles}}{{$$.Dir}}/{{.}} {{end}}' $$package); \
		if [ "$$files" ]; then \
			gofmt_output=$$(gofmt -d -s $$files 2>&1); \
			if [ "$$gofmt_output" ]; then \
				echo "\033[1;33m" \
				echo "$$gofmt_output"; \
				echo "\033[31m  Gofmt failure ❌\033[0m";\
				exit 1; \
			fi; \
		fi; \
	done
	@echo "\033[92m  Gofmt success ✅\033[0m"
	@echo
	@echo • Checking golint
	@output=$$($(GOLINT) ./...); \
	if [ "$$output" ]; then \
		while IFS= read -r line; do \
			echo "  - \033[1;33m$$line\033[0m"; \
		done <<< "$$output" ; \
		echo "\033[31m  Golint failure ❌\033[0m"; \
		exit 1; \
	fi;
	@echo "\033[92m  Golint success ✅\033[0m"

fix-lint:
	go fmt ./...