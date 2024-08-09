# Project Submissions Guidelines

#### Project structure
* Before we jump to packaging our application, make sure the source code follows a similar structure with `client` and `server` packages.
```bash
src
├── Dockerfile
├── Project\ 1.iml
├── Summary.txt
├── Readme.md
├── client
│   ├── StoreClient.java
├── deploy.sh
├── run_client.sh
└── server
│   ├── StoreServer.java
│   ├── KVStoreInt.java
│   ├── KVStore.java
│   ├── Acceptor.java
│   ├── Proposer.java
│   ├── Learner.java
│   ├── Message.java
    
```
* Compile the code using `javac server/*.java client/*.java`
* RMI server usage should then be similar to `java server.StoreServer <port-number>>`
* RMI client usage should then be similar to `java client.StoreClient <host-name> <port-number> 5`
* You may run multiple instances of the client and server at the same time in different terminal windows using the above command
* Stop the client using the following command `quit`.
* Docker has not been set up for this assignment.
* Check the logs in the src folder for a sample run of the application.
