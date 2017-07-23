package visualizer;
import com.sun.imageio.plugins.jpeg.JPEG;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import net.sourceforge.plantuml.SourceStringReader;

public class Converter_new {
   
   String usercode,planttext;
   static HashMap<String, String> sequencePlantUML = new HashMap<String, String>();
   static HashMap<String, String> individualClassDiagramList = new HashMap<>();

  public Converter_new( String data) throws Exception {	   
		
         this.usercode = data;	                   
          planttext = sourceToPlantText(usercode);
          System.out.println(planttext);
          Image_Generator im=new Image_Generator(planttext);
          im.genretate();
       
   }
   static String sourceToPlantText(String userCode){
       try{
           String END_PLANTUML = "@enduml";
           String START_PLANTUML = "@startuml";
           String classKey = "class ";
           String interfaceKey = "interface ";
           String extendsKey = " --|> ";
           String dependencyKey = " --> ";
           Set<String> parents = new TreeSet<String>();
           String initialCode = "\'hide icon at class name \nhide circle\nskinparam classAttributeIconSize 0"
           + "\nskinparam stereotypeCBackgroundColor White\n\'parameters class\nskinparam class {\n"
           + "BackgroundColor White \nArrowColor Blue \nBorderColor Blue\n}";
           String createObjectPattern = "(new)(\\s+)([^\\s]+)(\\s*)(\\(.*?\\))(;)";
           String classPattern = "[\\w.]* *(?:public|final|) *(abstract|) *(?:public|final|) "
           + "*(class|interface|enum|@interface) +([\\w. <>,]*?)(?: +extends +([\\w. <>,]*?)|)(?: +implements +([\\w. <>,]*?)|) *\\{";
           String privateVariablePattern = "private\\s+(\\w*)\\s+([\\w, ]*)\\s*;";
           String publicVariablePattern = "public\\s+(\\w*)\\s+([\\w, ]*)\\s*;";
           String protectedVariablePattern = "protected\\s+(\\w*)\\s+([\\w, ]*)\\s*;";
			String defaultVariablePattern = "default\\s+(\\w*)\\s+([\\w, ]*)\\s*;";
			String withoutModifierVariablePattern = "\\s+(\\w*)\\s+([\\w, ]*)\\s*;";
           //String methodPattern = "([\\w\\<\\>\\[\\]]+)+\\s+([\\w\\<\\>\\[\\]]+)+\\s+(\\w+) *\\([^\\)]*\\) *(\\{)";
           String methodPattern = "(public|protected|private|static|abstract|\\s) +([\\w\\<\\>\\[\\]]+)+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])";
			String constructorPattern = "([\\w\\<\\>\\[\\]]+)+\\s+(\\w+) *\\([^\\)]*\\) *(\\{)";
           String methodArgumentsPattern = "\\(([^)]+)\\)";
           Pattern pattern = Pattern.compile(classPattern);
           userCode = commentRemover(userCode);
           Matcher matcher = pattern.matcher(userCode);
           StringBuffer plantUMLText = new StringBuffer();
           plantUMLText.append(START_PLANTUML);
           plantUMLText.append("\n");
           //plantUMLText.append(initialCode);
           plantUMLText.append("\n");
           
           
           StringBuffer plantUMLTextForSequence = new StringBuffer();
           plantUMLTextForSequence.append(START_PLANTUML);
           plantUMLTextForSequence.append("\n");
           plantUMLTextForSequence.append("autonumber");
           plantUMLTextForSequence.append("\n");
           
           while(matcher.find()) {
               plantUMLText.append(classKey);
               String mainClassName = matcher.group(3);
               plantUMLText.append(mainClassName);
               plantUMLText.append("\n");
               
               //Checking whether a class extends another class
               if(matcher.group(4) != null){
                   parents.add(matcher.group(4));
                   plantUMLText.append(matcher.group(4)+" <|-down- "+mainClassName);
                   plantUMLText.append("\n");
               }
               
               //Checking for interfaces
               Set<String> interfaces = parentInterfacesChecking(matcher.group(5));
               for(String s : interfaces){
                   plantUMLText.append(interfaceKey+s);
                   plantUMLText.append("\n");
                   plantUMLText.append(classKey+mainClassName+extendsKey+s);
                   plantUMLText.append("\n");
               }
           }
           
           String[] classes = userCode.split(classKey);
           for(int i = 1 ; i <classes.length ; i++){
               String classBody = classKey + " "+classes[i].trim();
               Pattern p = Pattern.compile(classPattern);
               Matcher m = p.matcher(classBody);
               if(m.find()){
                   String className = m.group(3);
                   if(className != null){
                       Pattern pa = Pattern.compile(createObjectPattern,Pattern.MULTILINE);
                       Matcher ma = pa.matcher(classBody);
                       Set<String> dependencies = new HashSet<>();
                       while (ma.find()){
                           String usedClassName=ma.group(3);
                           if(usedClassName != null && dependencies.add(usedClassName)){
                               plantUMLText.append(classKey+className+dependencyKey+usedClassName);
                               plantUMLText.append("\n");
                               
                               plantUMLTextForSequence.append(className + "-->" + usedClassName + ":<<create>");
                               plantUMLTextForSequence.append("\n");
                           }
                       }
                   }
               }
           }
           for(int i = 1 ; i <classes.length ; i++){
               String classBody = classKey + " "+classes[i].trim();
               Pattern p = Pattern.compile(classPattern);
               Matcher m = p.matcher(classBody);
               StringBuffer individualPlantUML = new StringBuffer();
               if(m.find()){
                   String className = m.group(3);
                   
                   //Creating class
                   classBody = classBody.replace("static", "");
                   classBody = classBody.replace("final", "");
                   plantUMLText.append(classKey +className+ "{");
                   individualPlantUML.append(START_PLANTUML);
                   individualPlantUML.append("\n");
                   individualPlantUML.append(initialCode);
                   individualPlantUML.append("\n");
                   individualPlantUML.append(classKey +className+ "{");
                   Pattern privateVariablesPattern = Pattern.compile(privateVariablePattern);
                   Matcher privateVariableMatcher = privateVariablesPattern.matcher(classBody);
                   
                   //Adding private Variable
                   while (privateVariableMatcher.find()) {
                       if(privateVariableMatcher.group(2).contains(",")){
                           String[] variables = privateVariableMatcher.group(2).split(",");
                           for(int k=0; k<variables.length; k++){
                               plantUMLText.append("\n");
                               plantUMLText.append("-"+variables[k]+":"+privateVariableMatcher.group(1));
                               individualPlantUML.append("\n");
                               individualPlantUML.append("-"+variables[k]+":"+privateVariableMatcher.group(1));  
                           }
                       }else{
                           plantUMLText.append("\n");
                           plantUMLText.append("-"+privateVariableMatcher.group(2)+":"+privateVariableMatcher.group(1));
                           individualPlantUML.append("\n");
                           individualPlantUML.append("-"+privateVariableMatcher.group(2)+":"+privateVariableMatcher.group(1));
                       }
                   }
                   
                   //Adding public Variable
                   Pattern publicVariablesPattern = Pattern.compile(publicVariablePattern);
                   Matcher publicVariableMatcher = publicVariablesPattern.matcher(classBody);
                   
                   while (publicVariableMatcher.find()) {
                       if(publicVariableMatcher.group(2).contains(",")){
                           String[] variables = publicVariableMatcher.group(2).split(",");
                           for(int k=0; k<variables.length; k++){
                               plantUMLText.append("\n");
                               plantUMLText.append("+"+variables[k]+":"+publicVariableMatcher.group(1));
                               individualPlantUML.append("\n");
                               individualPlantUML.append("+"+variables[k]+":"+publicVariableMatcher.group(1));
                           }
                       }else{
                           plantUMLText.append("\n");
                           plantUMLText.append("+"+publicVariableMatcher.group(2)+":"+publicVariableMatcher.group(1));
                           individualPlantUML.append("\n");
                           individualPlantUML.append("+"+publicVariableMatcher.group(2)+":"+publicVariableMatcher.group(1));
                       }
                       
                   }
                   
                   //Adding protected Variable
                   Pattern protectedVariablesPattern = Pattern.compile(protectedVariablePattern);
                   Matcher protectedVariableMatcher = protectedVariablesPattern.matcher(classBody);
                   //Adding private Variable
                   while (protectedVariableMatcher.find()) {
                       if(protectedVariableMatcher.group(2).contains(",")){
                           String[] variables = protectedVariableMatcher.group(2).split(",");
                           for(int k=0; k<variables.length; k++){
                               plantUMLText.append("\n");
                               plantUMLText.append("#"+variables[k]+":"+protectedVariableMatcher.group(1));
                               individualPlantUML.append("\n");
                               individualPlantUML.append("#"+variables[k]+":"+protectedVariableMatcher.group(1));
                           }
                       }else{
                           plantUMLText.append("\n");
                           plantUMLText.append("#"+protectedVariableMatcher.group(2)+":"+protectedVariableMatcher.group(1));
                           individualPlantUML.append("\n");
                           individualPlantUML.append("#"+protectedVariableMatcher.group(2)+":"+protectedVariableMatcher.group(1));
                       }
                   }
                   
					//Adding default Variable
                   Pattern defaultVariablesPattern = Pattern.compile(defaultVariablePattern);
                   Matcher defaultVariableMatcher = defaultVariablesPattern.matcher(classBody);
                   //Adding default Variable
                   while (defaultVariableMatcher.find()) {
                       if(defaultVariableMatcher.group(2).contains(",")){
                           String[] variables = defaultVariableMatcher.group(2).split(",");
                           for(int k=0; k<variables.length; k++){
                               plantUMLText.append("\n");
                               plantUMLText.append("~"+variables[k]+":"+defaultVariableMatcher.group(1));
                               individualPlantUML.append("\n");
                               individualPlantUML.append("~"+variables[k]+":"+defaultVariableMatcher.group(1));
                           }
                       }else{
                           plantUMLText.append("\n");
                           plantUMLText.append("~"+defaultVariableMatcher.group(2)+":"+defaultVariableMatcher.group(1));
                           individualPlantUML.append("\n");
                           individualPlantUML.append("~"+defaultVariableMatcher.group(2)+":"+defaultVariableMatcher.group(1));
                       }
                   }

                   //Adding without modifier Variable
                   Pattern withoutModifierVariablesPattern = Pattern.compile(withoutModifierVariablePattern);
                   Matcher withoutModifierVariableMatcher = withoutModifierVariablesPattern.matcher(classBody);

                   while (withoutModifierVariableMatcher.find()) {
                       if(withoutModifierVariableMatcher.group(2).contains(",")){
                           String[] variables = withoutModifierVariableMatcher.group(2).split(",");
                           for(int k=0; k<variables.length; k++){
                               if(!withoutModifierVariableMatcher.group(1).contains("public") && !withoutModifierVariableMatcher.group(1).contains("private") && !withoutModifierVariableMatcher.group(1).contains("return")){
                                   plantUMLText.append("\n");
                                   plantUMLText.append("~"+variables[k]+":"+withoutModifierVariableMatcher.group(1));
                                   individualPlantUML.append("\n");
                                   individualPlantUML.append("~"+variables[k]+":"+withoutModifierVariableMatcher.group(1));
                               }
                           }
                       }else{
                           if(!withoutModifierVariableMatcher.group(1).contains("public") && !withoutModifierVariableMatcher.group(1).contains("private") && !withoutModifierVariableMatcher.group(1).contains("return")){
                               plantUMLText.append("\n");
                               plantUMLText.append("~"+withoutModifierVariableMatcher.group(2)+":"+withoutModifierVariableMatcher.group(1));
                               individualPlantUML.append("\n");
                               individualPlantUML.append("~"+withoutModifierVariableMatcher.group(2)+":"+withoutModifierVariableMatcher.group(1));
                           }
                       }
                   }
					
                   //Adding private methods
                   Pattern constructorsPattern = Pattern.compile(constructorPattern);
                   Matcher constructorsMatcher = constructorsPattern.matcher(classBody);
					while (constructorsMatcher.find()) {
                       plantUMLText.append("\n");
                       individualPlantUML.append("\n");
                       String methodDeclaration = constructorsMatcher.group(0);
                       
                       //Finding method arguments
                       Pattern methodsArgumentPattern = Pattern.compile(methodArgumentsPattern);
                       Matcher methodArgumentsMatcher = methodsArgumentPattern.matcher(methodDeclaration);
							
                       if(methodArgumentsMatcher.find()){
                           //No need to add public for constructor since it is not a return type
                           if(constructorsMatcher.group(1).contains("public")){
                              
                               plantUMLText.append("+"+constructorsMatcher.group(2)+"("+methodArgumentsMatcher.group(1)+")");
                               individualPlantUML.append("+"+constructorsMatcher.group(2)+"("+methodArgumentsMatcher.group(1)+")");
                               
                               plantUMLTextForSequence.append(className+ "--->" + className + ":" + constructorsMatcher.group(2) + "(" + methodArgumentsMatcher.group(1) + ")");
                               plantUMLTextForSequence.append("\n");
                           }
                           
                       }else{
                           //No need to add public for constructor since it is not a return type
                           if(constructorsMatcher.group(1).contains("public")){
                               plantUMLText.append("+"+constructorsMatcher.group(2)+"()");
                               individualPlantUML.append("+"+constructorsMatcher.group(2)+"()");
                               
                               plantUMLTextForSequence.append(className+ "--->" + className + ":" + constructorsMatcher.group(2) + "()");
                                plantUMLTextForSequence.append("\n");
                           }
                            
                       }
						
                   }
					
					
					Pattern methodsPattern = Pattern.compile(methodPattern);
                   Matcher methodsMatcher = methodsPattern.matcher(classBody);
                   while (methodsMatcher.find()) {
                       plantUMLText.append("\n");
                       individualPlantUML.append("\n");
                       String methodDeclaration = methodsMatcher.group(0);

                       //Finding method arguments
                       Pattern methodsArgumentPattern = Pattern.compile(methodArgumentsPattern);
                       Matcher methodArgumentsMatcher = methodsArgumentPattern.matcher(methodDeclaration);
							
                       if(methodArgumentsMatcher.find()){
                           //No need to add public for constructor since it is not a return type
                           if(methodsMatcher.group(1).contains("public") && methodsMatcher.group(4).contains("{")){
                               plantUMLText.append("+"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                               individualPlantUML.append("+"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                           }else if(methodsMatcher.group(1).contains("private")){
                               plantUMLText.append("-"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                               individualPlantUML.append("-"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                           }else if(methodsMatcher.group(1).contains("protected")){
                               plantUMLText.append("#"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                               individualPlantUML.append("#"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                           }else if(!methodsMatcher.group(2).contains("public")){
                               plantUMLText.append("~"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                               individualPlantUML.append("~"+methodsMatcher.group(3)+"("+methodArgumentsMatcher.group(1)+"):"+methodsMatcher.group(2));
                           }
                           
                           plantUMLTextForSequence.append(className+ "--->" + className + ":" + methodsMatcher.group(3) + "(" + methodArgumentsMatcher.group(1)+ "):" + methodsMatcher.group(2));
                            plantUMLTextForSequence.append("\n");
                           
                       }else{
                           //No need to add public for constructor since it is not a return type
                           if(methodsMatcher.group(1).contains("public") && methodsMatcher.group(4).contains("{")){
                               plantUMLText.append("+"+methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                               individualPlantUML.append("+"+	methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                           }else if(methodsMatcher.group(1).contains("private")){
                               plantUMLText.append("-"+methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                               individualPlantUML.append("-"+	methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                           }else if(methodsMatcher.group(1).contains("protected")){
                               plantUMLText.append("#"+methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                               individualPlantUML.append("#"+	methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                           }else if(!methodsMatcher.group(2).contains("public")){
                               plantUMLText.append("~"+methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                               individualPlantUML.append("~"+	methodsMatcher.group(3)+"():"+methodsMatcher.group(2));
                           }
                           
                           plantUMLTextForSequence.append(className+ "--->" + className + ":" + methodsMatcher.group(3) + "():" +methodsMatcher.group(2));
                            plantUMLTextForSequence.append("\n");
                       }
                   }
                   
                   plantUMLText.append("\n");
                   plantUMLText.append("}");
                   plantUMLText.append("\n");
                   individualPlantUML.append("\n");
                   individualPlantUML.append("}");
                   individualPlantUML.append("\n");
                   individualPlantUML.append(END_PLANTUML);
                   individualClassDiagramList.put(className, individualPlantUML.toString());
               }
           }
           plantUMLText.append(END_PLANTUML);
           plantUMLTextForSequence.append(END_PLANTUML);
           sequencePlantUML.put("sequenceUMLKey",plantUMLTextForSequence.toString());
           //System.out.println(plantUMLText.toString());System.exit(0);
           return plantUMLText.toString();
       }catch(Exception e){
           e.printStackTrace();
           return "";
       }
   }
 
   private static String commentRemover(String javaSourceFileContent)
   {
       StringBuilder buffer = new StringBuilder();
       int cursor = 0;
       while (cursor < javaSourceFileContent.length()) {
           char currentCharacter = javaSourceFileContent.charAt(cursor);
           if ((currentCharacter == "\n".charAt(0)) || (currentCharacter == "\r".charAt(0)) || (currentCharacter == "\t".charAt(0)))
           {
               buffer.append(" ");
               cursor++;
           } else if (currentCharacter == "/".charAt(0)) {
               if (cursor + 1 < javaSourceFileContent.length()) {
                   char nextCharacter = javaSourceFileContent.charAt(cursor + 1);
                   if (nextCharacter == "/".charAt(0)) {
                       cursor = getComment(cursor + 2, javaSourceFileContent);
                   } else if (nextCharacter == "*".charAt(0)) {
                       buffer.append(" ");
                       cursor = getNextMultiLineIndex(cursor + 2, javaSourceFileContent);
                   } else {
                       buffer.append(currentCharacter);
                       cursor++;
                   }
               } else {
                   buffer.append(currentCharacter);
                   cursor++;
               }
           } else if (currentCharacter == "<".charAt(0)) {
               if (cursor + 1 < javaSourceFileContent.length()) {
                   cursor = getNextIndex(cursor + 1, javaSourceFileContent);
               } else {
                   buffer.append(currentCharacter);
                   cursor++;
               }
           } else if (currentCharacter == "'".charAt(0)) {
               if (cursor + 1 < javaSourceFileContent.length()) {
                   cursor = getEndLine(cursor + 1, javaSourceFileContent);
                   buffer.append("'");
                   buffer.append("'");
               } else {
                   buffer.append(currentCharacter);
                   cursor++;
               }
           } else if (currentCharacter == "\"".charAt(0)) {
               if (cursor + 1 < javaSourceFileContent.length()) {
                   cursor = getNextEndOfStringContent(cursor + 1, javaSourceFileContent);
                   buffer.append("\"");
                   buffer.append("\"");
               } else {
                   buffer.append(currentCharacter);
                   cursor++;
               }
           } else {
               buffer.append(currentCharacter);
               cursor++;
           }
       }
       
       return buffer.toString().trim();
   }
   
   private static int getNextEndOfStringContent(int beginningIndex, String str)
   {
       int index = beginningIndex;
       boolean found = false;
       
       while ((index < str.length()) && (!found)) {
           char currentCharacter = str.charAt(index);
           if (currentCharacter == "\\".charAt(0)) {
               char nextCharacter = str.charAt(index + 1);
               if ((nextCharacter == "\\".charAt(0)) || (nextCharacter == "\"".charAt(0))) {
                   index++;
               }
           } else if (currentCharacter == "\"".charAt(0)) {
               found = true;
           }
           index++;
       }
       
       if (!found) {
           index = beginningIndex;
       }
       
       return index;
   }
   
   
   private static int getEndLine(int beginningIndex, String str)
   {
       int index = beginningIndex;
       boolean found = false;
       
       while ((index < str.length()) && (!found)) {
           char currentCharacter = str.charAt(index);
           if (currentCharacter == "\\".charAt(0)) {
               char nextCharacter = str.charAt(index + 1);
               if ((nextCharacter == "\\".charAt(0)) || (nextCharacter == "'".charAt(0))) {
                   index++;
               }
           } else if (currentCharacter == "'".charAt(0)) {
               found = true;
           }
           index++;
       }
       
       if (!found) {
           index = beginningIndex;
       }
       
       return index;
   }
   
   private static int getNextIndex(int beginningIndex, String str)
   {
       int index = beginningIndex;
       int numberOfGenerics = 1;
       boolean stopSearch = false;
       
       while ((index < str.length()) && (numberOfGenerics != 0) && (!stopSearch)) {
           char currentCharacter = str.charAt(index);
           if (currentCharacter == "/".charAt(0)) {
               if (index + 1 < str.length()) {
                   char nextCharacter = str.charAt(index + 1);
                   if (nextCharacter == "/".charAt(0)) {
                       stopSearch = true;
                   } else if (nextCharacter == "*".charAt(0)) {
                       stopSearch = true;
                   } else {
                       index++;
                   }
               } else {
                   index++;
               }
           } else if (currentCharacter == "'".charAt(0)) {
               stopSearch = true;
           } else if (currentCharacter == "\"".charAt(0)) {
               stopSearch = true;
           } else if (currentCharacter == "@".charAt(0)) {
               stopSearch = true;
           } else if (currentCharacter == "<".charAt(0)) {
               numberOfGenerics++;
               index++;
           } else if (currentCharacter == ">".charAt(0)) {
               numberOfGenerics--;
               index++;
           } else {
               index++;
           }
       }
       
       if ((numberOfGenerics != 0) || (stopSearch)) {
           index = beginningIndex;
       }
       
       return index;
   }
   
   private static int getComment(int beginningIndex, String str)
   {
       int index = beginningIndex;
       boolean found = false;
       
       while ((index < str.length()) && (!found)) {
           char currentCharacter = str.charAt(index);
           if ((currentCharacter == "\n".charAt(0)) || (currentCharacter == "\r".charAt(0))) {
               index++;
               found = true;
           } else {
               index++;
           }
       }
       
       return index;
   }
   
   
   private static int getNextMultiLineIndex(int beginningIndex, String str)
   {
       int index = beginningIndex;
       boolean found = false;
       
       while ((index < str.length()) && (!found)) {
           char currentCharacter = str.charAt(index);
           if (currentCharacter == "*".charAt(0)) {
               if (index + 1 < str.length()) {
                   char nextCharacter = str.charAt(index + 1);
                   if (nextCharacter == "/".charAt(0)) {
                       index += 2;
                       found = true;
                   } else {
                       index++;
                   }
               } else {
                   index++;
               }
           } else {
               index++;
           }
       }
       
       if (!found) {
           index = beginningIndex;
       }
       
       return index;
   }
   
   private static Set<String> parentInterfacesChecking(String parentsString){
       Set<String> parents = new HashSet<String>();
       if(parentsString != null){
           StringTokenizer tokenizer = new StringTokenizer(parentsString, ",");
           while (tokenizer.hasMoreTokens()) {
               String interfaceName = tokenizer.nextToken().replace(" ", "");
               parents.add(interfaceName);
               //System.out.println(interfaceName);
           }
       }
       return parents;
   }  

   
   static JsonValue jsonInt(long l) {
       return Json.createArrayBuilder().add(l).build().getJsonNumber(0);
   }
   
   static JsonValue jsonReal(double d) {
       return Json.createArrayBuilder().add(d).build().getJsonNumber(0);
   }
   
   static JsonValue jsonString(String S) {
       return Json.createArrayBuilder().add(S).build().getJsonString(0);
   }
}
