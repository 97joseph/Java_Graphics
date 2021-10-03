import java.awt.Robot;

public class mainC
{
   public static void main(String arg[])
   {
   try {
        Robot robot = new Robot();

        // Simulate a mouse click
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        // Simulate a key press
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);

} catch (AWTException e) {
        e.printStackTrace();
}
}
}