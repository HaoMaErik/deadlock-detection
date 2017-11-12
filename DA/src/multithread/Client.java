package multithread;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Administrator
 */
public class Client {

    //all dishes
    private ArrayList<String> dishes = new ArrayList<String>();
    private Hashtable<String, String[]> ht = new Hashtable<String, String[]>();

    static String[] resourceForPasta = {"pot", "chopsticks"};
    static String[] resourceForSteak = {"wok", "cookingshovel"};
    static String[] resourceForRice = {"wok", "chopsticks", "knife", "choppingboard"};

    private ArrayList<String> resources_hold = new ArrayList<String>();
    String currentDish = "";
    static String id = "";
    private String server;
    private int port;

    public Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String hostName, int portNumber, String id) throws IOException {
        ht.put("pasta", resourceForPasta);
        ht.put("steak", resourceForSteak);
        ht.put("friedrice", resourceForRice);
        this.id = id;
        this.server = hostName;
        this.port = portNumber;
        dishes.add("pasta");
        dishes.add("steak");
        dishes.add("friedrice");

        ArrayList<String> ar = new ArrayList<String>();
        for (String key : ht.keySet()) {
            ar.add(key);
        }
        Enumeration e = ht.elements();
        System.out.println("Dishes are: " + ht.keySet());
        int count = 0;
        while (e.hasMoreElements()) {
            System.out.println("The " + ar.get(count) + " dish needs: " + Arrays.toString((String[]) e.nextElement()));
            count++;
        }
    }

    public void start() throws IOException {
        try {
            socket = new Socket(this.server, this.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + this.server);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to "
                    + this.port);
            System.exit(1);
        }
    }

    public PrintWriter getPrintWriter() {
        return this.out;
    }

    public BufferedReader getBufferedReader() {
        return this.in;
    }

//---------------------------------------methods-------------------------------------------------
    private boolean checkForDish(String dish) {
        boolean r = false;
        for (String dishe : dishes) {
            if (dishe.equals(dish)) {
                r = true;
            }
        }
        return r;
    }

    private boolean checkForResource(String resource) {
        String currentDish = getCurrentDish();
        String[] resources = ht.get(currentDish);
        return Arrays.asList(resources).contains(resource);
    }

    private String getCurrentDish() {
        String r = currentDish;
        return r;
    }

    private void changeCurrentDish(String newDish) {
        JSONObject send = new JSONObject();
        send.put("type", "choose_dish");
        send.put("clientId", id);

        this.out.println(send);
    }

    public void chooseDish(String dish) {
        currentDish = dish;
    }

    public void sendResource(String resource) {
        JSONObject send = new JSONObject();
        send.put("type", "request_for_resource");
        send.put("resource", resource);
        send.put("clientId", id);

        this.out.println(send);
    }

    public void sendDish(String dish) {

        JSONObject send = new JSONObject();
        send.put("type", "send_dish");
        send.put("dish", dish);
        send.put("clientId", id);
        this.out.println(send);
    }

    public boolean checkDish() {
        return resources_hold.containsAll(Arrays.asList(ht.get(currentDish)));
    }

    public String checkInput(String s, String s1) {
        switch (s) {
            case "get":
                if (checkForResource(s1)) {
                    //send the resource request
                    sendResource(s1);
                } else {
                    System.out.println("The resource you selcted is either not for this dish or resource is not present");
                    return "error";
                }
                break;
            case "makedish":
            	if(!currentDish.equals("unknown")){
	                if (checkDish()) {
	                    sendDish(s1);
	                    currentDish="unknown";
	                    
	                } else {
	                    System.out.println("You dont have all the resources needed");
	                    return "error";
	                }
            	}
            	else{
            		System.out.println("You have not selected a dish");
                    return "error";
            	}
                break;
            case "changedish":
                if (checkForDish(s1)) {
                    if (currentDish.equals("")) {
                        chooseDish(s1);
                        System.out.println("You are making " + s1);
                        
                    } else {
                        chooseDish(s1);
                        changeCurrentDish(s1);
                        System.out.println("You are making " + s1);
                        return "error";
                    }
                } else {
                    System.out.println("Wrong dish name, please try it again.");
                    System.out.println("Enter dish:");
                    return "error";
                }
                break;
            default:
            	System.out.println("Wrong command");
            	return "error";
        }
        return "true";
    }

//--------------------------------------end of methods-------------------------------------------
    //----------------------------------------start of main funcion------------------------------------------------------------------------------------------   
    public static void main(String[] args) throws IOException {
        String result = "";
        String hostName;
        int portNumber;
        String id = "";
        //System.out.println(args.length);
        if (args.length == 1) {
            hostName = "localhost";
            portNumber = 4444;
            id = args[0];
        } else if (args.length != 2) {
            hostName = "localhost";
            portNumber = 4444;
            id = "Erik";
        } else {
            hostName = args[0];
            portNumber = Integer.parseInt(args[1]);
        }

        Client c = new Client(hostName, portNumber, id);
        c.start();

        BufferedReader in = c.getBufferedReader();
        PrintWriter out = c.getPrintWriter();

        String fromServer;
        String fromUser;

        BufferedReader stdIn
        = new BufferedReader(new InputStreamReader(System.in));
        JSONObject objectForName = new JSONObject();

        objectForName.put("type", "name");
        objectForName.put("content", id);
        objectForName.put("id", id);
        

        out.println(objectForName);
//=====================================================================================================================================================     
        while ((fromServer = in.readLine()) != null) {
        	JSONParser parser = new JSONParser();

            try {
                Object obj = parser.parse(fromServer);
                JSONObject jsonObject = (JSONObject) obj;

                String type = (String) jsonObject.get("type");

                switch (type) {
//------------------------------------------after the client just connected to the server----------------------------------------------                    
                    case "connected":
                        String content = (String) jsonObject.get("content");
                        System.out.println(content);
                        System.out.println("Waiting for other player");
                        break;
                        
                    case "ready":
                    	System.out.println("Both players connected");
                    	System.out.println("Please choose the first dish:");
                    	
                        fromUser = stdIn.readLine();

                        String dish = fromUser.toLowerCase();
                        while (c.checkInput("changedish", dish).equals("error")) {
                            fromUser = stdIn.readLine();

                            dish = fromUser.toLowerCase();

                        }

                        System.out.println("Please choose the resources for " + dish);

                        do {
                            System.out.println("Enter command:");

                            fromUser = stdIn.readLine();
                            String command = fromUser.toLowerCase();

                            String[] split = command.split(" ");
                            while (split.length != 2 || split[0].equals("makedish")) {
                                if (split[0].equals("makedish")) {
                                    break;
                                }
                                System.out.println("Wrong command. Enter again:");
                                fromUser = stdIn.readLine();
                                command = fromUser.toLowerCase();
                                split = command.split(" ");
                            }
                            if (!split[0].equals("makedish")) {
                                result = c.checkInput(split[0], split[1]);
                            } else {
                                result = c.checkInput(command, c.currentDish);
                            }
                        } while (result.equals("error"));
                        break;

//-----------------------------------------when the server send the client grant message----------------------------------------------------------------
                    case "grant":
                        String granted_resource = (String) jsonObject.get("resource");
                        System.out.println("Resource granted: " + granted_resource);

                        break;
                    case "winner":
                    	String message = (String) jsonObject.get("content");
                    	System.out.println(message);
                    	break;
//----------------------------------------------------------------------------------------------
                    case "request_for_resource":
                        String success = (String) jsonObject.get("success");
                        String resource = (String) jsonObject.get("resource");
                        if (success.equals("true")) {
                            c.resources_hold.add(resource);
                            System.out.println("The requested resource was allocated to you.");
                        } else if (success.equals("false")) {
                            System.out.println("The requested resource is held by someone else.");
                        }
                        result = "";

                        do {
                            System.out.println("Enter command:");

                            fromUser = stdIn.readLine();
                            String command = fromUser.toLowerCase();

                            String[] split = command.split(" ");
                            while (split.length != 2 || split[0].equals("makedish")) {
                                if (split[0].equals("makedish")) {
                                    break;
                                }
                                System.out.println("Wrong command. Enter again:");
                                fromUser = stdIn.readLine();
                                command = fromUser.toLowerCase();
                                split = command.split(" ");
                            }
                            if (!command.equals("makedish")) {
                                result = c.checkInput(split[0], split[1]);
                            } else {
                                result = c.checkInput(command, c.currentDish);
                            }
                        } while (result.equals("error"));

                        break;
                    case "send_dish":

                        success = (String) jsonObject.get("success");
                        String dish1 = (String) jsonObject.get("dish");
                        if (success.equals("true")) {

                            System.out.println(dish1 + " has been cooked");
                            c.dishes.remove(dish1);
                        } else if (success.equals("false")) {
                            System.out.println("The requested resource is held by someone else.");
                        }
                        result = "";

                        do {
                        	
                            System.out.println("Enter command:");
                            fromUser = stdIn.readLine();
                            String command = fromUser.toLowerCase();

                            String[] split = command.split(" ");
                            while (split.length != 2 || split[0].equals("makedish")) {
                                if (split[0].equals("makedish")) {
                                    break;
                                }
                                System.out.println("Wrong command. Enter again:");
                                fromUser = stdIn.readLine();
                                command = fromUser.toLowerCase();
                                split = command.split(" ");
                            }
                            if (!command.equals("makedish")) {
                                result = c.checkInput(split[0], split[1]);
                            } else {
                                result = c.checkInput(command, c.currentDish);
                            }
                        } while (result.equals("error"));

                        break;

//-----------------------------------------when the server sends the resource not available message-----------------------------------------------------
                }

            } catch (ParseException pe) {
            }

        }
        

        //========================================================================================================================================================           
    }
}
