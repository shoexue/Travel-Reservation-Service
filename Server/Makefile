all: compile-server-rmi

RMIInterface.jar: Server/Interface/*.java Server/TCP/TCPResourceManagerProxy.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/*.java Server/TCP/TCPResourceManagerProxy.java
	jar cvf RMIInterface.jar Server/Interface/*.class Server/TCP/TCPResourceManagerProxy.class

compile-server-rmi: Server/RMI/*.java Server/TCP/*.java Server/Interface/*.java Server/Common/*.java
	javac Server/RMI/*.java Server/TCP/*.java Server/Interface/*.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class
	rm -f Server/TCP/*.class
	rm -f RMIInterface.jar
