import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;

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
	boolean itemBlock;
	boolean canRemove;
	boolean isRacket;
	RectBlock(int _x, int _y, int _w, int _h, boolean _r){
		x=_x;
		y=_y;
		w=_w;
		h=_h;
		canRemove = _r;
		collideFlag = 0;
		isRacket = false;
		int temp = (int)(Math.random()*10);
		if(temp<5)
			itemBlock=false;
		else
			itemBlock=true;
	}
	@Override
	void draw(Graphics2D g) {
		if(canRemove==false) {
			if(isRacket==false) {
				c = new Color(100,100,100);
//				GradientPaint gp = new GradientPaint(x+w/2,y-2, new Color(100, 100, 100), x+w/2, y+h+2, new Color(0,0,0));
//				g.setPaint(gp);
//				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}
			else {
				c = new Color(150,100,100);
//				GradientPaint gp = new GradientPaint(x+w/2,y-2, new Color(251, 198, 198), x+w/2, y+h+2, new Color(55,37,37));
//				g.setPaint(gp);
//				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}
		}
		else {
			if(itemBlock==true){
				c = new Color(200,200,0);
//				GradientPaint gp = new GradientPaint(x+w/2,y-2, new Color(253, 253, 0), x+w/2, y+h+2, new Color(70,70, 0));
//				g.setPaint(gp);
//				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
			}	
			else{
				c = new Color(150,100,150);
//				GradientPaint gp = new GradientPaint(x+w/2,y-2, new Color(253, 200, 253), x+w/2, y+h+2, new Color(53,35, 53));
//				g.setPaint(gp);
//				g.fill(new Rectangle2D.Float(x-3, y-3, w+6, h+6));
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

class Ball extends GameObject{
	float x, y, r;
	float prev_x, prev_y;
	float vx, vy;
	Color c;
	int stage;
	
	Ball(float x2, float y2, int _stage){
		x = x2;
		y = y2;
		r = 5;
		stage = _stage;
		
		float speed = 100;
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
		if(y-r>800)
			return true;
		return false;
	}

	@Override
	void collisionResolution(GameObject in) {
		RectBlock wall = (RectBlock) in;
		if(wall.isCollide(this)==false) return;
		// 벽의 왼쪽, 오른쪽, 위, 아래에서 공이 부딪힐 때 공의 좌표 및 방향 변환
		// 벽의 위쪽
		if(prev_y+r<wall.y) {
			y=wall.y-r; vy=-vy;
		}
		// 벽의 아래쪽
		if(prev_y-r>wall.y+wall.h) {
			y=wall.y+wall.h+r; vy=-vy;
		}
		// 벽의 왼쪽
		if(prev_x+r<wall.x) {
			x=wall.x-r; vx=-vx;
		}
		// 벽의 오른쪽
		if(prev_x-r>wall.x+wall.w) {
			x=wall.x+wall.w+r; vx=-vx;
		}
	}

	@Override
	boolean isRemove() {
		return false;
	}
}

class Hw5GamePanel extends JPanel implements KeyListener, Runnable{
	int stage; // 게임 스테이지
	int width, height;
	float dt = 1/30.0f; // 공의 변화가 1/30초에 한번씩 일어나게 하기 위함
	LinkedList<GameObject> objs = new LinkedList<GameObject>();
	RectBlock racket = new RectBlock(300,605,170,30, false);
	Hw5GamePanel(){
		racket.isRacket=true;
		stage = 2;
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
		
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		for(GameObject o : objs)
			o.draw(g2);
		racket.draw(g2);
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_LEFT){
			if(racket.x>20)
				racket.x-=20;
		}
		if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
			if(racket.x<=590)
				racket.x+=20;
		}
		repaint();
	}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void run() {
		Ball bonusBall1, bonusBall2;
		float temp_x, temp_y;
		try {
			while(true) {
				for(GameObject o : objs)
					o.update(dt);
				// 공과 벽돌이 충돌했을 때
				for(GameObject o1 :objs) {
					if(o1 instanceof Ball) {
						// 라켓과 공이 충돌했을 때
						o1.collisionResolution(racket);
						for(GameObject o2:objs) {
							if(o1==o2) continue;
							if(o2 instanceof RectBlock)
								o1.collisionResolution(o2);
						}
					}
				}
				
				Iterator<GameObject> it = objs.iterator();
				while(it.hasNext()) {
					// 공이 바닥으로 떨어질때
					if(it.next().isOut()==true)
						it.remove();
				}
				
				Iterator<GameObject> it2 = objs.iterator();
				while(it2.hasNext()) {
					// 공이 바닥으로 떨어질때
					if(it2.next().isRemove()==true)
						it2.remove();
				}
				
				repaint();
				Thread.sleep((int)(dt*1000));
			}
		}
		catch(InterruptedException e){
			
		}
	}
	
}

class Hw5StartPanel extends JPanel{
	
}

class Hw5EndPanel extends JPanel{
	
}

class Hw5Panel extends JPanel{
	
	
	Hw5Panel(){
		
	}
}

public class Hw5 extends JFrame {
	Hw5(){
		setTitle("Java Homework5");
		setSize(800,800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(new Hw5GamePanel());
		setVisible(true);
	}
	public static void main(String[] args) {
		new Hw5();
	}
}
