package md.obama.learning;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

// Terminology:
// - Vertex: a point in either 2D or 3D space
// - Primitive: a simple shape consisting of one or more vertices

public class TimerDemo {

	public static final int WIDTH=880, HEIGHT=512;
	private static enum State {
		INTRO, MAIN_MENU, GAME;
	}
	
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
	
	private State state = State.INTRO;
	
    public TimerDemo(){
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
        
    	int x = 100;
    	int y = 100;
    	int dx = 1;
    	int dy = 1;
    	
        
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
       lastFrame = getTime();
        
        
        while (!Display.isCloseRequested()) {
            // Clear the 2D contents of the window.
            glClear(GL_COLOR_BUFFER_BIT);
            
            
            //render();
            //checkInput();
            
            
            int delta = getDelta();
            x += delta * dx * 0.1;
            y += delta * dy * 0.1;
            System.out.println(getDelta());
            
            glRecti(x,y, x+30, y+30);
            
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
        new TimerDemo();
    }
}








