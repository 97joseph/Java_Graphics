import java.util.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.DisplayMode;



public class DogeMain
{
   public static void main(String arg[])
   {
      try{
         Display.setDisplayMode(new DisplayMode(World.ResolutionX,World.ResolutionY));
         Display.setVSyncEnabled(true);
         Display.setTitle("Battleground");
         Display.create();
      }
      catch(Exception e){
         System.out.println("Error setting up display");
         System.exit(0);
      }
      World.glRend = new GLSLRenderer(World.PATH+"Shaders/VERTEX.txt",World.PATH+"Shaders/FRAGMENT.txt");
      World.healthDropRate = 500;
      World.music = true;
      World.loadWorld("World1");
      World.Gravity = -9.81;
      UI fadeUI = new UI();
      fadeUI.setColor(1,0,0,.8f);
      fadeUI.setPosRelative(0,0);

      gamePlayer.splitscreen();
      Display.destroy();
      Mouse.destroy();
      Keyboard.destroy();
   }
   
   public static String gameModes[] = {"Two player", "Single player", "Multiplayer"};
   public static ArrayList<UI> UIs = new ArrayList<UI>();
}