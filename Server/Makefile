.PHONY: clean run lint fix-lint race build build-linux

SHELL = bash
GOPATH ?= $(shell go env GOPATH)
GOOS ?= $(shell go env GOOS)
GOARCH ?= $(shell go env GOARCH)
GOLINT ?= $(shell go list -f {{.Target}} golang.org/x/lint/golint)
GOFLAGS ?= $(GOFLAGS:) -mod=vendor
LDFLAGS = -w -s
GO = go
BUILD_HASH = $(shell git rev-parse HEAD)

DIST_ROOT=dist
PWD = $(shell pwd)
DOCKERREGISTRY = registry.gitlab.com/jigsawcorp/log3900

ifeq ($(RELEASE), True)
DOCKERNAME = $(DOCKERREGISTRY)/server
else
DOCKERNAME = $(DOCKERREGISTRY)/server-dev
endif

build: dist/dict/en_US.txt dist/dict/en_US_bad.txt dist/dict/fr_QC.txt dist/dict/fr_QC_bad.txt
	$(GO) build -i -o $(DIST_ROOT)/server ./cmd/server
	
build-linux:
ifneq ($(GOOS)/$(GOARCH), linux/amd64)
	GOOS=linux \
	GOARCH=amd64 \
	CGO_ENABLED=1 \
	$(GO) build -i -ldflags="$(LDFLAGS)" -o $(DIST_ROOT)/server-linux ./cmd/server
else
	CGO_ENABLED=1 \
	$(GO) build -i -ldflags="$(LDFLAGS)" -o $(DIST_ROOT)/server-linux ./cmd/server
endif

package: dist/server-linux build/server/Dockerfile
	docker build -t $(DOCKERNAME) -f build/server/Dockerfile .

deploy: package
	docker push $(DOCKERNAME)

clean:
	$(GO) clean ./...
	rm -rf $(DIST_ROOT)/

run: build
	@cp config.yaml $(DIST_ROOT)/
	cd $(DIST_ROOT) && ./server

race:
	$(GO) run -race ./cmd/server

lint:
	@echo • Checking gofmt
	@for package in $$(go list ./...); do \
		echo "  - Checking "$$package; \
		files=$$(go list -f '{{range .GoFiles}}{{$$.Dir}}/{{.}} {{end}}' $$package); \
		if [ "$$files" ]; then \
			gofmt_output=$$(gofmt -d -s $$files 2>&1); \
			if [ "$$gofmt_output" ]; then \
				echo -e "\033[1;33m" \
				echo "$$gofmt_output"; \
				echo -e "\033[31m  Gofmt failure ❌\033[0m";\
				exit 1; \
			fi; \
		fi; \
	done
	@echo -e "\033[92m  Gofmt success ✅\033[0m"
	@echo
	@echo • Checking golint
	@output=$$($(GOLINT) ./...); \
	if [ "$$output" ]; then \
		while IFS= read -r line; do \
			echo -e "  - \033[1;33m$$line\033[0m"; \
		done <<< "$$output" ; \
		echo -e "\033[31m  Golint failure ❌\033[0m"; \
		exit 1; \
	fi;
	@echo -e "\033[92m  Golint success ✅\033[0m"

fix-lint:
	$(GO) fmt ./...

ci-package: build/ci/Dockerfile
	docker build -t $(DOCKERREGISTRY)/server-ci -f build/ci/Dockerfile .

ci-deploy: ci-package
	docker push $(DOCKERREGISTRY)/server-ci


dist/dict/en_US.txt:
	mkdir -p dist/dict/
	curl https://log3900.blob.core.windows.net/polydraw/dict/en_US.txt -o $@

dist/dict/en_US_bad.txt:
	mkdir -p dist/dict/
	curl https://log3900.blob.core.windows.net/polydraw/badwords/en_US.txt -o $@

dist/dict/fr_QC.txt:
	mkdir -p dist/dict/
	curl https://log3900.blob.core.windows.net/polydraw/dict/fr_QC.txt -o $@

dist/dict/fr_QC_bad.txt:
	mkdir -p dist/dict/
	curl https://log3900.blob.core.windows.net/polydraw/badwords/fr_QC.txt -o $@