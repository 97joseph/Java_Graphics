import java.util.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Graphics.*;
import java.net.Socket;


   
public class dogeClient
{

   public static void main(String ar[]) throws IOException
   {
      World.GAMETYPE = "CLIENT";
      String serverAddress = JOptionPane.showInputDialog("HOST IP:");
           
        
        
        
      JFrame frame = new JFrame("Battleground");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setSize(World.WorldSizeX, World.WorldSizeY);
      /*set up graphics vars*/
      Graphics g = frame.getGraphics();
      Graphics2D g2 = (Graphics2D) g;
      BufferedImage offscreenImage = new BufferedImage(World.WorldSizeX,World.WorldSizeY,BufferedImage.TYPE_INT_ARGB);
      Graphics Offg = offscreenImage.createGraphics();
      /*keystroke listnener*/
      KeyController kController = new KeyController();
      MouseController mController = new MouseController();
      frame.addKeyListener(kController);
      frame.addMouseListener(mController);
     /*define world things*/
      World.Background= World.LoadImage(World.PATH+"background.png");
      World.ProjectileImage = new ImageIcon(World.PATH+"notagif.gif");
      World.HealthImage = new ImageIcon(World.PATH+"HealthPack.gif");
      World.MagicImage = new ImageIcon(World.PATH+"magicimage.gif");
      World.ExplosionImage = new ImageIcon(World.PATH+"explosion.gif");
      World.gameOverImage = new ImageIcon(World.PATH+"gameover.png");
      World.replayImage = new ImageIcon(World.PATH+"replay.png");
      World.fadeImage = new ImageIcon(World.PATH+"fade.png");
      World.quitImage = new ImageIcon(World.PATH+"quit.png");
      long frametick =0;     
      long lastDropFrame = 0;
      Random theRandomNumber  = new Random();
      soundThread DeathSound  = new soundThread(World.PATH+"ohmygod.wav");              
      soundThread MortalKombat = new soundThread(World.PATH+"mortalkombat.wav");
      MortalKombat.start();  
        
      FantasyFighter Doge = new FantasyFighter(700,500,World.PATH+"Doge/");
      BufferedImage placementImage = World.LoadImage(World.PATH+"Mario/left.png");
      Doge.speed = 300;
      Doge.LeftChar = 65;
      Doge.RightChar = 68;
      Doge.JumpChar = 87;
      Doge.StrikeChar = 83;
      Doge.gameID = 0;
      
      
      

         
      while(!kController.quit)
      {
         Socket s = new Socket(serverAddress, 80);
         World.output = new ObjectOutputStream(s.getOutputStream());
         World.input = new ObjectInputStream(s.getInputStream());
      
         System.out.println("writing doge");
         if(Doge.gameID == 0)
         {
            World.output.writeObject(new Packet("CONNECT",null));
         }
         else
         {   
            World.output.writeObject(new Packet("CHARACTER",Doge.toData()));
         }
         
         Packet readPacket = new Packet();
         Data readData = new Data();
         try{
            readPacket = (Packet)World.input.readObject();
            if(readPacket.header.equals("DATA"))
            {
               readData = (Data)readPacket.data;
               World.gameCharactersData = readData.gCharacters;
               World.GameObjects = readData.gObjects;
            }
            else if(readPacket.header.equals("ID"))
            {
               Doge.gameID = (int)readPacket.data;
            }
         }
         catch(ClassNotFoundException e){}
      
         System.out.println("downloaded characters and objects");
        
         frametick = System.currentTimeMillis();
         World.mouseX = (int)MouseInfo.getPointerInfo().getLocation().getX();
         World.mouseY = (int)MouseInfo.getPointerInfo().getLocation().getY();
         World.windowX = (int)frame.getLocation().getX();
         World.windowY = (int)frame.getLocation().getY();
         Offg.drawImage(World.Background,World.x,0,null);
      
         if(World.keys[Doge.LeftChar])
         {
            double amount = Doge.speed/World.FPS;
            Doge.move(-Doge.speed/World.FPS);
            int WindowPosition=(int)(-(World.x+amount));
            boolean less = WindowPosition>Doge.xPos;
            boolean greater = WindowPosition+World.WorldSizeX<(Doge.xPos+World.CharacterSizeX);
         
            if(!less&&!greater)
            {
               if(World.x+amount>0)
               {
                  World.x=0;
               }
               else if(World.x+amount<-1346)
               {
                  World.x=-1346;
               }
               else
               {
                  World.x+=amount;
               }
            }
         }
         else if(World.keys[Doge.RightChar])
         {
            double amount = -Doge.speed/World.FPS;
            Doge.move(Doge.speed/World.FPS);
            int WindowPosition=(int)(-(World.x+amount));
            boolean less = WindowPosition>Doge.xPos;
            boolean greater = WindowPosition+World.WorldSizeX<(Doge.xPos+World.CharacterSizeX);
         
            if(!less&&!greater)
            {
               if(World.x+amount>0)
               {
                  World.x=0;
               }
               else if(World.x+amount<-1346)
               {
                  World.x=-1346;
               }
               else
               {
                  World.x+=amount;
               }
            }
         }
         if(World.keys[Doge.StrikeChar])
         {
            Doge.strike();
         
         }
         if(World.keys[Doge.JumpChar])
         {
            Doge.jump();
         }
         Doge.updateState();
         World.gameCharactersData.add(Doge.toData());
         
         for(int i =0; i<World.gameCharactersData.size(); i++) //start downloaded updates
         {
            
            FantasyFighterData TheCharacter = World.gameCharactersData.get(i);
            //Render the Character
            String usageString = World.PATH+"NoImage.png";
            switch(TheCharacter.facing)
            {
               case -1:
                  usageString = TheCharacter.ImagePath+"left.png";
                  break;
               case 1:
                  usageString = TheCharacter.ImagePath+"right.png";
                  break;
               default:
                  usageString = TheCharacter.ImagePath+"left.png";
                  break;
            }                  
            if(!World.loadedImages.containsKey(usageString))
            {
               World.loadedImages.put(usageString,World.LoadImage(usageString));            
            }
            Offg.drawImage(World.loadedImages.get(usageString),World.x+(int)TheCharacter.xPos,
                                                  (World.WorldSizeY-(int)TheCharacter.yPos)-World.CharacterSizeY,
                                                   World.CharacterSizeX,World.CharacterSizeY,null);
            //Render the Health bar
            Offg.setColor(Color.RED);
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.WorldSizeY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5)
                                                         ,World.CharacterSizeX,5);
            Offg.setColor(Color.GREEN);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.WorldSizeY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5),
                                                           (int)((double)World.CharacterSizeX*((double)TheCharacter.health/100)),5);             
            //Render the magic bar
            Offg.setColor(Color.LIGHT_GRAY );
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.WorldSizeY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10)
                                                         ,World.CharacterSizeX,5);
            Offg.setColor(Color.BLUE);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.WorldSizeY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10),
                                                           (int)((double)World.CharacterSizeX*((double)TheCharacter.magic/50)),5);
         }// end character updates
      
         
         for(int i=0;i<World.GameObjects.size(); i++) //start downloaded projectile updates
         {
            GameObject TheProjectile = World.GameObjects.get(i);
            Image BufIcon = World.ProjectileImage.getImage();     
            int sizex =50 ,sizey =50;      
            if(TheProjectile.type.equalsIgnoreCase("health"))
            {
               BufIcon = World.HealthImage.getImage();
            }
            else if(TheProjectile.type.equalsIgnoreCase("magic"))
            {
               BufIcon = World.MagicImage.getImage();
            }
            else if(TheProjectile.type.equalsIgnoreCase("explosion"))
            {
               BufIcon = World.ExplosionImage.getImage();
               sizex = 150;
               sizey = 150;
            } 
            Offg.drawImage(BufIcon,World.x+(int)TheProjectile.x,(World.WorldSizeY-(int)TheProjectile.y)-sizex,sizex,sizey,null);
         }//end game object downloads
        
        
         for(int x=0;x<World.Objects.length;x++) // render ground start
         {
            Offg.drawLine(World.x+World.Objects[x].x,World.WorldSizeY-World.Objects[x].y,World.x+World.Objects[x].sx,World.WorldSizeY-World.Objects[x].y);
         }// render ground end                  
      
      
      
         g2.drawImage(offscreenImage,0,0,null);
         World.wait(1);
         World.FPS = 1/((double)(System.currentTimeMillis()-frametick)/1000);
         ++World.framecount;
      }//end while
   }
}
