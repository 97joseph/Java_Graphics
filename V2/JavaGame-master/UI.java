import org.lwjgl.input.Mouse;;
class UI
{
   String imagePath, text;
   float[] color;
   int x, y;
   int sizex, sizey;
   
   public UI()
   {
      color = new float[4];
      imagePath = "";
      text = "";
   }
   public void setImage(String path)
   {
      imagePath = path;
   }
   public void setText(String t)
   {
      text=t;
   }
   public void setColor(float r, float g, float b, float a)
   {
      color[0] = r;
      color[1] = g;
      color[2] = b;
      color[3] = a;
   }
   public void setPos(int a, int b)
   {
      x=a;
      y=b;
   }
   public void setSize(int a, int b)
   {
      sizex=a;
      sizey=b;
   }
   public void setPosRelative(float a, float b)
   {
      setPos((int)(a*World.ResolutionX),(int)(b*World.ResolutionY));
   }
   public void setSizeRelative(float a, float b)
   {
      setSize((int)(a*World.ResolutionX),(int)(b*World.ResolutionY));
   }
   public boolean mouseIn()
   {
      return mouseIn(0);
   }
   public boolean mouseIn(int threshold)
   {
      int mousey = World.ResolutionY-Mouse.getY();
      return ((Mouse.getX()-x >threshold && (sizex+x)-Mouse.getX()>threshold)
          && (mousey-y >threshold &&(sizey+y)-mousey >threshold));
   }
   public void draw(GLSLRenderer g)
   {
      if(!imagePath.equals(""))
      {
         GLImage im = World.setImage(imagePath,false);
         im.drawText(text,1f,0f,1,1,1,World.PATH+"OCRAEXT.TTF",90);
         im.loadTexture();
         im.enable("text",0);
         g.setInt("useTextures",1);
      }
      else
      {
         g.setInt("useTextures",0);
      }
      g.setColor(color[0],color[1],color[2],color[3]);
      g.drawRect(x,y,sizex,sizey);
      
   }
}