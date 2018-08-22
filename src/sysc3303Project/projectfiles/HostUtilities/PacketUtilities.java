/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles.HostUtilities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PacketUtilities {
    
    
    public static final int DATA_LENGTH = 516;
    public static byte[] newData = new byte[DATA_LENGTH];
    public static final int DATA_LENGTH_ACKOWLEDGEMENT_PACKET = 4;
    
    
    public static DatagramPacket createEmptyPacket(){
        DatagramPacket packet = new DatagramPacket(newData, DATA_LENGTH);
        return packet;
        
    }
    
    public static DatagramPacket createEmptyACKOWLEDGEMENTPacket(){
        
        byte[] rawData = new byte[DATA_LENGTH_ACKOWLEDGEMENT_PACKET];
        
        DatagramPacket packet = new DatagramPacket( rawData, DATA_LENGTH_ACKOWLEDGEMENT_PACKET);
        
        return packet;
    }
    
    public static int getPacketIDD(DatagramPacket packet){
        return packet.getData()[1];
        
    }
    
    public static int getBlockNumber(DatagramPacket packet) {
        
        byte[] blockID = {packet.getData()[2], packet.getData()[3]};
        
        ByteBuffer wrapped = ByteBuffer.wrap(blockID);
        Short num = wrapped.getShort();
        
        return ((int) num);
        
    }
    
    public static String getPacketType(DatagramPacket packet){
        if( getPacketIDD(packet)==1 ) 
            return "Read Request";
        
        else if ( getPacketIDD(packet)==2 ) 
            return "Write Request";
        
        else if ( getPacketIDD(packet)==3 ) 
            return "Data";
        
        else if ( getPacketIDD(packet)==4 ) 
            return "ACKNOWLEDGEMENT";
        
        else 
            return "Expected Type! This should NOT happen!";
    }
    
    public static String getPacketName(DatagramPacket p){
        if( isReadRequestPacket(p) ) 
            return "READREQUEST Packet";
        
        else if( isWriteRequestPacket(p) ) 
            return "WRITEREQUEST Packet";
        
        else if( isPacketDATA(p) ) 
            return "DATA Packet";
        
        else if( isACKOWLEDGEMENTPacket(p) ) 
            return "ACKNOWLEDGEMENT Packet";
        
        else 
            return "ERROR Packet";
    }
    
    public static String getPacketName(int opcode){
        if( opcode == 1 ) 
            return "READREQUEST Packet";
        
        else if( opcode == 2 ) 
            return "WRITEREQUEST Packet";
        
        else if( opcode == 3 ) 
            return "DATA Packet";
        
        else if( opcode == 4 ) 
            return "ACKNOWLEDGEMENT Packet";
        
        else 
            return "ERROR Packet";
    }
    
    public static boolean isReadRequestPacket(DatagramPacket p){
        return getPacketIDD(p) == 1;
    }
    
    public static boolean isWriteRequestPacket(DatagramPacket p){
        return getPacketIDD(p) == 2;
        
    }
    
    public static boolean isACKOWLEDGEMENTPacket(DatagramPacket p){
        return getPacketIDD(p) == 4;
        
    }
    
    public static boolean isPacketDATA(DatagramPacket p){
        return getPacketIDD(p) == 3;
        
    }
    
    public static boolean isLastPacket(DatagramPacket packet){
        return packet.getLength() < DATA_LENGTH;
        
    }
    
    public static void sendPacket(DatagramPacket packet, DatagramSocket socket) {
        
        if( socket.isClosed() ){
            IOStreams.print("Socket is now closed, unable to send packets");
        }
        
        
        try {
            socket.send(packet);
            
            String data_string = new String( packet.getData() );
            
            IOStreams.print(
                            "\n----------------------------Sent Data Packet Information------------------------" +
                            "\nPacket Type: " + getPacketType(packet) +
                            "\nPacket Destination: " + packet.getAddress() +
                            "\nDestination Port: " + packet.getPort() +
                            "\nPacket Data(Byte): "+ Arrays.toString( packet.getData() ) +
                            "\nPacket Data(string): " +  data_string +
                            "\nPacket Offset: " + packet.getOffset() +
                            "\nSocket Address: " + packet.getSocketAddress() +
                            "\n---------------------------------------------------------------------------\n"
                           );
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static DatagramPacket receivePacket(DatagramPacket packet, DatagramSocket socket) {
        
        if( socket.isClosed() ) {
            System.out.print("Socket is now closed, unable to receive packets");
            
        }
        
        try {
            socket.receive(packet);
            
            String data_string = new String( packet.getData() );
            
            System.out.print(
                             "\n**************************Received Data Packet Information**************************" +
                             "\nPacket Type: " + getPacketType(packet) +
                             "\nPacket Source: " + packet.getAddress() +
                             "\nSource Port: " + packet.getPort() +
                             "\nPacket Data(Byte): "+ Arrays.toString( packet.getData() ) +
                             "\nPacket Data(string): " +  data_string +
                             "\nPacket Offset: " + packet.getOffset() +
                             "\n******************************************************************************\n" )
                ;
            
            
        } catch ( SocketTimeoutException e){
            
            System.out.println("\n timeou, no packet received");
            socket.close();
            System.exit(1);
            
        } catch (IOException e) {e.printStackTrace();
        }
        
        return packet;
    }
    
    public static void printPacket( DatagramPacket packet ){
        IOStreams.print(
                        "\n----------------------------Data Packet Information------------------------" +
                        "\nPacket Type: " + getPacketType(packet) +
                        "\nPacket Destination: " + packet.getAddress() +
                        "\nDestination Port: " + packet.getPort() +
                        "\nPacket Data(String): "+ Arrays.toString( packet.getData() ) +
                        "\nPacket Data(Byte): " + packet.getData() +
                        "\nPacket Offset: " + packet.getOffset() +
                        "\nSocket Address: " + packet.getSocketAddress() +
                        "\n---------------------------------------------------------------------------\n"
                       );
    }
    
    public static boolean isErrorPacket(DatagramPacket packet){
        if( getPacketIDD(packet) == 6) 
            IOStreams.error("File already exist.\n");
        
        return getPacketIDD(packet) == 5 || getPacketIDD(packet) == 7;
    }
}