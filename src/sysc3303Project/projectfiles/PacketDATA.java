/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles;

import java.io.ByteArrayOutputStream;

public class PacketDATA extends Packet {
    
    private int ackBlockNumber = 0;
    private byte[] newFileData = null;
    private static final int OPCODE = 3;
    public static final int MAXFILELENGTH = 512; 
    private static final int MINackBLOCKNUMBER = 1;
    private static final int MAXackBLOCKNUMBER = 0xFFFF;
    private static final int PACKETHEADERLENGTH = 4;
    
    
    
    PacketDATA(int blockNumber, byte[] fileData, int fileDataLength){
        
        if (blockNumber < MINackBLOCKNUMBER || blockNumber > MAXackBLOCKNUMBER ) {
            throw new IllegalArgumentException("Invalid block number");
            
        }
        
        if ( fileData == null && fileDataLength != 0 ) {
            throw new IllegalArgumentException( "Data length must be 0 if data is null");
            
        }
        
        if (fileData != null && (fileDataLength > fileData.length || fileDataLength > MAXFILELENGTH || fileDataLength < 0)) {
            throw new IllegalArgumentException("Invalid data length passed");
            
        }
        
        this.packetType = Type.DATA;
        this.ackBlockNumber = blockNumber;
        
        if (fileData == null || fileDataLength == 0) {
            this.newFileData = new byte[0];
            
        } 
        
        else {
            this.newFileData = new byte[fileDataLength];
            System.arraycopy(fileData, 0, this.newFileData, 0, fileDataLength);
            
        }
    }
    
    
    public byte[] gettingFileData() {
        return newFileData;
    }
    
    
    public int getBlockNumbers() {
        return ackBlockNumber;
    }
    
    
    
    
    public boolean lastDataPacket() {
        return (newFileData.length < MAXFILELENGTH);
    }
    
    
    static PacketDATA createFromBytes(byte[] packetData, int packetLength)
        throws IllegalArgumentException {
        
        
        if (packetData == null) {
            throw new IllegalArgumentException("No valid data found");
            
        }
        
        
        
        if (packetLength > packetData.length || packetLength < PACKETHEADERLENGTH || packetLength > Packet.MAXPACKETLENGTH) {
            
            throw new IllegalArgumentException("Invalid packet length");
            
        }
        
        // Verify opcode
        if (packetData[0] != 0 || packetData[1] != OPCODE) {
            throw new IllegalArgumentException("Invalid opcode");
        }
        
        
        int blockNumber = ((packetData[2] << 8) & 0xFF00) | (packetData[3] & 0xFF);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        stream.write(packetData, PACKETHEADERLENGTH, packetLength-PACKETHEADERLENGTH);
        
        packetData = stream.toByteArray();
        
        return new PacketDATA(blockNumber, packetData, packetData.length);
        
    }
    
    
    
    
    @Override
    public byte[] generatingData() {
        
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        stream.write(0);
        stream.write(OPCODE);
        stream.write(ackBlockNumber >> 8);
        stream.write(ackBlockNumber);
        stream.write(newFileData, 0, newFileData.length);
        
        return stream.toByteArray();
    }   
}