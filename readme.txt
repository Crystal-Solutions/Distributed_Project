# Distributed_Project
Develop a simple overlay-based solution that allows a set of nodes to share contents (e.g., music files) among each other. Consider a set of nodes connected via some overlay topology. Each of the nodes has a set of files that it is willing to share with other nodes. A node in the system (X) that is looking for a particular file issues a query to identify a node (Y) containing that particular file. Once the node is identified, the file can be exchanged between X and Y.

After completing this project, you will have developed a solution to search for contents in a distributed system. You will be able to:

design, develop, and debug overlay-based applications such as a simple search engine to find contents in a distributed system
use RPCs or web services to develop distributed systems
measure an analyze the performance of a distributed system


# Run Phase 2
The project is configured to run in windows environment.  
Start the Bootstrap Server seperately.  
Change the `start.bat` file according to the bootstrap server ip and port. 
Each line in the `start.bat` file is in following format. Each line is to start a node. Default file has 4 lines which will start 4 nodes.
	`start cmd /k "java -cp out\production\ClientProject\ Main <IP_BS_SERVER> <PORT_BS_SERVER> <IP_OF_THE_NODE> <RECIEVE_IP_OF_THE_NODE> <SEND_IP_OF_THE_NODE> <USERNAME>"

Current format is prepared to run if you are running a BS server on port 55555 on localhost.
* Important
	RECIEVE_PORT_OF_THE_NODE, SEND_PORT_OF_THE_NODE should be unique to each node.
	
Then run `start.bat` file
Which will start n number of terminals. (n is the number of nodes)
You can enter the query from any terminal and it will print responses from each node
To start start nodes on different servers you can just move the project there and modify the start.bat file accordingly.


# Run Phase 3
Start with 10 nodes in the localhost; Just double click the start_10.bat. It will start 10 terminals.
In these terminals you can enter serch queries.
Further there is a web UI configured to work with all nodes. To see that, in your browser navigate to http://localhost:9080.
There will be a web UI. In that therw will be a text box callse "Number of Nodes". Enter 10 and press 'Generate' button.
It will automatically connect to local nodes and display there states. Further you can enter serch queries from the we UI.

#Team
130594B - B.J.C.Thilakarathna 
130147J - S.W.Ediriweera 
130197K - N.R.Hasantha 
130385K - M.P.M.Munasinghe 
