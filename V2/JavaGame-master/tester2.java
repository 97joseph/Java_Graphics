import java.util.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Graphics.*;
import java.net.Socket;
import java.net.ServerSocket;



   
public class gamePlayer
{

   public static void splitscreen()
   {
      World.GAMETYPE = "SPLIT";
      JFrame frame = new JFrame("Battleground");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setSize(World.ResolutionX, World.ResolutionY);
      /*set up graphics vars*/
      Graphics g = frame.getGraphics();
      Graphics2D g2 = (Graphics2D) g;
      BufferedImage offscreenImage = new BufferedImage(World.ResolutionX,World.ResolutionY,BufferedImage.TYPE_INT_ARGB);
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
     World.setDefaultCharacters();
     long frametick =0;     
     long lastDropFrame = 0;
     Random theRandomNumber  = new Random();
     soundThread DeathSound  = new soundThread(World.PATH+"ohmygod.wav");              
     soundThread MortalKombat = new soundThread(World.PATH+"mortalkombat.wav");
     MortalKombat.start();  
        
     while(!kController.quit)
     {
        
        frametick = System.currentTimeMillis();
        World.mouseX = (int)MouseInfo.getPointerInfo().getLocation().getX();
        World.mouseY = (int)MouseInfo.getPointerInfo().getLocation().getY();
        World.windowX = (int)frame.getLocation().getX();
        World.windowY = (int)frame.getLocation().getY();
        
        Offg.drawImage(World.Background,World.x,0,null);
        for(int i =0; i<World.gameCharacters.size(); i++) //start character updates
        {
            FantasyFighter TheCharacter = World.gameCharacters.get(i);
            if(World.keys[TheCharacter.LeftChar])
            {
                TheCharacter.move(-TheCharacter.speed/World.FPS);
            }
            else if(World.keys[TheCharacter.RightChar])
            {
                TheCharacter.move(TheCharacter.speed/World.FPS);
            }
            if(World.keys[TheCharacter.StrikeChar])
            {
               TheCharacter.strike();

            }
            if(World.keys[TheCharacter.JumpChar])
            {
               TheCharacter.jump();
            }
            
            TheCharacter.updateState();                
            
            

            //Render the Character
            Offg.drawImage(TheCharacter.getImage(),World.x+(int)TheCharacter.xPos,
                                                  (World.ResolutionY-(int)TheCharacter.yPos)-World.CharacterSizeY,
                                                   World.CharacterSizeX,World.CharacterSizeY,null);
            //Render the Health bar
            Offg.setColor(Color.RED);
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5)
                                                         ,World.CharacterSizeX,5);
            Offg.setColor(Color.GREEN);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5),
                                                           (int)((double)World.CharacterSizeX*((double)TheCharacter.getHealth()/100)),5); 
            
            //Render the magic bar
            Offg.setColor(Color.LIGHT_GRAY );
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10)
                                                         ,World.CharacterSizeX,5);

            Offg.setColor(Color.BLUE);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10),
                                                           (int)((double)World.CharacterSizeX*((double)TheCharacter.getMagic()/50)),5);
         if(TheCharacter.isDead())
         {
            World.gameCharacters.remove(i);
            World.GameObjects.add(new GameObject((int)TheCharacter.xPos,(int)TheCharacter.yPos,0,0,0,"explosion",false));
            World.ingame = false;
            int soundId=theRandomNumber.nextInt(4);
            if(soundId==0){
                DeathSound = new soundThread(World.PATH+"ohmygod.wav");
            }else if(soundId==1){
                DeathSound = new soundThread(World.PATH+"roundlost.wav");
            }else if(soundId==2){
                DeathSound = new soundThread(World.PATH+"cry.wav");
            }else if(soundId==3){
                DeathSound = new soundThread(World.PATH+"getnoscoped.wav");
            }    
            DeathSound.start();
            MortalKombat.stop();
         }
         
        }// end character updates

         
        //Update projectiles
        if(frametick-lastDropFrame >5000) //start random health pack
        {
           lastDropFrame = frametick;        
           World.GameObjects.add(new GameObject(theRandomNumber.nextInt(1600)+100,750,((theRandomNumber.nextInt(2)*2)-1)*50,0,10,"health",true));
        }//end random health pack

        for(int i=0;i<World.GameObjects.size(); i++)
        {
         GameObject TheProjectile = World.GameObjects.get(i);
         TheProjectile.x += TheProjectile.vx/World.FPS;
         if(TheProjectile.Gravity)
         {
          double x[] = World.getPhysics(World.Objects, TheProjectile.x, TheProjectile.y, TheProjectile.vy);
          TheProjectile.vy = x[0];
          TheProjectile.y = x[1];
         }
         else
         {    
          TheProjectile.y += TheProjectile.vy/World.FPS;
         }
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
           
          Offg.drawImage(BufIcon,World.x+(int)TheProjectile.x,(World.ResolutionY-(int)TheProjectile.y)-sizex,sizex,sizey,null);
        }//end game object updates
        
        
         for(int x=0;x<World.Objects.length;x++) // render ground start
         {
            Offg.drawLine(World.x+World.Objects[x].x,World.ResolutionY-World.Objects[x].y,World.x+World.Objects[x].sx,World.ResolutionY-World.Objects[x].y);
         }// render ground end                  
         
         
         //GAME OVER
         
         if(!World.ingame)
         {
            
            Offg.drawImage(World.gameOverImage.getImage(),0,0,1000,769,null);
            Offg.drawImage(World.replayImage.getImage(),750,200,250,100,null);
            Offg.drawImage(World.quitImage.getImage(),750,500,250,100,null);
            Offg.drawImage(World.gameCharacters.get(0).getImage(),250,250,250,250,null);
            if((World.mouseX-World.windowX >750 && World.mouseX-World.windowX<1000)
            && (World.mouseY-World.windowY > 200 &&World.mouseY-World.windowY < 300)) //inside replay button
            {
               Offg.drawImage(World.fadeImage.getImage(),750,200,250,100,null);
               if(World.mouse[1])
               {
                  World.ingame =true;
                  MortalKombat = new soundThread(World.PATH+"mortalkombat.wav");
                  MortalKombat.start();               
                  DeathSound.stop();
                  World.gameCharacters.clear();
                  World.GameObjects.clear();
                  World.setDefaultCharacters();
               }
            }          
            if((World.mouseX-World.windowX >750 && World.mouseX-World.windowX<1000)
            && (World.mouseY-World.windowY > 500 && World.mouseY-World.windowY< 600)) //inside quit button
            {
               Offg.drawImage(World.fadeImage.getImage(),750,500,250,100,null);
               if(World.mouse[1])
               {
                  kController.quit = true;
                  System.exit(0);
               }
            }         
         }
         g2.drawImage(offscreenImage,0,0,null);
         World.wait(1);
         World.FPS = 1/((double)(System.currentTimeMillis()-frametick)/1000);
         ++World.framecount;
      }
   }
   public static void Client(String ip) throws IOException
   {
      World.GAMETYPE = "CLIENT";
  
           
        
        
        
      JFrame frame = new JFrame("Battleground");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setSize(World.ResolutionX, World.ResolutionY);
      /*set up graphics vars*/
      Graphics g = frame.getGraphics();
      Graphics2D g2 = (Graphics2D) g;
      BufferedImage offscreenImage = new BufferedImage(World.ResolutionX,World.ResolutionY,BufferedImage.TYPE_INT_ARGB);
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
         Socket s = new Socket(ip, 80);
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
            boolean greater = WindowPosition+World.ResolutionX<(Doge.xPos+World.CharacterSizeX);
         
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
            boolean greater = WindowPosition+World.ResolutionX<(Doge.xPos+World.CharacterSizeX);
         
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
                                                  (World.ResolutionY-(int)TheCharacter.yPos)-World.CharacterSizeY,
                                                   World.CharacterSizeX,World.CharacterSizeY,null);
            //Render the Health bar
            Offg.setColor(Color.RED);
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5)
                                                         ,World.CharacterSizeX,5);
            Offg.setColor(Color.GREEN);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+5),
                                                           (int)((double)World.CharacterSizeX*((double)TheCharacter.health/100)),5);             
            //Render the magic bar
            Offg.setColor(Color.LIGHT_GRAY );
            Offg.fillRect(World.x+(int)TheCharacter.xPos,(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10)
                                                         ,World.CharacterSizeX,5);
            Offg.setColor(Color.BLUE);
            Offg.fillRect((World.x+(int)TheCharacter.xPos),(World.ResolutionY-(int)TheCharacter.yPos)-(World.CharacterSizeY+10),
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
            Offg.drawImage(BufIcon,World.x+(int)TheProjectile.x,(World.ResolutionY-(int)TheProjectile.y)-sizex,sizex,sizey,null);
         }//end game object downloads
        
        
         for(int x=0;x<World.Objects.length;x++) // render ground start
         {
            Offg.drawLine(World.x+World.Objects[x].x,World.ResolutionY-World.Objects[x].y,World.x+World.Objects[x].sx,World.ResolutionY-World.Objects[x].y);
         }// render ground end                  
      
      
      
         g2.drawImage(offscreenImage,0,0,null);
         World.wait(1);
         World.FPS = 1/((double)(System.currentTimeMillis()-frametick)/1000);
         ++World.framecount;
      }//end while
   }
   public static void Server() throws IOException
   {
     World.GAMETYPE = "SERVER";       
     ServerSocket listener = new ServerSocket(80);
     long frametick =0;     
     long lastDropFrame = 0;
     Random theRandomNumber  = new Random();
     int players = 0;
      try {
      while (true) {
        Socket socket = listener.accept();  
        World.output = new ObjectOutputStream(socket.getOutputStream());
        World.input = new ObjectInputStream(socket.getInputStream());
        Packet readPacket = new Packet();//READ DATA FROM CLIENTS
        Data readData = new Data();
        Data outData = new Data();
        try{
            readPacket = (Packet)World.input.readObject();
            if(readPacket.header.equals("CONNECT"))
            {
               ++players;
               World.output.writeObject(new Packet("ID",players));
               System.out.println("new player");
            }
            else if(readPacket.header.equals("CHARACTER"))
            {
               FantasyFighterData readFight = (FantasyFighterData)readPacket.data;
               if(World.gameCharactersData.size() >= (readFight.gameID))
               {
                  World.gameCharactersData.set(readFight.gameID-1, readFight);
               }
               else
               {
                  World.gameCharactersData.add(readFight);
               }
               outData.gCharacters = World.gameCharactersData;
               outData.gObjects = World.GameObjects;
               World.output.writeObject(new Packet("DATA",outData));
            }
            else if(readPacket.header.equals("OBJECT"))
            {
               GameObject readObj = (GameObject)readPacket.data;
               World.GameObjects.add(readObj);
            }
        }
        catch(ClassNotFoundException e){}
        
        frametick = System.currentTimeMillis();        
        //Update projectiles
        if(frametick-lastDropFrame >5000) //start random health pack
        {
           lastDropFrame = frametick;        
           World.GameObjects.add(new GameObject(theRandomNumber.nextInt(1600)+100,750,((theRandomNumber.nextInt(2)*2)-1)*100,0,10,"health",true));
        }//end random health pack

        for(int i=0;i<World.GameObjects.size(); i++)
        {
            GameObject TheProjectile = World.GameObjects.get(i);
            TheProjectile.x += TheProjectile.vx/World.FPS;
            if(TheProjectile.Gravity)
            {
                double x[] = World.getPhysics(World.Objects, TheProjectile.x, TheProjectile.y, TheProjectile.vy);
                TheProjectile.vy = x[0];
                TheProjectile.y = x[1];
            }
            else
            {    
                TheProjectile.y += TheProjectile.vy/World.FPS;
            }
         }       
         World.wait(1);
         World.FPS = 1/((double)(System.currentTimeMillis()-frametick)/1000)*2;
         ++World.framecount;
      }}
      finally {
         listener.close();
      }
   }

}
