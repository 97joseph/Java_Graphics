//lights
import java.util.ArrayList;
import org.lwjgl.opengl.GL20;

public class Light
{
   public static ArrayList<Light> lights = new ArrayList<Light>();
   float Intensity;
   float xPos, yPos;
   float r,g,b;
   public Light(float inten,float x, float y, float ra,float ba, float ga)
   {
      Intensity = inten;
      xPos = x;
      yPos = y;
      r=ra;
      b=ba;
      g=ga;
   }
   public void removeSelf()
   {
      if(lights.indexOf(this)!=-1)
         lights.remove(lights.indexOf(this));
   }
   public void render()
   {
     Light.lights.add(this);
     float[] lightPositionsX = new float[lights.size()];
     float[] lightPositionsY = new float[lights.size()];
     float[] lightIntensities = new float[lights.size()];
     float[] lightRedComponents = new float[lights.size()];
     float[] lightBlueComponents = new float[lights.size()];
     float[] lightGreenComponents = new float[lights.size()];
     for(int i=0;i<lights.size();i++)
     {
       lightPositionsX[i] = World.glRend.toGL(lights.get(i).xPos+World.x,0);
       lightPositionsY[i] = World.glRend.toGL(lights.get(i).yPos,1);
       lightIntensities[i] = lights.get(i).Intensity;
       lightRedComponents[i] = lights.get(i).r;
       lightBlueComponents[i] = lights.get(i).g;
       lightGreenComponents[i] = lights.get(i).b;
     }
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightX"),World.makeFloatBuffer(lightPositionsX));
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightY"),World.makeFloatBuffer(lightPositionsY));
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightR"),World.makeFloatBuffer(lightRedComponents));
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightG"),World.makeFloatBuffer(lightBlueComponents));
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightB"),World.makeFloatBuffer(lightGreenComponents));
     GL20.glUniform1(GL20.glGetUniformLocation(World.glRend.program,"lightIntensities"),World.makeFloatBuffer(lightIntensities));
     GL20.glUniform1i(GL20.glGetUniformLocation(World.glRend.program,"lightNum"),lights.size());
   }

}