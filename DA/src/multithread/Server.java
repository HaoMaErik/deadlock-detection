package multithread;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Erik
 */
public class Server {

    // an ArrayList to keep the list of the Client and Resource
    private static ArrayList<String> clients;
    private static ArrayList<ServerThread> threads;
    private static ArrayList<Resource> resources;
    private static HashMap<String, Integer> completeStatus;
    //private static ArrayList<RoomList> rooms;

    // to display time
    private static SimpleDateFormat sdf;

    // the port number to listen for connection
    private int port;
    private JSONArray j = new JSONArray();

    // the boolean that will be turned of to stop the server
    private boolean keepGoing;

    static private Graph g;

    private int noofclients;

    /*
     
     *  server constructor that receive the port to listen to for connection as parameter
     
     *  in console
     
     */
    public Server(int port) {

        this.port = port;

        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");

        g = new Graph();

        Resource R1 = new Resource(1, "wok", false, "");
        Resource R2 = new Resource(2, "chopsticks", false, "");
        Resource R3 = new Resource(3, "cookingshovel", false, "");
        Resource R4 = new Resource(4, "knife", false, "");
        Resource R5 = new Resource(5, "choppingboard", false, "");
        Resource R6 = new Resource(6, "pot", false, "");
        noofclients = 0;

        // ArrayList for the Client list
        clients = new ArrayList<String>();
        threads = new ArrayList<ServerThread>();
        resources = new ArrayList<Resource>();
        resources.add(R1);
        resources.add(R2);
        resources.add(R3);
        resources.add(R4);
        resources.add(R5);
        resources.add(R6);
        completeStatus = new HashMap<String, Integer>();
        //rooms = new ArrayList<RoomList>();

    }

    public void start() {

        keepGoing = true;

        /* create socket server and wait for connection requests */
        try {

            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (keepGoing) {

                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();      // accept connection

                // if I was asked to stop
                if (!keepGoing) {
                    break;
                }

                ServerThread t = new ServerThread(socket, ++noofclients);  // make a thread of it

                clients.add(String.valueOf(noofclients));  // save all clients to the ArrayList
                threads.add(t);
                t.start();

            }

            // I was asked to stop
            try {

                serverSocket.close();

                for (int i = 0; i < threads.size(); ++i) {

                    ServerThread tc = threads.get(i);

                    try {

                        tc.in.close();

                        tc.out.close();

                        tc.socket.close();

                    } catch (IOException ioE) {

                        // not much I can do
                    }

                }

            } catch (IOException e) {

                display("Exception closing the server and clients: " + e);

            }

        } // something went bad
        catch (IOException e) {

            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";

            display(msg);

        }

    }

    /*
     
     * For the GUI to stop the server
     
     */
    protected void stop() {

        keepGoing = false;

        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {

            new Socket("localhost", port);

        } catch (IOException e) {

            // nothing I can really do
        }

    }

    /*
     
     * Display an event (not a message) to the console or the GUI
     
     */
    private static void display(String msg) {

        String time = sdf.format(new Date()) + " " + msg;

        System.out.println(time);

    }

    private static String checkWinner(String clientId) {
        
        for (int i = 0; i < completeStatus.size(); i++) {
            if (completeStatus.get(clientId) == 3) {
                //code for broadcast winner
            	return clientId;
            }
        }
        return "false";
    }

    private static String receiveDish(String clientId) {
        completeStatus.put(clientId, (completeStatus.get(clientId) + 1));
        removeAllResources(clientId);
        String temp = checkWinner(clientId);
        return temp;
    }

    private static synchronized boolean grantResource(String resourceName, String clientID) {
        boolean grant = true;
        if (checkResource(resourceName) == false) {
            for (int j = 0; j < resources.size(); j++) {
                if (resourceName.equals(resources.get(j).getName())) {
//                    System.out.println(clientID);
//                    System.out.println(resources.get(j).getOwner());
                    Server.serverAddEdge(clientID, resources.get(j).getOwner());

                }
            }
            grant = false;
        } else {
            for (int i = 0; i < resources.size(); i++) {
                if (resourceName.equals(resources.get(i).getName())) {
                    resources.get(i).setOwner(clientID);
                    resources.get(i).setIsOwned(true);
                }
            }
        }

        return grant;
    }

    private static void removeAllResources(String clientId) {
        //       System.out.println("Resources released by "+clientId+ " are ");

        for (int i = 0; i < resources.size(); i++) {
            //            System.out.println("Resource "+resources.get(i).getName()+ " Owner: "+ resources.get(i).getOwner());
            if (resources.get(i).getOwner().equals(clientId)) {

                resources.get(i).setOwner("");
                resources.get(i).setIsOwned(false);

//                System.out.print(resources.get(i).getName()+" ");
            }
        }
  }

    private static boolean checkResource(String resourceName) {
        boolean check = true;
        for (int i = 0; i < resources.size(); i++) {
            if (resourceName.equals(resources.get(i).getName())) {
                if (resources.get(i).getIsOwned() == true) {

                    check = false;

                }
            }
        }
        return check;
    }


    /*
     
     *  to broadcast a message to all Clients
     
     */
    //have problems
    private static synchronized void broadcast(String message) throws IOException {

        // add HH:mm:ss and \n to the message
        

        String messageLf =  "\n"+message + "\n";

        // display message on console or GUI
        System.out.print(messageLf);
        JSONObject j = new JSONObject();
        j.put("type", "winner");
        //j.put("identity", id);
        j.put("content", messageLf);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = threads.size(); --i >= 0;) {

            ServerThread ct = threads.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(j)) {

                threads.remove(i);

                display("Disconnected Client " + ct.name + " removed from list.");

            }

        }

    }

    // for a client who logoff using the LOGOUT message
    synchronized static void remove(int id) {

        // scan the array list until we found the Id
        for (int i = 0; i < threads.size(); ++i) {

            ServerThread ct = threads.get(i);

            // found it
            if (ct.id == id) {

                threads.remove(i);

                return;

            }

        }
    }
        
        @SuppressWarnings("deprecation")
		synchronized static void removeAll() {

            // scan the array list until we found the Id
            for (int i = 0; i < threads.size(); ++i) {

                ServerThread ct = threads.get(i);
                ct.stop();
                
                }

            }

    

    synchronized static void serverAddVertex(String name) {
        Vertex v = new Vertex(name);
        g.addVertex(v);
        System.out.println("New Vertex for " + name + " is added!!!");
//        System.out.println(g.getVerticies());
    }

    synchronized static void serverAddEdge(String fromName, String toName) {
        Vertex v1 = g.findVertexByName(fromName);
        Vertex v2 = g.findVertexByName(toName);
        g.addEdge(v1, v2, 0);

        if (g.findCycles().length != 0) {
            System.out.println("WARNING!!!!!!!!!!!Deadlcok detected!!!!!!");
        }
        for (int i = 0; i < g.findCycles().length; i++) {
            System.out.println(g.findCycles()[i]);
        }

    }

    /*
     
     *  To run as a console application just open a console window and:
    
     * > java Server
    
     * > java Server portNumber
     
     * If the port number is not specified 4444 is used
     
     */
    public static void main(String[] args) {

        // start server on port 4444 unless a PortNumber is specified
        //int portNumber = 4444;
        CommandLineValues values = new CommandLineValues();
        CmdLineParser parser = new CmdLineParser(values);

        try {
            // parse the command line options with the args4j library
            parser.parseArgument(args);
            // print values of the command line options
//            System.out.println(values.getPort());

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(-1);
        }

        // create a server object and start it
        Server server = new Server(values.getPort());

        server.start();

    }

    /**
     * One instance of this thread will run for each client
     */
    public class ServerThread extends Thread {

        private Socket socket = null;
        public PrintWriter out;
        BufferedReader in;

        private String name;
        private int id;
        private ArrayList<String> resources_hold;
        private ArrayList<String> dishes;
        private String currentDish;
        private boolean win;

        private Vertex v;

        public ServerThread(Socket socket, int id) throws IOException {
            super("KKMultiServerThread");
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.id = id;

            this.v = new Vertex(this.id + "");

        }
//-------------------------------------methods-------------------------------------------

        private void reply_resource_grant(String resource) {
            JSONObject obj = new JSONObject();
            obj.put("type", "grant");
            obj.put("resource", resource);
            this.out.println(obj);
        }

        private boolean if_finish_currentDish() {
            boolean r = false;
            switch (currentDish) {
                case "pasta":
                case "steak":
                    if (resources_hold.size() == 2) {
                        r = true;
                    }
                case "fried rice":
                    if (resources_hold.size() == 4) {
                        r = true;
                    }
            }
            return r;
        }

        private boolean writeMsg(JSONObject j) throws IOException {

            // if Client is still connected send the message to it
            if (!socket.isConnected()) {

                this.socket.close();

                return false;

            }

            out.println(j.toJSONString());

            return true;

        }

//----------------------------------end of methods---------------------------------------
        public void run() {

//=========================================================================================================================================================
            String inputLine, outputLine;
//            KnockKnockProtocol kkp = new KnockKnockProtocol();
//            outputLine = kkp.processInput(null);

            JSONObject obj0 = new JSONObject();
            obj0.put("type", "connected");
            obj0.put("content", "You have connected to the server... ...\nFollowing is the rule:"
                    + "\n1.Enter the name of the Dish you choose;" + "\n2.Use 'get ...' command fist to get your resources;"
                    + "\n3.Use 'makedish ...' once you get all the needed resources;"
                    + "\n4.Use 'changedish ...' to change to another dish when you cant get the resources from your current dish.");
            out.println(obj0);
            while(threads.size()!=2){
            	System.out.println("waiting for another player");
            	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            obj0 = new JSONObject();
            obj0.put("type", "ready");
            out.println(obj0);
            
            try {
                while ((inputLine = in.readLine()) != null) {

                    JSONParser parser = new JSONParser();

                    try {
                        Object obj = parser.parse(inputLine);
                        JSONObject jsonObject = (JSONObject) obj;
                        String clientId = "";
                        String type = (String) jsonObject.get("type");
                        switch (type) {
                            case "choose_dish":
                                clientId = (String) jsonObject.get("clientId");
                                Server.removeAllResources(clientId);
                                break;
                            case "send_dish":
                                String dish = (String) jsonObject.get("dish");
                                clientId = (String) jsonObject.get("clientId");
                                String temp = Server.receiveDish(clientId);
                                JSONObject obj2 = new JSONObject();
                                obj2.put("type", "send_dish");
                                obj2.put("success", "true");
                                obj2.put("dish", dish);
                                if(!temp.equals("false")){
                                	broadcast(temp+" is the winner");
                                	removeAll();
                                }
                                out.println(obj2);
                               
                                break;
                            case "request_for_resource":
                                String resource = (String) jsonObject.get("resource");
                                System.out.println(jsonObject);
                                clientId = (String) jsonObject.get("clientId");
                                
                                /*
                            check if the resource is availble:
                            Availabe: return message successful, choose another
                            Not available: return message to wait
                                 */
                                boolean check = Server.grantResource(resource, clientId);

                                if (check == true) {
                                    JSONObject obj3 = new JSONObject();
                                    
                                    obj3.put("type", "request_for_resource");
                                    obj3.put("success", "true");
                                    obj3.put("resource", resource);
                                    out.println(obj3);
                                    
                                 } 
                                else {
                                    JSONObject obj3 = new JSONObject();
                                    obj3.put("type", "request_for_resource");
                                    obj3.put("success", "false");
                                    obj3.put("resource", resource);
                                    out.println(obj3);
                                }
                                break;

                            case "name":
                                String name = (String) jsonObject.get("content");
                                String id = (String) jsonObject.get("id");
                                completeStatus.put(id, 0);
                                System.out.println(completeStatus);
                                Server.serverAddVertex(name);
                                break;
                        }

                    } catch (ParseException pe) {
                    }
                }
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }

//==========================================================================================================================================================
        }

    }

    public static class CommandLineValues {

        // Give it a default value of 4444 sec
        @Option(name = "-p", aliases = {"port"}, usage = "Port Address")
        private int port = 4444;

        public int getPort() {
            return port;
        }

    }

}
