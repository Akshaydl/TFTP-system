/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles;

import java.io.ByteArrayOutputStream;

public class PacketError extends Packet {
    
    private static final int OpCODE = 5;
    
    private ErrorType errorType;
    private String errorMessage;
    
    private static final int MINIMUM_LENGTH = 5;
    
    
    
    public enum ErrorType {
        NOT_DEFINED(0), 
            FILE_NOT_FOUND(1), 
            ACCESS_VIOLATION(2), 
            DISC_FULL_OR_ALLOCATION_EXCEEDED(3), 
            ILLEGAL_OPERATION(4),
            UNKOWN_TID(5), 
            FILE_ALREADY_EXISTS(6), 
            NO_SUCH_USER(7);
        
        private int code;
        
        ErrorType(int code) {
            this.code = code;
        }
        
        int getCode() {
            return code;
        }
        
        static ErrorType get(int code) throws IllegalArgumentException {
            for (ErrorType t : ErrorType.values()) {
                if (code == t.code) {
                    return t;
                }
            }
            throw new IllegalArgumentException();
        }
    }
    
    PacketError(ErrorType errorType, String errorMessage) throws IllegalArgumentException {
        
        if (errorType == null) {
            
            throw new IllegalArgumentException();
            
        }
        
        this.errorType = errorType;
        
        this.packetType = Type.ERROR;
        
        this.errorMessage = (null == errorMessage) ? "" : errorMessage;
        
    }
    
    public ErrorType getErrorType() {
        return this.errorType;
        
    }
    
    public int getCode() {
        return this.errorType.getCode();
        
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
        
    }
    
    public boolean shouldAbortTransfer() {
        return (this.errorType != ErrorType.UNKOWN_TID);
    }
    
    
    @Override
    public byte[] generatingData() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        
        stream.write(0);
        stream.write(OpCODE);
        
        
        stream.write(errorType.getCode() >> 8);
        stream.write(errorType.getCode());
        
        
        byte[] tempByteArr = errorMessage.getBytes();
        stream.write(tempByteArr, 0, tempByteArr.length);
        stream.write(0);
        
        
        return stream.toByteArray();
    }
    
    static PacketError createFromBytes(byte[] packetData, int packetLength)
        throws IllegalArgumentException {
        
        
        if (packetData == null) {
            throw new IllegalArgumentException();
        }
        
        
        
        if (packetLength > packetData.length || packetLength < MINIMUM_LENGTH || packetLength > Packet.MAXPACKETLENGTH) {
            throw new IllegalArgumentException();
        }
        
        
        if (packetData[0] != 0 || packetData[1] != OpCODE || packetData[2] != 0) {
            throw new IllegalArgumentException();
        }
        
        
        
        int errorCode = packetData[3];
        ErrorType errorType = ErrorType.get(errorCode);
        
        
        StringBuilder errorMessage = new StringBuilder();
        
        for (int i = 4; i < (packetLength - 1); i++) {
            if (0 == packetData[i]) {
                
                throw new IllegalArgumentException();
                
            } else {
                errorMessage.append((char)packetData[i]);
                
            }
        }
        
        
        if (packetData[packetLength - 1] != 0) {
            throw new IllegalArgumentException();
        }
        
        return new PacketError(errorType, errorMessage.toString());
    }
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        
        
        str.append(0);
        str.append((byte) OpCODE);
        
        
        str.append((byte) errorType.getCode() >> 8);
        str.append((byte) errorType.getCode());
        
        
        str.append(errorMessage);
        str.append(0);
        
        
        return str.toString();
        
    }
}