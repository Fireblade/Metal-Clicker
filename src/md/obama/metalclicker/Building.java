package md.obama.metalclicker;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class Building {
	
	public int id;                                           //id type of building
	public int level=1;         //Start at level 1           //Level of building
	public int gridx, gridy;                                 //Grid location on the map
	public double metalCost=15, metalUsed=0, metalGen=0;     //Metal  related
	public double energyCost=1, energyUsed=0, energyGen=0;   //energy
	public double boostGen=0, boostBy=1;
	public double clickGen=0;
	
	public Texture texture;
	
	public boolean selected = false;
	private UnicodeFont font;
	
	Building(int id, int x, int y, String texture){
		this.id = id;
		this.gridx = x;
		this.gridy = y;
		this.texture = loadTexture(texture);
		initFont();
	}
	
	
	boolean inBounds(int mousex, int mousey){
		if(mousex == gridx && mousey == gridy){
			return true;
		}
		return false;
	}
	
	
	public void draw() {
		texture.bind();
		int x = (gridx*32)+MetalClicker.XOFFSET;
		int y = (gridy*32)+16;
		
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
		
		font.drawString(x+1, y+20, String.valueOf(level), Color.orange);
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
	
	
	private void initFont() {
		font = new UnicodeFont(new java.awt.Font ("Verdana", Font.BOLD, 10));
		font.getEffects().add(new ColorEffect(java.awt.Color.white));
		font.addNeheGlyphs();
		try {
			font.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getX() {
		return gridx;
	}
	public void setX(int x) {
		this.gridx = x;
	}
	public int getY() {
		return gridy;
	}
	public void setY(int y) {
		this.gridy = y;
	}
	public double getMetalCost() {
		return metalCost;
	}
	public void setMetalCost(double metalCost) {
		this.metalCost = metalCost;
	}
	public double getMetalUsed() {
		return metalUsed;
	}
	public void setMetalUsed(double metalUsed) {
		this.metalUsed = metalUsed;
	}
	public double getMetalGen() {
		return metalGen;
	}
	public void setMetalGen(double metalGen) {
		this.metalGen = metalGen;
	}
	public double getEnergyCost() {
		return energyCost;
	}
	public void setEnergyCost(double energyCost) {
		this.energyCost = energyCost;
	}
	public double getEnergyUsed() {
		return energyUsed;
	}
	public void setEnergyUsed(double energyUsed) {
		this.energyUsed = energyUsed;
	}
	public double getEnergyGen() {
		return energyGen;
	}
	public void setEnergyGen(double energyGen) {
		this.energyGen = energyGen;
	}
	public double getClickGen() {
		return clickGen;
	}
	public void setClickGen(double clickGen) {
		this.clickGen = clickGen;
	}

}
