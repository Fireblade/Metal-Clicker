package md.obama.learning.entity;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;


public class PongGame {

	public static final int WIDTH = 640, HEIGHT = 480;
	private boolean isRunning = true;
	
	private long lastFrame;
	private Ball ball;
	private Bat bat;

	
	public static void main(String[] args) {
		new PongGame();
	}
	
	public PongGame(){
		setUpDisplay();
		setUpOpenGL();
		setUpEntities();
		setUpTimer();
		while(isRunning){
			render();
			logic(getDelta());
			input();
			
			Display.update();
			Display.sync(60);
			if(Display.isCloseRequested()){
				isRunning = false;
			}
		}
		Display.destroy(); //when game is no longer running, End display.
	}
	
	private void input() {
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
			bat.setDY(-.2);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
			bat.setDY(.2);
		}
		else {
			bat.setDY(0);
		}
	}

	private long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	private int getDelta(){
		long currentTime = getTime();
		int delta = (int) (currentTime - lastFrame);
		lastFrame = currentTime;
		return delta;
	}
	private void setUpDisplay(){
    	try {
            // Sets the width of the display to 640 and the height to 480
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            
            Display.setTitle("Pong Demo");
            // Creates and shows the display
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
	}

	private void setUpOpenGL(){
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
		
	}
	
	private void setUpEntities(){
		bat = new Bat(10, (HEIGHT /2) - 80/2, 10, 80);
		ball = new Ball(WIDTH/2 - 10/2, HEIGHT /2 - 10 /2, 10, 10);
		ball.setDX(-0.1);
	}
	
	private void setUpTimer(){
		lastFrame = getTime();
	}
	
	private void render(){
		glClear(GL_COLOR_BUFFER_BIT);
		ball.draw();
		bat.draw();
		
		
	}
	
	private void logic(int delta){
		ball.update(delta);
		bat.update(delta);
		if (ball.getX() <= bat.getX() + bat.getWidth()
		&& ball.getX() >= bat.getX() && ball.getY() >= bat.getY()
		&& ball.getY() <= bat.getY() + bat.getHeight()){
			ball.setDX(0.3);
		}
			
	}
	
	private static class Bat extends AbstractMovableEntity {

		public Bat(double x, double y, double width, double height) {
			super(x, y, width, height);
		}

		@Override
		public void draw() {
			glRectd(x,y, x + width, y + height);
		}
		
	}
	
	private static class Ball extends AbstractMovableEntity {

		public Ball(double x, double y, double width, double height) {
			super(x, y, width, height);
		}

		@Override
		public void draw() {
			glRectd(x,y, x + width, y + height);
		}
		
	}
	
}
