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
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL20;

   
public class gamePlayer
{

   public static void splitscreen()
   {
      World.GAMETYPE = "SPLIT";
     //define world things/
     World.ProjectileImage = World.setImage("Textures/notagif.gif",true);
     World.HealthImage = World.setImage("Textures/HealthPack.gif",true);
     World.MagicImage = World.setImage("Textures/magicimage.gif",true);
     World.ExplosionImage = World.setImage("Textures/explosion.gif",true);
     World.blackImage = World.setImage(World.PATH+"Textures/black.png");
     World.HitMarkerImage = World.setImage(World.PATH+"Textures/marker.png");
     World.setDefaultCharacters();
     long frametick =0;     
     long lastDropFrame = 0;
     long startTime = System.currentTimeMillis(); 
     UI QuitButton = new UI();
     UI ReplayButton = new UI();
     UI gameOverUI = new UI();
     gameOverUI.setImage(World.PATH+"Textures/doge.PNG");
     gameOverUI.setColor(1,1,1,1);
     gameOverUI.setPos(0,0);
     gameOverUI.setSizeRelative(1,1);
     ReplayButton.setImage(World.PATH+"Textures/replay.PNG");
     ReplayButton.setColor(1,1,1,1);
     QuitButton.setImage(World.PATH+"Textures/quit.PNG");
     QuitButton.setColor(1,0,0,1);
     Random theRandomNumber  = new Random();
     soundThread DeathSound  = new soundThread(World.PATH+"Sounds/ohmygod.wav");              
     soundThread MortalKombat = new soundThread(World.PATH+"Sounds/mortalkombat.wav");
     boolean textures = false;
     MortalKombat.start();
     while(true)
     {
        Light.lights.clear();
        frametick = System.currentTimeMillis();
        int stretchx,stretchy;
        stretchx = World.ResolutionX>World.BackgroundX?World.ResolutionX:World.BackgroundX;     
        stretchy = World.ResolutionY>World.BackgroundY?World.ResolutionY:World.BackgroundY;     
        int timeLoc = ARBShaderObjects.glGetUniformLocationARB(World.glRend.program,"time");
        ARBShaderObjects.glUniform1fARB(timeLoc,((float)(frametick-startTime))/5000);
       for(int i =0; i<World.gameCharacters.size(); i++) //start character updates
        {
            FantasyFighter TheCharacter = World.gameCharacters.get(i);
            if(Keyboard.isKeyDown(TheCharacter.LeftChar))
            {
                TheCharacter.move(-TheCharacter.speed/World.FPS);
            }
            else if(Keyboard.isKeyDown(TheCharacter.RightChar))
            {
                TheCharacter.move(TheCharacter.speed/World.FPS);
            }

            if(Keyboard.isKeyDown(TheCharacter.StrikeChar))
            {
               TheCharacter.strike();

            }
            if(Keyboard.isKeyDown(TheCharacter.JumpChar))
            {
               TheCharacter.jump();
            }
            
            TheCharacter.updateState();                
           

         if(TheCharacter.isDead())
         {
            World.gameCharacters.remove(i);
            for(int x = 0;x<25;x++)
            {
               World.GameObjects.add(new Box((int)TheCharacter.xPos,(int)TheCharacter.yPos,0,0,0,"explosion",false));
            }
            World.ingame = false;
            int soundId=theRandomNumber.nextInt(4);
            if(soundId==0){
                DeathSound = new soundThread(World.PATH+"Sounds/ohmygod.wav");
            }else if(soundId==1){
                DeathSound = new soundThread(World.PATH+"Sounds/roundlost.wav");
            }else if(soundId==2){
                DeathSound = new soundThread(World.PATH+"Sounds/cry.wav");
            }else if(soundId==3){
                DeathSound = new soundThread(World.PATH+"Sounds/getnoscoped.wav");
            }    
            DeathSound.start();
            MortalKombat.stop();
         }
         
        }// end character updates

         
        //Update projectiles
        if(frametick-lastDropFrame >World.healthDropRate) //start "random" health pack
        {
           lastDropFrame = frametick;        
           World.GameObjects.add(new Box(theRandomNumber.nextInt(World.BackgroundX)+1,750,((theRandomNumber.nextInt(2)*2)-1)*50,0,10,"health",true));
        }//end random health pack
        for(int i=0;i<World.GameObjects.size(); i++)
        {
         GameObject TheProjectile = World.GameObjects.get(i);
         TheProjectile.update();
          if(TheProjectile.y==World.objectFloor)
            World.GameObjects.remove(i);
        }//end game object updates

         //------------------------------RENDER STAGE--------------------\\
         //sun.render();
         for(int i=0;i<World.lights.size();i++)
            World.lights.get(i).render();
         World.Background.enable("text",0);
         World.glRend.setColor(1,1,1,.9f);
         World.glRend.drawRect(World.x,0f,stretchx,stretchy);
         
         World.glRend.setInt("useTextures",0);
         World.glRend.setColor(0,0,1,1);
         for(int x=0;x<World.Objects.size()&&World.showPhysics;x++) // render ground start
         {
            World.glRend.drawLine(World.x+World.Objects.get(x).x,World.ResolutionY-World.Objects.get(x).y,World.x+World.Objects.get(x).sx,World.ResolutionY-World.Objects.get(x).sy);
         }// render ground end                  

         for(int i=0;i<World.GameObjects.size()||i<World.gameCharacters.size();i++)
         {
            if(i<World.GameObjects.size())
               World.GameObjects.get(i).render(World.glRend);
            if(i<World.gameCharacters.size())
               World.gameCharacters.get(i).render(World.glRend);
            
         }

        

         //GAME OVER
         if(!World.ingame)
         {
            QuitButton.setPos(750,500);
            QuitButton.setSize(250,100);      
            ReplayButton.setPos(750,200);
            ReplayButton.setSize(250,100);            
            if(World.gameCharacters.size()>0)
            {
               World.gameCharacters.get(0).getImage().enable("text",0);
               World.glRend.drawRect(250,250,250,250);
            }
            if(ReplayButton.mouseIn()) //inside replay button
            {
               ReplayButton.setColor(.5f,.5f,.5f,.5f);
               if(Mouse.isButtonDown(0))
               {
                  World.ingame =true;
                  MortalKombat = new soundThread(World.PATH+"Sounds/mortalkombat.wav");
                  MortalKombat.start();               
                  DeathSound.stop();
                  World.gameCharacters.clear();
                  World.GameObjects.clear();
                  World.setDefaultCharacters();
               }
            }          
            if(QuitButton.mouseIn()) //inside quit button
            {
               QuitButton.setColor(.5f,.5f,.5f,.5f);
               if(Mouse.isButtonDown(0))
               {
                  System.exit(0);
               }
            }
            //gameOverUI.draw(World.glRend);
            QuitButton.draw(World.glRend);
            ReplayButton.draw(World.glRend);
            ReplayButton.setColor(1,1,1,1);
            QuitButton.setColor(1,1,1,1);         
         }
         if(Display.isCloseRequested())
         {
           System.exit(0);
         }
         
         GL20.glUniform1i(GL20.glGetUniformLocation(World.glRend.program, "time"), (int)(startTime-System.currentTimeMillis()));  
         Display.update();
         World.wait(1);
         World.FPS = 1/((double)(System.currentTimeMillis()-frametick)/1000);
         ++World.framecount;

      }
   }
   /*public static void Client(String ip) throws IOException
   {
      World.GAMETYPE = "CLIENT";
  
           
        
        
        
      JFrame frame = new JFrame("Battleground");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setSize(World.ResolutionX, World.ResolutionY);
      //set up graphics vars
      Graphics g = frame.getGraphics();
      Graphics2D g2 = (Graphics2D) g;
      BufferedImage offscreenImage = new BufferedImage(World.ResolutionX,World.ResolutionY,BufferedImage.TYPE_INT_ARGB);
      Graphics Offg = offscreenImage.createGraphics();
      //keystroke listnener
      KeyController kController = new KeyController();
      MouseController mController = new MouseController();
      frame.addKeyListener(kController);
      frame.addMouseListener(mController);
     //define world things
     World.Background= World.setImage("Worlds/World2/background.gif",true);
      World.ProjectileImage = new GLImage(World.PATH+"notagif.gif");
      World.HealthImage = new GLImage(World.PATH+"HealthPack.gif");
      World.MagicImage = new GLImage(World.PATH+"magicimage.gif");
      World.ExplosionImage = new GLImage(World.PATH+"explosion.gif");
      World.gameOverImage = new GLImage(World.PATH+"gameover.png");
      World.replayImage = new GLImage(World.PATH+"replay.png");
      World.fadeImage = new GLImage(World.PATH+"fade.png");
      World.quitImage = new GLImage(World.PATH+"quit.png");
      long frametick =0;     
      long lastDropFrame = 0;
      Random theRandomNumber  = new Random();
      soundThread DeathSound  = new soundThread(World.PATH+"ohmygod.wav");              
      soundThread MortalKombat = new soundThread(World.PATH+"chiv.mp3");
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
         Offg.drawImage(World.Background.getImage(),World.x,0,null);
      
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
        
        
         for(int x=0;x<World.Objects.size();x++) // render ground start
         {
            Offg.drawLine(World.x+World.Objects.get(x).x,World.ResolutionY-World.Objects.get(x).y,World.x+World.Objects.get(x).sx,World.ResolutionY-World.Objects.get(x).sy);
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
        if(frametick-lastDropFrame >World.healthDropRate) //start random health pack
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
   }*/

}