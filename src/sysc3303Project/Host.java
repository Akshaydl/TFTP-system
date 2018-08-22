/*
 @author Syed Arsal Abbas
 */

package sysc3303Project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import sysc3303Project.projectfiles.*;

public class Host extends Thread {
    
    private int port;
    private Server server;
    protected DatagramSocket datagramSocket;
    
    
    public Host (Server server, int boundPort) {
        this.server    = server;
        this.port = boundPort;
        
    }
    
    public DatagramSocket getSocket() {
        return datagramSocket;
    }
    
    @Override
    public void run() {
        server.incrementThreadCount();
        
        try {datagramSocket = new DatagramSocket(port);
        }catch (SocketException se) {
            System.out.println("Failed to bind to port: "+ port);
            System.exit(1);
        }
        
        try {
            Logging.testing("Server is listening for request on port: "+ port, server.getVb());
            
            while(!datagramSocket.isClosed()) {
                DatagramPacket dp = Packet.creatingDatagramForReceiving();
                datagramSocket.receive(dp);
                
                try {
                    Packet packet = Packet.creatingFromDatagram(dp);
                    if (packet instanceof ReadWriteRequestPacket) {
                        Logging.testing("Received transfer packer, starting new Transfer thread", server.getVb());
                        
                        TrivialTransferHandler tftpTransferHandler =
                            server.newTransferThread( (ReadWriteRequestPacket) packet, dp.getAddress(), dp.getPort());
                        
                        tftpTransferHandler.start();
                        
                    } else {
                        
                        if (!(packet instanceof PacketError)) {
                            
                            DatagramSocket errorSocket = new DatagramSocket();
                            String errMsg = "Received the wrong kind of packet on request listener.";
                            PacketError errorPacket =
                                Packet.creatingErrorPacket(PacketError.ErrorType.ILLEGAL_OPERATION, errMsg);
                            dp = errorPacket.generatingDatagram(dp.getAddress(),dp.getPort());
                            
                            errorSocket.send(dp);
                            errorSocket.close();
                            
                            Logging.testing("Sending illegal operation error packet with message: "+ errMsg, server.getVb());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    
                    DatagramSocket errorSocket = new DatagramSocket();
                    Logging.testing("Server Received invalid request packet", server.getVb());
                    PacketError tftpErrorPacket = Packet.creatingErrorPacket(PacketError.ErrorType.ILLEGAL_OPERATION, e.getMessage());
                    dp = tftpErrorPacket.generatingDatagram(dp.getAddress(), dp.getPort());
                    errorSocket.send(dp);
                    errorSocket.close();
                }
            }
            
        } catch (IOException io) {
            
            io.printStackTrace();}
        
        datagramSocket.disconnect();
        
        Logging.testing("Request listener is thread has stopped", server.getVb());
        server.decrementThreadCount();
        
    }
    
}
