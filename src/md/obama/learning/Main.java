package md.obama.learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;


public class Main {
	
	private Texture wood;
	
	public static final int WIDTH=880, HEIGHT=512;
	
	private List<Box> shapes = new ArrayList<Box>(16);
	private boolean somethingIsSelected = false;
	private volatile boolean randomColorCooldown = false;
	
	public Main(){
		try{
			Display.setDisplayMode(new DisplayMode(880,512));
			Display.setTitle("Metal Clicker 2");
			Display.create();
		} catch(LWJGLException e){
			e.printStackTrace();
		}
		
		shapes.add(new Box(15, 15));
		shapes.add(new Box(100,150));
		
		wood = loadTexture("planks_redstone");
		
		//Initialization code openGL
		glMatrixMode(GL_PROJECTION);  //state based projection, hey openGL, open.
		glLoadIdentity();
		glOrtho(0, 880, 512, 0, 1, -1); //will set the top left of the screen x,y to 0.. 1,-1 for non-3d
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
		
		
		
		while(!Display.isCloseRequested()) {
			//Render
			glClear(GL_COLOR_BUFFER_BIT);
			
			wood.bind();
			
			glColor3f(1,1,1); //White
			glBegin(GL_QUADS);
				glTexCoord2f(0,0);
				glVertex2i(400,400);
				glTexCoord2f(1,0);
				glVertex2i(450,400);
				glTexCoord2f(1,1);
				glVertex2i(450,450);
				glTexCoord2f(0,1);
				glVertex2i(400,450);
			glEnd();
				
				
			
			while (Keyboard.next()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
					shapes.add(new Box(15,15));
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
				Display.destroy();
				System.exit(0);
			}
			
			
			
//			int mousey = HEIGHT - Mouse.getY();
//			System.out.println(mousey);
			//a dynamic Y using getDY() needs to be -Mouse.getDY();
			
			for (Box box : shapes ){
				if(Mouse.isButtonDown(0) && box.inBounds(Mouse.getX(), HEIGHT - Mouse.getY())
						&& !somethingIsSelected){
					somethingIsSelected = true;
					box.selected = true;
					System.out.println("You clicked me!");
				}
				
				if (Mouse.isButtonDown(2) && box.inBounds(Mouse.getX(), HEIGHT - Mouse.getY()) //Middle mouse button
						&& !somethingIsSelected){ 
					box.randomizeColors();
					randomColorCooldown = true;
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} finally {
								randomColorCooldown = false;
							}
							
						}
						
					}).run();
				}
				
				if (Mouse.isButtonDown(1)){
					box.selected = false;
					somethingIsSelected = false;
				}
				
				if (box.selected){
					box.update(Mouse.getDX(), -Mouse.getDY());
				}
				
				box.draw();
			}
			
			
			/*glBegin(GL_QUADS);
				glVertex2i(400,400); //upper left
				glVertex2i(500,400); //upper right
				glVertex2i(500,500); //bottom right
				glVertex2i(400,500); //bottom left
			glEnd();
			
			glBegin(GL_LINES);
				glVertex2i(100,100); //2d, integer
				glVertex2i(200,200);
				glVertex2i(250,400);
				glVertex2i(100,100);
			glEnd();
			*/
			
			Display.update();
			Display.sync(60);
		}
		
		Display.destroy();
		
	}
	
	private Texture loadTexture(String key){
		try {
			return TextureLoader.getTexture("PNG", new FileInputStream(new File("res/" + key + ".png")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static class Box {
		public int x, y;
		public boolean selected = false;
		private float colorRed, colorBlue, colorGreen;
		
		Box(int x, int y){
			this.x = x;
			this.y = y;
			
			Random randomGenerator= new Random();
			colorRed = randomGenerator.nextFloat();
			colorBlue = randomGenerator.nextFloat();
			colorGreen = randomGenerator.nextFloat();
		}
		
		boolean inBounds(int mousex, int mousey){
			if(mousex > x && mousex < x+50
				&& mousey > y && mousey < y+50){
				return true;
			}
			return false;
		}
		
		void update(int dx, int dy) {
			x += dx;
			y += dy;
		}
		
		void randomizeColors(){
			Random randomGenerator= new Random();
			colorRed = randomGenerator.nextFloat();
			colorBlue = randomGenerator.nextFloat();
			colorGreen = randomGenerator.nextFloat();
		}
		
		void draw(){
			glColor3f(colorRed, colorBlue, colorGreen);
			
			glBegin(GL_QUADS);
				glVertex2f(x, y);
				glVertex2f(x + 50, y);
				glVertex2f(x + 50, y + 50);
				glVertex2f(x, y + 50);
			glEnd();
			
		}
	}

	public static void main(String[] args) {
		new Main();
	}

}
