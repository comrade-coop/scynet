#!/usr/bin/env bash
set -o errexit -o pipefail -o noclobber -o nounset

logfile=run-dev.log

# Via https://stackoverflow.com/a/14203146/4168713 (M2)
opts=$(getopt -l 'telepresence' -o 't' -n "$0" -- "$@")
telepresence_enabled=no
eval set -- "$opts"
while true; do
  case "$1" in
    -t|--telepresence)
      telepresence_enabled=yes
      shift
      ;;
    --)
      shift
      break
      ;;
    *)
      echo "[ BUG] Failed to parse arguments"
      exit 3
      ;;
  esac
done


function main {
  set_process "starting development environment"; echo
  needed="minikube kubectl sbt docker "
  if [ $telepresence_enabled != no ]; then
    needed+="telepresence "
  fi
  ensure_intalled $needed
  start_cluster
  start_kafka
  start_parity
  start_schema_registry
  build_harvester
  start_harvester
  echo_ok "finished starting the development environment"
  if [ $telepresence_enabled != no ]; then
    run_telepresence
  fi
}

function ensure_intalled {
  set_process "checking dependencies"; echo
  should_exit=''
  for i in $@; do
    if command -v $i >/dev/null 2>&1; then
      echo_ok "$i found"
    else
      echo_fail "$i is needed to run this script, but not installed"
      should_exit=1
    fi
  done
  if [ $should_exit ]; then
    exit 1
  fi
}

function start_cluster {
  set_process "starting cluster"
  if minikube status >/dev/null; then
    echo_ok "cluster already running"
  else
    run minikube start --kubernetes-version=v1.11.0 --memory 3072 --disk-size 40g
    echo_ok "started cluster"
  fi
}

function start_kafka {
  config_folder="`dirname $0`/kubernetes-kafka"
  set_process "configuring kafka"; echo
  set_process "configuring storage for kafka"
  run kubectl apply -f $config_folder/configure/minikube-storageclass-broker.yml
  run kubectl apply -f $config_folder/configure/minikube-storageclass-zookeeper.yml
  echo_ok "configured storage"
  set_process "configuring namespaces for kafka"
  run kubectl apply -f $config_folder/00-namespace.yml
  run kubectl apply -f $config_folder/rbac-namespace-default/
  echo_ok "configured namespaces"
  set_process "configuring the rest of kafka"
  run kubectl apply -f $config_folder/zookeeper/
  run kubectl apply -f $config_folder/kafka/
  echo_ok "configured kafka"
}

function start_parity {
  config_folder="`dirname $0`/parity"
  set_process "configuring parity"; echo
  set_process "configuring storage for parity"
  run kubectl apply -f $config_folder/configure/minikube-storageclass.yml
  echo_ok "configured storage"
  set_process "configuring the rest of parity"
  run kubectl apply -f $config_folder/
  echo_ok "configured parity"
}

function start_schema_registry {
  config_folder="`dirname $0`/schema-registry"
  set_process "configuring schema registry"; echo
  run kubectl apply -f $config_folder/
  echo_ok "configured schema registry"
}

function build_harvester {
  set_process "building harvester components"; echo
  run eval `minikube docker-env`
  set_process "building kafka-producer-blockchain"
  (run cd ../harvester/kafka-producer/kafka-producer-blockchain; run sbt docker)
  echo_ok "built kafka-producer-blockchain"
}

function start_harvester {
  config_folder="`dirname $0`/harvester"
  set_process "configuring harvester"; echo
  set_process "creating harvester"
  run kubectl apply -f $config_folder/
  echo_ok "configured harvester"
}


function run_telepresence {
  set_process "starting telepresence"; echo
  user_shell=$(ps -p $(ps -p $$ -oppid=) -ocommand=)

  set_process "creating telepresence namespace"
  echo '{"apiVersion": "v1", "kind": "Namespace", "metadata": {"name": "telepresence"}}' | kubectl apply -f -
  echo_ok "created telepresence namespace"

  set_process "running telepresence (with the $user_shell shell)"
  echo -n -e "\e[2K"
  echo "Will now run your current shell inside telepresence."
  echo "You may be prompted for sudo access, as it needs it to set things up."
  echo "You can stop it at any time by ^D (Ctrl-D) or typing exit"
  run_noredirect telepresence --namespace telepresence --also-proxy=172.17.0.0/16 --run $user_shell
  echo_ok "Telepresence session finished"
}

function run {
  echo "[ RUN] $@" >> run-dev.log
  "$@" >> run-dev.log 2>> run-dev.log
}

function run_noredirect {
  echo "[ RUN] $@" >> run-dev.log
  echo "(output not redirected)" >> run-dev.log
  "$@"
}

current_process='working'
function print_process {
  echo -n -e "\e[2K${current_process^}...\e[0m\r"
  # echo -n -e "\e[2K[\e[0;30m....\e[0m] ${current_process^}\r"
}

function set_process {
  current_process="$@"
  echo "[PART] ${current_process^}..." >> run-dev.log
  print_process
}

function handle_exit {
  echo_fail "An error occured while $current_process"
  echo -e "Here is a snip of the last 10 lines of \e[0;31m$logfile\e[0m:"
  head -n-1 $logfile | tail -n10
}

function echo_ok {
  echo "[ OK ] ${@^}!" >> run-dev.log
  echo -e "\e[2K[\e[0;32m OK \e[0m] ${@^}!"
  print_process
}

function echo_fail {
  echo "[FAIL] ${@^}!" >> run-dev.log
  echo -e "\e[2K[\e[0;31mFAIL\e[0m] ${@^}!"
  print_process
}

trap handle_exit EXIT
rm -f $logfile
main
trap - EXIT
