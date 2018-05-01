/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project;

import java.io.*;
import java.net.*;
import java.util.*;
import sysc3303Project.projectfiles.*;

public class Client {
    
    private static int DEFAULT_PORT = PortConfiguration.SERVER_PORT;
    int serverRequestPort;
    private boolean mode = false;
    private boolean vB;
    private static String defaultDir = System.getProperty("user.dir") + "/clientFiles/";;
    InetAddress serverAddress;
    public boolean serverGiven = false;
    
    
    public Client() {
        this.vB = true;
    }
    
    
    public boolean getVB(){
        return vB;
        
    }
    
    public boolean getMode() {
        return mode;
        
    }
    
    public void setServer(InetAddress serverAddress, int serverRequestPort) {
        this.serverAddress = serverAddress;
        this.serverRequestPort = serverRequestPort;
        
    }
    
    public void setServerRequestPort(int serverRequestPort) {
        this.serverRequestPort = serverRequestPort;
        
    }
    
    public void switchVB() {
        this.vB = !this.vB;
    }
    
    public void switchMode() {
        this.mode = !this.mode;
    }
    
    public String getConnectionString() {
        return "Currently connected to: " + addressToString(serverAddress, serverRequestPort);
    }
    
    public String getPublicFolder() {
        return defaultDir;
    }
    
    public void stop() {
        System.out.println("Client is shutting down!");
    }
    
    public Connections getConnection() throws AbortException {
        try {
            Connections conn = new Connections();
            
            if (serverAddress == null) {
                throw new AbortException("Server address not specified");
            }
            
            conn.setRemoteAddress(serverAddress);
            
            conn.setRequestPort(serverRequestPort);
            return conn;
            
            
        } catch (SocketException e) {
            String errMsg = "Failed to connect to " + serverAddress.toString()
                + ":" + serverRequestPort;
            System.out.println(errMsg);
            throw new AbortException(errMsg);
            
        }
    }
    
    static private String addressToString(InetAddress addr, int port) {
        if (addr == null) {
            return "not connected";
        }
        
        return addr.toString() + ":" + port;
    }
    
    public void sendFileToTheServer( String fileName) {
        
        try {
            String filePath = getPublicFolder() + fileName;
            
            // Check that file exists
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("Cannot find file: " + fileName);
                return;
            }
            
            // Check read permissions
            if (!file.canRead()) {
                System.out.println("Cannot read file: " + fileName);
                return;
            }
            
            
            FileInputStream fs = new FileInputStream(file);
            
            // Send request
            Connections conn = getConnection();
            conn.setVb(this.vB);
            
            ReadWriteRequestPacket reqPacket = Packet.createWriteRequestPacket(fileName,
                                                                               ReadWriteRequestPacket.Modes.OCTET);
            
            
            conn.sendReadWriteRequest(reqPacket);
            
            
            
            int blockNumber = 0;
            
            byte[] data = new byte[512];
            int bytesRead = 0;
            
            do {
                
                conn.receiveAcknowledgement(blockNumber); blockNumber++;
                
                bytesRead = fs.read(data);
                
                // Special case when file size is multiple of 512 bytes
                if (bytesRead == -1) {
                    bytesRead = 0;
                    data = new byte[0];
                }
                
                conn.sendData(blockNumber, data, bytesRead);
            } while (bytesRead == PacketDATA.MAXFILELENGTH);
            
            conn.receiveAcknowledgement(blockNumber);
            
            InputOutputStreams.print("Successfully sent file \'" + fileName + "\' to server");
            fs.close();
            
        } catch (AbortException e) {
            InputOutputStreams.print("Failed to send " + fileName + ": " + "\""+ e.getMessage() + "\"");
            
        } catch (IOException e) {
            
            
            InputOutputStreams.print("IOException: failed to send " + fileName + ": "+ "\"" + e.getMessage() + "\"");
            
        }
    }
    
    public void receiveFileFromTheServer(String fileName) {
        String filePath = getPublicFolder() + fileName;
        try {
            
            
            
            File file = new File(filePath);
            if (file.exists() && !file.canWrite()) {
                System.out.println("Cannot overwrite file: " + fileName);
                return;
            }
            
            Connections conn = getConnection();
            conn.setVb(this.vB);
            
            FileOutputStream fs = new FileOutputStream(filePath);
            
            ReadWriteRequestPacket reqPacket = Packet.creatingReadRequestPacket(fileName,
                                                                                ReadWriteRequestPacket.Modes.OCTET);
            
            conn.sendReadWriteRequest(reqPacket);
            
            PacketDATA pk;
            
            int blockNumber = 1;
            
            do {
                
                pk = conn.receiveData(blockNumber);
                
                try {
                    
                    fs.write(pk.gettingFileData());
                    fs.getFD().sync();
                } catch (SyncFailedException e) {
                    file.delete();
                    fs.close();
                    conn.sendVoidDiscFull("Failed to sync with disc, likely is full");
                    return;
                }
                conn.sendAcknowledgement(blockNumber);
                blockNumber++;
                
            } while (!pk.lastDataPacket());
            
            
            InputOutputStreams.print("Done receiving file \'" + fileName + "\' from server");
            fs.close();
            
        } catch (AbortException e) {
            new File(filePath).delete();
            InputOutputStreams.print("Failed to get " + fileName + ": " + "\""
                                         + e.getMessage() + "\"");
            
        } catch (IOException e) {
            new File(filePath).delete();
            InputOutputStreams.print("IOException: failed to get " + fileName + ": " + "\"" + e.getMessage() + "\"");
            
        }
    }
    
    private static void changeDirectory(String f) {
        if (f.toCharArray()[f.length()-1] != '/')
        {
            f = f + '/';
            
        }
        File folder = new File (f);
        
        if (folder.isDirectory())
        {
            Client.defaultDir = f;
        } else {
            InputOutputStreams.print(f + " is not a directory.");
        }
    }
    
    public static void listOfFiles() {
        java.io.File[] files = new java.io.File(defaultDir).listFiles();
        if (files != null) {
            for (java.io.File f : files) InputOutputStreams.print(f.getName());
            InputOutputStreams.print(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
        } else InputOutputStreams.print("No files found");
    }
    
    
    public static void printHelpOptions() {
        InputOutputStreams.print("---------------CURRENT HELP MENU----------------");        
        InputOutputStreams.print("help : show the help menu");
        InputOutputStreams.print("mode : toggle between normal and testing");
        InputOutputStreams.print("vB : toggle vB mode off or on");
        InputOutputStreams.print("write FILENAME: send the file to the server from client");
        InputOutputStreams.print("read FILENAME: get the file from the server");
        InputOutputStreams.print("stop: stop the client");
        InputOutputStreams.print("ls: list all files in the working directory");
        InputOutputStreams.print("cd <DIRECTORY>: change the working directory"); 
        InputOutputStreams.print("rm <FILENAME>: delete a given file in the directory");
        InputOutputStreams.print("show CONNECTION: show the current connection information: IP address & port number");
        InputOutputStreams.print("connect IP|hostname: set the server IP or hostname (eg. connect 192.168.1.8)"); 
        InputOutputStreams.print("connect IP|hostname:portnumber:> set the server IP or hostname and the port number!!"); 
        InputOutputStreams.print("<---------------------------------------------->");
        
        
    }
    
    public static void main(String args[]) {
        Client c = new Client();
        Scanner scanner = new Scanner(System.in);
        
        try {
            c.setServer(InetAddress.getLocalHost(), DEFAULT_PORT );
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for(;;) {
            
            InputOutputStreams.print("Current working directory " + defaultDir);
            
            if (!c.getMode()) {
                InputOutputStreams.print("You are on Normal mode: Client <------------> Server ");
                
            } else {
                InputOutputStreams.print("You are on Test mode: Client ----> SIMulator <---- Server ");
                
            }
            
            if (c.getVB()) {
                InputOutputStreams.print("Verbose is  ON.");
                
            } else {
                InputOutputStreams.print("Verbose is  OFF.");
                
            }
            
            if(!c.serverGiven){
                
                System.out.print("Enter Server IP address: ");
                String serverIP = scanner.nextLine();
                
                try {
                    c.serverAddress = InetAddress.getByName(serverIP);
                    
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                    
                }
                
                c.serverGiven = true;
            }
            
            System.out.print("Client: ");
            
            
            String cmdLine = scanner.nextLine().toLowerCase();
            
            String[] command = cmdLine.split("\\s+"); //This groups all white spaces as a delimiter.
            
            
            // Continue if blank line was passed
            if (command.length == 0 || command[0].length() == 0) {
                continue;
                
            }
            
            
            if (command[0].equals("help")) {
                
                InputOutputStreams.print("Available commands:");
                
                printHelpOptions();
            } else if (command[0].equals("stop")) {
                InputOutputStreams.print("Stopping client");
                c.stop();
                scanner.close();
                return;
                
                
                
            } else if (command[0].equals("read")
                           && command.length > 1 && command[1].length() > 0) {
                c.receiveFileFromTheServer(command[1]);
                
            } else if ((command[0].equals("write"))
                           && command.length > 1 && command[1].length() > 0) {
                c.sendFileToTheServer(command[1]);
                
                
            } else if (command[0].equals("ls")) {
                listOfFiles();
            } else if (command[0].equals("mode")) {
                if (c.getMode()) {
                    c.switchMode();
                    c.setServerRequestPort(PortConfiguration.SERVER_PORT);
                } else {
                    
                    c.switchMode();
                    c.setServerRequestPort(PortConfiguration.CLIENT_PORT);
                    
                    
                }
            }else if (command[0].equals("vB")) {
                if (c.getVB()) {
                    c.switchVB();
                } else {
                    c.switchVB();
                }
                
                
            }else if ((command[0].equals("connect")) && command.length > 1
                          
                          && command[1].length() > 0) {
                
                try {
                    String connectComponents[] = command[1].split(":");
                    
                    int serverPort = DEFAULT_PORT;
                    
                    if (connectComponents.length >= 2) {
                        try {
                            
                            serverPort = Integer.parseInt(connectComponents[1]);
                            if (serverPort < 0) {
                                
                                System.out.println("Invalid port number. Port number cannot be negative. Failed to connect.");
                                continue;
                                
                            }
                            
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port number. Port number must be an integer. Failed to connect.");
                            
                            continue;
                            
                        }
                    }
                    c.setServer(InetAddress.getByName(connectComponents[0]),
                                serverPort);
                } catch (UnknownHostException e) {
                    
                    System.out.println("Failed to connect to " + command[1]);
                }
            } else if (command[0].equals("show") && command.length > 1) {
                if (command[1].equals("connection")) {
                    System.out.println(c.getConnectionString());
                    
                } else {
                    System.out.println("Invalid command. These are the available commands:");
                    printHelpOptions();
                }
            } else if ((command[0].equals("cd"))
                           && command.length > 1 && command[1].length() > 0) {
                
                
                
                changeDirectory(command[1]);
                
                
                
            } else if (command[0].equals("rm")
                           && command.length > 1 && command[1].length() > 0) {
                
                
                File f = new File(defaultDir + command[1]);
                
                if (f.exists()) {
                    f.delete();
                } else {
                    
                    InputOutputStreams.print("Cannot find file " + command[1]);
                }
            }else {
                System.out.println("Invalid command. These are the available commands:");
                printHelpOptions();
                
                
            }
            
        }
        
    }
}
