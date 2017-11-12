# deadlock detection
A group project with Naman and Yifeng.

This program is a Cooking Game, that is used to detect a deadlock.
We are using a Wait for Graph algorithm to detect if a deadlock exists.

The Game:

Objective: Complete making three dishes to complete the game.

Running the program: open the project and click run server first then client, here we need to run client file twice to create 2 players.

Procedure:
To complete a dish you need to do the following:
1. Choose the dish. Command - changedish <name> .For the initialization user is asked for the dish
2. Get resources for the dish. Command - get <resourcename>
3. If resource unavailable, change the dish, step 1, but lose all your resources.
4. Make the dish, once you have all the resources. Command - makedish
5. Choose next dish, step 1

Once you have completed all tasks the game will show the Winner.

For Distributed algorithms, we are creating a node for every client that is created and whenever a requested resource is not allocated to the user we create an edge to represent that it is waiting for that resource, for example if process A is waiting for process B to release a resource 1. We create an Edge from node A to node B with name 1.

When within the Graph, a cycle is created, such that multiple processes are waiting for one another and cannot proceed furthur, a deadlock is detected in the server.
To solve this we can use changedish <dish> command by a client to give up all his resources.

The code for drawing graph with nodes and edges are referenced from the link below with little changes.
http://www.java2s.com/Code/Java/Collections-Data-Structure/Adirectedgraphdatastructure.htm
