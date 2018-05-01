/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles;

import sysc3303Project.Server;

import java.io.*;
import java.net.*;

public class TrivialTransferHandler extends Thread{
    
    private Connections connection;
    private String fileName;
    private String filePath;
    private boolean isRRQ;
    private Server server;
    private boolean vB;
    
    
    
    public TrivialTransferHandler(Server server, ReadWriteRequestPacket packet, InetAddress toAddress, int toPort) {
        try {
            
            this.server = server;
            
            connection = new Connections();
            connection.setRemoteAddress(toAddress);
            connection.setRemoteTid(toPort);
            
            this.fileName = packet.getFilename();
            
            vB = server.getVb();
            connection.setVb(vB);
            
            this.filePath = server.getPublicFolder() + fileName;
            this.isRRQ = packet.isReadRequest();
            
        } catch (SocketException se) {
            InputOutputStreams.print("Failed to open datagramSocket for transfer for" + fileName);
            
        }
    }
    
    @Override
    public void run() {
        server.incrementThreadCount();
        
        if (fileName.charAt(0) == '.') {
            connection.sendAccessViolation("This server reject transfering unix hidden files( files that start with a \".\")");
            
        } else if (isRRQ) {
            this.sendFilesToClient();
            
        } else {
            
            this.receiveFileFromClient();
            
        }
        
        server.decrementThreadCount();
    }
    
    public void sendFilesToClient() {
        
        int blockNumber = 1;
        
        FileInputStream fs;
        
        try {
            
            File file = new File(filePath);
            
            if (!file.exists()) {
                throw new FileNotFoundException();
                
            }
            
            if (!file.isAbsolute()) {
                connection.sendAccessViolation("Trying to access file in private area");
                return;
                
            }
            
            
            fs = new FileInputStream(file);
            
            int bytesRead;
            
            byte[] data = new byte[PacketDATA.MAXFILELENGTH];
            
            do {
                
                bytesRead = fs.read(data);
                
                if (bytesRead == -1) {
                    bytesRead = 0;
                    data = new byte[0];
                }
                
                try {
                    connection.sendData(blockNumber, data, bytesRead);
                    connection.receiveAcknowledgement(blockNumber);
                    
                } catch (AbortException e) {
                    
                    Logging.testing("Aborting transfer of " + fileName + ": " + e.getMessage(), vB);
                    fs.close();
                    return;
                    
                }
                blockNumber++;
            } while (bytesRead == PacketDATA.MAXFILELENGTH);
            
            fs.close();
            
            InputOutputStreams.print("Done sending file \'" + fileName + "\' to client");
            
        } catch (FileNotFoundException e1) {
            Logging.testing("File not found: " + fileName, vB);
            connection.sendFileNotFound("Could not find: " + fileName);
            return;
            
        } catch (IOException e) {
            Logging.testing("IOException: " + e.getMessage(), vB);
            return;
            
        }
    }
    
    public void receiveFileFromClient() {
        try {
            
            File file = new File(filePath);
            if (file.exists()) {
                
                
                connection.sendFileAlreadyExists(fileName + " already exists");
                return;
            }
            
            if (!file.isAbsolute()) {
                
                
                connection.sendAccessViolation("Trying to access file in private area");
                return;
            }
            
            if (!file.getParentFile().canWrite()) {
                
                
                connection.sendAccessViolation("Cannot write to a readonly folder");
                return;
            }
            
            FileOutputStream fs = new FileOutputStream(file);
            int blockNumber = 0;
            PacketDATA dataPk;
            
            do {
                try {
                    
                    
                    connection.sendAcknowledgement(blockNumber);
                    dataPk = connection.receiveData(++blockNumber);
                    
                    if (file.canWrite()) {
                        fs.write(dataPk.gettingFileData());
                        fs.getFD().sync();
                    } else {
                        
                        connection.sendAccessViolation("Cannot write to a readonly file");
                        return;
                    }
                } catch (AbortException e) {
                    fs.close();
                    file.delete();
                    Logging.testing("Aborting transfer of " + fileName + ": "
                                        + e.getMessage(), vB);
                    return;
                } catch (SyncFailedException e) {
                    fs.close();
                    file.delete();
                    
                    connection.sendVoidDiscFull("Failed to sync with disc, likely is full");
                    return;
                }
            } while (!dataPk.lastDataPacket());
            
            
            try {
                connection.sendAcknowledgement(blockNumber);
            } catch (Exception e) { }
            
            InputOutputStreams.print("Done receiving file \'" + fileName + "\' from client");
            fs.close();
        } catch (FileNotFoundException e) {
            
            new File(filePath).delete();
            
            connection.sendAccessViolation("Cannot write to a readonly file");
            return;
            
        } catch (IOException e) {
            new File(filePath).delete();
            InputOutputStreams.print("IOException with file: " + fileName);
            connection.sendVoidDiscFull(e.getMessage());
            return;
        }
    }
}