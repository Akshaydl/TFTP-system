/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles;

import java.net.InetAddress;
import java.net.DatagramPacket;
import sysc3303Project.projectfiles.ReadWriteRequestPacket.*;

public abstract class Packet {
    
    static final int MAXPACKETLENGTH = 516;
    private static final int MINPACKETLENGTH = 4;
    
    public enum Type {
        READREQUEST, WRITEREQUEST, DATA, ACKNOWLEDGEMENT, ERROR
    }
    
    Type packetType;
    
    public Type getTrivialPacketType() {
        return packetType;
    }
    
    
    
    public static ReadWriteRequestPacket creatingReadRequestPacket(String fileName, Modes modes) {
        
        return new ReadWriteRequestPacket(fileName, Actions.READ, modes);
        
    }
    
    
    
    public static ReadWriteRequestPacket createWriteRequestPacket(String fileName, Modes modes) {
        
        return new ReadWriteRequestPacket(fileName, Actions.WRITE, modes);
        
    }
    
    
    public static AcknowlegementPacket creatingACKNOWLEDGEMENTPAcket(int blockLength) {
        
        return new AcknowlegementPacket(blockLength);
        
    }
    
    
    public static PacketDATA creatingDataPacket(int blockNumber, byte[] data, int dataLength) {
        return new PacketDATA(blockNumber, data, dataLength);
        
    }
    
    public static Packet creatingFromDatagram(DatagramPacket datagram) throws IllegalArgumentException {
        
        
        return Packet.createFromBytes(datagram.getData(),
                                      datagram.getLength());
    }
    
    public static PacketError creatingErrorPacket(PacketError.ErrorType errorType, String errorMessage) {
        return new PacketError(errorType, errorMessage);
    }
    
    
    private static Packet createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
        
        if (packetData.length < packetLength || packetLength < MINPACKETLENGTH) {
            throw new IllegalArgumentException( "packet Length is less than minimum length");
        }
        
        
        if (packetData[0] != 0) {
            throw new IllegalArgumentException("Invalid opcode");
            
        }
        
        switch (packetData[1]) {
            
            case 1:
                return ReadWriteRequestPacket.createFromBytes(packetData, packetLength); //READREQUEST
                
            case 2:
                return ReadWriteRequestPacket.createFromBytes(packetData, packetLength); //WRITEREQUEST
                
            case 3:
                return PacketDATA.createFromBytes(packetData, packetLength); //DATA
                
            case 4:
                return AcknowlegementPacket.createFromBytes(packetData, packetLength);  // ACKNOWLEDGEMENT
                
            case 5:
                
                return PacketError.createFromBytes(packetData, packetLength);  // Error
                
            default:
                throw new IllegalArgumentException("Invalid opcode");
        }
    }
    
    public static DatagramPacket creatingDatagramForReceiving() {
        return new DatagramPacket(new byte[MAXPACKETLENGTH], MAXPACKETLENGTH);
        
    }
    
    public DatagramPacket generatingDatagram(InetAddress remoteAddress, int remotePort) {
        byte data[] = this.generatingData();
        return new DatagramPacket(data, data.length, remoteAddress, remotePort);
    }
    
    public abstract byte[] generatingData();   
}