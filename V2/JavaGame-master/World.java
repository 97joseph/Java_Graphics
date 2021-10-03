import java.awt.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.sound.sampled.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.DisplayMode;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class World
{
   public static ArrayList<PhysicsObject> Objects = new ArrayList<PhysicsObject>();
   public static ArrayList<GameObject> GameObjects = new ArrayList<GameObject>();
   public static ArrayList<FantasyFighter> gameCharacters = new ArrayList<FantasyFighter>();
   public static ArrayList<FantasyFighterData> gameCharactersData = new ArrayList<FantasyFighterData>();
   
   public static ArrayList<Font> Fonts = new ArrayList<Font>();
   public static ArrayList<String> StringsOfFonts = new ArrayList<String>();   
   public static ArrayList<GLImage> Images = new ArrayList<GLImage>();
   public static ArrayList<String> StringsOfImages = new ArrayList<String>();
   
   public static ArrayList<Light> lights = new ArrayList<Light>();
   
   public static String PATH = "C:/Users/Nick/Google Drive/java/DATA/";//"H:/My Documents/Computer Science/Java/samplefightimages/";
   public static GLSLRenderer glRend;
   public static String GAMETYPE; // enum of CLIENT, SPLIT, SERVER
   public static int x = 0, y = 400;
   public static int windowX=0,windowY=0;
   public static int objectFloor = -100;
   public static boolean keys[] = new boolean[100];   
   public static boolean mouse[] = new boolean[10];
   public static int healthDropRate = 5000, maxSlope = 1;
   public static int CharacterSizeX = 75, CharacterSizeY=150;
   public static int ResolutionX = 1600, ResolutionY = 900;
   public static int BackgroundX=0, BackgroundY=0;
   public static int mouseX=0, mouseY=0;   
   public static int framecount = 0;
   public static double FPS = 100;
   public static double Gravity = -9.81;
   public static GLImage Background;
   public static GLImage ProjectileImage;
   public static GLImage HealthImage;
   public static GLImage HitMarkerImage;
   public static GLImage MagicImage;
   public static GLImage ExplosionImage;
   public static GLImage blackImage;
   public static boolean showPhysics = true;
   public static boolean ingame = true;
   public static boolean music = true;
   public static ObjectInputStream input;
   public static ObjectOutputStream output;

   public static void setDefaultCharacters()
   {
         /*Doge things */
      x=0;
      FantasyFighter Doge = new FantasyFighter(700,500,"Deadmon/");
      Doge.speed = 500;
      Doge.LeftChar = Keyboard.KEY_A;
      Doge.RightChar =  Keyboard.KEY_D;
      Doge.JumpChar =  Keyboard.KEY_W;
      Doge.StrikeChar =  Keyboard.KEY_S;
      FantasyFighter Doge2 = new FantasyFighter(766,500,"Wood/");
      Doge2.speed = 300;
      Doge2.LeftChar =  Keyboard.KEY_LEFT;
      Doge2.RightChar =  Keyboard.KEY_RIGHT;
      Doge2.JumpChar =  Keyboard.KEY_UP;
      Doge2.StrikeChar =  Keyboard.KEY_DOWN;
      World.gameCharacters.add(Doge);
      World.gameCharacters.add(Doge2);

   }

   public static int getLowerBounds(ArrayList<PhysicsObject> Objects, double x, double y)
   {
      int heighest = -100;
      for(int i=0;i<Objects.size();i++)
      {
         if(Objects.get(i).x<x && Objects.get(i).sx>x)
         {
            int height = (int)Objects.get(i).getPoint(x);
            if(height <= y && height > heighest)
            {
               heighest = height;
            }
          }
       }
       return heighest;
   }
   public static int getLowerBound(ArrayList<PhysicsObject> Objects, double x, double y)
   {
      int heighest = -100;
      int rIndex = 0;
      for(int i=0;i<Objects.size();i++)
      {
         if(Objects.get(i).x<x && Objects.get(i).sx>x)
         {
            int height = (int)Objects.get(i).getPoint(x);
            if(height <= y && height > heighest)
            {
               heighest = height;
               rIndex = i;
            }
          }
       }
       return rIndex;
   }
   public static double[] getPhysics(ArrayList<PhysicsObject> Objects, double x, double y, double vely) //returns {position, velocity down}.
   {
      y+=3;
      int lowerBounds = getLowerBounds(Objects,x,y);
      double returnDouble[] = new double[2];
      if(vely+World.getGravity()>objectFloor)
      {
         returnDouble[0]=vely+World.getGravity();
      }
      else
      {
         returnDouble[0] = -100;
      }
      if(y+(vely/World.FPS)*10>lowerBounds)
      {
         returnDouble[1]= (y-3)+(vely/World.FPS)*10;
      }
      else
      {
         returnDouble[1]=lowerBounds;
      }
      return returnDouble;   
   }
   public static double getGravity()
   {
      return Gravity/(FPS/20);
   }

   public static void move(double amount)
   {
      if(GAMETYPE.equals("SPLIT"))
      {
         int FarthestForward = -1;
         int FarthestBackward = 99999;
         for(int i=0;i<gameCharacters.size();i++)
         {
            FantasyFighter TheFighter = gameCharacters.get(i);
            if(TheFighter.xPos>FarthestForward)
            {
               FarthestForward = (int)TheFighter.xPos;
            }
            
            if(TheFighter.xPos<FarthestBackward)
            {
               FarthestBackward = (int)TheFighter.xPos;  
            }
         }
         int WindowPosition=(int)(-(x+amount));
         boolean less = WindowPosition>FarthestBackward;
         boolean greater = WindowPosition+World.ResolutionX<(FarthestForward+World.CharacterSizeX);
   
         if(!less&&!greater&&BackgroundX>ResolutionX)
         {
            if(x+amount>0)
            {
               x=0;
            }
            else if(x+amount<-(BackgroundX-ResolutionX))
            {
               x=-(BackgroundX-ResolutionX);
            }
            else
            {
               x+=amount;
            }
         }   
      }
      else
      {
        
      }
   }
   public static void wait(int milliseconds)
   {  
      long ctime = System.currentTimeMillis();
      while(System.currentTimeMillis()-ctime<milliseconds)
      {}
   } 
   public static GLImage setImage(String path)
   {
      return setImage(path,false);
   }
   public static GLImage setImage(String path,boolean ref,boolean load)
   {
      if(ref)
      {
         path = World.PATH+path;
      }
      path = path.toLowerCase();
      if(StringsOfImages.indexOf(path)!=-1)
      {
         return Images.get(StringsOfImages.indexOf(path));
      }
      GLImage tempImg = new GLImage(path);
      if(load)
         tempImg.loadTexture();
      Images.add(tempImg);
      StringsOfImages.add(path);
      return tempImg;
   }
   public static GLImage setImage(String path, boolean ref)
   {
      return setImage(path,ref,true);
   }
   public static Font setFont(int FontFormat,String FontPath,int style, int size)
   {
      String detectString = FontPath+","+style+","+size;
      if(StringsOfFonts.indexOf(detectString)!=-1)
      {
         return Fonts.get(StringsOfFonts.indexOf(detectString));
      }
      Font theFont = new Font(FontPath,style,size);
      try
      {
         theFont = Font.createFont(FontFormat,new File(FontPath)).deriveFont(style,size);
      }
      catch (Throwable t){;}
      Fonts.add(theFont);
      StringsOfFonts.add(detectString);
      return theFont;
      
   }
   public static void loadWorld(String name)
   {
      String absPath = World.PATH+"Worlds/"+name+"/";
      File folder= new File(absPath);
      for(File fil : folder.listFiles())
      {
         String path = fil.getName();
         int dotDex = path.indexOf(".");
         if(path.substring(0,dotDex).equalsIgnoreCase("background"))
            World.Background = setImage(absPath+path,false);
      }
      World.BackgroundX = World.Background.data.getWidth(null);
      World.BackgroundY = World.Background.data.getHeight(null);
      String line = null;
      try
      {
         BufferedReader r = new BufferedReader(new FileReader(absPath+"data.txt"));
         String editing = null;
         try
         {
            while((line = r.readLine())!=null)
            {
               if(line.charAt(0) == '#')
               {
                  editing = line.substring(1,line.length());
               }
               else
               {
                  String[] parts = line.split(",");
                  if(editing.equals("PHYSICS"))
                  {
                     if(parts.length>=4)
                     {
                        int a,b,c,d;

                        double stretchx = World.ResolutionX>World.BackgroundX?(double)ResolutionX/BackgroundX:1;     
                        double stretchy = World.ResolutionY>World.BackgroundY?(double)ResolutionY/BackgroundY:1;   
                        a = (int)(stretchx * (double)Integer.valueOf(parts[0]));
                        b = (int)(stretchy * (double)Integer.valueOf(parts[1]));
                        c = (int)(stretchx * (double)Integer.valueOf(parts[2]));
                        d = (int)(stretchy * (double)Integer.valueOf(parts[3]));
                        if(parts.length==4)
                           Objects.add(new PhysicsObject(a,b,c,d));
                        else
                           Objects.add(new PhysicsObject(a,b,c,d,(double)Double.valueOf(parts[4])));
                     }
                     else
                     {
                        System.out.println("WARNING: "+line+" contains bad amount of parameters. (correct:4 or 5)");
                     }
                  }

                  else if(editing.equals("RESOLUTION"))
                  {
                     if(parts.length!=2)
                        System.out.println("WARNING: "+line+" contains bad amount of parameters. (correct:2)");
                     else
                     {
                        World.BackgroundX = (int)Integer.valueOf(parts[0]);
                        World.BackgroundY = (int)Integer.valueOf(parts[1]);
                     }         

                  }
                  else if(editing.equals("LIGHTS"))
                  {
                     if(parts.length!=6)
                          System.out.println("WARNING: "+line+" contains bad amount of parameters. (correct:6)");
                     else
                     {
                        float a,b;   
                        a = (Float.valueOf(parts[1]));
                        b = (World.BackgroundY-Float.valueOf(parts[2]));
                        lights.add(new Light(Float.valueOf(parts[0]),a,b,
                                             Float.valueOf(parts[3]),Float.valueOf(parts[4]),Float.valueOf(parts[5])));
                     }

                  }
               }
            }
         }catch(IOException e){}   
      }catch(FileNotFoundException e){System.out.println("file not found: "+absPath+"data.txt");}
    }
    public static float[] makeFloatArray(ArrayList<Float> arr) {
      Float[] farr = new Float[arr.size()];
      farr = arr.toArray(farr);
      float[] kek = new float[arr.size()];
      for(int i=0;i<arr.size();i++)
      {
         kek[i] = farr[i].floatValue();
      }
      return kek; 
   
   }
   public static FloatBuffer makeFloatBuffer(float[] arr) {
    ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
    bb.order(ByteOrder.nativeOrder());
    FloatBuffer fb = bb.asFloatBuffer();
    fb.put(arr);
    fb.rewind();
    return fb;
  }
}
class soundThread extends Thread
{
   File	soundFile;
   public int playframe;
   public soundThread(String filename)
   {
      playframe = World.framecount;
      soundFile = new File(filename);
   }

   public void run()
   {
      playframe = World.framecount;
     //read sound file
	   AudioInputStream	audioInputStream = null;
	   try
		{
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
      AudioFormat	audioFormat = audioInputStream.getFormat();
		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
												 audioFormat);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);
			//open audio line
			line.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		line.start();//start recieving data

		int	nBytesRead = 0;
		byte[]	abData = new byte[128000];
		while (nBytesRead != -1&&World.music)
		{
			try
			{
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead >= 0)
			{
				int	nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}
      //wait to finish sound				
		line.drain();

		/*
		  sound finished
		*/
		line.close();
      
   }

}



class KeyController implements KeyListener
{
   boolean quit;
   public KeyController()
   {
     quit = false;
   }
   public void keyPressed(KeyEvent key) {
     World.keys[key.getKeyCode()] = true;
   }
   public void keyReleased(KeyEvent key) { 
     World.keys[key.getKeyCode()] = false;   
   }
   
   public void keyTyped(KeyEvent e) { /* ... */ }
}

class MouseController implements MouseListener
{
   boolean quit;
   public MouseController()
   {
     quit = false;
   }
   public void mousePressed(MouseEvent button) { 
     World.mouse[button.getButton()] = true;
     System.out.println("kek: "+button.getButton());
   }
   public void mouseReleased(MouseEvent button) { 
     World.mouse[button.getButton()] = false;   
   }
   
   public void mouseExited(MouseEvent e) { /* ... */ }
   public void mouseClicked(MouseEvent e) { /* ... */ }
   public void mouseEntered(MouseEvent e) { /* ... */ }
}   

class PhysicsObject
{
   int x,y,sx,sy;
   double friction;
   public ArrayList<String> Properties;
   public PhysicsObject(int a, int b, int c, int d)
   {
      x=a;
      y=b;
      sx=c;
      sy=d;
      friction = 0;

   }   
   public PhysicsObject(int a, int b, int c, int d, double f)
   {
      x=a;
      y=b;
      sx=c;
      sy=d;
      friction = f;

   }
   public double getSlope()
   {
      return (double)(sy-y)/(sx-x);
   }
   public double getIntercept()
   {
      return (double)(sy-(getSlope()*sx));
   }
   public double getPoint(double point)
   {
      return getSlope()*point+getIntercept();   
   }
}


class Packet implements Serializable 
{
   public String header;
   public Object data;
   
   public Packet(String h, Object d)
   {
      header = h;
      data = d;
   }
   public Packet()
   {
      header = "null";
      data = new Object();
   }
}

class Data implements Serializable
{
   ArrayList<GameObject>gObjects;
   ArrayList<FantasyFighterData>gCharacters;
   public Data()
   {
      gObjects = new ArrayList<GameObject>();
      gCharacters = new ArrayList<FantasyFighterData>(); 
   }
}


class FantasyFighterData implements Serializable
{
   public String ImagePath;
   public int facing, health, magic, gameID;
   public double xPos, yPos;
   
   public FantasyFighterData(String ip, int f, int h, int m, int gid, double xp, double yp)
   {
      ImagePath = ip;
      facing = f;
      health = h;
      magic = m;
      gameID = gid;
      xPos = xp;
      yPos = yp;
   }
}   