package md.obama.metalclicker;

import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import md.obama.metalclicker.Building;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import static org.lwjgl.opengl.GL11.*;

public class MetalClicker {
	
	private List<Building> builds = new ArrayList<Building>(225);
	
	private UnicodeFont font;
	
	public static final int WIDTH=880, HEIGHT=512;
	public static final int GRIDSIZE = 15;
	public static final int XOFFSET=384;
	
	private boolean antiAlias = true;
	private boolean draw_grid = true;
	
	private int fps = 60;
	private int ticks=0;
	private int mx, my, gridx, gridy;
	
	//Building stats
	public int stat_id;                                                    //id type of building
	public int stat_level;                                                 //Level of building
	public int stat_gridx, stat_gridy;                                     //Grid location on the map
	public double stat_metalCost=0, stat_metalUsed=0, stat_metalGen=0;     //Metal  related
	public double stat_energyCost=0, stat_energyUsed=0, stat_energyGen=0;  //energy
	public double stat_clickGen=0;                                         //Metal click boost
	
	
	private long lastFrame;

	private boolean somethingIsSelected=false;

	private Building selecting=null;
	
	DecimalFormat df = new DecimalFormat("#.##");
	

	public MetalClicker() {
		init();             //initialize
		lastFrame = getTime();
		
		builds.add(new Building(1,5,5,"test"));
		
		while (!Display.isCloseRequested()){
            glClear(GL_COLOR_BUFFER_BIT);
            //Clear the buffer before we start
            
            int delta = getDelta();
            mx = Mouse.getX()+1;
            my = HEIGHT - Mouse.getY()+1;
            gridx = Math.round(((mx-XOFFSET)/32));
            gridy = Math.round(((my-16)/32));
            
            
            render();
            //System.out.println(delta);
            ticks++;
            if(ticks%fps==0) { //every second do this.
            	//System.out.println(ticks/fps);
            }
            
            
            Display.update();
            Display.sync(fps);
        }
        Display.destroy();
        System.exit(0);
		
	}
	
	private void render() {
		Color.white.bind();
		glDisable(GL_TEXTURE_2D);
		
		glColor4f(0.1f, 1f, 0.1f, 1f);
		glRecti(0,0, WIDTH, HEIGHT); //Whole background
		
		glColor4f(0.5f, 0.5f, 0.5f, 1f);
		glRecti(0,0, XOFFSET, HEIGHT);
		glRecti(864,0, 880, HEIGHT);
		glRecti(XOFFSET,0, 880, 16);
		glRecti(XOFFSET, 496, 880, 512);
		
		if(draw_grid){
			for(int i = 1; i<15; i++){
				glBegin(GL_LINES);
					glVertex2i(XOFFSET + (i*32),16); //2d, integer
					glVertex2i(XOFFSET + (i*32),496);
				glEnd();
			}
			
			for(int i = 0; i<15; i++){
				glBegin(GL_LINES);
					glVertex2i(XOFFSET,16  + (i*32)); //2d, integer
					glVertex2i(864,16  + (i*32));
				glEnd();
			}
		}
		

		glEnable(GL_TEXTURE_2D);
		
		for(Building ding : builds){
			if(Mouse.isButtonDown(0) && ding.inBounds(gridx, gridy) && ding!=selecting){
				selecting = ding;
				ding.selected = true;
				System.out.println("You clicked me!");
				
				getStats(ding);
			}
			ding.draw();
		}
		
		font.drawString(0, 0, mx + ", " + my, Color.yellow);
		font.drawString(0, 24, gridx + ", " + gridy, Color.yellow);
		
		if(selecting!=null){ //if we selected a building, Display its stats.
			drawStats();
		}
	}

	private void drawStats() {
		font.drawString(0, 200, "Level:", Color.yellow);
		font.drawString(200, 200, String.valueOf(stat_level), Color.yellow);
		font.drawString(0, 214, "Cost in Metal:", Color.yellow);
		font.drawString(200, 214, makeString(stat_metalCost), Color.yellow);
		font.drawString(0, 228, "Cost in Energy:", Color.yellow);
		font.drawString(200, 228, makeString(stat_energyCost), Color.yellow);
		font.drawString(0, 242, "Total energy used:", Color.yellow);
		font.drawString(200, 242, makeString(stat_energyUsed), Color.yellow);
	}

	private void getStats(Building ding) {
		stat_id         = ding.id;
		stat_level      = ding.level;
		stat_gridx      = ding.gridx;
		stat_gridy      = ding.gridy;
		stat_metalCost  = ding.metalCost;
		stat_metalUsed  = ding.metalUsed;
		stat_metalGen   = ding.metalGen;
		stat_energyCost = ding.energyCost;
		stat_energyUsed = ding.energyUsed;
		stat_energyGen  = ding.energyGen;
		stat_clickGen   = ding.clickGen;

	}

	public String makeString(Double make){
		if(make>=1000000000) {
			return df.format(make/1000000000)+"b"; }
		else if(make>=1000000) {
				return df.format(make/1000000)+"m"; }
			else if(make>=1000) {
				return df.format(make/1000)+"k"; }
		return df.format(make);
	}
	
	
	private void initFont() {
		//Font awtFont = new Font("Times New Roman", Font.BOLD, 12);
		//font = new TrueTypeFont(awtFont, true);
		font = new UnicodeFont(new java.awt.Font ("Verdana", Font.BOLD, 12));
		font.getEffects().add(new ColorEffect(java.awt.Color.white));
		font.addNeheGlyphs();
		try {
			font.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}

	}


	
	public void init() {
		initGL();       //Setup openGL
		initFont();     //Setup our fonts
	}

	private void initGL() {
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.setTitle("Metal Clicker 2");
			Display.create();
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			Display.destroy();
			System.exit(0);
		}
		
		
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0,WIDTH,HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		
		glEnable(GL_TEXTURE_2D);
		//glShadeModel(GL_SMOOTH);
		//glDisable(GL_DEPTH_TEST);
		//glDisable(GL_LIGHTING);
		
		//glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//glClearDepth(1);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		//glViewport(0,0,WIDTH,HEIGHT);
		//glMatrixMode(GL_MODELVIEW);
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
	
	public static void main(String[] args) {
		new MetalClicker();
	}
}
