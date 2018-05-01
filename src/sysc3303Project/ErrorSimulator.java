/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project;

import java.net.*;
import java.util.Scanner;
import java.io.IOException;
import sysc3303Project.projectfiles.*;

public class ErrorSimulator {
    
    private enum ErrCommands {
        NORMAL("normal"), ERROR_CHANGE_OPCODE
            ("mode 0"), ERROR_REMOVE_FILENAME_DELIMITER
            ("mode 1"), ERROR_REMOVE_MODE_DELIMITER("mode 2"), ERROR_MODIFY_MODE
            ("mode 3"), ERROR_APPEND("mode 4"), ERROR_SHRINK_PACKET("mode 5"), ERROR_REMOVE_FILENAME
            ("mode 6"), ERROR_INVALID_TID("mode 7"), ERROR_LOSE_PACKET
            ("mode 8"), ERROR_DELAY_PACKET("mode 9"), ERROR_DUPLICATE_PACKET
            ("mode 10"), ERROR_APPEND_DATA("mode 11"), ERROR_APPEND_ACK
            ("mode 12"), ERROR_SHRINK_DATA("mode 13"), ERROR_SHRINK_ACK
            ("mode 14"), ERROR_CHANGE_BLOCK_NUM("mode 18");
        
        
        private ErrCommands(final String text) {
            this.text = text;
            
        }
        
        private final String text;
        
        @Override
        public String toString() {
            return text;
            
        }
        
        public boolean menu1() {
            return ( this == ERROR_CHANGE_OPCODE 
                        || this == ERROR_LOSE_PACKET);
            
        }
        
        public boolean menu2() {
            return ( this == ERROR_DELAY_PACKET
                        ||  this == ERROR_DUPLICATE_PACKET
                        ||  this == ERROR_INVALID_TID 
                        ||  this == ERROR_CHANGE_BLOCK_NUM);
        }
        
        public boolean menu3() {
            return ( this == ERROR_DELAY_PACKET
                        ||  this == ERROR_DUPLICATE_PACKET
                        ||  this == ERROR_INVALID_TID
                        ||  this == ERROR_CHANGE_BLOCK_NUM
                        ||  this == ERROR_SHRINK_ACK  
                        ||  this == ERROR_APPEND_DATA
                        ||  this == ERROR_LOSE_PACKET  
                        ||  this == ERROR_APPEND_ACK
                        ||  this == ERROR_SHRINK_DATA  
                        ||  this == ERROR_CHANGE_OPCODE);
        }
        
    }
    private enum PacketType {
        
        DATA("mode 15"), ACKNOWLEGMENT("mode 16"), REQUEST("mode 17");
        
        /**
         * @param text
         */
        private PacketType(final String text) {
            this.text = text;
            
        }
        
        private final String text;
        
        
        @Override
        public String toString() {
            return text;
        }
    }
    
    
    private int blockNumber;
    private int newBlocknum;
    protected int totalthreadCount = 0;
    private boolean changed = false;
    protected boolean stopping = false;
    protected InetAddress serverAddress;
    protected RequestReceivedThread requestReceived;
    public int serverPort = PortConfiguration.SERVER_PORT;
    public int clientPort = PortConfiguration.CLIENT_PORT;
    private ErrCommands commandError = ErrCommands.NORMAL;
    private PacketType packetType = PacketType.ACKNOWLEGMENT;
    
    
    /**
     * Constructor
     */
    public ErrorSimulator() {
        
        try {
            boolean isValid = false;
            
            while (!isValid) {
                isValid = true;
                System.out.print("Connect to:");
                @SuppressWarnings("resource") Scanner scanner = new Scanner(System.in);
                String command = scanner.nextLine().toLowerCase();
                
                if (command.equalsIgnoreCase("localhost"))
                    serverAddress = InetAddress.getLocalHost();
                
                else if (Character.isDigit(command.charAt(0)))
                    serverAddress = InetAddress.getByName(command);
                
                else {
                    System.out.println("localhost: for localhost\n");
                    isValid = false;
                }
            }
            
            requestReceived = new RequestReceivedThread();
            requestReceived.start();
            
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        ErrorSimulator errSim = new ErrorSimulator();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            
            System.out.print("SystemCommand: ");
            
            String command = scanner.nextLine().toLowerCase();
            
            if (command.length() == 0) {
                continue;
                
            }
            
            if (command.equals("help")) {
                printHelp();
                errSim.commandError = ErrCommands.NORMAL;
                
            } else if (command.equals("stop")) {
                System.out.println("Stopping simulator (when current transfers finish)");
                errSim.stop();
                scanner.close();
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_CHANGE_BLOCK_NUM.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_CHANGE_BLOCK_NUM;
                
                
            } else if (command.equalsIgnoreCase(ErrCommands.NORMAL.toString())) {
                
                errSim.commandError = ErrCommands.NORMAL;
                
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_CHANGE_OPCODE.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_CHANGE_OPCODE;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_REMOVE_FILENAME_DELIMITER.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_REMOVE_FILENAME_DELIMITER;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_MODIFY_MODE.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_MODIFY_MODE;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_REMOVE_MODE_DELIMITER.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_REMOVE_MODE_DELIMITER;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_APPEND.toString())) {
                
                boolean isValid = false;
                
                while (!isValid) {
                    System.out.print("Choose Packet Type:  data packet(mode 11) or ackPacket(mode 12):");
                    
                    command = scanner.nextLine().toLowerCase();
                    
                    if (command .equalsIgnoreCase(ErrCommands.ERROR_APPEND_DATA.toString())) {
                        
                        errSim.packetType = PacketType.DATA;
                        errSim.commandError = ErrCommands.ERROR_APPEND_DATA;
                        isValid = true;
                        
                    } else if (command.equalsIgnoreCase(ErrCommands.ERROR_APPEND_ACK.toString())) {
                        
                        errSim.packetType = PacketType.ACKNOWLEGMENT;
                        errSim.commandError = ErrCommands.ERROR_APPEND_ACK;
                        isValid = true;
                        
                    } else {
                        System.out.println("Invalid command");
                    }
                    
                }
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_SHRINK_PACKET.toString())) {
                
                boolean isValid = false;
                
                while (!isValid) { 
                    
                    System.out.print("Choose Packet Type:  data packet(mode 13) or ack Packet(mode 14):");
                    command = scanner.nextLine().toLowerCase();
                    
                    if (command.equalsIgnoreCase(ErrCommands.ERROR_SHRINK_DATA.toString())) {
                        
                        errSim.packetType = PacketType.DATA;
                        errSim.commandError = ErrCommands.ERROR_SHRINK_DATA;
                        isValid = true;
                        
                    } else if (command.equalsIgnoreCase(ErrCommands.ERROR_SHRINK_ACK.toString())) {
                        
                        errSim.packetType = PacketType.ACKNOWLEGMENT;
                        errSim.commandError = ErrCommands.ERROR_SHRINK_ACK;
                        isValid = true;
                        
                    } else {
                        System.out.println("Invalid command");
                        
                    }
                }
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_REMOVE_FILENAME.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_REMOVE_FILENAME;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_INVALID_TID.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_INVALID_TID;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_LOSE_PACKET.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_LOSE_PACKET;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_DELAY_PACKET.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_DELAY_PACKET;
                
            } else if (command.equalsIgnoreCase(ErrCommands.ERROR_DUPLICATE_PACKET.toString())) {
                
                errSim.commandError = ErrCommands.ERROR_DUPLICATE_PACKET;
                
            } else {
                
                errSim.commandError = ErrCommands.NORMAL;
                printHelp();
            }
            
            if (errSim.commandError.menu1()) {
                
                boolean isValid = false;
                
                while (!isValid) {
                    isValid = true;
                    System.out.print("Choose Packet Type:  data packet(mode 15) or ack Packet(mode 16) or  request Packet(mode 17):");
                    command = scanner.nextLine().toLowerCase();
                    
                    if (command.equalsIgnoreCase(PacketType.REQUEST.toString())) {
                        errSim.packetType = PacketType.REQUEST;
                        
                    } else if (command.equalsIgnoreCase(PacketType.ACKNOWLEGMENT.toString())) {
                        
                        errSim.packetType = PacketType.ACKNOWLEGMENT;
                        
                    } else if (command.equalsIgnoreCase(PacketType.DATA.toString())) {
                        
                        errSim.packetType = PacketType.DATA;
                    } else {
                        
                        System.out.println("Invalid command, valid commands are('mode 15', 'mode 16', 'mode 17')");
                        isValid = false;
                        
                    }
                }
                
            } else if (errSim.commandError.menu2()) {
                
                boolean isValid = false;
                while (!isValid) {
                    
                    isValid = true;
                    System.out.print("Choose Packet Type:  data packet(mode 15) or ack Packet(mode 16):");
                    command = scanner.nextLine().toLowerCase();
                    
                    if (command.equalsIgnoreCase(PacketType.ACKNOWLEGMENT.toString())) {
                        
                        errSim.packetType = PacketType.ACKNOWLEGMENT;
                        
                    } else if (command.equalsIgnoreCase(PacketType.DATA .toString())) {
                        
                        errSim.packetType = PacketType.DATA;
                        
                    } else {
                        
                        System.out.println("Invalid command, valid commands are(data or ack)");
                        isValid = false;
                        
                    }
                }
            }
            
            if (errSim.commandError.menu3() && errSim.packetType != PacketType.REQUEST) {
                
                errSim.blockNumber = chooseTheBlockNum("Choose the Block Number: ");
                
                if (errSim.commandError == ErrCommands.ERROR_CHANGE_BLOCK_NUM)
                    
                    errSim.newBlocknum = chooseTheBlockNum("Change the Block Number to: ");
                
            } 
        }
    }
    
    private static void printHelp() {
        System.out.println("   Available commands:   ");
        System.out.println("help : Display the help menu");
        System.out.println("stop : stop the Error Simulator");
        System.out.println("normal : normal mode ");
        System.out.println("mode 0  : change packet OpCode ");
        System.out.println("mode 1 : Remove the byte '0' after the file name");
        System.out.println("mode 2 : Remove the byte '0' after the mode");
        System.out.println("mode 3 : Modify the string mode");
        System.out.println("mode 4 : Append more data to the packet");
        System.out.println("mode 5 : Shrink the packet");
        System.out.println("mode 6 : Remove File name from the packet");
        System.out.println("mode 7 : Change the port number");
        System.out.println("mode 8 : Lose the packet");
        System.out.println("mode 9 : Delay the packet");
        System.out.println("mode 10 : Duplicate the packet");
        System.out.println("mode 18 : Change Block Number");
        
    }
    
    synchronized public void incrementThreadCount() {
        totalthreadCount++;
        
    }
    
    synchronized public void decrementThreadCount() {
        totalthreadCount--;
        
        if (totalthreadCount <= 0) {
            notifyAll();
            
        }
    }
    
    synchronized public int getThreadCount() {
        return totalthreadCount;
        
    }
    
    public void stop() {
        
        requestReceived.getSocket().close();
        
        while (getThreadCount() > 0) {
            try {
                wait();
                
            } catch (InterruptedException e) {
                System.out.println("Failed to stop properly. Process was interrupted. ");
                
                System.exit(1);
            }
        }
        
        System.out.println("Error simulator is closed now.");
        System.out.println();
        System.exit(0);
    }
    
    
    private class RequestReceivedThread extends Thread {
        
        private DatagramSocket socket;
        
        public RequestReceivedThread() {
            
            try {
                socket = new DatagramSocket(clientPort);
                
            } catch (SocketException e) {
                System.out.println("Count not properly bind to the port: " + clientPort);
                System.exit(1);
                
            }
        }
        
        public void run() {
            try {
                
                incrementThreadCount();
                
                while (!socket.isClosed()) {
                    DatagramPacket dataGramPacket = Packet.creatingDatagramForReceiving();
                    socket.receive(dataGramPacket);
                    
                    if (commandError == ErrCommands.ERROR_LOSE_PACKET && packetType == PacketType.REQUEST) {
                        socket.receive(dataGramPacket);
                        
                    }
                    
                    changed = false;
                    new ForwardThread(dataGramPacket).start();
                    
                }
                
            } catch (IOException e) {   }
            
            decrementThreadCount();
            
        }
        
        public DatagramSocket getSocket() {
            return socket;
        }
    }
    
    private class ForwardThread extends Thread {
        
        
        private int timeoutMs = 15000;
        private InetAddress clientAddress;
        private int clientPort, serverPort;
        private DatagramPacket requestPacket;
        private DatagramSocket datagramSocket;
        
        ForwardThread(DatagramPacket requestPacket) {
            this.requestPacket = requestPacket;
            Logging.printingReceivePacketDetails(this.requestPacket);
        }
        
        public void run() {
            try {
                
                incrementThreadCount();
                datagramSocket = new DatagramSocket();
                datagramSocket.setSoTimeout(timeoutMs);
                clientAddress = requestPacket.getAddress();
                clientPort = requestPacket.getPort();
                serverPort = 6900;
                
                System.out.println("Sending request to server ");
                
                DatagramPacket datagramPacket = new DatagramPacket(requestPacket.getData(), requestPacket.getLength(), serverAddress, serverPort);
                
                DatagramPacket modPacket;
                if (commandError == ErrCommands.ERROR_CHANGE_OPCODE && packetType == PacketType.REQUEST) {
                    modPacket = changeOpCode(datagramPacket); 
                    datagramSocket.send(modPacket);
                } else if (commandError == ErrCommands.ERROR_REMOVE_FILENAME_DELIMITER) {
                    modPacket = modifyTheFileNameTrailingByte(datagramPacket);
                    datagramSocket.send(modPacket);
                } else if (commandError == ErrCommands.ERROR_REMOVE_FILENAME) {
                    modPacket = removeTheFileName(datagramPacket);
                    datagramSocket.send(modPacket);
                } else if (commandError == ErrCommands.ERROR_REMOVE_MODE_DELIMITER) {
                    modPacket = removingModeTrailingByte(datagramPacket);
                    datagramSocket.send(modPacket);
                } else if (commandError == ErrCommands.ERROR_MODIFY_MODE) {
                    modPacket = modifyTheMode(datagramPacket);
                    datagramSocket.send(modPacket);
                } else {
                    modPacket = datagramPacket;
                    datagramSocket.send(datagramPacket);
                }  
                
                if (commandError != ErrCommands.NORMAL) {
                    System.out.println("Original Unmodified Packet");
                    Logging.printingSendPacketDetails(datagramPacket);
                    System.out.println("Sent Modified packet to server");
                    Logging.printingSendPacketDetails(modPacket);
                }
                
                System.out.println("Receiving packet from server");
                datagramPacket = Packet.creatingDatagramForReceiving();
                datagramSocket.receive(datagramPacket);
                Logging.printingReceivePacketDetails(datagramPacket);
                serverPort = datagramPacket.getPort();
                
                while (true) {
                    
                    System.out.println("Forwarding packet to client");
                    datagramPacket = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(),clientAddress, clientPort);
                    
                    if (commandError == ErrCommands.ERROR_APPEND_DATA && Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA && ((PacketDATA) Packet .creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(appendingData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_SHRINK_DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(shrinkingTheData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_SHRINK_ACK&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(shrinkingTheData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_APPEND_ACK&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(appendAcknowlegdmentPacket(datagramPacket));
                    } else if (commandError == ErrCommands.ERROR_INVALID_TID) {
                        
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            DatagramSocket invalidSocket = new DatagramSocket();
                            invalidSocket.setSoTimeout(timeoutMs);
                            invalidSocket.send((datagramPacket));
                            invalidSocket.close();
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            DatagramSocket invalidSocket = new DatagramSocket();
                            invalidSocket.setSoTimeout(timeoutMs);
                            invalidSocket.send((datagramPacket));
                            invalidSocket.close();
                        }
                        
                        Logging.printingSendPacketDetails(datagramPacket);
                        datagramSocket.send(datagramPacket);
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_OPCODE&& packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(changeOpCode(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_OPCODE&& packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(changeOpCode(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_BLOCK_NUM&& packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber&& !changed) {
                        datagramSocket.send(changeBlockNum(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_BLOCK_NUM&& packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber&& !changed) {
                        datagramSocket.send(changeBlockNum(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_LOSE_PACKET) {
                        
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            
                            System.out.println("Lost the data Packet received from Server");
                            System.out.println("Receiving data packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            serverPort = datagramPacket.getPort();
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            
                            System.out.println("Lost the ack Packet from the server");
                            System.out.println("Receiving the same data packet from client");
                            
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            // Send that back to server
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                        }
                        
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), clientAddress,clientPort);
                        
                        datagramSocket.send(sendDp);
                        
                    } else if (commandError == ErrCommands.ERROR_DELAY_PACKET) {
                        
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            System.out.println("Delay the data Packet from the server");
                            System.out.println("Waiting to get  Data packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            DatagramPacket dp2 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            System.out.println("Forwarding data packet to client");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Waiting to get  ack packet from client");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            System.out.println("Forwarding ack packet to server");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Forwarding delayed data packet to client");
                            datagramPacket = new DatagramPacket(dp2.getData(),dp2.getLength(), clientAddress, clientPort);
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            System.out.println("Delay the ACKNOWLEGMENT Packet from server");
                            System.out.println("Receiving the same data packet from client");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Waiting to get  ack packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                        }
                        
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), clientAddress,clientPort);
                        datagramSocket.send(sendDp);
                        
                    } else if (commandError == ErrCommands.ERROR_DUPLICATE_PACKET) {
                        
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            System.out.println("Duplicate the Packet");
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            System.out.println("Forwarding data packet to client");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Waiting to get  ack packet from Client ");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            
                            System.out.println("Forwarding ack packet to server");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Forwarding the duplicate data packet to cleint");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            System.out.println("Duplicate the Packet");
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            System.out.println("Forwarding ack packet to client");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Waiting to get data packet from Client ");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            
                            System.out.println("Forwarding data packet to server");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Forwarding duplicate ack packet to cleint");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), clientAddress, clientPort);
                            
                        }
                        
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), clientAddress,clientPort);
                        datagramSocket.send(sendDp);
                        
                    } else {
                        datagramSocket.send(datagramPacket);
                        
                    }
                    
                    
                    System.out.println("Waiting to get packet from client");
                    datagramPacket = Packet.creatingDatagramForReceiving();
                    datagramSocket.receive(datagramPacket);
                    
                    
                    System.out.println("Forwarding packet to server");
                    datagramPacket = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(),serverAddress, serverPort);
                    
                    if (commandError == ErrCommands.ERROR_APPEND_DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(appendingData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_SHRINK_DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(shrinkingTheData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_SHRINK_ACK && Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(shrinkingTheData(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_APPEND_ACK && Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(appendAcknowlegdmentPacket(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_INVALID_TID) {
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            DatagramSocket invalidSocket = new DatagramSocket();
                            invalidSocket.setSoTimeout(timeoutMs);
                            invalidSocket.send((datagramPacket));
                            invalidSocket.close();
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            DatagramSocket invalidSocket = new DatagramSocket();
                            invalidSocket.setSoTimeout(timeoutMs);
                            invalidSocket.send((datagramPacket));
                            invalidSocket.close();
                        }
                        datagramSocket.send(datagramPacket);
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_OPCODE&& packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                        datagramSocket.send(changeOpCode(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_OPCODE&& packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                        datagramSocket.send(changeOpCode(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_BLOCK_NUM&& packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber&& !changed) {
                        datagramSocket.send(changeBlockNum(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_CHANGE_BLOCK_NUM&& packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber&& !changed) {
                        datagramSocket.send(changeBlockNum(datagramPacket));
                        
                    } else if (commandError == ErrCommands.ERROR_LOSE_PACKET) {
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            System.out.println("Lose the Packet");
                            
                            System.out.println("Lost the Packet");
                            System.out.println("Receiving data packet from client");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            System.out.println("Lose the Packet");
                            
                            System.out.println("Receiving the same data packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                        }
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), serverAddress,serverPort);
                        datagramSocket.send(sendDp);
                        
                    } else if (commandError == ErrCommands.ERROR_DELAY_PACKET) {
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            System.out.println("Delay the Packet");
                            System.out.println("Waiting to get data packet from client");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            DatagramPacket dp2 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            
                            System.out.println("Forwarding data packet to server");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Waiting to get ack packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            
                            System.out.println("Forwarding ack packet to client");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), clientAddress, clientPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Forwarding packet to server");
                            datagramPacket = new DatagramPacket(dp2.getData(),dp2.getLength(), serverAddress, serverPort);
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            
                            System.out.println("Delay the ACKNOWLEGMENT Packet from client");
                            System.out.println("Receiving the same data packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            System.out.println("Waiting to get ack packet from client");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                        }
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), serverAddress,serverPort);
                        datagramSocket.send(sendDp);
                        
                    } else if (commandError == ErrCommands.ERROR_DUPLICATE_PACKET) {
                        if (packetType == PacketType.DATA&& Packet.creatingFromDatagram(datagramPacket) instanceof PacketDATA&& ((PacketDATA) Packet.creatingFromDatagram(datagramPacket)).getBlockNumbers() == blockNumber) {
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            System.out.println("Duplicate the Packet");
                            System.out.println("Forwarding data packet to server");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Waiting to get ack packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            System.out.println("Forwarding ack packet to client");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), clientAddress, clientPort);
                            
                            System.out.println("Forwarding duplicate data packet to server");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                            
                        } else if (packetType == PacketType.ACKNOWLEGMENT&& Packet.creatingFromDatagram(datagramPacket) instanceof AcknowlegementPacket&& ((AcknowlegementPacket) Packet.creatingFromDatagram(datagramPacket)).getackBlockNumber() == blockNumber) {
                            DatagramPacket dp1 = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength());
                            System.out.println("Duplicate the Packet");
                            System.out.println("Forwarding ack packet to server");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                            datagramSocket.send(datagramPacket);
                            
                            
                            System.out.println("Waiting to get data packet from server");
                            datagramPacket = Packet.creatingDatagramForReceiving();
                            datagramSocket.receive(datagramPacket);
                            
                            
                            System.out.println("Forwarding data packet to client");
                            datagramPacket = new DatagramPacket(datagramPacket.getData(),datagramPacket.getLength(), clientAddress, clientPort);
                            
                            
                            System.out.println("Forwarding duplicate ack packet to server");
                            datagramPacket = new DatagramPacket(dp1.getData(),dp1.getLength(), serverAddress, serverPort);
                        }
                        DatagramPacket sendDp = new DatagramPacket(datagramPacket.getData(), datagramPacket.getLength(), serverAddress,serverPort);
                        datagramSocket.send(sendDp);
                    } else {
                        datagramSocket.send(datagramPacket);
                    }
                    
                    
                    System.out.println("Waiting to get packet from server");
                    datagramPacket = Packet.creatingDatagramForReceiving();
                    datagramSocket.receive(datagramPacket);
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Socket timeout: closing thread. (Transfer may have simply been finished)");
            } catch (IOException e) {
                System.out.println("Socket error: closing thread.");
            }
            
            decrementThreadCount();
        }
    }
    
    private DatagramPacket changeOpCode(DatagramPacket packet) {
        byte[] data = packet.getData();
        data[0] = 1;
        data[1] = 8;
        return new DatagramPacket(data, data.length, packet.getAddress(),packet.getPort());
        
    }
    
    private DatagramPacket removingModeTrailingByte(DatagramPacket packet) {
        return new DatagramPacket(packet.getData(), packet.getLength() - 1,packet.getAddress(), packet.getPort());
        
    }
    
    private DatagramPacket modifyTheFileNameTrailingByte(DatagramPacket packet) {
        byte[] data = packet.getData();
        
        int i = 1;
        
        while (data[++i] != 0 && i < packet.getLength());
        data[i] = 65;
        return new DatagramPacket(data, data.length, packet.getAddress(),packet.getPort());
    }
    
    private DatagramPacket modifyTheMode(DatagramPacket packet) {
        byte[] data = packet.getData();
        
        int i = 1;
        while (data[++i] != 0 && i < packet.getLength());
        
        byte[] invalidMode = ("abc").getBytes();
        
        i++;
        
        for (int index = 0; index < invalidMode.length; index++)
            data[i + index] = invalidMode[index];
        
        return new DatagramPacket(data, data.length, packet.getAddress(),packet.getPort());
    }
    
    
    private DatagramPacket removeTheFileName(DatagramPacket packet) {
        byte[] data = packet.getData();
        
        int i = 1;
        while (data[++i] != 0 && i < packet.getLength());
        byte[] modData = new byte[data.length - (i - 2)];
        
        System.arraycopy(data, 0, modData, 0, 2);
        
        System.arraycopy(data, i, modData, 2, modData.length - 2);
        return new DatagramPacket(modData, modData.length, packet.getAddress(),packet.getPort());
    }
    
    private DatagramPacket appendingData(DatagramPacket packet) {
        byte[] data = packet.getData();
        
        for (int i = 516; i < data.length; i++)
            data[i] = (byte) 0xFF;
        
        return new DatagramPacket(data, data.length, packet.getAddress(),packet.getPort());
    }
    
    private DatagramPacket shrinkingTheData(DatagramPacket packet) {
        byte[] data = packet.getData();
        byte[] modData = new byte[2];
        modData[0] = data[0];
        modData[1] = data[1];
        return new DatagramPacket(modData, modData.length, packet.getAddress(),packet.getPort());
    }
    
    
    private DatagramPacket appendAcknowlegdmentPacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        byte[] modData = new byte[data.length + 2];
        System.arraycopy(data, 0, modData, 0, data.length);
        modData[data.length] = data[0];
        modData[data.length + 1] = data[1];
        return new DatagramPacket(modData, modData.length, packet.getAddress(),packet.getPort());
    }
    
    private DatagramPacket changeBlockNum(DatagramPacket packet) {
        byte[] data = packet.getData();
        data[2] = (byte) ((newBlocknum >> 8) & 0xFF);
        data[3] = (byte) (newBlocknum & 0xFF);
        
        changed = true;
        
        return new DatagramPacket(data, packet.getLength(),packet.getAddress(), packet.getPort());
    }
    
    private static int chooseTheBlockNum(String message) {
        boolean validBlock = false;
        @SuppressWarnings("resource")
        Scanner scanner2 = new Scanner(System.in);
        int blockNumber = 0;
        
        while (!validBlock) {
            System.out.print(message);
            if (scanner2.hasNextInt()) {
                blockNumber = scanner2.nextInt();
                
                if (blockNumber > 0 ) {
                    validBlock = true;
                    
                } else {
                    System.out.println("This is not a valid command!!");
                }
            } else {
                scanner2.nextLine();
                System.out.println("This is not a valid command!!");
                
            }
        }
        
        return blockNumber;
    }
}