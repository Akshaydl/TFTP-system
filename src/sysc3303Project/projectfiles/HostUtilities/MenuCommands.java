/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles.HostUtilities;

public class MenuCommands {
    
    private static void printMenuCommands(int i){
        switch( i ){
            
            case 0:
                IOStreams.print( "Choose a packet packetType to generate error: \n" +
                                "\t1. READREQUEST Packet\n" +
                                "\t2. WRITEREQUEST Packet\n" +
                                "\t3. DATA Packet\n" +
                                "\t4. ACKNOWLEDGEMENT Packet" );
                break;
                
            case 1:
                IOStreams.print( "Choose an error packetType: \n" +
                                "\t1. Lose a packet\n" +
                                "\t2. Delay a packet\n" +
                                "\t3. Duplicate a packet\n" +
                                "\t4. Illegal TFTP operation\n" +
                                "\t5. Unknown transfer ID" );
                break;
        }
    }
    
    
    public static  void startMenu(){
        
        int delay = 0;
        int stage = 0;
        int opCode = 0;
        String newInput;
        int blockNum = 0;
        int errorCode = 0;
        int operation = 0;
        
        
        
        while( stage >= 0 ){
            switch ( stage ){
                case 0:
                    printMenuCommands( stage );
                    stage = 1;
                    
                    while ( opCode < 1 || opCode > 4){
                        newInput  = IOStreams.inputString(">").trim().toLowerCase();
                        
                        if( newInput.equals("exit") || newInput.equals("quit") || newInput.equals("q") ) {
                            stage = -1;
                            break;
                            
                        }
                        
                        opCode = IOStreams.stringToint( newInput );
                    }
                    
                    if( opCode > 2 ){
                        IOStreams.print( "Please enter the block number:> " );
                        
                        int output1 = askForTheInt();
                        
                        if ( output1 == -1 ) 
                            stage = -1;
                        else 
                            blockNum = output1;
                    }
                    
                    break;
                    
                case 1:
                    printMenuCommands( stage );
                    stage = 1000;
                    
                    while( errorCode < 1 || errorCode > 5){
                        newInput = IOStreams.inputString(">");
                        if( newInput.equals("exit") || newInput.equals("quit") || newInput.equals("q") ) {
                            stage = -1;
                            break;
                            
                        }
                        if( IOStreams.isInt(newInput) ) 
                            
                            errorCode = Integer.parseInt( newInput );
                        
                    }
                    
                    if( errorCode == 2){
                        IOStreams.print( "Please enter a time(millisecond) for delay: " );
                        
                        int output2 = askForTheInt();
                        
                        if( output2 == -1 )
                            stage = -1;
                        else 
                            delay = output2;
                    }
                    
                    if( errorCode == 4){
                        
                        int i;
                        IOStreams.print( "Please choose a specific packetType error to generate: ");
                        IOStreams.print( "1. Change packet opcode" );
                        IOStreams.print( "2. Append more data to the packet");
                        IOStreams.print( "3. Shrink the packet");
                        IOStreams.print( "4. Change the port number - Invalid TID");
                        
                        if( opCode == 1 || opCode == 2){
                            IOStreams.print( "5. Remove the byte '0' after the file name");
                            IOStreams.print( "6. Remove the byte '0' after the mode");
                            IOStreams.print( "7. Modify the string mode");
                            IOStreams.print( "8. Remove File name from the packet");
                        }
                        
                        while( true ){
                            i = askForTheInt();
                            
                            int maximum = 5;
                            
                            if( opCode == 1 || opCode == 2) 
                                maximum = 9;
                            
                            if( i < maximum ) 
                                break;
                            
                        }
                        
                        if( i == -1 )
                            stage = -1;
                        
                        else 
                            operation = i;
                        
                    }
                    
                    break;
                    
                case 1000:
                    
                    IOStreams.print( "Confirm the settings: " );
                    
                    IOStreams.print( "Target packet: " + PacketUtilities.getPacketName( opCode ) );
                    
                    if( opCode > 2) 
                        
                        IOStreams.print( "Block number: " + blockNum );
                    
                    IOStreams.print( "Error Type: " + getErrorName( errorCode ) );
                    
                    if( errorCode == 2)
                        IOStreams.print( "The packet will be delayed for " + delay + " milliseconds(" + delay/1000 + " seconds)." );
                    if( errorCode == 4) 
                        IOStreams.print( "The illegal operation: " + getOpName( operation ) );
                    
                    boolean newBoolean = true;
                    
                    IOStreams.print("Type 'yes/y' to confirm the setting and generate the error, packetType 'no/n' otherwise.");
                    
                    while ( newBoolean ){
                        String str = IOStreams.inputString( ">" ).trim().toLowerCase();
                        
                        if( str.equals("yes") || str.equals("y") ){
                            HostPacketProcessorUtil.setErrorMode( true );
                            HostPacketProcessorUtil.setErrorOpCode( opCode );
                            HostPacketProcessorUtil.setErrorBlockNum( blockNum );
                            HostPacketProcessorUtil.setErrorCode( errorCode );
                            HostPacketProcessorUtil.setDelayTime( delay );
                            HostPacketProcessorUtil.setOperation( operation );
                            newBoolean = false;
                            
                        }
                        
                        else if ( str.equals("no") || str.equals("n") || shallWeQuit( str ) ) {
                            IOStreams.print( "Setting has been aborted." );
                            newBoolean = false;
                            
                        }
                        
                    }
                    
                    stage = -1;
                    break;
            }
            
        }
    }
    
    private static String getErrorName( int errorCode ){
        
        switch( errorCode ){
            
            case 1:
                return "Lose a packet";
                
            case 2:
                return "Delay a packet";
                
            case 3:
                return "Duplicate a packet";
                
            case 4:
                return "Illegal TFTP operation";
                
            case 5:
                return  "Unknown transfer ID";
                
            default:
                return "Avoid This!!!!";
        }
    }
    
    static String getOpName( int i ){
        switch( i ){
            case 1:
                return "change packet OpCode";
                
            case 2:
                return "append more data to the packet";
                
            case 3:
                return "shrink the packet";
                
            case 4:
                return "change the port number - Invalid TID";
                
            case 5:
                return "remove the byte '0' after the file name";
                
            case 6:
                return "remove the byte '0' after the mode";
                
                
            case 7:
                return "modify the string mode";
                
            case 8:
                return "remove File name from the packet";
                
            default:
                return "Avoid This";
        }
    }
    
    private static boolean shallWeQuit( String string ){
        
        return string.equals("exit") 
            || string.equals("quit") 
            || string.equals("q");
        
    }
    
    
    private static int askForTheInt() {
        
        String rawInput = "-2";
        
        while( IOStreams.stringToint( rawInput ) <= 0 ){
            rawInput = IOStreams.inputString( ">" );
            
            if( shallWeQuit( rawInput ) ) {
                return -1;
                
            }
            
        }
        
        return IOStreams.stringToint( rawInput );
    }
}