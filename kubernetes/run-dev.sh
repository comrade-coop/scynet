#!/usr/bin/env bash
set -o errexit -o pipefail -o noclobber -o nounset

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
      echo_fail "Bug"
      exit 3
      ;;
  esac
done


function main {
  set_process "starting development environment"; echo
  needed="minikube kubectl "
  if [ $telepresence_enabled != no ]; then
    needed+="telepresence "
  fi
  ensure_intalled $needed
  start_cluster
  start_kafka
  start_parity
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
  set_process "starting cluster"; echo
  if minikube status >/dev/null; then
    echo_ok "cluster already running"
  else
    minikube start --kubernetes-version=v1.11.0
    echo_ok "cluster started"
  fi
}

function start_kafka {
  set_process "starting kafka"; echo
  if kubectl get statefulset kafka --namespace=kafka >/dev/null 2>/dev/null; then
    echo_ok "kafka already configured"
  else
    config_folder="`dirname $0`/kubernetes-kafka"
    set_process "creating storages"
    kubectl apply -f $config_folder/configure/minikube-storageclass-broker.yml
    kubectl apply -f $config_folder/configure/minikube-storageclass-zookeeper.yml
    echo_ok "created storage configs"
    set_process "creating namespaces"
    kubectl apply -f $config_folder/00-namespace.yml
    kubectl apply -f $config_folder/rbac-namespace-default/
    echo_ok "created namespaces"
    set_process "creating app definitions"
    kubectl apply -f $config_folder/zookeeper/
    kubectl apply -f $config_folder/kafka/
    echo_ok "kafka started"
  fi
}

function start_parity {
  set_process "starting parity"; echo
  if kubectl get statefulset parity --namespace=parity >/dev/null 2>/dev/null; then
    echo_ok "parity already configured"
  else
    config_folder="`dirname $0`/parity"
    set_process "creating storages"
    kubectl apply -f $config_folder/configure/minikube-storageclass.yml
    echo_ok "created storage configs"
    set_process "creating everything else"
    kubectl apply -f $config_folder/
    echo_ok "parity started"
  fi
}

function run_telepresence {
  set_process "starting telepresence"; echo
  user_shell=$(ps -p $(ps -p $$ -oppid=) -ocommand=)
  echo -n -e "\e[2K"
  echo "Will now run your current shell inside telepresence."
  echo "You may be prompted for sudo access, as it needs it to set things up."
  echo "You can stop it at any time by ^D (Ctrl-D) or typing exit"
  telepresence --run $user_shell
  echo_ok "Telepresence session finished"
}

current_process='working'
function print_process {
  echo -n -e "\e[2K${current_process^}...\r"
}

function set_process {
  current_process="$@"
  print_process
}

function handle_exit {
  echo
  echo "An error occured while $current_process!"
  echo "More details might be available above."
}

function echo_ok {
  echo -e "\e[2K[\e[0;32m OK \e[0m] ${@^}!"
  print_process
}
function echo_fail {
  echo -e "\e[2K[\e[0;31mFAIL\e[0m] ${@^}!"
  print_process
}

trap handle_exit EXIT
main
trap - EXIT
