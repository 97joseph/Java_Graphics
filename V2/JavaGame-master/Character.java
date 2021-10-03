import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;






public abstract class Character {
   private long lastJumpFrame;
   private long lastStrikeFrame;
   public int shootTime;
   public int LeftChar, RightChar, JumpChar, StrikeChar;
   public double jumpVelocity; // velocity to add when jumping
   public double xPos, yPos;   // x and y position
   public double speed;        // walkspeed in pixels/frame
   public BufferedImage Img;   // character image
   public double velocity[];   // velocity (x,y)
   public int gameID;
   public int facing;
   public String resourcePath;
   

   public Character(int x, int y, String p)
   {
      facing = 1;
      xPos = x;
      yPos = y;
      velocity = new double[]{0,0,0};
      lastJumpFrame = 0;
      jumpVelocity = 100;
      shootTime = 2;
      resourcePath = World.PATH+"Players/"+p;
      
   }
      //get image in the array of images.

      
   public boolean canStrike()
   {     
      if(System.currentTimeMillis()-lastStrikeFrame > shootTime)
      {
         lastStrikeFrame = System.currentTimeMillis();
         return true;
      }
      return false;
   }
     
   public void jump()
   {
      if(onGround())
      {
         velocity[1] = jumpVelocity;
         lastJumpFrame = World.framecount;
      }
      else
      {
         //System.out.println("Cannot jump; currently jumping.");
      }
   }
   public void move(double amount)
   {
      velocity[0] = amount;
   }
   public void shiftAmount(double amount)
   {
      if(xPos+amount<-World.x)
      {
         World.move(-amount);
         xPos=-World.x;

      }
      else if(xPos+World.CharacterSizeX+amount>-World.x+World.ResolutionX)//size
      {
         World.move(-amount);
         xPos=-World.x+World.ResolutionX-World.CharacterSizeX; // size
      }
      else
      {
         xPos+=amount;
      }
   }
   public void animateStrike(int Amount)
   {
     if(canStrike())
      {
         if(World.GAMETYPE.equals("SPLIT"))
         {
            if(facing == 1)
            {
              World.GameObjects.add(new Box((int)xPos+World.CharacterSizeX+10,(int)yPos+World.CharacterSizeY/2,750,30,25,"projectile",true));
            }
            else
            {
              World.GameObjects.add(new Box((int)xPos-(World.CharacterSizeX+10),(int)yPos+World.CharacterSizeY/2,-750,30,25,"projectile",true));
            }    
         }
         else 
         {
            try
            {
               if(facing == 1)
               {
                 World.output.writeObject(new Packet("Object",new Box((int)xPos+World.CharacterSizeX+10,(int)yPos+World.CharacterSizeY/2,750,30,25,"projectile",true)));
               }
               else
               {
                 World.output.writeObject(new Packet("Object",new Box((int)xPos-(World.CharacterSizeX+10),(int)yPos+World.CharacterSizeY/2,-750,30,25,"projectile",true)));
               }   
            }
            catch(IOException e){}
         }
      }
   }
   public boolean onGround()
   {
      return   yPos==World.getLowerBounds(World.Objects,xPos+(World.CharacterSizeX/2),yPos);
   }
   public void updateState()
   {
      PhysicsObject l = World.Objects.get(World.getLowerBound(World.Objects,xPos+(World.CharacterSizeX/2),yPos));
      double x[] = World.getPhysics(World.Objects,xPos+(World.CharacterSizeX/2),yPos,velocity[1]);
      velocity[1] = x[0];
      yPos = x[1];
      boolean facingSlope = l.getSlope()/Math.abs(l.getSlope()) == facing;
      double shift = velocity[0];
      if(onGround())
      {
         if(Math.abs(l.getSlope())>World.maxSlope&&facingSlope)
            velocity[0] = 0;      
         shift = shift*(facingSlope?1-Math.abs(l.getSlope()):1+Math.abs(l.getSlope()));
         velocity[0]*=l.friction;
      }
      shiftAmount(shift);
   }

}