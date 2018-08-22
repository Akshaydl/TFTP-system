/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles.HostUtilities;


import java.net.*;

public class HostPacketProcessorUtil implements Runnable {
    
    private static int CLIENT_PORT = 2300;
    private static int SERVER_PORT = 69;
    
    public static int getClientPort() {
        return CLIENT_PORT;
        
    }
    
    public static void setClientPort(int port){ 
        CLIENT_PORT = port; 
        
    }
    
    
    
    public static int getServerPort() { return SERVER_PORT; }
    public static void setServerPort(int port) { SERVER_PORT = port; }
    
    private int port_client;
    
    private int port_server;
    
    private int ID;
    
    public int getID() { return this.ID; }
    
    private static int idCount = 0;
    
    private static DatagramSocket recievedSocket;
    
    private DatagramSocket socket_receSend;
    
    private DatagramPacket requestPacket = PacketUtilities.createEmptyPacket();
    
    private DatagramPacket dataPacket = PacketUtilities.createEmptyPacket();
    
    private DatagramPacket ackPacket = PacketUtilities.createEmptyACKOWLEDGEMENTPacket();
    
    private boolean isLast = false;
    
    private static boolean errorMode = false;
    
    private static int errorOpCode = 0;
    
    private static int errorBlockNum = 0;
    
    private static int errorCode = 0;
    
    private static int delay = 0;
    
    private InetAddress clientAddress;
    
    private static InetAddress serverAddress;
    
    public static InetAddress getServerAddress() { return serverAddress; }
    
    public static void setServerAddress( InetAddress newServerAddress ){ serverAddress = newServerAddress; }
    
    private static int Operation = 0;
    
    public static int getOperation() { return Operation; }
    
    public static void setOperation(int i) { Operation = i; }
    
    public static void setErrorMode(boolean bool){ errorMode = bool; }
    
    public static boolean getErrorMode() { return errorMode; }
    
    public static void setErrorOpCode( int i ){ errorOpCode = i; }
    
    public static int getErrorOpCode() { return errorOpCode; }
    
    public static void setErrorBlockNum( int i ){ errorBlockNum = i; }
    
    public static int getErrorBlockNum() { return errorBlockNum; }
    
    public static int getErrorCode() { return errorCode; }
    
    public static void setErrorCode( int i ) { errorCode = i; }
    
    public static int getDelay() { return delay; }
    
    public static void setDelayTime( int i ) { delay = i; }
    
    private static boolean receiving = false;
    
    private String requestPacketType = "";
    
    public static String getReceving(){
        
        if( receiving ) 
            return "true";
        else 
            return "false";
    }
    
    private static String NOTNEEDED_DATA = "\n\tNothing special";
    
    public static void setNotNeededData(String msg){ NOTNEEDED_DATA = msg; }
    
    public static String getNotNeededData() { return NOTNEEDED_DATA; }
    
    private static int DivideBy = 30;
    
    public static int getDividedBy(){ return DivideBy; }
    
    public static void setDividedBy(int i){ DivideBy = i; }
    
    private static String modifiedMode = "modified_mode";
    
    public static void setModifiedMode(String newMode){ modifiedMode = newMode; }
    
    public static String getModifiedMode() { return modifiedMode; }
    
    private boolean is_error_there = false;
    
    
    @Override
    public void run() {
        
        this.ID = ++idCount;
        
        IOStreams.printHostProcess( "Packet Processor, ID: " + this.ID + " has started!" );
        
        String stage = "request";
        
        boolean isRunning = true;
        while( isRunning ){
            switch( stage ){
                case "request":
                    
                    receiveRequestPacket();
                    
                    this.port_client = this.requestPacket.getPort();
                    
                    this.clientAddress = this.requestPacket.getAddress();
                    
                    openSocketForReceiveAndSend();
                    
                    this.requestPacket.setPort( SERVER_PORT );
                    
                    this.requestPacket.setAddress( serverAddress );
                    
                    if( errorMode && !this.is_error_there ) 
                        errorSim( this.requestPacket );
                    
                    PacketUtilities.sendPacket(this.requestPacket, this.socket_receSend);
                    
                    if( PacketUtilities.isReadRequestPacket(this.requestPacket) ) {
                        this.requestPacketType = "READREQUEST";
                        stage = "data";
                        
                    }
                    else if( PacketUtilities.isWriteRequestPacket(this.requestPacket) ) {
                        this.requestPacketType = "WRITEREQUEST";
                        stage = "ack";
                        
                    }
                    
                    else{
                        //error
                        IOStreams.print("Error!");
                    }
                    
                    break;
                    
                case "data":
                    
                    receiveDataPacket();
                    
                    if( PacketUtilities.isErrorPacket(this.dataPacket) ){ //error packet
                        //ignore file-already-exist error
                        IOStreams.error("An error packet has been received, re-try to receive the DATA packet.");
                        
                        
                    }else{ //normal operation
                        
                        if( errorMode && !this.is_error_there ) 
                            errorSim( this.dataPacket );
                        
                        sendDataPacket();
                        
                        if( PacketUtilities.isLastPacket(this.dataPacket) ) 
                            this.isLast = true;
                        
                        
                        stage = "ack";
                    }
                    
                    break;
                    
                case "ack":
                    
                    receiveAckPacket();
                    
                    if( PacketUtilities.isErrorPacket( this.ackPacket ) ){
                        
                        IOStreams.error("An error packet has been received, re-try to receive the ACKNOWLEDGEMENT packet.");
                    }
                    else{ 
                        
                        if( errorMode && !this.is_error_there ) 
                            errorSim( this.ackPacket );
                        
                        
                        sendAckPacket();
                        
                        
                        if( this.isLast ){
                            IOStreams.printHostProcess("Last data packet has been processed, file transfer completed!");
                            IOStreams.print("-Press 'enter' to continue-");
                            isRunning = false;
                            break;
                        }
                        
                        stage = "data";
                    }
                    
                    break;
                    
                default:
                    break;
            }
        }
        
        this.socket_receSend.close();
        
    }
    
    
    private synchronized void receiveRequestPacket() {
        
        try {
            
            
            while ( receiving ) {
                IOStreams.printHostProcess( "Another packet processor is receiving, packet processor #"
                                               + this.ID + " is waiting... "
                                               + "\n-Press 'enter' to enter more commands-");
                
                wait();
                
            }
            
            
            
            receiving = true;
            
            
            recievedSocket = new DatagramSocket( CLIENT_PORT );
            
            
            IOStreams.printHostProcess("Packet processor #" + this.ID + " is trying to receive a request packet from client..."
                                           + "\n-Press 'enter' to enter more command-");
            
        } catch (SocketException e) {
            
            IOStreams.error("Unable to make datagramSocket listen on port" + CLIENT_PORT );
            
            e.printStackTrace();
            
            
        } catch (InterruptedException e){
            e.printStackTrace();
            
            
        }
        
        while( true ){
            PacketUtilities.receivePacket( this.requestPacket, recievedSocket);
            
            if( PacketUtilities.getPacketIDD( this.requestPacket) >2 ) 
                IOStreams.error("\nError packet has been received, re-try to receive another packet.\n");
            else 
                break;
        }
        
        recievedSocket.close();
        
        receiving = false;
        notifyAll();
    }
    
    private void openSocketForReceiveAndSend(){
        try {
            this.socket_receSend = new DatagramSocket();
            
        } catch (SocketException e) {
            IOStreams.error("Unable to open a datagramSocket for receive and send. Exit...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
            
        }
    }
    
    
    private void receiveDataPacket(){
        IOStreams.printHostProcess("Listener is trying to receive a DATA Packet");
        
        
        if( this.requestPacketType.equals("READREQUEST") ){
            
            
            PacketUtilities.receivePacket(this.dataPacket, this.socket_receSend);
            
            
            this.port_server = this.dataPacket.getPort();
            
            
            this.dataPacket.setPort( this.port_client );
            
            
            this.dataPacket.setAddress( this.clientAddress );
            
        } else if( this.requestPacketType.equals("WRITEREQUEST") ){
            
            PacketUtilities.receivePacket(this.dataPacket, this.socket_receSend);
            
            
            this.dataPacket.setPort( this.port_server );
            
            
            this.dataPacket.setAddress( serverAddress );
            
        }
        
    }
    
    private void sendDataPacket(){
        IOStreams.printHostProcess("Listener is trying to send a DATA Packet");
        PacketUtilities.sendPacket(this.dataPacket, this.socket_receSend);
        
    }
    
    
    private void receiveAckPacket(){
        
        IOStreams.printHostProcess("Listener is trying to receive a ACKNOWLEDGEMENT Packet");
        
        if( this.requestPacketType.equals("READREQUEST") ){
            
            
            PacketUtilities.receivePacket(this.ackPacket, this.socket_receSend);
            
            
            this.ackPacket.setPort( this.port_server );
            
            
            this.ackPacket.setAddress( serverAddress );
            
        }else if( this.requestPacketType.equals("WRITEREQUEST") ){
            
            
            PacketUtilities.receivePacket(this.ackPacket, this.socket_receSend);
            
            
            this.port_server = this.ackPacket.getPort();
            
            
            this.ackPacket.setPort( this.port_client );
            
            
            this.ackPacket.setAddress( this.clientAddress );
            
        }
        
    }
    
    private void sendAckPacket(){
        
        IOStreams.printHostProcess("Listener is trying to send a ACKNOWLEDGEMENT Packet");
        PacketUtilities.sendPacket(this.ackPacket, this.socket_receSend);
        
    }
    
    private void errorSim(DatagramPacket packet){
        
        if( (errorOpCode  == 1 || errorOpCode == 2) && compareOpcode(packet) ){ 
            
            switch( errorCode ){
                
                case 1: //lose a packet
                    
                    IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) + " will be lost. Listener will re-try to receive another one." );
                    
                    receiveRequestPacket();
                    
                    //set the destination port of request packet to 69
                    
                    this.requestPacket.setPort( SERVER_PORT );
                    
                    
                    break;
                    
                case 2: //delay a packet
                    
                    IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) +
                                                     " will be delayed for " + delay + " milliseconds(" + delay/1000 + " seconds).");
                    
                    delayPacket();
                    
                    break;
                    
                case 3: 
                    
                    
                    IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) +
                                                     " will be duplicated. ");
                    
                    sendDuplicatedPacket( packet );
                    
                    break;
                    
                case 4: 
                    
                    
                    IOStreams.printSimulatorErrorMsg( "Illegal TFTP operation, " +
                                                     MenuCommands.getOpName( Operation ) +
                                                     ", will be simulated on " + PacketUtilities.getPacketType( packet ) + "." );
                    
                    illTFTPOp( packet );
                    
                    break;
                    
                case 5: 
                    
                    
                    IOStreams.printSimulatorErrorMsg( "Invalid transfer ID will be simulated on " + PacketUtilities.getPacketType( packet ) + "." );
                    
                    changePort( packet );
                    
                    break;
                    
                default:
                    
                    IOStreams.error("Invalid error code!");
                    
                    break;
                    
            }
            
            this.is_error_there = true;
            
        }else if( (errorOpCode == 3 || errorOpCode == 4) && compareOpcode(packet) ){ //for DATA and WRITEREQUEST
            
            if( compareBlockNum(packet) ){
                
                switch( errorCode ){
                    
                    case 1: //lose a packet
                        
                        IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) +
                                                         
                                                         " will be lost. Listener will re-try to receive another one." );
                        
                        
                        if( PacketUtilities.getPacketIDD( packet ) == 3){
                            receiveDataPacket(); 
                            
                        } //DATA
                        
                        else{ 
                            receiveAckPacket();
                        } //ACKNOWLEDGEMENT
                        
                        break;
                        
                    case 2: //delay a packet
                        
                        IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) +
                                                         " will be delayed. "  );
                        
                        
                        delayPacket();
                        
                        break;
                        
                    case 3: //duplicate a packet
                        
                        IOStreams.printSimulatorErrorMsg(PacketUtilities.getPacketType( packet ) +
                                                         " will be duplicated. ");
                        
                        sendDuplicatedPacket( packet );
                        
                        break;
                        
                    case 4:
                        
                        IOStreams.printSimulatorErrorMsg( "Illegal TFTP operation, " +
                                                         
                                                         MenuCommands.getOpName( Operation ) +
                                                         ", will be simulated on " + PacketUtilities.getPacketType( packet ) + ".");
                        
                        illTFTPOp( packet );
                        
                        break;
                        
                    case 5:
                        
                        
                        IOStreams.printSimulatorErrorMsg( "Invalid transfer ID will be simulated on " + PacketUtilities.getPacketType( packet ) + "." );
                        
                        changePort( packet );
                        
                        break;
                        
                    default:
                        
                        IOStreams.error("Invalid error code");
                        
                        break;
                        
                }
                
            }
            
            this.is_error_there = true;
        }
        else if( errorOpCode <= 0 || errorOpCode > 4) {
            IOStreams.error( "Invalid opcode to generate error on!");
        }
    }
    
    private boolean compareOpcode( DatagramPacket packet ){ 
        return packet.getData()[1] == errorOpCode; 
        
    }
    
    
    private static void delayPacket(){
        try {
            Thread.currentThread();
            Thread.sleep( delay );
            
        } catch (InterruptedException e) {
            
            IOStreams.error("Cannot make current thread sleep!");
            
            e.printStackTrace();
            
        }
    }
    
    private boolean compareBlockNum( DatagramPacket packet ){
        
        return errorBlockNum == PacketUtilities.getBlockNumber( packet );
        
    }
    
    private void sendDuplicatedPacket(DatagramPacket packet){
        
        
        IOStreams.printSimulatorErrorMsg( "This is the first time " + PacketUtilities.getPacketName( packet ) + " being sent.");
        PacketUtilities.sendPacket(packet, this.socket_receSend);
        IOStreams.printSimulatorErrorMsg( "This is the second time " + PacketUtilities.getPacketName( packet ) + " being sent.");
        
    }
    
    private void illTFTPOp(DatagramPacket packet){
        
        byte data[], temp[];
        int count;
        switch( Operation ){
            
            case 1:
                
                
                int r = (int) (Math.random() * 4);
                
                data = packet.getData();
                
                data[1] = (byte) r;
                
                packet.setData( data );
                
                break;
                
            case 2:
                
                data = new byte[ 516 + NOTNEEDED_DATA.length() ];
                
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength() );
                
                for(int i = 0; i < NOTNEEDED_DATA.length(); i ++) 
                    data[516 + i] = NOTNEEDED_DATA.getBytes()[i];
                
                packet.setData( data );
                
                break;
                
            case 3:
                
                int newLength = 516/DivideBy;
                
                data = new byte[ newLength ];
                
                System.arraycopy(packet.getData(), 0, data, 0, newLength );
                
                packet.setData( data );
                
                break;
                
            case 4:
                
                changePort( packet );
                
                break;
                
            case 5: 
                
                data = packet.getData();
                
                count = 0;
                
            for( byte b : data) {
                if (b == 0 && count != 0) break;
                count++;
                
            }
            
            data[count] = 1;
            
            packet.setData( data );
            
            break;
            
            case 6: //remove the 0 after mode
                
                data = packet.getData();
                
                count = 0;
                int times = 0;
                
            for( byte b : data ){
                if( b == 0 && count != 0) 
                    times += 1;
                
                if( times >= 2) 
                    break;
                
                count++;
                
            }
            
            data[count] = 1;
            
            packet.setData( data );
            
            break;
            
            case 7:
                
                data = packet.getData();
                
                count = 0;
                
                int start = 0;
                
                int end = 0;
                
            for( byte b : data ) {
                
                if( b == 0 && count != 0 ){
                    
                    if( start == 0 ) 
                        start = count;
                    
                    else {
                        end = count;
                        break;
                    }
                }
                count++; 
                
            }
            
            
            temp = new byte[data.length-end];
            
            System.arraycopy(data, end, temp, 0, data.length-end);
            
            IOStreams.print( start + "," + end + "," + (data.length-end) + "\n");
            
            
            System.arraycopy(modifiedMode.getBytes(), 0, data, start, modifiedMode.getBytes().length  );
            
            
            System.arraycopy( temp, 0, data, modifiedMode.getBytes().length+start, data.length-(modifiedMode.getBytes().length+start) );
            
            
            packet.setData( data );
            
            break;
            
            case 8: 
                
                data = packet.getData();
                
                int j;
                
                for( j = 2; j < data.length; j++) if( data[j] == 0 ) break;
                
                temp = new byte[data.length-j];
                System.arraycopy(data, j, temp, 0, temp.length);
                
                System.arraycopy(temp, 0, data, 2, temp.length);
                
                packet.setData( data );
                
                break;
                
            default:
                
                IOStreams.error("Incorrect illegal TFTP error code!");
                
                break;
        }
    }
    
    private void changePort(DatagramPacket packet){
        
        int oldPort = packet.getPort();
        
        int newPort = oldPort;
        
        while( newPort == oldPort ) newPort = (int) (Math.random() * 65536);
        packet.setPort( newPort );
    }    
}