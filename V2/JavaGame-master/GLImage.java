import java.awt.Image;
import javax.swing.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL13;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import static org.lwjgl.opengl.GL11.*;
class GLImage
{
   BufferedImage data;
   int imageIndexes[];
   
   public GLImage(String path)
   {
    data = toB(new ImageIcon(path).getImage());
    imageIndexes = new int[10];
    imageIndexes[0]=-1;
   }
   public void enable(String textureName, int channel)
   {
     GL20.glUniform1i(GL20.glGetUniformLocation(World.glRend.program, textureName), channel);
     GL13.glActiveTexture(GL13.GL_TEXTURE0+channel);
     glBindTexture(GL_TEXTURE_2D, imageIndexes[0]); //Bind texture ID
   }
   
   public void drawText(String text, float xPos, float yPos,int r, int g, int b,String FontName, int FontSize)
   {
      Graphics2D textDrawer = data.createGraphics();
      textDrawer.setColor(new Color(r,g,b));

      textDrawer.setFont(World.setFont(Font.TRUETYPE_FONT,FontName,Font.BOLD,FontSize));
      FontMetrics fm = textDrawer.getFontMetrics();
      int x = (int)(xPos*(float)data.getWidth());
      int y = (int)(yPos*(float)data.getHeight());
      textDrawer.drawString(text,x,y);
      textDrawer.dispose();
   }  
   
   public  BufferedImage toB(Image img)
   {
    if (img instanceof BufferedImage)
    {
        return (BufferedImage) img;
    }

    // Create a buffered image with transparency
    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    // Return the buffered image
    return bimage;
   }  
   public void loadTexture(){
        
        int[] pixels = new int[data.getWidth() * data.getHeight()];
        data.getRGB(0, 0, data.getWidth(), data.getHeight(), pixels, 0, data.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(data.getWidth() * data.getHeight() * 4); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < data.getHeight(); y++){
            for(int x = 0; x < data.getWidth(); x++){
                int pixel = pixels[y * data.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));             // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); 
        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:
        if(imageIndexes[0]!=-1)
          glDeleteTextures(imageIndexes[0]);
      int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
        
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      
        //Return the texture ID so we can bind it later again 
         imageIndexes[0]= textureID;
   }

}