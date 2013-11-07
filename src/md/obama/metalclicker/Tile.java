package md.obama.metalclicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Tile {
	
	private String[][] tiles = new String[30][30];
	
	private Texture tileGrass;
	private Texture tileWater0;
	private Texture tileWater1;
	private Texture tileWater2;
	//private Texture tileWater3;
	private Texture tileSand;
	private Texture tileSandCactus0;
	private Texture tileSandCactus1;
	private Texture tileSandCactus2;
	private Texture tileSandCactus3;
	private Texture tileMetalTL0;
	private Texture tileMetalTR0;
	private Texture tileMetalBL0;
	private Texture tileMetalBR0;
	
	protected MetalClicker game;
	
	public boolean containsShaped(int xx, int yy, String str1, String str2, String str3, String str4){
		if ((tiles[xx][yy]) ==( str1)
		 && (tiles[xx+1][yy]) == (str2)
		 && (tiles[xx][yy+1]) == (str3)
		 && (tiles[xx+1][yy+1]) == (str4)) {
					return true;
				}
		else return false;
	}

	public boolean containsShapeless(int xx, int yy, String str1, String str2, String str3, String str4){
		boolean bool=false;
		boolean[] blist = new boolean[4];
		blist[0] = false; blist[1] = false; blist[2] = false; blist[3] = false;
		String[] list = new String[4];
		list[0] = str1; list[1] = str2;
		list[2] = str3; list[3] = str4;
		for (int i=0; i<4; i++){
			for (int x=0; x<2; x++){
				for (int y=0; y<2; y++){
					if(list[i]=="") blist[i] = true;    //if its empty, set it to true as we are not looking for it
					else {
						if (tiles[xx+x][yy+y]==list[i]) blist[i] = true;
					}
				}
			}
		}
		if (blist[0] && blist[1] && blist[2] && blist[3]) 
			bool=true; //if all come out true, the grid contains what we are looking for.
		return bool;
	}
	
	public boolean canPlaceHere(int id) {
		int ttx = game.gridx*2;
		int tty = game.gridy*2;
		
		switch (id) {
			case 1:
				if(containsShaped(ttx,tty, "metaltl0", "metaltr0", "metalbl0", "metalbr0")){
					return true;
				}
				else System.out.println("Error, Extractor must be placed on metal deposits");
				break;
			case 2:
				if(containsShapeless(ttx,tty, "grass", "", "", "")){
					return true;
				}
				else System.out.println("Error, Booster must be placed on grass");
				break;
			case 3:
				if(containsShapeless(ttx,tty, "grass", "", "", "")){
					return true;
				}
				else if(containsShapeless(ttx,tty, "sand", "", "", "")){
					return true;
				}
				else System.out.println("Error, Solar must be placed on grass or sand");
				break;
		}
		return false;
	}
	
	public String getTile(int x, int y){
		return tiles[x][y];
	}
	
	public void initTextures(MetalClicker game){
		this.game = game;
		
		tileGrass = loadTexture("grass");
		tileWater0 = loadTexture("water0");
		tileWater1 = loadTexture("water1");
		tileWater2 = loadTexture("water2");
		//tileWater3 = loadTexture("water3");
		tileSand = loadTexture("sand");
		tileSandCactus0 = loadTexture("sandcactus0");
		tileSandCactus1 = loadTexture("sandcactus1");
		tileSandCactus2 = loadTexture("sandcactus2");
		tileSandCactus3 = loadTexture("sandcactus3");
		tileMetalTL0 = loadTexture("metaltl0");
		tileMetalTR0 = loadTexture("metaltr0");
		tileMetalBL0 = loadTexture("metalbl0");
		tileMetalBR0 = loadTexture("metalbr0");
	}
	
	public void generateLevel(){
		//Base default grass reset
		for(int x=0; x<30; x++){
			for(int y=0; y<30; y++){
				tiles[x][y] = "grass"; //can add possible random tiles from here. Most likely for metal deposits
			}
		}
		
		Random gen = new Random();
		int xx, yy, count=0, dir, pdir=5;
		
		xx = gen.nextInt(30);          
		yy = gen.nextInt(30);
		tiles[xx][yy] = "water"; //base start tile.
		dir = gen.nextInt(4);    //Random direcion
		while(count < 125){
			
			do{                  //to prevent going backwards (previous direction) 
				dir = gen.nextInt(4);
			} while (dir==0 && pdir==1 || dir==1 && pdir==0 || dir==3 && pdir==4 || dir==4 && pdir==3);
		
			pdir = dir;
			
			switch(dir){
				default:
    			case 0:
    				xx++;
    				if(xx>=30) {         //we don't want it to go out of the border, send it back.
    					xx=29;
    				} else {
    					tiles[xx][yy] = "water";
    					}
    				break;
    			case 1:
    				xx--;
    				if(xx<=-1) {
    					xx=0;
    				} else {
    					tiles[xx][yy] = "water";
    				}
    				break;
    			case 2:
    				yy++;
    				if(yy>=30) {
    					yy=29;
    				} else {
    					tiles[xx][yy] = "water";
    				}
    				break;
    			case 3:
    				yy--;
    				if(yy<=-1) {
    					yy=0;
    				} else {
    					tiles[xx][yy] = "water";
    				}
    				break;
			}
			count++;
		}
		//Now a sand section
		
		xx = gen.nextInt(30);
		yy = gen.nextInt(30);
		tiles[xx][yy] = "sand";
		dir = gen.nextInt(4);
		count=0;
		while(count < 75){
			do{                  //to prevent going backwards (previous direction) 
				dir = gen.nextInt(4);
			} while (dir==0 && pdir==1 || dir==1 && pdir==0 || dir==3 && pdir==4 || dir==4 && pdir==3);
		
			pdir = dir;
			
			switch(dir){
				default:
    			case 0:
    				xx++;
    				if(xx>=30) {
    					xx=29;
    				} else {
    					tiles[xx][yy] = "sand";
    					if(gen.nextInt(8)==1){
    						tiles[xx][yy] = "sandcactus"+gen.nextInt(4);
    					}
    				}
    				break;
    			case 1:
    				xx--;
    				if(xx<=-1) {
    					xx=0;
    				} else {
    					tiles[xx][yy] = "sand";
    					if(gen.nextInt(8)==1){
    						tiles[xx][yy] = "sandcactus"+gen.nextInt(4);
    					}
    				}
    				break;
    			case 2:
    				yy++;
    				if(yy>=30) {
    					yy=29;
    				} else {
    					tiles[xx][yy] = "sand";
    					if(gen.nextInt(8)==1){
    						tiles[xx][yy] = "sandcactus"+gen.nextInt(4);
    					}
    				}
    				break;
    			case 3:
    				yy--;
    				if(yy<=-1) {
    					yy=0;
    				} else {
    					tiles[xx][yy] = "sand";
    					if(gen.nextInt(8)==1){
    						tiles[xx][yy] = "sandcactus"+gen.nextInt(4);
    					}
    				}
    				break;
			}
			count++;
		}
    	count=0;
    	int attempts=0, gridX, gridY;
    	while(count <10 && attempts <= 500){
    		gridX = gen.nextInt(13);
    		gridY = gen.nextInt(13);
    		xx = (gridX*2)+2;
    		yy = (gridY*2)+2;

    		if(tiles[xx][yy]=="grass" && tiles[xx+1][yy]=="grass" &&
    				tiles[xx][yy+1]=="grass" && tiles[xx+1][yy+1]=="grass")
    		{	
    			tiles[xx][yy] = "metaltl0";
    			tiles[xx+1][yy] = "metaltr0";
    			tiles[xx][yy+1] = "metalbl0";
    			tiles[xx+1][yy+1] = "metalbr0";
        		count++;
        		attempts++;
    		}
    	}
	}
	
	public void getTileBind(int xx, int yy) {
		switch(tiles[xx][yy]) {
			case "grass":
				tileGrass.bind();
				break;
			case "water":
				int use = game.fps*2;      //speed control over how fast we change water tile.
				if(game.ticks%use<=(use/4)) tileWater0.bind();
				else if(game.ticks%use<=(use/4)*2) tileWater1.bind();
				else if(game.ticks%use<=(use/4)*3) tileWater2.bind();
				else if(game.ticks%use<=(use/4)*4) tileWater1.bind();
				break;
			case "sand":
				tileSand.bind();
				break;
			case "sandcactus0":
				tileSandCactus0.bind();
				break;
			case "sandcactus1":
				tileSandCactus1.bind();
				break;
			case "sandcactus2":
				tileSandCactus2.bind();
				break;
			case "sandcactus3":
				tileSandCactus3.bind();
				break;
			case "metaltl0":
				tileMetalTL0.bind();
				break;
			case "metaltr0":
				tileMetalTR0.bind();
				break;
			case "metalbl0":
				tileMetalBL0.bind();
				break;
			case "metalbr0":
				tileMetalBR0.bind();
				break;
		}
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
	
}
