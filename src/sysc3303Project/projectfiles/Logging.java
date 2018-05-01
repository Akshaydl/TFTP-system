/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class Logging {
    
    public static void testing(String message, boolean vb) {
        if (vb) {
            System.out.println("Thread #" + Thread.currentThread().getId() + "        " + message);
        }  
    }
    
    static public void printingSendPacketDetails(Packet.Type t, DatagramPacket dp) {
        
        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        System.out.print(
                         "\n**************************Sent Packet Information**************************" +
                         "\nThread #: " + Thread.currentThread().getId() +
                         "\nPacket Type: " + t +
                         "\nPacket Destination: " + dp.getAddress() +
                         "\nDestination Port: " + dp.getPort() +
                         "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                         "\nPacket Data(String): " + data +
                         "\nPacket Offset: " + dp.getOffset() +
                         "\nSocket Address: " + dp.getSocketAddress() +
                         "\n******************************************************************************\n");
    }
    
    static public void printingSendPacketDetails(DatagramPacket dp)
    {
        
        String data = null;
        try {
            data = new String(dp.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        System.out.print(
                         "\n**************************Sent Packet Information**************************" +
                         "\nThread #: " + Thread.currentThread().getId() +
                         "\nPacket Destination: " + dp.getAddress() +
                         "\nDestination Port: " + dp.getPort() +
                         "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                         "\nPacket Data(String): " + data +
                         "\nPacket Offset: " + dp.getOffset() +
                         "\nSocket Address: " + dp.getSocketAddress() +
                         "\n******************************************************************************\n");
    }
    
    static public void printingReceivePacketDetails(Packet.Type t, DatagramPacket dp) {
        String data = null;
        
        try {
            
            data = new String(dp.getData(), "UTF-8");
            
        } catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
            
        }
        
        System.out.print(
                         "\n**************************Received Packet Information**************************" +
                         "\nThread #: " + Thread.currentThread().getId() +
                         "\nPacket Type: " + t +
                         "\nPacket Source: " + dp.getAddress() +
                         "\nSource Port: " + dp.getPort() +
                         "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                         "\nPacket Data(String): " + data +
                         "\nPacket Offset: " + dp.getOffset() +
                         "\n******************************************************************************\n");
        
    }
    
    static public void printingReceivePacketDetails(DatagramPacket dp) {
        
        String data = null;
        
        try {
            data = new String(dp.getData(), "UTF-8");
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            
        }
        
        
        System.out.print(
                         "\n**************************Received Packet Information**************************" +
                         "\nThread #: " + Thread.currentThread().getId() +
                         "\nPacket Source: " + dp.getAddress() +
                         "\nSource Port: " + dp.getPort() +
                         "\nPacket Data(Byte): " + Arrays.toString(dp.getData()) +
                         "\nPacket Data(String): " + data +
                         "\nPacket Offset: " + dp.getOffset() +
                         "\n******************************************************************************\n");
        
    }
}
