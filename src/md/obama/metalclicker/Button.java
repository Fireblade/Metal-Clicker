package md.obama.metalclicker;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Button {
	
	public String name;                                      //Name of the button
	public int x, y;                                 //Grid location on the map
	
	public Texture texture;
	
	public boolean show = true;
	
	Button(String name, int x, int y, String texture){
		this.name = name;
		this.x = x;
		this.y = y;
		this.texture = loadTexture(texture);
		
	}
	
	boolean inBounds(int mousex, int mousey){
		if(mousex >= x && mousey >= y
			&& mousex <= x+32 && mousey <= y+32){
			return true;
		}
		return false;
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
	
	public void draw() {
		texture.bind();
		
		glColor3f(1,1,1); //White
		glBegin(GL_QUADS);
			glTexCoord2f(0,0);
			glVertex2i(x,y);
			glTexCoord2f(1,0);
			glVertex2i(x+32,y);
			glTexCoord2f(1,1);
			glVertex2i(x+32,y+32);
			glTexCoord2f(0,1);
			glVertex2i(x,y+32);
		glEnd();
	}
	
}
