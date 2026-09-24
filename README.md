# comp512 programming assignment 1

To run the RMI resource manager:

```
cd Server/
./run_server.sh [<rmi_name>] # starts a single ResourceManager
./run_servers.sh # convenience script for starting multiple resource managers
```

To run the RMI client:

```
cd Client
./run_client.sh [<server_hostname> [<server_rmi_name>]]
```

## Distributed RMI setup

Build both sides:

```sh
cd Server
make
cd ../Client
make
```

For a local run, start these commands in separate terminals:

```sh
cd Server
./run_server.sh Flights
./run_server.sh Cars
./run_server.sh Rooms
./run_middleware.sh localhost localhost localhost
```

Connect the unchanged client to the middleware:

```sh
cd Client
./run_client.sh localhost Middleware
```

For a distributed run, start each resource manager on its own machine and pass
their hostnames to `run_middleware.sh` in Flights, Cars, Rooms order. Pass the
middleware hostname to `run_client.sh`.

## Distributed TCP setup

Build the server and client as above. For a local run, use separate ports and
start these commands in separate terminals:

```sh
cd Server
./run_tcp_server.sh Flights 3101
./run_tcp_server.sh Cars 3102
./run_tcp_server.sh Rooms 3103
./run_tcp_middleware.sh 3100 localhost 3101 localhost 3102 localhost 3103
```

Connect the TCP client to the middleware:

```sh
cd Client
./run_tcp_client.sh localhost 3100
```

For a five-machine run, start one resource manager on each resource host, run
the middleware on a fourth host with the three resource host/port pairs, and
pass the middleware host and port to the client on the fifth host.
