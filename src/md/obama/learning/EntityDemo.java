package md.obama.learning;

import md.obama.learning.entity.AbstractEntity;
import md.obama.learning.entity.AbstractMovableEntity;
import md.obama.learning.entity.Entity;
import md.obama.learning.entity.MovableEntity;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

// Terminology:
// - Vertex: a point in either 2D or 3D space
// - Primitive: a simple shape consisting of one or more vertices

public class EntityDemo {
	
	private long lastFrame;
	private long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	private int getDelta(){
		long currentTime = getTime();
		int delta = (int) (currentTime - lastFrame);
		lastFrame = currentTime;
		return delta;
	}
	
	private static class Box extends AbstractMovableEntity {

		public Box(double x, double y, double width, double height) {
			super(x, y, width, height);
			// TODO Auto-generated constructor stub
		}
		
		@Override 
		public void draw(){
			glRectd(x, y, x+ width, y + height);
		}
	}
	
	private static class Point extends AbstractEntity {

		public Point(double x, double y) {
			super(x, y, 1, 1);
			
		}

		@Override
		public void draw() {
			glBegin(GL_POINTS);
				glVertex2d(x, y);
			glEnd();
		}

		@Override
		public void update(int delta) {
			//do nothing
		}
		
	}
	

	public static final int WIDTH=880, HEIGHT=512;
	private static enum State {
		INTRO, MAIN_MENU, GAME;
	}
	
	private State state = State.INTRO;
	
    public EntityDemo(){
    	try {
            // Sets the width of the display to 640 and the height to 480
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            
            Display.setTitle("StateDemo");
            // Creates and shows the display
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        //initialization for Entities
    	MovableEntity box = new Box(100, 100, 50, 50);
    	Entity point = new Point(10, 10);
    	
    	
        //Initialization code for OpenGl
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
        lastFrame = getTime();
        
        while (!Display.isCloseRequested()) {
            // Clear the 2D contents of the window.
            glClear(GL_COLOR_BUFFER_BIT);
            
            point.setlocation(Mouse.getX(), HEIGHT - Mouse.getY() - 1);
            
            int delta = getDelta();
            point.update(delta);
            box.update(delta);
            
            if (box.intersects(point)){
            	box.setDX(0.2);
            }
            
            point.draw();
            box.draw();
            
            
            
            //render();
            //checkInput();
            
            
            
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        System.exit(0);
    }
    
    private void render() {
    	switch(state){
    	case INTRO:
    		glColor3f(1.0f, 0f, 0f);
    		glRectf(0,0, 640, 480);
    		break;
    	case GAME:
    		glColor3f(0f, 1.0f, 0f);
    		glRectf(0,0, 640, 480);
    		break;
    	case MAIN_MENU:
    		glColor3f(0f, 0f, 1.0f);
    		glRectf(0,0, 640, 480);
    		break;
    	}
    }
    
    private void checkInput(){
    	switch (state) {
    	case INTRO:
    		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
    			state = State.MAIN_MENU;
    		}
    		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
    			Display.destroy();
    			System.exit(0);
    		}
    		break;
    	case GAME:
    		if (Keyboard.isKeyDown(Keyboard.KEY_BACK)){
    			state = State.MAIN_MENU;
    		}
    		break;
    	case MAIN_MENU:
    		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
    			state = State.GAME;
    		}
    		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
    			state = State.INTRO;
    		}
    		break;
    	}
    }
    
    
    public static void main(String[] args) {
        new EntityDemo();
    }
}








