import java.util.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Graphics.*;
import java.net.ServerSocket;
import java.net.Socket;


   
public class dogeServer
{

   public static void main(String ar[]) throws IOException
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
