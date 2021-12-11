import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;

abstract class GameObject{
	GameObject(){
	}
	
	abstract void draw(Graphics2D g); // 오브젝트를 화면에 그림
	abstract void update(float dt); // 시간에 따른 오브젝트의 변화
	abstract void collisionResolution(GameObject in); // 게임 오브젝트끼리 충돌했을 때 생기는 변화
	abstract boolean isOut();
	abstract boolean isRemove();
}

class RectBlock extends GameObject{
	int x, y, w, h;
	Color c;
	int collideFlag;
	int itemBlockCollisionFlag;
	boolean itemBlock;
	boolean canRemove;
	boolean isRacket;
	GradientPaint gp1, gp2, gp3, gp4;
	RectBlock(int _x, int _y, int _w, int _h, boolean _r){
		x=_x;
		y=_y;
		w=_w;
		h=_h;
		canRemove = _r;
		collideFlag = 0;
		itemBlockCollisionFlag = 0;
		isRacket = false;
		itemBlock=false;
		int temp = (int)(Math.random()*10);
		if(temp>=5 && canRemove==true)
			itemBlock=true;
		
		gp1 = new GradientPaint(x+w/2,y-2, new Color(100, 100, 100), x+w/2, y+h+2, new Color(0,0,0));
		gp2 = new GradientPaint(x+w/2,y-2, new Color(251, 198, 198), x+w/2, y+h+2, new Color(55,37,37));
		gp3 = new GradientPaint(x+w/2,y-2, new Color(253, 253, 0), x+w/2, y+h+2, new Color(70,70, 0));
		gp4 = new GradientPaint(x+w/2,y-2, new Color(253, 200, 253), x+w/2, y+h+2, new Color(53,35, 53));
	}
	@Override
	void draw(Graphics2D g) {
		if(canRemove==false) {
			if(isRacket==false) {
				c = new Color(100,100,100);
				g.setPaint(gp1);
				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}
			else {
				c = new Color(150,100,100);
				g.setPaint(gp2);
				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}
		}
		else {
			if(itemBlock==true){
				c = new Color(200,200,0);
				g.setPaint(gp3);
				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}	
			else{
				c = new Color(150,100,150);
				g.setPaint(gp4);
				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}	
		}
		g.setColor(c);
		g.fillRect(x, y, w, h);
	}
	@Override
	void update(float dt) {return;}
	@Override
	void collisionResolution(GameObject in) {
	}
	
	// 충돌이 일어났는지 확인하는 함수
	boolean isCollide(GameObject o) {
		Ball b = (Ball)o;
		if(b.y+b.r>y&&b.y-b.r<y+h&&b.x+b.r>x&&b.x-b.r<x+w){
			if(canRemove==true)
				collideFlag++;
			return true;
		}
		return false;
	}
	@Override
	boolean isOut() {
		return false;
	}
	@Override
	boolean isRemove() {
		if(collideFlag>0)
			return true;
		return false;
	}
}

class Ball extends GameObject implements LineListener{
	float x, y, r;
	float prev_x, prev_y;
	float vx, vy;
	Color c;
	int stage;
	Clip clip1, clip2, clip3;
	
	Ball(float x2, float y2, int _stage){
		try {
			clip1 = AudioSystem.getClip();
			clip2 = AudioSystem.getClip();
			clip3 = AudioSystem.getClip();
			URL url1 = getClass().getClassLoader().getResource("normalblock.wav");
			URL url2 = getClass().getClassLoader().getResource("itemblock.wav");
			URL url3 = getClass().getClassLoader().getResource("racket.wav");
			AudioInputStream normalBlockAudio = AudioSystem.getAudioInputStream(url1);
			AudioInputStream itemBlockAudio = AudioSystem.getAudioInputStream(url2);
			AudioInputStream racketAudio = AudioSystem.getAudioInputStream(url3);
			clip1.open(normalBlockAudio);
			clip2.open(itemBlockAudio);
			clip3.open(racketAudio);
		} catch (LineUnavailableException| UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clip1.addLineListener(this);
		clip2.addLineListener(this);
		clip3.addLineListener(this);
		
		x = x2;
		y = y2;
		r = 5;
		stage = _stage;
		
		float speed = 70.0f;
		vx = -speed-(stage*70);
		vy = -speed-(stage*70);
		prev_x = x;
		prev_y = y;
		c = Color.LIGHT_GRAY;
	}
	
	@Override
	void draw(Graphics2D g) {
		g.setColor(c);
		g.fillOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
	}

	@Override
	void update(float dt) {
		prev_x = x;
		prev_y = y;
		x = x + vx*dt;
		y = y + vy*dt;
	}
	
	boolean isOut() {
		if(y>700)
			return true;
		return false;
	}

	@Override
	void collisionResolution(GameObject in) {
		RectBlock wall = (RectBlock) in;
		if(wall.isCollide(this)==false) return;
		// 벽의 왼쪽, 오른쪽, 위, 아래에서 공이 부딪힐 때 공의 좌표 및 방향 변환
		// 벽의 위쪽
		if(prev_y+r<=wall.y) {
			y=wall.y-r; vy=-vy;
		}
		// 벽의 아래쪽
		if(prev_y-r>=wall.y+wall.h) {
			y=wall.y+wall.h+r; vy=-vy;
		}
		// 벽의 왼쪽
		if(prev_x+r<=wall.x) {
			x=wall.x-r; vx=-vx;
		}
		// 벽의 오른쪽
		if(prev_x-r>=wall.x+wall.w) {
			x=wall.x+wall.w+r; vx=-vx;
		}
		
		if(wall.itemBlock==true){
			wall.itemBlockCollisionFlag = 1;
			clip3.start();
			clip3.setFramePosition(0);
		}
		else if(wall.isRacket==false){
			clip1.start();
			clip1.setFramePosition(0);
		}
		else {
			clip2.start();
			clip2.setFramePosition(0);
		}
	}

	@Override
	boolean isRemove() {
		return false;
	}

	@Override
	public void update(LineEvent event) {
		// TODO Auto-generated method stub
		
	}
}

class Hw5GamePanel extends JPanel implements Runnable, LineListener{
	Hw5EndPanel ep;
	int currentScore = 0, highScore = 0;
	int stage; // 게임 스테이지
	int width, height;
	int ballCount, blockCount;
	int addBonusBallFlag = 0, clearFlag = 0;
	float tempX = 0, tempY = 0, tempVx = 0, tempVy = 0;
	float dt = 1/30.0f; // 공의 변화가 1/30초에 한번씩 일어나게 하기 위함
	Clip clip;
	LinkedList<GameObject> objs = new LinkedList<GameObject>();
	RectBlock racket = new RectBlock(300,605,170,30, false);
	Hw5GamePanel(int _s){
		try {
			clip = AudioSystem.getClip();
			URL url = getClass().getClassLoader().getResource("levelup.wav");
			AudioInputStream levelUpAudio = AudioSystem.getAudioInputStream(url);
			clip.open(levelUpAudio);
		} catch (LineUnavailableException| UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clip.addLineListener(this);
		
		racket.isRacket=true;
		stage = _s;
		blockCount = (stage+2)*(stage+2);
		ballCount = 1;
		width = 750;
		height = 350;
		
		// 게임 맵 벽과 공 생성
		objs.add(new RectBlock(0,0,800,15, false));
		objs.add(new RectBlock(0,0,15,800, false));
		objs.add(new RectBlock(770,0,800,800, false)); 
		objs.add(new Ball(392, 600, stage));
	
		for(int i=0;i<2+stage;i++) {
			for(int j=0;j<2+stage;j++)
			{
				objs.add(new RectBlock(j*(width/(stage+2))+23, i*(height/(stage+2))+20, width/(stage+2)-10, height/(stage+2)-10, true));
			}
		}
		
		Thread t = new Thread(this);
		t.start();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// 배경 색 적용
		Graphics2D g2 = (Graphics2D)g;
		GradientPaint gp = new GradientPaint(0,0,Color.black,0,getHeight(),new Color(127,127,167));
		g2.setPaint(gp);
		g2.fill(new Rectangle2D.Float(0,0,getWidth(),getHeight()));
		
		// 오브젝트 그리기
		for(GameObject o : objs)
			o.draw(g2);
		racket.draw(g2);
	}
	@Override
	public void run() {
		try {
			int size;
			while(true) {	
				if(clearFlag==0) {
					for(GameObject o : objs)
						o.update(dt);

					// objs의 값이 변경될 수 있기 때문에 미리 size 변수에 저장
					size = objs.size();
					for(int i=0;i<size;i++) {
						GameObject o = objs.get(i);
						// 공과 라켓의 충돌 검사
						if(o instanceof Ball) {
							o.collisionResolution(racket);
							for(int j=0;j<size;j++) {
								GameObject o2 = objs.get(j);
								// 공과 블록의 충돌 검사
								if(o2 instanceof RectBlock) {
									o.collisionResolution(o2);
									// 만약 블록이 보너스 블록이면 공을 추가 
									if(((RectBlock) o2).itemBlockCollisionFlag==1) {
										Ball b1 = new Ball(((Ball) o).x, ((Ball) o).y, stage);
										b1.vx = ((Ball) o).vx*0.8f;
										b1.vy = ((Ball) o).vy*1.2f;
										objs.add(b1);
										
										Ball b2 = new Ball(((Ball) o).x, ((Ball) o).y, stage);
										b2.vx = ((Ball) o).vx*1.2f;
										b2.vy = ((Ball) o).vy*0.8f;
										objs.add(b2);
										
										ballCount+=2;
										((RectBlock) o2).itemBlockCollisionFlag=0;
									}
								}
							}
						}
					}
					
					
					Iterator<GameObject> it3 = objs.iterator();
					while(it3.hasNext()) {
						// 공이 바닥으로 떨어질때
						if(it3.next().isOut()==true){
							ballCount--;
							it3.remove();
							if(ballCount==0)
								break;
						}
					}
					
					if(ballCount == 0){
						break;
					}
					
					Iterator<GameObject> it4 = objs.iterator();
					while(it4.hasNext()) {
						// 사라져야 하는 블록
						if(it4.next().isRemove()==true){
							it4.remove();
							currentScore+=10;
							if(currentScore>highScore)
								highScore=currentScore;
							blockCount--;
						}
					}
				}
				if(clearFlag==1) {
					clip.start();
					racket.x = 300;
					racket.y = 605;
					stage++;
					ballCount = 1;
					blockCount = (stage+2)*(stage+2);
					objs.add(new RectBlock(0,0,800,15, false));
					objs.add(new RectBlock(0,0,15,800, false));
					objs.add(new RectBlock(770,0,800,800, false)); 
					objs.add(new Ball(392, 600, stage));
					
					for(int i=0;i<2+stage;i++) {
						for(int j=0;j<2+stage;j++)
						{
							objs.add(new RectBlock(j*(width/(stage+2))+23, i*(height/(stage+2))+20, width/(stage+2)-10, height/(stage+2)-10, true));
						}
					}
					clearFlag=0;
					clip.setFramePosition(0);
					repaint();
				}
				
				if(blockCount==0) {
					objs.clear();
					clearFlag=1;
				}
				
				repaint();
				Thread.sleep((int)(dt*1000));
			}
			
			
		}
		catch(InterruptedException e){
			
		}
	}
	@Override
	public void update(LineEvent event) {
		// TODO Auto-generated method stub
		
	}
	
}

class Hw5StartPanel extends JPanel implements Runnable{
	String t1 = "Java Programming";
	String t2 = "Homework #5";
	String t3 = "Block Breaker";
	String t4 = "PRESS SPACEBAR TO PLAY!";
	int flickerFlag;
	Font f1, f2, f3;
	boolean startFlag;
	Hw5StartPanel(){
		flickerFlag = 0;
		startFlag=false;
		f1 = new Font("Arial", Font.PLAIN, 50);
		f2 = new Font("Arial", Font.BOLD, 80);
		f3 = new Font("Arial", Font.BOLD, 30);
		
		Thread t = new Thread(this);
		t.start();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint(0,0,Color.black,0,getHeight(),new Color(127,127,167));
		g2.setPaint(gp);
		g2.fill(new Rectangle2D.Float(0,0,getWidth(),getHeight()));
		
		g2.setColor(Color.white);
		g2.setFont(f1);
		g2.drawString(t1, 180, 180);
		g2.drawString(t2, 235, 250);
		
		g2.setFont(f2);
		g2.drawString(t3, 130, 430);
		
		g2.setColor(Color.red);
		g2.setFont(f3);
		g2.drawString(t4, 180, 600);
	}
	@Override
	public void run() {
		try {
			while(true) {
				if(flickerFlag==1) {
					t4 = "";
					flickerFlag=0;
				}
				else {
					t4 = "PRESS SPACEBAR TO PLAY!";
					flickerFlag=1;
				}
				Thread.sleep(100);
				repaint();
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Hw5EndPanel extends JPanel implements Runnable, LineListener{
	int flickerFlag;
	String t1 = "Game Over";
	String t2 = "High Score: ";
	String t3 = "Your Score: ";
	String t4 = "PRESS SPACEBAR!";
	Font f1, f2, f3;
	Clip clip;
	Hw5EndPanel(int highScore, int currentScore){
		try {
			clip = AudioSystem.getClip();
			URL url = getClass().getClassLoader().getResource("gameover.wav");
			AudioInputStream gameOverAudio = AudioSystem.getAudioInputStream(url);
			clip.open(gameOverAudio);
		} catch (LineUnavailableException| UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clip.addLineListener(this);
		clip.start();
		clip.setFramePosition(0);
		
		flickerFlag=0;
		t2+=highScore;
		t3+=currentScore;
		f1 = new Font("Arial", Font.BOLD, 80);
		f2 = new Font("Arial", Font.BOLD, 40);
		f3 = new Font("Arial", Font.PLAIN, 40);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		GradientPaint gp = new GradientPaint(0,0,Color.black,0,getHeight(),new Color(127,127,167));
		g2.setPaint(gp);
		g2.fill(new Rectangle2D.Float(0,0,getWidth(),getHeight()));
		
		g2.setColor(Color.white);
		g2.setFont(f1);
		g2.drawString(t1, 200, 200);
		
		g2.setColor(Color.gray);
		g2.setFont(f2);
		g2.drawString(t2, 250, 300);
		g2.drawString(t3, 250, 350);
		
		g2.setColor(Color.red);
		g2.setFont(f3);
		g2.drawString(t4, 200, 500);
	}

	@Override
	public void run() {
		try {
			while(true) {
				if(flickerFlag==1) {
					t4 = "";
					flickerFlag=0;
				}
				else {
					t4 = "PRESS SPACEBAR!";
					flickerFlag=1;
				}
				Thread.sleep(100);
				repaint();
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update(LineEvent event) {
		// TODO Auto-generated method stub
		
	}
}


public class Hw5 extends JFrame implements KeyListener, Runnable, LineListener{
	Hw5GamePanel gp;
	Hw5StartPanel sp;
	Hw5EndPanel ep;
	int highScore;
	int gameModeFlag;
	int vi;
	Clip clip;
	Hw5(){
		try {
			clip = AudioSystem.getClip();
			URL url1 = getClass().getClassLoader().getResource("gamemenu.wav");
			AudioInputStream gameMenuAudio = AudioSystem.getAudioInputStream(url1);
			clip.open(gameMenuAudio);
		} catch (LineUnavailableException| UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clip.addLineListener(this);
		clip.start();
		clip.setFramePosition(0);
		
		highScore = 0;
		gameModeFlag=1;
		setTitle("Java Homework5");
		setSize(800,800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		sp = new Hw5StartPanel();
		add(sp);
		
		Thread t = new Thread(this);
		t.start();
		
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		
		setVisible(true);
	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		
	}

	
	public static void main(String[] args) {
		new Hw5();
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// 게임 시작화면에서 스페이스바 누르면 게임 시작
		if(gameModeFlag==1 && e.getKeyCode()==KeyEvent.VK_SPACE) {
			clip.setFramePosition(0);
			clip.stop();
			gp = new Hw5GamePanel(1);
			this.remove(sp);
			this.add(gp);
			gameModeFlag=2;
			setVisible(true);
		}
		
		// 게임 실행 중 좌우 방향키를 이용해 라켓 이동
		if(gameModeFlag==2 && e.getKeyCode()==KeyEvent.VK_LEFT) {
			if(gp.racket.x>30)
				gp.racket.x-=(10+vi);
			vi++;
		}
		if(gameModeFlag==2 && e.getKeyCode()==KeyEvent.VK_RIGHT) {
			if(gp.racket.x<=580)
				gp.racket.x+=(10+vi);
			vi++;
		}
		
		if(gameModeFlag==3&&e.getKeyCode()==KeyEvent.VK_SPACE) {
			this.remove(ep);
			this.add(sp);
			gameModeFlag=1;
			clip.start();
			setVisible(true);
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		vi=0;
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(gameModeFlag==2) {
				while(true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(gp.ballCount==0){
						gameModeFlag=3;
						if(gp.highScore>highScore)
							highScore = gp.highScore;
						ep = new Hw5EndPanel(highScore, gp.currentScore);
						this.remove(gp);
						this.add(ep);
						setVisible(true);
						break;
					}
				}
			}
		}
	}

	@Override
	public void update(LineEvent event) {
		// TODO Auto-generated method stub
		
	}
}
