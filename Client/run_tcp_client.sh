#!/bin/bash

# Usage: ./run_tcp_client.sh [middleware-host [middleware-port]]

java -cp ../Server/RMIInterface.jar:. Client.TCPClient "$@"
