/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles;

import java.io.ByteArrayOutputStream;

public class ReadWriteRequestPacket extends Packet {
    
    private static final int MINIMUMLENGTH = 10; 
    
    public static enum Actions {
        READ, WRITE
            
    }
    
    public static enum Modes {
        ASCII, OCTET
            
    }
    
    private String fileName = ""; 
    private Actions actions; 
    private Modes modes;
    
    ReadWriteRequestPacket(String fileName, Actions actions, Modes modes) throws IllegalArgumentException {
        
        if (fileName == null || fileName.length() == 0 || actions == null || modes == null) {
            
            String message = "Missing data in the request packet";
            
            if (fileName == null || fileName.length() == 0) {
                message = "Missing file name";
                
            }else if (actions == null) {
                message = "Not a read or write request";
                
            }else if (modes == null) {
                message = "Invalid transfer modes";
                
            }
            
            throw new IllegalArgumentException(message);
        }
        
        this.fileName = fileName;
        this.actions = actions;
        this.modes = modes;
        this.packetType = (actions == Actions.READ) ? Type.READREQUEST : Type.WRITEREQUEST;
    }
    
    public String getFilename() {
        return fileName;
        
    }
    
    
    public boolean isReadRequest() {
        return (actions == Actions.READ);
    }
    
    
    static ReadWriteRequestPacket createFromBytes(byte[] packetData, int packetLength) throws IllegalArgumentException {
        
        Actions actions;
        String filename;
        Modes modes;
        
        
        if (packetData == null || packetData.length < packetLength || packetLength < MINIMUMLENGTH) {
            throw new IllegalArgumentException("Data is not long enough");
            
        }
        
        if (packetData[0] != 0) {
            throw new IllegalArgumentException("Invalid OP code");
            
            
        } else if (packetData[1] == 1) {
            actions = Actions.READ;
            
            
        } else if (packetData[1] == 2) {
            actions = Actions.WRITE;
            
        } else {
            int opcode = ((packetData[0] << 8) & 0xFF00) | (packetData[1] & 0xFF);
            throw new IllegalArgumentException("Invalid OP code: " + opcode);
            
        }
        
        int i = 1;
        StringBuilder filenameBuilder = new StringBuilder();
        
        while (packetData[++i] != 0 && i < packetLength) {
            filenameBuilder.append((char) packetData[i]);
            
            
        }
        
        filename = filenameBuilder.toString();
        
        if (packetData[i] != 0) {
            throw new IllegalArgumentException("Must have a 0 after filename");
            
            
        }
        
        
        
        StringBuilder modeStrBuilder = new StringBuilder();
        while (packetData[++i] != 0 && i < packetLength) {
            modeStrBuilder.append((char) packetData[i]);
            
            
        }
        
        
        String modeStr = modeStrBuilder.toString().toLowerCase();
        
        if (modeStr.equals("netascii")) {
            modes = Modes.ASCII;
            
            
            
        } else if (modeStr.equals("octet")) {
            modes = Modes.OCTET;
            
        } else {
            String errMsg;
            if (modeStr == null || modeStr.isEmpty()) {
                errMsg = "Missing transfer modes";
                
            } else {
                errMsg = "Invalid transfer modes: " + modeStr;
                
            }
            
            throw new IllegalArgumentException(errMsg);
            
        }
        
        if (packetData[packetLength - 1] != 0) {
            throw new IllegalArgumentException( "Trailing 0 not found after modes");
        }
        
        
        return new ReadWriteRequestPacket(filename, actions, modes);
    }
    
    @Override
    public byte[] generatingData() {
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(0);
        
        
        if (actions == Actions.WRITE) {
            
            stream.write(2); // write request flag byte
            
        } else {
            stream.write(1); // read request flag byte
            
        }
        
        byte[] tempByteArr = fileName.getBytes();
        stream.write(tempByteArr, 0, tempByteArr.length);
        stream.write(0);
        
        tempByteArr = modes.toString().toLowerCase().getBytes();
        stream.write(tempByteArr, 0, tempByteArr.length);
        stream.write(0);
        return stream.toByteArray();
    }    
}