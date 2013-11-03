package md.obama.mc2d;

import java.awt.Font;
import java.io.File;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUseProgram;

// Terminology:
// - Vertex: a point in either 2D or 3D space
// - Primitive: a simple shape consisting of one or more vertices

public class Boot {

	public static final int WIDTH=640, HEIGHT=480;
	//public BlockGrid grid;
	private BlockType selection = BlockType.STONE;
	
	private TrueTypeFont font;
	private static DecimalFormat formatter = new DecimalFormat("#.##");
	private static FloatBuffer perspectiveProjectionMatrix = reserveData(16);
	private static FloatBuffer orthographicProjectionMatrix = reserveData(16);
	
	private int selector_x=0, selector_y=0;
	
	
    public Boot(){
    	try {
            // Sets the width of the display to 640 and the height to 480
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle("Minecraft 2d");
            // Creates and shows the display
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    	
    	//grid = new BlockGrid();
    	//grid.setAt(10, 10, BlockType.STONE);
        
        
        glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
        font = new TrueTypeFont(awtFont, false);
        while (!Display.isCloseRequested()) {
            // Clear the 2D contents of the window.
            glClear(GL_COLOR_BUFFER_BIT);
            
            
            //render();
            //checkInput();
            input();
            
            //grid.draw();
            //drawSelectionBox();

            font.drawString(100, 50, "THE LIGHTWEIGHT JAVA GAMES LIBRARY", Color.yellow);
            
            
            
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        System.exit(0);
    }
    
    
    private static FloatBuffer reserveData(int size) {
    	FloatBuffer data = BufferUtils.createFloatBuffer(size);
    	return data;
    }
    
    private void drawSelectionBox() {
    	int x = selector_x * World.BLOCK_SIZE;
    	int y = selector_y * World.BLOCK_SIZE;
    	int x2 = x + World.BLOCK_SIZE;
    	int y2 = y + World.BLOCK_SIZE;
    	if (/*grid.getAt(selector_x, selector_y).getType() != BlockType.AIR ||*/ selection == BlockType.AIR) {
    		glBindTexture(GL_TEXTURE_2D, 0);
    		glColor4f(1f, 1f, 1f, 0.5f); //50 percent alpha
    		glBegin(GL_QUADS);
    			glVertex2f(x,y);
    			glVertex2f(x2, y);
    			glVertex2f(x2,y2);
    			glVertex2f(x,y2);
    		glEnd();
    		glColor4f(1f, 1f, 1f, 1f); //reset transparency 
    	}
    	else {
    		glColor4f(1f, 1f, 1f, 0.5f); //50 percent alpha
    		//new Block(selection, selector_x, selector_y);
    		new Block(selection, selector_x * World.BLOCK_SIZE, selector_y * World.BLOCK_SIZE).draw();

    		glColor4f(1f, 1f, 1f, 1f); //reset transparency 
    	}
    }
    
    private void input() {
    	int mousex = Mouse.getX();
    	int mousey = HEIGHT - Mouse.getY() - 1;
    	boolean mouseClicked = Mouse.isButtonDown(0);
    	int selector_x = Math.round(mousex / World.BLOCK_SIZE);
    	int selector_y = Math.round(mousey / World.BLOCK_SIZE);
    	if(mouseClicked){

    		//System.out.println(grid_x + ", " + grid_y);
    		//grid.setAt(selector_x, selector_y, selection);
    	}
    	
    	while (Keyboard.next()) {
    		if(Keyboard.getEventKey() == Keyboard.KEY_RIGHT && Keyboard.getEventKeyState()) {
    			if(!(selector_x + 1 > World.BLOCKS_WIDTH - 2)) {
    				selector_x += 1;
    			}
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_LEFT && Keyboard.getEventKeyState()) {
    			if(!(selector_x - 1 < 0)) {
    				selector_x -= 1;
    			}
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_UP && Keyboard.getEventKeyState()) {
    			if(!(selector_y - 1 < 0)) {
    				selector_y -= 1;
    			}
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_DOWN && Keyboard.getEventKeyState()) {
    			if(!(selector_y + 1 > World.BLOCKS_HEIGHT - 2)) {
    				selector_y += 1;
    			}
    		}
    		
    		
    		//SAVE AND LOAD
    		if(Keyboard.getEventKey() == Keyboard.KEY_S) {
    			//grid.save(new File("save.xml"));
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_L) {
    			//grid.load(new File("save.xml"));
    		}
    		//CHANGING OF BLOCK TYPE
    		if(Keyboard.getEventKey() == Keyboard.KEY_1) {
    			selection = BlockType.STONE;
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_2) {
    			selection = BlockType.DIRT;
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_3) {
    			selection = BlockType.GRASS;
    		}
    		if(Keyboard.getEventKey() == Keyboard.KEY_4) {
    			selection = BlockType.AIR;
    		}
    		
    		//CLEAR
    		if(Keyboard.getEventKey() == Keyboard.KEY_C) {
    			//grid.clear();
    		}
    		
    		if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
    			Display.destroy();
    			System.exit(0);
    		}
    	}
    }
   
    public static void main(String[] args) {
        new Boot();
    }
}








