#!/bin/bash

# Usage: ./run_tcp_middleware.sh <listen-port> <flight-host> <flight-port> <car-host> <car-port> <room-host> <room-port>

if [ "$#" -ne 7 ]; then
	echo "Usage: $0 <listen-port> <flight-host> <flight-port> <car-host> <car-port> <room-host> <room-port>" >&2
	exit 1
fi

java Server.TCP.TCPMiddlewareServer "$@"
