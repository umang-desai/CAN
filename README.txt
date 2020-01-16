P2P Content Addressable Network. Author: Umang Desai
Built a scalable distributed hash table network using JAVA, RMI. The idea is to help manage a large hash table over internet like scalability. This can also support distributed data storage.

• Implemented CAN, a structured and scalable P2P system in two-dimensional coordinate space zone using JAVA RMI
• Provided file insert/retrieve, viewing peer information, routing mechanism and node join/leave protocol


Instructions to use can network.

Compile:
javac *.java

RUN Bootstrap Node:
java BootstrapNode

JOIN network:
java Peer GenesisNode 3000

INSERT keyword into network:
insert umang 1234

SEARCH keyword in network:
search umang

LEAVE network:
leave

VIEW peer:
view peername

VIEW all peers:
view all
