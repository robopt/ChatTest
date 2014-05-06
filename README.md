## ChatTest

Simple java application to show the use of ServerSockets and Threads.

## Usage
Move Server.java to a box which will be the host. Compile `javac Server.java` and run with a specified port `java Server 8080` will open a server on 8080. Move Client.java to any machines you wish to chat on, compile `javac Client.java` and run with required arguments. Required arguments are host and port, e.g. `java Client localhost 8080` will attempt to connect to localhost on port 8080.

## Changelog

#### May 5, 2014
- (robopt): initial commit
- (robopt): logout messages added
- (robopt): change nickname added
- (robopt): /quit command added
- (robopt): added documentation


## Todo
- Login broadcast message
- Echo port on login incase they pick port 0
