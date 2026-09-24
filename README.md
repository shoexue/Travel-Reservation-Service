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
