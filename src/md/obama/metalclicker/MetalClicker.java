package md.obama.metalclicker;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import md.obama.metalclicker.Building;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;

public class MetalClicker {
	
	private List<Building> builds = new ArrayList<Building>(225);
	private Building[][] grid = new Building[15][15];
	
	private List<Button> buttons = new ArrayList<Button>(10); //Amount of buttons
	
	private UnicodeFont font;
	
	public static final int WIDTH=880, HEIGHT=512;
	public static final int GRIDSIZE = 15;
	public static final int XOFFSET=384;
	
	//private boolean antiAlias = true;
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
	
	//Game stats
	public double metalBank=8;
	public double metalPerSecond=1;
	public long metalClicked=0;
	public double metalPerClick=1;
	public double metalFromClicking=0;
	public double metalFromBuildings=0;
	
	private long lastFrame;

	private boolean somethingIsSelected=false;
	private boolean placing = false;
	private boolean moving = false;
	private boolean selling = false;
	
	private int place_id;
	private String place_texture;
	private Texture texture;

	private Building selecting=null;
	
	DecimalFormat df = new DecimalFormat("#.##");
	

	public MetalClicker() {
		init();             //initialize
		lastFrame = getTime();
		
		Building ding = new Building(1,5,5,"solarpanel");
		builds.add(ding);
		grid[5][5] = ding;
		buttons.add(new Button("buyextractor",320, 32, "extractor", 32, 32, 1, this));
		buttons.add(new Button("buysolarpanel",320, 96, "solarpanel", 32, 32, 1, this));
		buttons.add(new Button("upgrade",16, 380, "upgrade", 48, 16, 2, this));
		
		while (!Display.isCloseRequested()){
            glClear(GL_COLOR_BUFFER_BIT);
            //Clear the buffer before we start
            
            int delta = getDelta();
            mx = Mouse.getX()+1;
            my = HEIGHT - Mouse.getY()+1;
            gridx = Math.round(((mx-XOFFSET)/32));
            gridy = Math.round(((my-16)/32));
            
            input();
            
            render();
            //System.out.println(delta);
            ticks++;
            if(ticks%fps==0) { //every second do this.
            	//System.out.println(ticks/fps);
            	perSecond(true); //set to true to add per second
            	if(ticks % (fps*300) == 0) {
            		//TODO Save game here
            	}
            }
            
            
            Display.update();
            Display.sync(fps);
        }
        Display.destroy();
        System.exit(0);
		
	}
	
	private void perSecond(boolean add) { //This calls every second and updates resources
		metalPerSecond = 0.1;             //Base metal per second
		double income = getIncome();
		metalPerSecond += income;
		if(add){                                  //if false, we dont add
			metalBank += this.metalPerSecond;
			metalFromBuildings += metalPerSecond;
		}
	}

	private double getIncome() {
		double meGen=0, meGenTemp;
		double enGen=0, enGenTemp;
		double boost;
		for(Building ding : builds){
			if (ding.id==1){ //Extractor
				meGenTemp = ding.metalGen;
				
				boost = getBoost(ding.gridx, ding.gridy);
				
				meGenTemp *= boost;
				meGen += meGenTemp;
			}
		}
		return meGen;
	}

	private double getBoost(int x, int y) {
		double boost = 1;
		if (x!=14 && grid[x+1][y]!=null) boost += grid[x+1][y].boostGen;
		if (x!=0  && grid[x-1][y]!=null) boost += grid[x-1][y].boostGen;
		if (y!=14 && grid[x][y+1]!=null) boost += grid[x][y+1].boostGen;
		if (y!=0  && grid[x][y-1]!=null) boost += grid[x][y-1].boostGen;
		
		return boost;
	}

	private void input() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKey() == Keyboard.KEY_1 && Keyboard.getEventKeyState()) {
				System.out.println("pressed 1");
				selectExtractor();
			}
		}
		
		if(placing) {
			if(Mouse.isButtonDown(0)){ //Left click
				if(gridx >= 0 && gridx <= 14 && gridy >= 0 && gridy <= 14) {
					if(grid[gridx][gridy] == null) {
						Building ding = new Building(1, gridx, gridy, place_texture);
						builds.add(ding);
						grid[gridx][gridy] = ding;
						System.out.println("Placed "+ getName(place_id));
						placing = false;
					} else System.out.println("Grid location already used");
				}
			}
			else if(Mouse.isButtonDown(1)){ //Right click
				placing = false;
			}
		}
		while(Mouse.next()) {
			if(Mouse.isButtonDown(0) && Mouse.getEventButtonState()){ //Left click
				if (!(gridx >= 0 && gridx <= 14 && gridy >= 0 && gridy <= 14)) { //if clicked outside playfield
					for(Button bton : buttons) {
						if(bton.inBounds(mx, my)) {
							if(bton.canWeDoThis()){
								pressedButton(bton.name);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void selectExtractor() {
		Random gen = new Random(System.currentTimeMillis());
		placing = true;
		place_id = 1;       //id of the building. extractor
		place_texture = "test"+gen.nextInt(3);
		texture = loadTexture(place_texture);
	}

	private void pressedButton(String name) {
		switch(name) {
			case "buyextractor":
				System.out.println(name);
				selectExtractor();
				break;
			case "upgrade":
				System.out.println(name);
				selecting.level+=1;
				selecting.metalCost  = (selecting.metalCost+2)*1.21;
				selecting.metalGen = (selecting.metalGen+0.70)*1.065f;
				selecting.energyUsed += selecting.energyCost;
				selecting.energyCost = (selecting.energyCost+0.01)*1.075;
				getStats(selecting);//update visuals, Show the new stats
				perSecond(false);   //update visuals, false so we dont add metal.
				break;
			
		}
		
	}

	private String getName(int id) {
		String string = "ERROR";
		switch (id) {
			case 1:
				string = "Metal Extractor";
				break;
			case 2:
				string = "Booster";
				break;
			case 3:
				string = "Solar Panel";
				break;
			case 4:
				string = "Water Mill";
				break;
			case 5:
				string = "Clicking Hut";
				break;
		}
		return string;
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
		
		if(gridx >= 0 && gridx <= 14 && gridy >= 0 && gridy <= 14) {
			int x = (gridx*32)+MetalClicker.XOFFSET;
			int y = (gridy*32)+16;
			glColor3f(.75f, 0.5f, 0f);
			glBegin(GL_LINES);
				glVertex2i(x,y);
				glVertex2i(x,y+32);
				glVertex2i(x,y+32);
				glVertex2i(x+32,y+32);
				glVertex2i(x+32,y+32);
				glVertex2i(x+32,y);
				glVertex2i(x+32,y);
				glVertex2i(x,y);
			glEnd();
		}
		
		glEnable(GL_TEXTURE_2D);
		
		for(Building ding : builds){ //Draw buildings and get stats if pressed
			if(Mouse.isButtonDown(0) && ding.inBounds(gridx, gridy) && ding!=selecting){
				selecting = ding;
				ding.selected = true;
				getStats(ding);
			}
			ding.draw();
		}
		for(Button bton : buttons) { //Draw buttons
			//if(bton.show) {
				bton.draw();
			//}
		}
		
		
		font.drawString(0, 0, mx + ", " + my, Color.yellow);
		font.drawString(0, 24, gridx + ", " + gridy, Color.yellow);
		if(placing) {
			font.drawString(0, 38, "Placing = true", Color.green);
			if(gridx >= 0 && gridx <= 14 && gridy >= 0 && gridy <= 14) {
				texture.bind();
				int x = (gridx*32)+XOFFSET;
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
			}
		}
		else font.drawString(0, 38, "Placing = false", Color.red);
		
		
		if(selecting!=null){ //if we selected a building, Display its stats.
			drawStats();
		}
		drawInfo();
	}

	private void drawInfo() { //Draw information like energy and metal 
		font.drawString(0, 482, "Current Metal:", Color.red);
		font.drawString(200, 482, makeString(metalBank), Color.orange);
		font.drawString(0, 496, "Metal per second:", Color.red);
		font.drawString(200, 496, makeString(metalPerSecond), Color.orange);
		
	}

	private void drawStats() { //Draw building stats when selected
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
	
	public String makeString(Double make){
		if(make>=1000000000000000L) {
			return df.format(make/1000000000000000L)+"Q"; }
		else if(make>=1000000000000L) {
			return df.format(make/1000000000000L)+"T"; }
		else if(make>=1000000000) {
			return df.format(make/1000000000)+"B"; }
		else if(make>=1000000) {
				return df.format(make/1000000)+"M"; }
			else if(make>=1000) {
				return df.format(make/1000)+"K"; }
		return df.format(make);
	}
	
	
	private void initFont() {
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

	public boolean isSelling() {
		return selling;
	}

	public Building getSelecting() {
		return selecting;
	}

}
