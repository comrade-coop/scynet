#!/usr/bin/env bash
set -e
set -o pipefail

cd `dirname $0`/..


function prefix {
  exec "${@:2}" 2>&1 | sed "s/^/[$(tput setaf 3 || true)$1$(tput sgr0 || true)] /"
}

prefix "BUILD" dotnet build ScynetLocalSilo.sln
prefix "LSILO" dotnet run --project src/Scynet.LocalSilo &
prefix "HATCH" dotnet run --project src/Scynet.HatcheryFacade &
trap 'kill -n 2 `jobs -p`;' INT
wait
kill -n 2 `jobs -p` > /dev/null
