/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import sysc3303Project.projectfiles.*;

public class Server {
    
    private static final int SERVERPORT = PortConfiguration.SERVER_PORT;
    private static String defaultDir = System.getProperty("user.dir") + "/serverFiles/";
    private String publicFolder = defaultDir; 
    private int totalThreadCount = 0;   
    private Host host;  
    private boolean vB;
    
    
    public Server() {
        new File(publicFolder).setWritable(true);
        vB = true;
        host = new Host(this, SERVERPORT);
        host.start();
        
    }
    
    public static void main(String [] args) {
        
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);
        
        for(;;) {
            
            InputOutputStreams.print("Current working directory " + server.publicFolder);
            
            if (server.getVb()) {
                InputOutputStreams.print("Verbose is turned on.");
                
            } else {
                InputOutputStreams.print("Verbose is turned off.");
                
            }
            
            System.out.print("Server: ");
            String cmdLine = scanner.nextLine().toLowerCase();
            String[] command =  cmdLine.split("\\s+");
            
            if (command[0].length() == 0) {
                continue;
            }
            
            if (command[0].equals("help")) {
                InputOutputStreams.print("\nAvailable commands:");
                InputOutputStreams.print("vB: toggle vB mode off or on");
                InputOutputStreams.print("stop: stop the server (when current transfers finish");
                InputOutputStreams.print("ls: List out the the public directory for file transfer");
                InputOutputStreams.print("rm <FILENAME>: delete the specified file from the folder");
                InputOutputStreams.print("cd <DIRECTORY>: Change the directory for file transfer. Specify path ");
                InputOutputStreams.print("defaultdir : Change the directory for file transfer to default public directory. (project folder)");
                InputOutputStreams.print("-------------------------------------------------------------------------------------------------\n");
                
            } else if (command[0].equals("stop")) {
                System.out.println("Stopping server when the current transfer finish");
                server.stop();
                scanner.close();
                
            }else if (command[0].equals("vB")) {
                server.toggleVerbose();
                
            }else if (command[0].equals("ls")){
                java.io.File[] files = new java.io.File(server.publicFolder).listFiles();
                if (files != null) {
                    for (java.io.File f : files) InputOutputStreams.print(f.getName());
                    InputOutputStreams.print(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
                } else InputOutputStreams.print("No files found");
                
                
            } else if ((command[0].equals("cd") && command.length > 1 && command[1].length() > 0) || (command[0].equals("defaultdir"))) {
                
                if (command[0].equals("defaultdir")){
                    server.publicFolder = defaultDir;
                } else {
                    if (command[1].toCharArray()[command[1].length() - 1] != '/') {
                        command[1] = command[1] + '/';
                    }
                    File folder = new File(command[1]);
                    
                    if (folder.isDirectory()) {
                        server.publicFolder = command[1];
                    } else {
                        InputOutputStreams.print(command[1] + " is not a directory.");
                    }
                }
                
            }  else if (command[0].equals("rm")
                            && command.length > 1 && command[1].length() > 0) {
                
                File f = new File(server.publicFolder + command[1]);
                
                if (f.exists()) {
                    f.delete();
                } else {
                    InputOutputStreams.print("Cannot find file " + command[1]);
                }
            } else {
                System.out.println("Invalid commands. Type the help command to get available commands: ");
            }
            
        }
        
    }
    
    synchronized public void incrementThreadCount() {
        totalThreadCount++;
    }
    
    synchronized public void decrementThreadCount() {
        totalThreadCount--;
        
        if (totalThreadCount <= 0) {
            notifyAll();
            
        }
    }
    
    synchronized public int getThreadCount() {
        
        return totalThreadCount;
        
    }
    
    public void stop() {
        host.getSocket().close();
        System.out.println("Stopping... waiting for threads to finish");
        
        while(getThreadCount() > 0) {
            
            
            try {
                wait();
            }catch (InterruptedException ie) {
                System.out.println("Stopping was interrupted. Failed to stop properly");
                System.exit(1);
            }
        }
        System.out.println("Exiting");
        System.exit(0);
    }
    
    public boolean getVb(){
        return vB;
    }
    
    public void toggleVerbose() {
        this.vB = !this.vB;
        
    }
    
    public String getPublicFolder() {
        return publicFolder;
        
    }
    public TrivialTransferHandler newTransferThread(ReadWriteRequestPacket packet, InetAddress address, int port) {
        return new TrivialTransferHandler(this, packet, address, port);
        
    }    
}