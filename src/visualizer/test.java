/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.err;
import java.util.Scanner;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *
 * @author USER
 */
 class test {
     {
         System.out.println("block");
     }
     
     
     static{
         System.out.println("static");
     }
   
     public static void main(String[] args) throws Exception {
        
        test t=new test();
        System.out.println("Hello");
        
        
        
        
        String file1ToCompile = "C:\\Users\\USER\\Documents\\NetBeansProjects\\Java2Uml\\src\\visualizer" + java.io.File.separator + "JDI2JSON.java";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        FileOutputStream errorStream = new FileOutputStream("C:\\Users\\USER\\Documents\\NetBeansProjects\\Java2Uml\\src\\visualizer\\Errors.txt");
        
        int compilationResult = compiler.run (null, null, errorStream, file1ToCompile);
        errorStream.flush();
        errorStream.close();
        
        File f=new File("C:\\Users\\USER\\Documents\\NetBeansProjects\\Java2Uml\\src\\visualizer\\Errors.txt");
        FileInputStream  fi=new FileInputStream(f);
        int content;
        while ((content= fi.read()) != -1) {
				// convert to char and display it
				System.out.print((char) content);
			}
        
        
        if(compilationResult == 0){
            System.out.println("Compilation is successful");
          }else{
               System.out.println("Compilation Failed");
               }       
    }
    
}
