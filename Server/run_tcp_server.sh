#!/bin/bash

# Usage: ./run_tcp_server.sh <Flights|Cars|Rooms> <port>

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 <Flights|Cars|Rooms> <port>" >&2
	exit 1
fi

java Server.TCP.TCPResourceManagerServer "$1" "$2"
