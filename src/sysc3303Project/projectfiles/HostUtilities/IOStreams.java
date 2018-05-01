/*
 @author 100989743 abdulrahim kaddoura
 @author 100835968 Syed Arsal Abbas
 @author 101007487 Qudus Agbalaya
 */

package sysc3303Project.projectfiles.HostUtilities;

import java.util.Scanner;

public class IOStreams {
    
    private static Scanner scanner = new Scanner(System.in);
    
    public static void print(String string) {
        System.out.println(string);
    }
    
    public static String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    public static boolean isInt(String string) {
        
        try {
            Integer.parseInt(string);
            return true;
            
        } catch (NumberFormatException e) {
            return false;
            
        }
    }
    
    public static int stringToint( String string ){
        
        if( isInt( string ) ) 
            
            return Integer.parseInt( string );
        
        else 
            
            return -1;
    }
    
    public static void printSimulatorErrorMsg( String string ){
        print( "\nSIMULATED ERROR MESSAGE: " + string );
    }
    
    
    public static void error(String string) { 
        
        System.err.println("\nERROR: " + string);
        
    }
    
    
    public static void printHostProcess(String string){ 
        
        System.out.println("\nHost is loading: " + string); 
        
    }
}