/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.SourceStringReader;

/**
 *
 * @author USER
 */
public class Image_Generator {
    
    String class_diagram,temp;

    public Image_Generator(String s) {
        this.class_diagram=s;
    }
    
    public void genretate() throws IOException
    {
        SourceStringReader reader = new SourceStringReader(class_diagram);
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
	         temp= reader.generateImage(os);
		os.close();
                
                OutputStream o=new FileOutputStream("b.png");
                reader.generateImage(o);
                reader.generateDiagramDescription(o);
                o.flush();
                o.close();
    }
    
    
}
