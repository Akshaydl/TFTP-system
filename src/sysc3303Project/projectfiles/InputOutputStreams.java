/*
 @author Syed Arsal Abbas
 */

package sysc3303Project.projectfiles;

import java.util.Scanner;

public class InputOutputStreams {
    
    private static Scanner scanner = new Scanner(System.in);
    
    
    public static final int MAX_FILE_SIZE = 33553920;
    
    public static void print(String string) {
        
        System.out.println(string);
    }
    
    public static String inputString(String prompt) {
        
        System.out.print(prompt);
        
        return scanner.nextLine();
    }
    
    public static String input() {
        System.out.print("\n");
        
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
    
    
    public static boolean isInt(String string, int output) {
        
        try {
            
            output = Integer.parseInt(string);
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
    
    public static void printSimulatorErrMsg( String string ){
        System.out.print( "\nSIMULATED ERROR MESSAGE: " + string );
        
    }
}