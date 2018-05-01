/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import sysc3303Project.projectfiles.HostUtilities.HostPacketProcessorUtil;
import sysc3303Project.projectfiles.HostUtilities.IOStreams;
import sysc3303Project.projectfiles.HostUtilities.SystemCommands;

public class Listener {
    
    private Listener(){
        try {
            HostPacketProcessorUtil.setServerAddress( InetAddress.getByName( "localhost") );
            
        } catch (UnknownHostException e) {
            IOStreams.error("Unable to set the default server address to local host");
            e.printStackTrace();
            
        }
        
        SystemCommands.initCommands();
        input();
    }
    
    private void input(){
        IOStreams.print( "TFTP Listener.Proxy started."+"\nType 'help' to get the list of commands.\n" );
        
        while (true) {
            
            IOStreams.print("Enter a command.");
            String inputtedCommand = IOStreams.inputString(">").trim().toLowerCase();
            try {
                
                if( SystemCommands.isSpecialCommand( inputtedCommand ) ) 
                    
                    SystemCommands.parseSpecialCommand( inputtedCommand );
                
                else if( !inputtedCommand.isEmpty() ) 
                    
                    SystemCommands.commandList.get( inputtedCommand ).runCommand();
                
            } catch ( NullPointerException e) {
                
                
                IOStreams.error("The specified command was not recognized.\n");
                
            } catch (IOException e){
                e.printStackTrace ();
                
            }
        }
    }
    
    
    public static void main(String[] args) {
        
        new Listener();
    }
}