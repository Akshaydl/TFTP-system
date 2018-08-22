/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles;

public class AcknowlegementPacket extends Packet {
    
    private int ackBlockNumber = 0;
    
    private static final int MINackBLOCKNUMBER = 0;
    private static final int MAXackBLOCKNUMBER = 0xffff;
    private static final int OpCODE = 4;
    private static final int PacketLENGTH = 4;
    
    
    
    AcknowlegementPacket(int blockNumber) throws IllegalArgumentException {
        if ( blockNumber < MINackBLOCKNUMBER || blockNumber > MAXackBLOCKNUMBER) {
            
            throw new IllegalArgumentException("Invalid block length");
            
        }
        
        this.ackBlockNumber = blockNumber;
        
        this.packetType = Type.ACKNOWLEDGEMENT;
        
    }
    
    
    public static AcknowlegementPacket createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
        
        
        if (packetData == null || packetData.length  < PacketLENGTH || packetLength != PacketLENGTH ) {
            throw new IllegalArgumentException("Incorrect packet length");
            
        }
        
        
        if (packetData[0] != 0 || packetData[1] != OpCODE ) {
            throw new IllegalArgumentException("Incorrect opcode");
            
        }
        
        
        int blockLength = ((packetData[2] << 8) & 0xFF00) | (packetData[3] & 0xFF);
        
        return new AcknowlegementPacket(blockLength);
        
    }
    
    public int getackBlockNumber() {
        return ackBlockNumber;
        
    }
    
    @Override
    public byte[] generatingData() {
        byte[] data = new byte[4];
        data[0] = 0;
        data[1] = (byte) OpCODE;
        data[2] = (byte) (ackBlockNumber >> 8);
        data[3] = (byte) (ackBlockNumber);
        return data;
        
    }
    
}

