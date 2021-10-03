import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.net.URL;
import java.io.*;
import org.lwjgl.opengl.GL11;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;



public class FantasyFighter extends Character
{
   //
   private GLImage outputImage;
   /** Amount of life points the character has*/ 
   private int health;
   /** amount of points to use in magic attacks */
   private int magicPoints;
   /** direction the character is super.facing, -1 is left, 1 is right. */

   /** String used to identify the robot in the output text */
   private String name;
   /**the name of the spell that magic points uses */
   private String spellName;
   /** random number used to generate attack points */
   private Random r;
   /** the path to all the media for this character*/
   private String ImagePath;
   
   /**
    * Constructor to create a FantasyFighter object.
    * sets name to Unknown.
    * sets spellname to unknown
    */
   public FantasyFighter(int x, int y, String p)
   {
      super(x,y,p);
      health = 100;
      magicPoints = 50;
      name = "Unknown";
      spellName = "UnknownSpell";
      r = new Random();
      outputImage= World.setImage("Textures/NoImage.png",true);
      
   }
    
   
   
   /** Sets the spell name given a string
    * @param nameToSet the new spell name
    */
   public void setSpellName(String nameToSet)
   {
      spellName = nameToSet;
   }
   
   
   /** Returns the spell name
    *
    * @return the fighter's current spell name
    */
   public String getSpellName()
   {
      return spellName;
   }
   
   
   /** Sets the character's name given a string
    *
    * @param nameToSet New name of the character
    */
   public void setName(String nameToSet)
   {
      name = nameToSet;
   }
   
   
   /** Returns the fighter's name
    *
    * @return The fighter's current name
    */
   public String getName()
   {
      return name;
   }
   
   
   /**Returns true if the fighter's health is at or below 0, else false
    *
    * @return whether or not the fighter's health is less than or equal to 0.
    */
   public boolean isDead()
   {
      return health<=0;
   }
   
   
   /** returns the amount of health points available to the fighter
    *
    * @return The fighter's current health amount
    */
   public int getHealth()
   {
      return health;
   }
   
   
   /** Adds a random number to the character's health
    *
    *
    */
   public void heal()
   {
      health += r.nextInt(50)+50;
   } 
   /** returns the amount of magic points the character currently has
    *
    * @return The fighter's current magic points.
    */
   public int getMagic()
   {
      return magicPoints;
   }
   
   
   /** subracts a specified amount from the character's health
    *
    * @param Amount The amount to remove from the fighter's health
    */
   public void takeDamage(int Amount)
   {
      health -= Amount;
   }
   
   
   /** sets the amount of health for the character
    *
    * @param Amount The amount to set the fighter's health to.
    */
   public void setHealth(int Amount)
   {
      health = Amount;
   }
   
   
   /** returns the amount of damage given by a magic attack. 
    *  using this requires the fighter to have at least 15 magic
    *  points
    * @return The damage to give from casting a spell
    */
   public int castSpell()
   {
      if(magicPoints-15>0)
      {
         magicPoints-=15;
         return (r.nextInt(41));
      }
      else
      {
         System.out.println("Not enough magic points.");
      }
      return 0;
   }
   
   /** Returns the amount of damage given by a strike attack.
    *  calls animateStrike() to create a projectile to hit with.
    *
    * @return Strike damage
    */
   public int strike()
   {  
      animateStrike(20);
      return 20;
   }
   /**
    * Animates an attack by creating a projectile and shooting it
    * only works if it can strike
    *
    * @param Amount the damage amount of the projectile to be created
    */
   
   
   public void jump()
   {
      super.jump();
   }
   /**
    * moves the character given an amount.
    * @param amount the amount to move in the 'right' direction
    */
   public void move(double amount)
   {
      super.move(amount);
      if(amount<0)
      {
         super.facing = -1;
         outputImage = World.setImage(resourcePath+"left.png",false);
      }
      else if(amount>0)
      {
         super.facing = 1;
         outputImage = World.setImage(resourcePath+"right.png",false);
      }
   }
   
   /** 
    * returns the image currently used to display the fighter
    * @return the fighter's image
    */
   public int getImageIndex()
   {
      return outputImage.imageIndexes[0];
   }
   public GLImage getImage()
   {   
      return outputImage;
   }
   
   /**
    * Updates the fighter's current in game state
    * This handles the fighter's physics and any collisions with 
    * in game objects (projectiles, health packs, or magic boosts)
    */
   public void updateState()
   {
      super.updateState();
      for(int i=0;i<World.GameObjects.size(); i++)
      {
         GameObject TheGameObject = World.GameObjects.get(i);
         if(TheGameObject.x-xPos < World.CharacterSizeX && TheGameObject.x-xPos >=-50) // Within the x and sizex
         {
           if(TheGameObject.y-yPos < World.CharacterSizeY && TheGameObject.y-yPos >=-50)
           {
           
               if(TheGameObject.type.equalsIgnoreCase("projectile"))
               {
                  takeDamage((int)TheGameObject.Value);
                  soundThread kek =  new soundThread(World.PATH+"Sounds/hitmarker.wav");
                  kek.start();
                  World.GameObjects.add(new Box((int)xPos,(int)TheGameObject.y,0,0,0,"hit",false));                  
               }
               else if(TheGameObject.type.equalsIgnoreCase("health"))
               {
                  if(getHealth()+TheGameObject.Value>100)
                  {
                     health = 100;
                  }
                  else
                  {
                     health = getHealth()+(int)TheGameObject.Value;
                  }
               }
               else if(TheGameObject.type.equalsIgnoreCase("magic"))
               {
                  if(magicPoints + TheGameObject.Value <= 50)
                  {
                     magicPoints += TheGameObject.Value;
                  }
                  else
                  {
                     magicPoints = 50;
                  }
               }
               if(!(TheGameObject.type.equalsIgnoreCase("explosion")||TheGameObject.type.equalsIgnoreCase("hit")))
               {
                 TheGameObject.removeSelf(); 
               }
            }
         }
      }
   }
   public void render(GLSLRenderer rend)
   {
             //Render the Character
            rend.setColor(1,1,1,1);
            rend.setInt("useTextures",1);
            getImage().enable("text",0);
            rend.drawRect(World.x+(int) xPos,
                                 (World.ResolutionY-(int) yPos)-World.CharacterSizeY,
                                  World.CharacterSizeX,
                                  World.CharacterSizeY);
            rend.setInt("useTextures",0);
            //Render the Health bar
            rend.setColor(1,0,0,1);
            rend.drawRect(World.x+(int) xPos,(World.ResolutionY-(int) yPos)-(World.CharacterSizeY+5)
                                                         ,World.CharacterSizeX,5);
            rend.setColor(0,1,0,1);
            rend.drawRect((World.x+(int) xPos),(World.ResolutionY-(int) yPos)-(World.CharacterSizeY+5),
                                                           (int)((double)World.CharacterSizeX*((double) getHealth()/100)),5); 
            
            //Render the magic bar
            rend.setColor(.5f,.5f,1,1);
            rend.drawRect(World.x+(int) xPos,(World.ResolutionY-(int) yPos)-(World.CharacterSizeY+10)
                                                         ,World.CharacterSizeX,5);

            rend.setColor(0,0,1,1);
            rend.drawRect((World.x+(int) xPos),(World.ResolutionY-(int) yPos)-(World.CharacterSizeY+10),
                                                           (int)((double)World.CharacterSizeX*((double) getMagic()/50)),5);
            rend.setColor(1,1,1,1);
            rend.setInt("useTextures",1);
            
        }
   public FantasyFighterData toData()
   {
      return new FantasyFighterData(ImagePath,super.facing,health, magicPoints, gameID, xPos, yPos);
   }
}