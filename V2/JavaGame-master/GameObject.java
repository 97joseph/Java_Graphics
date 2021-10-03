//GameObjects
import java.io.*;
import java.util.Random;


abstract class GameObject implements Serializable
{
   double x,y;
   double vx,vy;
   double Value;
   String type;
   boolean Gravity;
   long tickMade;
   public GameObject(int a, int b,int velx, int vely, int d,String t)
   {
      x=a;
      y=b;
      Value = d;
      type = t;
      Gravity = false;
      vx = velx;
      vy = vely;
      tickMade = System.currentTimeMillis();
   }
   public GameObject(int a, int b,int velx, int vely, int d, String t, boolean g)
   {
      x=a;
      y=b;
      Value = d;
      type = t;
      Gravity = g;
      vx = velx;
      vy = vely;
      tickMade = System.currentTimeMillis();
   }
   public void removeSelf()
   {
      if(World.GameObjects.indexOf(this)!=-1)
      {
         World.GameObjects.remove(World.GameObjects.indexOf(this));
      }
   }
   public abstract void render(GLSLRenderer rend);
   public abstract void update();
}

class Box extends GameObject
{
   Random rn  = new Random();
   String path = "Textures/Craggy.png";
   int sizex, sizey;
   Box(int a, int b, int velx, int vely, int d, String t, boolean g)
   {
      super(a,b,velx,vely,d,t,g);
   }
   public void render(GLSLRenderer rend)
   {
          World.glRend.setInt("useTextures",1);
          World.glRend.setColor(1,1,1,1);
          GLImage BufIcon = World.setImage(path,true);     
          BufIcon.enable("text",0);
          rend.drawRect(World.x+(int)x,(World.ResolutionY-(int)y)-sizex,sizex,sizey);    
   }
   public void update()
   {
      x += vx/World.FPS;
      if(Gravity)
      {
        double vel[] = World.getPhysics(World.Objects, x, y, vy);
        vy = vel[0];
        y = vel[1];
      }
      else
      {    
        y += vy/World.FPS;
      }
          sizex =50;
          sizey =50;      
          
          if(type.equalsIgnoreCase("health"))
          {
            path = "Textures/HealthPack.png";
          }
          else if(type.equalsIgnoreCase("projectile"))
          {
            Light theLight =  new Light(.1f,(float)x+sizex/2,(float)y+sizey/2,1,0,0);
            theLight.render();
          }
          else if(type.equalsIgnoreCase("magic"))
          {
             path = "Textures/projectile.png";
          }
          else if(type.equalsIgnoreCase("explosion"))
          {
             path = "Textures/explosion.png";
             int change = rn.nextInt(10);
             Value+=change;
             sizex = (int)Value;
             sizey = (int)Value;
             x-=change/2;
             y-=change/2;
            if(System.currentTimeMillis()-tickMade>500)
            {
               removeSelf();
            }
          }
          else if(type.equalsIgnoreCase("hit"))
          {
           path = "Textures/marker.png";
            for(int f = 0;f<5;f++)
            {
               int addx = rn.nextInt(20)-10;
               int addy = rn.nextInt(20)-10;
               Light theLight =  new Light(.1f,(float)x+sizex/2,(float)y+sizey/2,1,0.54f,0);
               theLight.render();
               World.GameObjects.add(new Box((int)x+addx,(int)y+addy,0,0
                                                    ,0,"explosion",false));
            }
            if(System.currentTimeMillis()-tickMade>100)
            {
               removeSelf();
            }
          }
   }
}