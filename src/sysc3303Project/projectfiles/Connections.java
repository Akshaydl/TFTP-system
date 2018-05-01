/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles;

import java.io.IOException;
import java.net.*;

public class Connections {
    
    private DatagramSocket datagramSocket;
    private InetAddress remoteAddress;
    
    private DatagramPacket resendDatagram;
    private int maxResendAttempts = 4;
    private int timeOut = 2000;
    
    private int requestPort = 6800;
    private int tiD = -1;
    private DatagramPacket datagramPacket = Packet.creatingDatagramForReceiving();
    
    private boolean vB = true;
    
    
    public Connections() throws SocketException {
        this(new DatagramSocket());
    }
    
    public Connections(int bindPort) throws SocketException {
        this(new DatagramSocket(bindPort));
    }
    
    public Connections(DatagramSocket socket) throws SocketException {
        this.datagramSocket = socket;
        socket.setSoTimeout(timeOut);
        Logging.testing("connected on port "+ socket.getLocalPort(), vB);
    }
    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    public void setRemoteTid(int remoteTid) {
        this.tiD = remoteTid;
        Logging.testing("setting remote tid to: " + remoteTid, vB);
    }
    
    public void sendReadWriteRequest(ReadWriteRequestPacket packet) throws IOException {
        resendDatagram =  packet.generatingDatagram(remoteAddress, requestPort);
        datagramSocket.send(packet.generatingDatagram(remoteAddress, requestPort));
        
    }
    
    public void setVb(boolean verbose) {
        this.vB = verbose;
    }
    
    public void setRequestPort(int requestPort) {
        this.requestPort = requestPort;
    }
    
    private void sendPacket(Packet packet) throws IOException {
        sendPacket(packet, false);
    }
    
    private void sendPacket(Packet packet, boolean cacheForResend)
        throws IOException {
        DatagramPacket dp = packet.generatingDatagram(remoteAddress, tiD);
        
        if (vB)
            Logging.printingSendPacketDetails(packet.getTrivialPacketType(), dp);
        
        
        if (cacheForResend) {
            resendDatagram = dp;
            
        } else {
            resendDatagram = null;
            
        }
        
        datagramSocket.send(dp);
    }
    
    public void sendAcknowledgement(int blockNumber) throws AbortException {
        
        try {
            sendPacket(Packet.creatingACKNOWLEDGEMENTPAcket(blockNumber), false);
            
        } catch (Exception e) {
            throw new AbortException(e.getMessage());
            
        }
    }
    
    private void echoAcknowledgement(int blockNumber) throws IOException {
        
        sendPacket(Packet.creatingACKNOWLEDGEMENTPAcket(blockNumber));
        
        Logging.testing("sent: ack #" + blockNumber + " in response to duplicate data", vB);
    }
    
    
    private void resendLastPacket() throws AbortException {
        if (resendDatagram == null) {
            return; 
        }
        
        try {
            
            datagramSocket.send(resendDatagram);
            Logging.testing("Resending last transfer packet.", vB);
            
            
        } catch (IOException e) {
            throw new AbortException(e.getMessage());
            
        }
    }
    
    private Packet receive() throws IOException, AbortException {
        while (true) {
            
            datagramSocket.receive(datagramPacket);
            
            
            if (tiD > 0 && (datagramPacket.getPort() != tiD || !(datagramPacket.getAddress()).equals(remoteAddress))) {
                
                Logging.testing("****** Received packet from invalid TID: " + addressToString(datagramPacket.getAddress(),
                                                                                              datagramPacket.getPort()), vB);
                
                sendUnknownTidError(datagramPacket.getAddress(),
                                    datagramPacket.getPort());
                continue;
            }
            
            
            try {
                Packet packet = Packet.creatingFromDatagram(datagramPacket);
                if (vB)
                    Logging.printingReceivePacketDetails(packet.packetType, datagramPacket);
                
                return packet;
                
            } catch (IllegalArgumentException e) {
                sendIllegalOperationError(e.getMessage());
            }
        }
    }
    
    private void sendIllegalOperationError(String message)  throws AbortException {
        try {
            PacketError pk = Packet.creatingErrorPacket(
                                                        
                                                        PacketError.ErrorType.ILLEGAL_OPERATION, message);
            
            sendPacket(pk);
            
            Logging.testing("Sending error packet (Illegal Operation) with message: " + message, vB);
            
            throw new AbortException(message);
            
        } catch (IOException e) {
            throw new AbortException(message);
        }
    }
    
    private void sendUnknownTidError(InetAddress address, int port) {
        try {
            String errMsg = "Stop hacking fool!";
            PacketError pk = Packet.creatingErrorPacket(
                                                        PacketError.ErrorType.UNKOWN_TID, errMsg);
            datagramSocket.send(pk.generatingDatagram(address, port));
            Logging.testing("*******  Sending error packet (Unknown TID) to "
                                + addressToString(address, port) + " with message: "
                                + errMsg, vB);
        } catch (Exception e) {
            // Ignore
        }
    }
    
    public void sendFileNotFound(String message) {
        try {
            PacketError pk = Packet.creatingErrorPacket(
                                                        PacketError.ErrorType.FILE_NOT_FOUND, message);
            
            sendPacket(pk);
            
            Logging.testing("Sending error packet (File not Found) with message: "
                                + message, vB);
        } catch (IOException e) {
            // Ignore
        }
    }
    
    public void sendVoidDiscFull(String message) {
        try {
            PacketError pk = Packet.creatingErrorPacket(
                                                        PacketError.ErrorType.DISC_FULL_OR_ALLOCATION_EXCEEDED,
                                                        message);
            
            sendPacket(pk);
            
            Logging.testing("Sending error packet (Disc Full) with message: " + message, vB);
            
        } catch (IOException e) {
            
            
        }
    }
    
    public void sendAccessViolation(String message) {
        try {
            PacketError pk = Packet.creatingErrorPacket(
                                                        PacketError.ErrorType.ACCESS_VIOLATION, message);
            sendPacket(pk);
            
            Logging.testing("Sending error packet (Access Violation) with message: "
                                
                                + message, vB);
        } catch (IOException e) {
            
            
            
        }
    }
    
    public void sendFileAlreadyExists(String message) {
        try {
            PacketError pk = Packet.creatingErrorPacket(
                                                        
                                                        PacketError.ErrorType.FILE_ALREADY_EXISTS, message);
            
            sendPacket(pk);
            
            Logging.testing("Sending error packet (File Already Exists) with message: "
                                
                                + message, vB);
            
        } catch (IOException e) {
            // Ignore
            
        }
    }
    
    public PacketDATA receiveData(int blockNumber)
        throws AbortException {
        
        PacketDATA pk = (PacketDATA) receiveExpectedPacket(
                                                           Packet.Type.DATA, blockNumber);
        
        
        if (tiD <= 0 && blockNumber == 1) {
            setRemoteTid(datagramPacket.getPort());
        }
        
        return pk;
    }
    
    public AcknowlegementPacket receiveAcknowledgement(int blockNumber) throws AbortException {
        
        AcknowlegementPacket pk = (AcknowlegementPacket) receiveExpectedPacket(Packet.Type.ACKNOWLEDGEMENT,
                                                                               
                                                                               blockNumber);
        
        
        if (tiD <= 0 && blockNumber == 0) {
            setRemoteTid(datagramPacket.getPort());
            
        }
        
        return pk;
    }
    
    public void sendData(int blockNumber, byte[] fileData, int fileDataLength) throws AbortException {
        try {
            
            PacketDATA pk = Packet.creatingDataPacket(blockNumber,
                                                      
                                                      fileData, fileDataLength);
            
            
            sendPacket(pk, true);
            
            Logging.testing("sent: data #" + blockNumber
                                
                                + ((pk.lastDataPacket()) ? " (last)" : ""), vB);
            
        } catch (Exception e) {
            
            
            throw new AbortException(e.getMessage());
        }
    }
    
    private String addressToString(InetAddress addr, int port) {
        return addr.toString() + ":" + port;
        
    }
    
    private Packet receiveExpectedPacket(Packet.Type type, int blockNumber) throws AbortException {
        
        int timeouts = 0;
        
        
        try {
            
            while (true) {
                try {
                    
                    
                    
                    Packet pk = receive();
                    
                    
                    if (pk.getTrivialPacketType() == type) {
                        if (pk.getTrivialPacketType() == Packet.Type.DATA) {
                            PacketDATA dataPk = (PacketDATA) pk;
                            if (dataPk.getBlockNumbers() == blockNumber) {
                                return dataPk;
                            } else if(dataPk.getBlockNumbers() < blockNumber) {
                                
                                
                                echoAcknowledgement(dataPk.getBlockNumbers());
                            } else {
                                
                                
                                sendIllegalOperationError("Received future data block number: "
                                                              + dataPk.getBlockNumbers());
                            }
                        } else if (pk.getTrivialPacketType() == Packet.Type.ACKNOWLEDGEMENT) {
                            AcknowlegementPacket ackPk = (AcknowlegementPacket) pk;
                            if (ackPk.getackBlockNumber() == blockNumber) {
                                return pk;
                            } else if (ackPk.getackBlockNumber() > blockNumber) {
                                sendIllegalOperationError("Received future ack block number: "
                                                              + ackPk.getackBlockNumber());
                            }
                        }
                    }else if (pk instanceof PacketError) {
                        PacketError errorPk = (PacketError) pk;
                        Logging.testing("Received error packet. Code: "
                                            + errorPk.getCode() + ", Type: "
                                            + errorPk.getErrorType().toString()
                                            + ", Message: \"" + errorPk.getErrorMessage()
                                            + "\"", vB);
                        
                        if (errorPk.shouldAbortTransfer()) {
                            Logging.testing("Aborting transfer", vB);
                            throw new AbortException(
                                                     errorPk.getErrorMessage());
                        } else {
                            Logging.testing("Continuing with transfer", vB);
                        }
                    } else if (pk instanceof ReadWriteRequestPacket) {
                        throw new AbortException(
                                                 "Received request packet within data transfer connection");
                    }
                }catch (SocketTimeoutException e) {
                    if (timeouts >= maxResendAttempts) {
                        throw new AbortException(
                                                 "Connection timed out. Giving up.");
                    }
                    
                    Logging.testing("Waiting to receive " + type + " #" + blockNumber
                                        + " timed out, trying again.", vB);
                    
                    timeouts++;
                    resendLastPacket();
                }
            }
        } catch (IOException e) {
            throw new AbortException(e.getMessage());
        }
    }    
}