#!/bin/bash

# Usage: ./run_middleware.sh <flight-host> <car-host> <room-host>

if [ "$#" -ne 3 ]; then
	echo "Usage: $0 <flight-host> <car-host> <room-host>" >&2
	exit 1
fi

./run_rmi.sh > /dev/null 2>&1
java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware "$1" "$2" "$3"
