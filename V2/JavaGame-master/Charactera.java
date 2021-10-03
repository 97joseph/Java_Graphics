import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;






public abstract class Character {
   private long lastJumpFrame;
   private long lastStrikeFrame;
   public int LeftChar, RightChar, JumpChar, StrikeChar;
   public double jumpVelocity; // velocity to add when jumping
   public double xPos, yPos;   // x and y position
   public double speed;        // walkspeed in pixels/frame
   public BufferedImage Img;   // character image
   public double velocity[];   // velocity (x,y)
   public int gameID;
   private String resourcePath;


   public Character(int x, int y, String p)
   {
      xPos = x;
      yPos = y;
      velocity = new double[]{0,0,0};
      lastJumpFrame = 0;
      jumpVelocity = 100;
      resourcePath = p;
      
   }
      //get image in the array of images.
   private BufferedImage setImage(String path,boolean ref)
   {
      if(ref)
      {
         path = resourcePath+path;
      }
      path = path.toLowerCase();
      if(StringsOfImages.contains(path))
      {
         return Images.get(StringsOfImages.indexOf(path));
      }
      BufferedImage tempImg = World.LoadImage(path);
      Images.add(tempImg);
      StringsOfImages.add(path);
      return tempImg;
   }

   public boolean canStrike()
   {     
      if(System.currentTimeMillis()-lastStrikeFrame > 1000)
      {
         lastStrikeFrame = System.currentTimeMillis();
         return true;
      }
      return false;
   }
     
   public void jump()
   {
      if(yPos==World.getLowerBounds(World.Objects,xPos+(World.CharacterSizeX/2),yPos))
      {
         velocity[1] = jumpVelocity;
         lastJumpFrame = World.framecount;
      }
      else
      {
         System.out.println("Cannot jump; currently jumping.");
      }
   }
   
   public void move(double amount)
   {
      if(xPos+amount<-World.x)
      {
         World.move(-amount);
         xPos=-World.x;

      }
      else if(xPos+World.CharacterSizeX+amount>-World.x+World.WorldSizeX)
      {
         World.move(-amount);
         xPos=-World.x+World.WorldSizeX-World.CharacterSizeX;
      }
      else
      {
         xPos+=amount;
      }
   }
   
   public void updateState()
   {
      double x[] = World.getPhysics(World.Objects,xPos+(World.CharacterSizeX/2),yPos,velocity[1]);
      velocity[1] = x[0];
      yPos = x[1];
   }
}