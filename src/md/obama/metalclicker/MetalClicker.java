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
	
	protected int fps = 60;
	protected int ticks=0;
	protected int mx, my, gridx, gridy, tilex, tiley;
	
	//Building stats
	public int stat_id;                                                    //id type of building
	public int stat_level;                                                 //Level of building
	public int stat_gridx, stat_gridy;                                     //Grid location on the map
	public double stat_metalCost=0, stat_metalUsed=0, stat_metalGen=0;     //Metal  related
	public double stat_energyCost=0, stat_energyUsed=0, stat_energyGen=0;  //energy
	public double stat_clickGen=0;                                         //Metal click boost
	private double stat_boostGen=0, stat_boostBy=0;
	
	//Buildings cost
	private int type1metal  = 10; //extractor
	private int type1energy = 2;
	private int type2metal  = 20; //booster
	private int type2energy = 1;
	private int type3metal  = 8;  //solar panel
	private int type3energy = 0;
	
	//Game stats
	public double metalBank=10000;
	public double metalBankShow=0;
	public double metalPerSecond=1;
	public long metalClicked=0;
	public double metalPerClick=1;
	public double metalFromClicking=0;
	public double metalFromBuildings=0;
	public double base_mps=0.1;
	public double base_energy=2;
	//Energy
	public double energyMax=0; //Max allowed energy
	public double energyUse=0; //Current energy consumption
	
	private long lastFrame;

	private boolean somethingIsSelected=false;
	private boolean placing = false;
	private boolean moving = false;
	private boolean selling = false;
	
	private int place_id;
	private String place_texture;
	
	Tile tile = new Tile();
	
	private Texture texture;

	private Building selecting=null;
	
	DecimalFormat df = new DecimalFormat("#.###");
	

	public MetalClicker() {
		init();             //initialize
		lastFrame = getTime();
		
		Building ding = new Building(1,5,5,"test");
		builds.add(ding);
		grid[5][5] = ding;
		buttons.add(new Button("buyextractor",320, 32, "extractor", 32, 32, 1, this, 32, 32));
		buttons.add(new Button("buybooster",320, 64, "booster", 32, 32, 1, this, 32, 32));
		buttons.add(new Button("buysolarpanel",320, 96, "solarpanel", 32, 32, 1, this, 32, 32));
		buttons.add(new Button("upgrade",16, 380, "upgrade", 64, 32, 2, this, 48, 16));
		
		tile.generateLevel();

		
		while (!Display.isCloseRequested()){
            glClear(GL_COLOR_BUFFER_BIT);
            //Clear the buffer before we start
            
            int delta = getDelta();
            mx = Mouse.getX()+1;
            my = HEIGHT - Mouse.getY()+1;
            gridx = Math.round(((mx-XOFFSET)/32));
            gridy = Math.round(((my-16)/32));
    		tilex = Math.round(((mx-XOFFSET)/16));
            tiley = Math.round(((my-16)/16));
            
            metalBankShow += ((metalBank - metalBankShow)/(fps/3)); 
            //We will display the metal as a constant increase instead of a jump.
            //We want a sudden burst so it slows down to where the play can see it
            
            
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
		metalPerSecond = base_mps;             //Base metal per second
		double income = getIncome();
		metalPerSecond += income;
		if(add){                                  //if false, we dont add
			metalBank += this.metalPerSecond;
			metalFromBuildings += metalPerSecond;
		}
		updateEnergy();
	}

	private void updateEnergy() {
		energyMax = 2;              //base reset
		energyUse = 0;                //base reset
		double enTemp, boost;
		float cutt;
		for(Building ding : builds){
			enTemp=0;
			enTemp += ding.energyGen;
			
			cutt = getBoostCutt(ding.id);
			boost = getBoost(ding.gridx, ding.gridy);
			if (boost != 0) boost = (boost/cutt);
			boost+=1;  //Reset the base boost multiplier to 1
			enTemp *= boost;
			
			ding.boostBy = boost;
			
			energyMax += enTemp;
			energyUse += ding.energyUsed;
		}
	}

	private float getBoostCutt(int id) {
		if(id==1) return 0.9f;    //extractor
		if(id==2) return 1f;    //booster
		if(id==3) return 1.75f;    //solar panel // Boost is halved.
		return 1;
	}

	private double getIncome() {
		double meGen=0, meGenTemp;
		double enGen=0, enGenTemp;
		double boost;
		float cutt;
		for(Building ding : builds){
			if (ding.id==1){ //Extractor
				meGenTemp = ding.metalGen;
				
				cutt = getBoostCutt(ding.id);
				boost = getBoost(ding.gridx, ding.gridy);
				if (boost != 0) boost = (boost/cutt);
				boost+=1;
				
				meGenTemp *= boost;
				meGen += meGenTemp;
			}
		}
		return meGen;
	}

	private double getBoost(int x, int y) {
		double boost = 0;
		if (x!=14 && grid[x+1][y]!=null) boost += grid[x+1][y].boostGen;
		if (x!=0  && grid[x-1][y]!=null) boost += grid[x-1][y].boostGen;
		if (y!=14 && grid[x][y+1]!=null) boost += grid[x][y+1].boostGen;
		if (y!=0  && grid[x][y-1]!=null) boost += grid[x][y-1].boostGen;
		
		return boost;
	}

	private void input() {
		while (Keyboard.next()) {
			if(Keyboard.getEventKeyState()){
				if (Keyboard.getEventKey() == Keyboard.KEY_1) {
					selectExtractor();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_2) {
					selectBooster();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_3) {
					selectSolarPanel();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_M) {
					metalBank += 1000000000000L;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_G) {
					tile.generateLevel();
				}
				
				if (Keyboard.getEventKey() == Keyboard.KEY_C) {
					System.out.println("clicked C");
					if(tile.containsShapeless(16,2, "metaltl0", "metaltr0", "metalbl0", "metalbr0")){
						selecting = grid[5][5];
						pressedButton("upgrade");
					}
				}
				
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
				if(placing){
					if(gridx >= 0 && gridx <= 14 && gridy >= 0 && gridy <= 14) {
						if(grid[gridx][gridy] == null) {
							int meCost = getTypeCostMetal(place_id);
							int enCost = getTypeCostEnergy(place_id);
							if (tile.canPlaceHere(place_id)){
								if(metalBank >= meCost && energyMax >= (energyUse + enCost) ) {
									metalBank-= meCost;
									addTypeCostMetal(place_id, 1);
									Building ding = new Building(place_id, gridx, gridy, place_texture);
									builds.add(ding);
									grid[gridx][gridy] = ding;
									setStartStats(place_id, ding);
									System.out.println("Placed "+ getName(place_id));
									placing = false;
								}
							}
						} else System.out.println("Grid location already used");
					}
				}
			}else if(Mouse.isButtonDown(1)){ //Right click
					placing = false;
					moving  = false;
			}
		}
	}

	
	private void setStartStats(int id, Building ding) {
		if(id==1){ //extractor 
			ding.metalGen   = 1;
			ding.metalCost  = 15;
			ding.metalUsed  = type1metal;
			ding.energyCost = 1.25f;
			ding.energyUsed = type1energy;
		}
		if(id==2){ //Booster
			ding.metalCost  = 25;
			ding.metalUsed  = type2metal;
			ding.energyCost = 1f;
			ding.energyUsed = type2energy;
			ding.boostGen   = 0.0825f;
		}
		if(id==3){ //Solar panel
			ding.metalCost = 11;
			ding.metalUsed = type3metal;
			int tx = gridx*2;
			int ty = gridy*2;
			float sand=0;
			if(tile.getTile(tx, ty)=="sand") sand+=0.1f;    //Placing in the desert gives bonus
			if(tile.getTile(tx+1, ty)=="sand") sand+=0.1f;
			if(tile.getTile(tx, ty+1)=="sand") sand+=0.1f;
			if(tile.getTile(tx+1, ty+1)=="sand") sand+=0.1f;
			ding.energyPlus = sand;
			ding.energyGen = 1+sand;
		}
		updateEnergy();
	}

	private int getTypeCostMetal(int id) {
		if(id==1) return type1metal;    //extractor
		if(id==2) return type2metal;
		if(id==3) return type3metal;
		return 0;
	}
	
	private int getTypeCostEnergy(int id) {
		if(id==1) return type1energy;    //extractor
		if(id==2) return type2energy;
		if(id==3) return type3energy;
		return 0;
	}
	private void addTypeCostMetal(int id, int add) { //add = How much to add by, can be negative number
		if(id==1) type1metal += add;     //extractor
		if(id==2) type2metal += (add*3); //Booster, increase by 3
		if(id==3) type3metal += add;     //solar panel
	}

	private void selectExtractor() {
		//Random gen = new Random(System.currentTimeMillis());
		placing = true;
		place_id = 1;       //id of the building. extractor
		place_texture = "extractor0";//+gen.nextInt(3);
		texture = loadTexture(place_texture);
	}
	
	private void selectBooster() {
		Random gen = new Random(System.currentTimeMillis());
		placing = true;
		place_id = 2;       //id of the building. Booster
		place_texture = "booster0";//+gen.nextInt(3);
		texture = loadTexture(place_texture);
	}
	
	private void selectSolarPanel() {
		//Random gen = new Random(System.currentTimeMillis());
		placing = true;
		place_id = 3;       //id of the building. solar panel
		place_texture = "solarpanel0";//+gen.nextInt(3);
		texture = loadTexture(place_texture);
	}

	private void pressedButton(String name) {
		switch(name) {
			case "buyextractor":
				System.out.println(name);
				selectExtractor();
				break;
			case "buybooster":
				System.out.println(name);
				selectBooster();
				break;
			case "buysolarpanel":
				System.out.println(name);
				selectSolarPanel();
				break;
				
			case "upgrade":
				System.out.println(name + ": " + getName(selecting.id));
				double enCost = selecting.energyCost;
				double meCost = selecting.metalCost;
				double enIncr = selecting.energyGen;
				float cutt = getBoostCutt(selecting.id);
				double boost = getBoost(selecting.gridx, selecting.gridy);
				if (boost != 0) boost = (boost/cutt)+1;
				boost+=1;
				enIncr*=boost;
				//if the upgrade of the energy with the boost adds onto it
				if (metalBank >= meCost && (energyMax+enIncr) >= (energyUse + enCost) ) {
					metalBank -= meCost;
					     if(selecting.id==1) upgradeExtractor();
					else if(selecting.id==2) upgradeBooster();
					else if(selecting.id==3) upgradeSolarPanel();
					
					
					selecting.level+=1;
					selecting.energyUsed += selecting.energyCost;
					getStats(selecting);//update visuals, Show the new stats
					perSecond(false);   //update visuals, false so we don't add metal.
				} else System.out.println("not enough metal and/or energy");
				break;
			
		}
		
	}

	private void upgradeExtractor() {
		selecting.metalCost  = (selecting.metalCost+1.5)*1.19;
		selecting.metalGen = (selecting.metalGen+0.70+(selecting.level/50))*1.08765f;
		selecting.energyCost = (selecting.energyCost+0.01)*1.075;
	}
	
	private void upgradeBooster() {
		selecting.metalCost  = (selecting.metalCost+3)*1.3;
		selecting.energyCost = (selecting.energyCost)*1.10345;
		selecting.boostGen   = (selecting.boostGen)*1.080;
	}
	
	private void upgradeSolarPanel() {
		selecting.metalCost  = (selecting.metalCost+2)*1.28;
		//selecting.energyGen   = (selecting.energyGen+1)*1.02; //metal clicker 1 formula
		selecting.energyGen   = (double) ((double) selecting.energyGen+ (double) selecting.energyPlus)+((double) selecting.level/10); //
		selecting.boostGen   = (selecting.boostGen+0.005);
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
		
		glEnable(GL_TEXTURE_2D);
		renderTiles();
		glDisable(GL_TEXTURE_2D);
		
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
		
		//Energy bar
		glDisable(GL_TEXTURE_2D);
		float eny = (float) (energyUse/energyMax);
		glColor4f(0f+eny, 0f, 1f-eny, 0.4f);
		eny*=480;
		glRecti(XOFFSET-8, (int) (16+eny), XOFFSET, (int) (eny + (496-eny)));
		glColor4f(1f, 1f, 0f, 1f);
		glRecti(XOFFSET-5, (int) (16+eny), XOFFSET-3, (int) (eny + (496-eny)));
		//glColor4f(0f, 0.8f, 0f, 0.4f);
		//glRecti(XOFFSET-8, 16, XOFFSET, (int) (16+eny));
		glEnable(GL_TEXTURE_2D);
		
		
		int tilex = Math.round(((mx-XOFFSET)/16));
        int tiley = Math.round(((my-16)/16));
		
		font.drawString(0, 52, mx + ", " + my, Color.yellow);
		font.drawString(0, 66, gridx + ", " + gridy, Color.yellow);
		font.drawString(0, 80, tilex + ", " + tiley, Color.yellow);
		font.drawString(354, 32, String.valueOf(type1metal), Color.red);
		font.drawString(354, 46, String.valueOf(type1energy), Color.blue);
		font.drawString(354, 64, String.valueOf(type2metal), Color.red);
		font.drawString(354, 78, String.valueOf(type2energy), Color.blue);
		font.drawString(354, 96, String.valueOf(type3metal), Color.red);
		font.drawString(354, 110, String.valueOf(type3energy), Color.blue);
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

	private void renderTiles() {
		for(int xx=0; xx<30; xx++){
			for(int yy=0; yy<30; yy++){
				tile.getTileBind(xx, yy);
				int x = (xx*16)+XOFFSET;
				int y = (yy*16)+16;
				
				glColor3f(1,1,1); //White
				glBegin(GL_QUADS);
					glTexCoord2f(0,0);
					glVertex2i(x,y);
					glTexCoord2f(1,0);
					glVertex2i(x+16,y);
					glTexCoord2f(1,1);
					glVertex2i(x+16,y+16);
					glTexCoord2f(0,1);
					glVertex2i(x,y+16);
				glEnd();
			}
		}
	}

	private void drawInfo() { //Draw information like energy and metal 
		font.drawString(0, 0, "Energy Limit:", Color.blue);
		font.drawString(200, 0, makeString(energyMax), Color.blue);
		font.drawString(0, 14, "Energy used:", Color.blue);
		font.drawString(200, 14, makeString(energyUse), Color.blue);
		
		font.drawString(0, 482, "Current Metal:", Color.red);
		font.drawString(200, 482, makeString(metalBankShow), Color.orange);
		font.drawString(300, 482, makeString(metalBank - metalBankShow), Color.orange);
		font.drawString(0, 496, "Metal per second:", Color.red);
		font.drawString(200, 496, makeString(metalPerSecond), Color.orange);
		
	}

	private void drawStats() { //Draw building stats when selected
		font.drawString(0, 200, "Level:", Color.yellow);
		font.drawString(200, 200, String.valueOf(stat_level), Color.yellow);
		font.drawString(0, 214, "Cost in Metal:", Color.yellow);
		font.drawString(200, 214, makeString(stat_metalCost), Color.yellow);
		drawMoreStats();
		
		if(metalBank <= selecting.metalCost){
			font.drawString(16, 400, "Need metal!   in... " + df.format(Math.ceil((selecting.metalCost-metalBank)/metalPerSecond)), Color.red);
		}
		if((energyMax) <= (energyUse + selecting.energyCost)){
			font.drawString(16, 414, "Need energy!", Color.blue);
		}
	}

	private void drawMoreStats() {
		if(stat_id==1){
			font.drawString(0, 228, "Cost in Energy:", Color.yellow);
			font.drawString(200, 228, makeString(stat_energyCost), Color.yellow);
			font.drawString(0, 242, "Total energy used:", Color.yellow);
			font.drawString(200, 242, makeString(stat_energyUsed), Color.yellow);
			font.drawString(0, 260, "Base Metal production:", Color.yellow);
			font.drawString(200, 260, makeString(stat_metalGen), Color.yellow);
			font.drawString(0, 274, "Metal production boosted:", Color.yellow);
			font.drawString(200, 274, makeString(stat_metalGen * stat_boostBy), Color.yellow);
			font.drawString(0, 288, "Boostiplier:", Color.yellow);
			font.drawString(200, 288, makeString(stat_boostBy), Color.yellow);
		}
		if(stat_id==2){
			font.drawString(0, 228, "Cost in Energy:", Color.yellow);
			font.drawString(200, 228, makeString(stat_energyCost), Color.yellow);
			font.drawString(0, 242, "Total energy used:", Color.yellow);
			font.drawString(200, 242, makeString(stat_energyUsed), Color.yellow);
			font.drawString(0, 260, "Boosts by:", Color.yellow);
			font.drawString(200, 260, makeString(stat_boostGen), Color.yellow);
			//font.drawString(0, 274, "Metal production boosted:", Color.yellow);
			//font.drawString(200, 274, makeString(stat_metalGen * stat_boostBy), Color.yellow);
			//font.drawString(0, 288, "Boostiplier:", Color.yellow);
			//font.drawString(200, 288, makeString(stat_boostBy), Color.yellow);
		}
		if(stat_id==3){
			font.drawString(0, 228, "Energy produced:", Color.yellow);
			font.drawString(200, 228, makeString(stat_energyGen), Color.yellow);
			font.drawString(0, 242, "Energy produced boosted:", Color.yellow);
			font.drawString(200, 242, makeString(stat_energyGen * selecting.boostBy), Color.yellow);
			font.drawString(0, 260, "Boostiplier:", Color.yellow);
			font.drawString(200, 260, makeString(selecting.boostBy), Color.yellow);
			font.drawString(0, 274, "Boosts by:", Color.yellow);
			font.drawString(200, 274, makeString(stat_boostGen), Color.yellow);
			//font.drawString(0, 288, "Boostiplier:", Color.yellow);
			//font.drawString(200, 288, makeString(stat_boostBy), Color.yellow);
		}
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
		stat_boostGen   = ding.boostGen;
		stat_boostBy    = ding.boostBy;
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
		make = Math.abs(make);
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
		font = new UnicodeFont(new java.awt.Font ("Vani", Font.BOLD, 12));
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
		tile.initTextures(this);
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
		glShadeModel(GL_SMOOTH);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glViewport(0,0,WIDTH,HEIGHT);
		glMatrixMode(GL_MODELVIEW);
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
