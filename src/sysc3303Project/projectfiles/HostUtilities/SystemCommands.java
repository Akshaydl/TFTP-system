/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles.HostUtilities;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SystemCommands {
    
    public static Map<String, Command> commandList = new HashMap<>();
    
    private static ArrayList<Thread> listOfThreads = new ArrayList<>();
    
    public interface Command{
        void runCommand() throws IOException;
    }
    
    public static void initCommands(){
        commandList.put("status", SystemCommands::status );
        commandList.put("exit", ()->System.exit(0) );
        commandList.put("quit", commandList.get("exit"));
        commandList.put("menu", SystemCommands::startMenuCommands );
        commandList.put("clear", SystemCommands::wicked );
        commandList.put("help", SystemCommands::helpMenu );
        commandList.put("threads", SystemCommands::listOfThreads );
        commandList.put("test", SystemCommands::serverAddress );
        commandList.put("address", SystemCommands::askServerAddress);
        commandList.put("message", SystemCommands::askExtraMessage);
        commandList.put("mode", SystemCommands::askModifiedMode);
        commandList.put("shrink", SystemCommands::askDividedNum);
    }
    
    private static void start(){
        try {
            while ( !HostPacketProcessorUtil.getServerAddress().isReachable(3000) ){
                IOStreams.error( "Unavailable server address." );
                askServerAddress();
            }
            
            
        } catch (IOException e) { e.printStackTrace();
        } catch (NullPointerException e2){
            IOStreams.error( "Null server address." );
            askServerAddress();
        }
        
        HostPacketProcessorUtil packetProcessor = new HostPacketProcessorUtil();
        
        Thread newThread = new Thread( packetProcessor );
        
        listOfThreads.add(newThread);
        
        newThread.start();
    }
    
    private static void status(){
        String packetName = PacketUtilities.getPacketName( HostPacketProcessorUtil.getErrorOpCode() );
        IOStreams.print("  Listener Status  ");
        IOStreams.print(   "Listener receives request packets from port " + HostPacketProcessorUtil.getClientPort() +
                        "\nListener forwards packets to server on port " + HostPacketProcessorUtil.getServerPort() +
                        "\nDestination server address: " + HostPacketProcessorUtil.getServerAddress().toString() +
                        "\nisReceiving: " + HostPacketProcessorUtil.getReceving() +
                        "\nError Modes: " + (HostPacketProcessorUtil.getErrorMode()?"On":"Off") );
        if( HostPacketProcessorUtil.getErrorMode() ){
            IOStreams.print( "\nPacket Type(error): " + packetName +
                            "\nError Code: " + HostPacketProcessorUtil.getErrorCode() );
        }
        if( HostPacketProcessorUtil.getErrorCode() == 2 ) 
            IOStreams.print( "Delay time: " + HostPacketProcessorUtil.getDelay() );
        if( HostPacketProcessorUtil.getErrorCode() == 4 ) {
            
            IOStreams.print( "Illgal TFTP operation: " + MenuCommands.getOpName( HostPacketProcessorUtil.getOperation() ) );
            
            if( HostPacketProcessorUtil.getOperation() == 2 ) 
                IOStreams.print( "Message will be added: " + HostPacketProcessorUtil.getNotNeededData());
            if( HostPacketProcessorUtil.getOperation() == 3 ) 
                IOStreams.print( "Packet will be shrieked by: " + HostPacketProcessorUtil.getDividedBy() + " times" );
            if( HostPacketProcessorUtil.getOperation() == 7 ) 
                IOStreams.print( "Modes will be substitute to: " + HostPacketProcessorUtil.getModifiedMode() );
        }
        
        if( HostPacketProcessorUtil.getErrorOpCode() > 2) 
            IOStreams.print("Block number: " + HostPacketProcessorUtil.getErrorBlockNum() );
        
        IOStreams.print("Number of threads currently running: " + Thread.activeCount() );
        IOStreams.print("************************************************************");
        
    }
    
    
    private static void startMenuCommands() { 
        MenuCommands.startMenu(); 
        
    }
    
    private static void wicked(){
        
        HostPacketProcessorUtil.setErrorMode( false );
        HostPacketProcessorUtil.setErrorOpCode( 0 );
        HostPacketProcessorUtil.setErrorBlockNum( 0 );
        HostPacketProcessorUtil.setDelayTime( 0 );
        HostPacketProcessorUtil.setErrorCode( 0 );
        HostPacketProcessorUtil.setOperation( 0 );
        HostPacketProcessorUtil.setDividedBy(30);
        HostPacketProcessorUtil.setNotNeededData("\n\t Nothing Fancy");
        HostPacketProcessorUtil.setModifiedMode("modified mode");
        
    }
    
    private static void helpMenu(){
        IOStreams.print("***********************************Command List**************************************");
        IOStreams.print("1. start:  \n\tTo start a new thread that receives and sends packets\n\t\tUsage: start [number of threads that will be created]-optional");
        IOStreams.print("2. menu:   \n\tTo start a menu that prompts users to enter error-simulating settings");
        IOStreams.print("3. clear:  \n\tTo clean all the already-existing-error-simulating settings");
        IOStreams.print("4. status: \n\tTo show the current status of proxy");
        IOStreams.print("5. address:\n\tTo change the destination server address");
        IOStreams.print("6. message:\n\tError sim option\n\t\tTo change the message that will appended at the end of a packet");
        IOStreams.print("7. mode:   \n\tError sim option\n\t\tTo change the mpde that will substitute the original mode");
        IOStreams.print("8. exit:   \n\tThis is the END");
        IOStreams.print("*************************************************************************************");
    }
    
    private static void listOfThreads(){
        for (Thread aThread : listOfThreads) {
            int id = Integer.parseInt(aThread.getName().split("-")[1]);
            IOStreams.print("Packet processor #" + (id + 1) + ":" +
                            "\n    Status:       " + (aThread.isAlive() ? "Active" : "Dead") +
                            "\n    State:        " + aThread.getState() +
                            "\n    Thread group: " + aThread.getThreadGroup() );
        }
    }
    
    public static boolean isSpecialCommand(String command){
        ArrayList<String> listOfSpecialCommands = new ArrayList<>();
        
        listOfSpecialCommands.add("start(.*)");
        listOfSpecialCommands.add("port(.*)");
        
        for( String string : listOfSpecialCommands ){
            
            if(command.matches(string) ) 
                return true;
            
        }
        
        return false;
        
    }
    
    public static void parseSpecialCommand( String command ){
        String[] words = command.split(" ", 3);
        
        switch( words[0] ){
            
            case "start":
                try{
                for(int i = 0; i < Integer.parseInt(words[1]); i++){
                    start();
                }
            } catch(NumberFormatException e){IOStreams.print("A valid \n");
            } catch(ArrayIndexOutOfBoundsException e){
                start();
            }
            
            break;
            
            case "port": //format: [port] [server]/[client] [port]
                
                try{ //when nothing is missing
                if( IOStreams.isInt( words[2]) ){
                    
                    switch( words[1] ) {
                        case "server":
                        case "s":
                            HostPacketProcessorUtil.setServerPort( Integer.parseInt( words[2] ) );
                            IOStreams.print("Now Host will listen on port " + Integer.parseInt( words[2] ) + " to receive request packets from client.\n");
                            break;
                            
                        case "client":
                        case "c":
                            HostPacketProcessorUtil.setClientPort( Integer.parseInt( words[2] ) );
                            IOStreams.print("Now Host will listen on port " + Integer.parseInt( words[2] ) + " to receive request packets from client.\n");
                            break;
                            
                        default:
                            IOStreams.print("The specified command was not recognized.\n");
                            break;
                            
                    }
                }
                
                else IOStreams.print("The command was not recognized.\n");
            }catch (ArrayIndexOutOfBoundsException e){ //when [port] is missing
                
                try{
                    
                    
                    if(!words[1].equals("server") && !words[1].equals("s") && !words[1].equals("client") && !words[1].equals("c") ){
                        IOStreams.print("The specified command was not recognized.\n");
                        
                    }
                    
                    else {
                        int portNumber = -1;
                        
                        IOStreams.print("Please input a port number: ");
                        while( portNumber < 0 || portNumber > 65536  ){
                            portNumber = IOStreams.stringToint( IOStreams.inputString(">") );
                            
                        }
                        
                        if( words[1].equals("server") || words[1].equals("s") ) {
                            HostPacketProcessorUtil.setServerPort( portNumber );
                            IOStreams.print("Now Host will forward request packets to the server through port " + portNumber + ".\n" );
                        }
                        
                        else{
                            HostPacketProcessorUtil.setClientPort( portNumber );
                            IOStreams.print("Now Host will listens on port " + portNumber + " to receive request packets from client.\n");
                            
                        }
                    }
                    
                } catch (ArrayIndexOutOfBoundsException e2 ){ //when both [server]/[server] [port] are missing
                    
                    int option = 0;
                    
                    int portNumber = -1;
                    
                    IOStreams.print("Choose one of the following: " +
                                    "\n  1==>Client" +
                                    "\n  2==>Server" );
                    
                    while( option != 1 && option != 2 ){
                        option = IOStreams.stringToint( IOStreams.inputString(">") );
                        
                    }
                    
                    IOStreams.print("Please input a port number: ");
                    
                    while( portNumber < 0 || portNumber > 65536  ){
                        portNumber = IOStreams.stringToint( IOStreams.inputString(">") );
                        
                    }
                    
                    if( option == 2 ) {
                        HostPacketProcessorUtil.setServerPort( portNumber );
                        IOStreams.print("Now Host will forward request packets to server through port " + portNumber + ".\n" );
                    }
                    else{
                        HostPacketProcessorUtil.setClientPort( portNumber );
                        IOStreams.print("Now host will listens on port " + portNumber + "to receive request packets from client.\n");
                    }
                }
            }
            
            break;
            
            default:
                IOStreams.print("sThe specified command was not recognized.\n");
                
        }
    }
    
    
    private static void serverAddress(){
        try {
            if( HostPacketProcessorUtil.getServerAddress().isReachable( 3000 ) ) {
                IOStreams.print( "Perfect server address: " + HostPacketProcessorUtil.getServerAddress().toString() );
                
            }else {
                IOStreams.print( "Not a valid server address.\n" );
                
            }
            
        } catch (IOException e) {
            IOStreams.error( "Unable to reach the server address: " + HostPacketProcessorUtil.getServerAddress().toString() );
            e.printStackTrace();
        }
    }
    
    private static void askServerAddress(){
        String address;
        IOStreams.print( "Please enter a server address."  + "\nEnter 'quit' or 'exit' to abort operation.");
        
        while( true ){
            
            try {
                address = IOStreams.inputString(">");
                
                if( address.equals("quit") || address.equals("exit") ) 
                    break;
                else if( address.equals("") ) 
                    continue;
                
                if ( InetAddress.getByName( address ).isReachable(3000) ) {
                    HostPacketProcessorUtil.setServerAddress( InetAddress.getByName( address ) );
                    IOStreams.print("Now server address is: " + HostPacketProcessorUtil.getServerAddress() );
                    break;
                    
                }else {
                    IOStreams.error( "Unable to the address. " );
                    IOStreams.print( "Please enter a server address." + "\nEnter 'quit' or 'exit' to abort operation." );
                    
                }
                
            } catch (IOException e) {IOStreams.error( "Unable to set the address." );
            }
        }
    }
    
    private static void askExtraMessage(){
        String message;
        IOStreams.print("Enter a message that will be appended at the end of a packet: " + "\nEnter 'quit' or 'exit' to abort operation.");
        
        while( true ){
            message = IOStreams.inputString( ">" );
            
            if( message.equals("quit") || message.equals("exit") ) 
                break;
            else if( message.length() < 5 ) 
                IOStreams.print( "Message is too short. ");
            else if( message.length() > 200 ) 
                IOStreams.print( "Message is too long. ");
            else{
                HostPacketProcessorUtil.setNotNeededData( "\n" + message );
                break;
            }
        }
    }
    
    private static void askModifiedMode(){
        String message;
        IOStreams.print("Enter a new mode that will be substitute the mode inside request packets: " + "\nEnter 'quit' or 'exit' to abort operation.");
        
        while( true ){
            message = IOStreams.inputString( ">" );
            
            if( message.equals("quit") || message.equals("exit") ) 
                break;
            else if( message.length() < 5 ) 
                IOStreams.print( "Modes is too short. ");
            else if( message.length() > 100 ) 
                IOStreams.print( "Modes is too long. ");
            else{
                HostPacketProcessorUtil.setModifiedMode( message );
                break;
            }
        }
    }
    
    private static void askDividedNum(){
        
        String input;
        IOStreams.print("Enter a number that will be divided by default data length," + "\nEnter 'quit' or 'exit' to abort this operation.");
        IOStreams.print("Current value is: " + HostPacketProcessorUtil.getDividedBy() + ". Packet length after shrink will be " + (516/HostPacketProcessorUtil.getDividedBy()) + " bytes." );
        
        while( true ){
            input = IOStreams.inputString( ">" );
            
            if( input.equals("quit") || input.equals("exit") ) 
                break;
            else if( IOStreams.isInt(input) ){
                if( IOStreams.stringToint(input) == 1 ) 
                    IOStreams.print("This is garbage.");
                
                else if( IOStreams.stringToint(input) == 0 ) 
                    IOStreams.print("Imagine that you have zero cakes and you split them evenly among zero friends. " +
                                    "\nHow many cakes does each person get? haha! It doesn't make sense. ");
                
                else if( IOStreams.stringToint(input) < 10 ) 
                    IOStreams.print("The number might be too small");
                
                if( IOStreams.stringToint(input) > 1 ) 
                    break;
            }
        }
        
        HostPacketProcessorUtil.setDividedBy( IOStreams.stringToint(input) );
    }
}