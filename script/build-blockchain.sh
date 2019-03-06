#!/usr/bin/env bash
set -e
set -o pipefail

cd `dirname $0`/../blockchain


function prefix {
  exec "${@:2}" 2>&1 | sed "s/^/[$(tput setaf 3 || true)$1$(tput sgr0 || true)] /"
}

prefix "DAE" go build -o bin/scynetd cmd/scynetd/main.go
prefix "CLI" go build -o bin/scynetcli cmd/scynetcli/main.go
