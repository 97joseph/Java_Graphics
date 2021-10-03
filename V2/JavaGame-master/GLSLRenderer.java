import java.awt.Image;
import javax.swing.*;

import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.applet.Applet;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
class GLSLRenderer
{
   int vertShader=0;
   int fragShader=0;
   int program=0;
   float r=1,g=1,b =1,a=0;
   ArrayList<Integer> glInts = new ArrayList<Integer>();
   ArrayList<String> glIntsStrings = new ArrayList<String>();
   GLSLRenderer(String vertexPath, String fragmentPath)
   {
      try {
            vertShader = createShader(vertexPath,ARBVertexShader.GL_VERTEX_SHADER_ARB);
            fragShader = createShader(fragmentPath,ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
    	}
    	catch(Exception exc) {
    		exc.printStackTrace();
    		return;
    	}
    	finally {
    		if(vertShader == 0 || fragShader == 0)
    			return;
    	}
    	
    	program = ARBShaderObjects.glCreateProgramObjectARB();
    	
    	if(program == 0)
    		return;
        
        /*
        * if the vertex and fragment shaders setup sucessfully,
        * attach them to the shader program, link the sahder program
        * (into the GL context I suppose), and validate
        */
        ARBShaderObjects.glAttachObjectARB(program, vertShader);
        ARBShaderObjects.glAttachObjectARB(program, fragShader);
        
        ARBShaderObjects.glLinkProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println(getLogInfo(program));
            return;
        }
        
        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
        	System.err.println(getLogInfo(program));
        	return;
        }
        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));

    }
   public void setInt(String boolName, int x)
   {
      if(glIntsStrings.indexOf(boolName) == -1)
      {
         glIntsStrings.add(boolName);
         glInts.add(x);
      }
      else
      {
         glInts.set(glIntsStrings.indexOf(boolName),x);
      }
   }
   public void setColor(float r2,float g2, float b2)
   {
      r=r2;
      g=g2;
      b=b2;
   }
   public void setColor(float r2, float g2, float b2, float a2)
   {
      a=a2;
      setColor(r2,g2,b2);
   }
   public void drawLine(float x, float y, float sx, float sy)
   {
        //glEnable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); 
        GL20.glUniform4f(GL20.glGetUniformLocation(program, "inputColor"),r,g,b,a);
        ARBShaderObjects.glUseProgramObjectARB(program);
        float winx = ((float)World.ResolutionX/2);
        float winy = ((float)World.ResolutionY/2);
        sx=(sx-winx)/winx;
        sy=(sy-winy)/winy;
        x=(x-winx)/winx;
        y=(y-winy)/winy;
        GL11.glBegin(GL11.GL_LINES);
        GL11.glTexCoord2f(0,0);
        GL11.glVertex3f(x,y, 0.0f);
        GL11.glTexCoord2f(1,0);
        GL11.glVertex3f(sx, sy, 0.0f);

        GL11.glEnd();  
   }
   public float toGL(float pos, int type)
   {
   
        float winx = ((float)World.ResolutionX/2);
        float winy = ((float)World.ResolutionY/2);
        if(type == 0)
         return (pos-winx)/winx;
        else
         return (pos-winy)/winy;
   
   }
   public void drawRect(float x, float y, float sx, float sy)
   {
        for(int i=0;i<glInts.size();i++)
        {
            GL20.glUniform1i(GL20.glGetUniformLocation(program, glIntsStrings.get(i)), glInts.get(i));
        }
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); 
        GL20.glUniform4f(GL20.glGetUniformLocation(program, "inputColor"),r,g,b,a);
        ARBShaderObjects.glUseProgramObjectARB(program);
        float winx = ((float)World.ResolutionX/2);
        float winy = ((float)World.ResolutionY/2);
        sx=((x+sx)-winx)/winx;
        sy=((y+sy)-winy)/winy;
        x=(x-winx)/winx;
        y=(y-winy)/winy;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0,0);
        GL11.glVertex3f(x,y, 0.0f);
        GL11.glTexCoord2f(1,0);
        GL11.glVertex3f(sx, y, 0.0f);
        GL11.glTexCoord2f(1,1);
        GL11.glVertex3f(sx,sy, 0.0f);
        GL11.glTexCoord2f(0,1);
        GL11.glVertex3f(x,sy, 0.0f);
        GL11.glVertex3f(x,-sy, 0.0f);
        GL11.glEnd();
   }
    private int createShader(String filename, int shaderType) throws Exception {
    	int shader = 0;
    	try {
	        shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
	        
	        if(shader == 0)
	        	return 0;
	        
	        ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
	        ARBShaderObjects.glCompileShaderARB(shader);
	        
	        if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
	            throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
	        
	        return shader;
    	}
    	catch(Exception exc) {
    		ARBShaderObjects.glDeleteObjectARB(shader);
    		throw exc;
    	}
    }
    
    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }
    
    private String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();
        
        FileInputStream in = new FileInputStream(filename);
        
        Exception exception = null;
        
        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            
            Exception innerExc= null;
            try {
            	String line;
                while((line = reader.readLine()) != null)
                    source.append(line).append('\n');
            }
            catch(Exception exc) {
            	exception = exc;
            }
            finally {
            	try {
            		reader.close();
            	}
            	catch(Exception exc) {
            		if(innerExc == null)
            			innerExc = exc;
            		else
            			exc.printStackTrace();
            	}
            }
            
            if(innerExc != null)
            	throw innerExc;
        }
        catch(Exception exc) {
        	exception = exc;
        }
        finally {
        	try {
        		in.close();
        	}
        	catch(Exception exc) {
        		if(exception == null)
        			exception = exc;
        		else
					exc.printStackTrace();
        	}
        	
        	if(exception != null)
        		throw exception;
        }
        
        return source.toString();
    }
}