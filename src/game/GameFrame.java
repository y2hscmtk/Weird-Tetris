package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

//게임이 진행될 프레임 생성
public class GameFrame extends JFrame {
	//게임을 총괄 관리하는 스레드
	private GameManageThread gameManagerThread;
	//블록 생성하는 스레드
	private BlockSpawnThread spawnThread;
	//생성한 블록이 떨어지도록 하는 스레드
	private FallingThread fallingThread;
	
	
	//생성될 블록의 래퍼랜스
	private Block block = null;
	
	//블록 래퍼랜스를 위치에 따라 저장하기 위해
	private Block[][] blockArray = new Block[10][10];
	
	//4가지 탐색 포인트 세로로 3칸, 대각선 3칸 * 2, 가로로 3칸
	private int[] dx = {-1,-1,0,1};
	private int[] dy = {0,1,1,1};
	
	//현재 블록이 있는지없는지에 따라 키 이동 이벤트가 결정됨
	private boolean blockStatus = false;
	
	//블록의 상태 표시 결정
	public void setBlockStatus(boolean bool) {
		this.blockStatus = bool;
	}
	
	//블록의 현재 존재상태 리턴 => 존재하면 true, 존재하지않으면 false
	public boolean isBlockExist() {
		return this.blockStatus;
	}

	//음악을 관리하는 클래스
	private Music music;
	
	//지워야할 벡터의 좌표를 보관할 벡터
	private Vector<Point> eraseVector = new Vector<Point>();
	
	//2차원 배열의 모든 인덱스를 돌면서 브루트포스
	public void checkAndModifyArray() {
		for(int i=0;i<blockArray.length;i++) {
			for(int j=0;j<blockArray.length;j++) {
				
				//해당좌표에 블록이 없다면 continue
				if(blockArray[i][j]==null)
					continue;
				
				//조사대상 블록의 타입 체크 => 어떤 색인지
				int blockType = blockArray[i][j].getType();
				
				for(int k=0;k<dx.length;k++) {
					int mi = i + dx[k];
					int mj = j + dy[k];
					
					//범위를 벗어나지 않는다면
					if(0<=mi&&mi<=9&&0<=mj&&mj<=9) {

						//해당좌표에 블록이 없다면 continue
						if(blockArray[mi][mj]==null)
							continue;
						
						//같은 타입의 블록이라면 
						if(blockArray[mi][mj].getType() == blockType) {
							//3칸의 조건이 만족되는지 확인하기위해
							int mi2 = i - dx[k];
							int mj2 = j - dy[k];
							
							//범위를 벗어나지 않는지 확인
							if(0<=mi2&&mi2<=9&&0<=mj2&&mj2<=9) {
								
								//해당좌표에 블록이 없다면 continue
								if(blockArray[mi2][mj2]==null)
									continue;
								
								//같은 타입의 블록인지를 확인
								if(blockArray[mi2][mj2].getType() == blockType) {
									//모두 만족한다면 벡터에 지워야할 벡터 삽입
									Point p1 = new Point(mi,mj);
									Point p2 = new Point(i,j);
									Point p3 = new Point(mi2,mj2);
									
									//모든 조건을 만족하므로 벡터에 같은 색 좌표들 삽입
									eraseVector.add(p1);
									eraseVector.add(p2);
									eraseVector.add(p3);
									//System.out.println(p1+" "+ p2+" "+p3);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
	
	public GameFrame() {
		setTitle("이상한 테트리스");
		setLayout(null);
		setSize(790,538);
		setVisible(true);
		
		//아이콘 설정
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image img = toolkit.getImage("tetris.png");
	    setIconImage(img);
		
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //x버튼을 눌러 프로그램을 종료하도록
	    //프레임이 생성될 위치를 지정(게임 화면 위치)
	    Dimension frameSize = this.getSize();
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    // (모니터화면 가로 - 프레임화면 가로) / 2, (모니터화면 세로 - 프레임화면 세로) / 2
	    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

	    this.setResizable(false); //크기 조절 불가능하게
	
		setContentPane(new GameRunningPanel());
		
		music = new Music("gameMusic.wav"); //시작화면 음악 삽입
	    music.loadAndStartMusic();
		
		//키 이동 이벤트
		
		//키가 눌렸을때 블록이 존재한다면 블록의 좌우 위치를 조절하도록 하기 위함
		addKeyListener(new KeyAdapter() {
			@Override
		    public void keyPressed(KeyEvent e) {
				if(isBlockExist()) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) { 
						//블록이 존재하는 동안에만 위의 과정을 실행해야함
						//영역을 벗어나지 않고, 해당위치에 블록이 없어야함
			        	int x = block.getX();
			        	int y = block.getY();
			        	if(0<=x-50&&x<=450) {
			        		if(blockArray[(x-50)/50][y/50+1]==null)
			        			block.setLocation(x-50,y);
			        	}
						//System.out.println(x-50);
			        }	
			        //오른쪽 키를 눌렀을 때
			        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			        	int x = block.getX();
			        	int y = block.getY();
			        	if(0<=x+50&&x+50<=450) {
			        		if(blockArray[(x+50)/50][y/50+1]==null)
			        			block.setLocation(x+50,y);
			        	}
			        		
			        }   
				}
		    }
		});
	}
	
	//게임이 진행되는 공간
	private class GameRunningPanel extends JPanel{
		//다음에 생성될 블록의 코드번호를 지정
		private int laterBlockCode;
		
		//점수 표시용 라벨
		private JLabel scoreLabel;
		
		//레벨 표시용 라벨
		private JLabel levelLabel;
		
		//캐릭터 표시용 라벨
		private JLabel characterLabel;
		
		//게임 오버 표시용 라벨
		private class GameOverLabel extends JLayeredPane{
			//게임 오버 이미지
			private ImageIcon gameOverImageIcon = new ImageIcon("gameOverImage.png");
			private Image image = gameOverImageIcon.getImage();
			
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.drawImage(image,0,0,gameOverImageIcon.getIconWidth(),gameOverImageIcon.getIconHeight(),null);
			}
		}
		
		private GameOverLabel gameOverLabel;
		
		
		//배경 이미지
		private ImageIcon backgroundImageIcon = new ImageIcon("background2.png");
		private Image backgroundImage = backgroundImageIcon.getImage();
		
		//캐릭터 이미지
		private ImageIcon characterIcon = new ImageIcon("character.png");
		
		
		//점수를 증가시킴
		public void plusScore(int score) {
			//현재 점수 가져오기
			int currentScore = Integer.parseInt(scoreLabel.getText());
			
			//score만큼 점수 올리기
			currentScore += score;
			
			//점수갱신
			scoreLabel.setText(Integer.toString(currentScore));
		}
				
		public GameRunningPanel() {
			setLayout(null);
			setSize(800,500);
			setVisible(true);
			
			//캐릭터 표시용
			characterLabel = new JLabel(characterIcon);
			characterLabel.setSize(characterIcon.getIconWidth(),characterIcon.getIconHeight());
			characterLabel.setLocation(535,300);
			add(characterLabel);
			
			
			
			//level단어 표시용
			JLabel level = new JLabel("Level : ");
			level.setFont(new Font("Arial Black",Font.BOLD,35));
			level.setSize(200,50);
			level.setForeground(Color.WHITE);
			level.setLocation(545,145);
			add(level);
			
			JLabel score = new JLabel("SCORE");
			score.setFont(new Font("Arial Black",Font.BOLD,50));
			score.setSize(200,80);
			score.setForeground(Color.WHITE);
			score.setLocation(540,0);
			add(score);
			
			scoreLabel = new JLabel("0");
			scoreLabel.setForeground(Color.WHITE);
			scoreLabel.setFont(new Font("Arial Black",Font.BOLD,40));
			scoreLabel.setSize(400,50);
			scoreLabel.setLocation(545,70);
			add(scoreLabel);
			
			//레벨 표시용 라벨
			levelLabel = new JLabel("1");
			levelLabel.setFont(new Font("Arial Black",Font.BOLD,35));
			levelLabel.setForeground(Color.WHITE);
			levelLabel.setSize(400,50);
			levelLabel.setLocation(685,145);
			add(levelLabel);
			
			
			gameOverLabel = new GameOverLabel();
//			gameOverLabel.setSize(500,300);
//			gameOverLabel.setLocation(0,160);
			add(gameOverLabel);
			
//			///
//			gameOverLabel.setLocation(0,160);
//			add(gameOverLabel);
//			///
			
			
			//최초 블록 딜레이는 500밀리초
			//블록을 생성하는 스레드 작성
			spawnThread = new BlockSpawnThread(this,500);
			//블록 생성 스레드 시작
			spawnThread.start();
			
			//게임을 총괄하는 스레드 생성 
			//2차원 블록을 검사하여 조건을 만족한다면 해당인덱스의 요소를 없앤다.
			//이후 점수라벨의 점수를 올린다.
			//특정 점수를 넘긴다면 라운드를 올리고, 블록이 떨어지는 속도를 증가시킨다.
			//맨 위칸에 블록이 올라가게된다면 게임종료
			gameManagerThread = new GameManageThread(this);
			gameManagerThread.start();
			
		}
		
		@Override
	    public void paintComponent(Graphics g) {
	       super.paintComponent(g); //그래픽 컴포넌트 설정
	       //배경 이미지
	       g.drawImage(backgroundImage, 0, 0, backgroundImageIcon.getIconWidth(),backgroundImageIcon.getIconHeight(),null); //이미지가 그려지는 시점 알림받지 않기
	    }
		
	}
	
	//전체적으로 게임을 관리하는 스레드를 작성해야함
	//블록이 생성되도록하고(블록을 생성하면, 해당블록이 떨어지는 스레드는 별도로 내장됨)
	//키를 인식하여 블록의 위치를 변경시키고
	//블록의 위치를 수시로 확인하여 2차원 배열안의 특정한 위치에 도달하면
	//=>이때 특정한 위치란 바닥이거나(가장 밑칸이거나), 다른 블록의 위에 도달하거나
	//블록이 더이상 떨어지지 않도록 해야함
	//블록의 동작이 멈추었을때, 땅에 내려온 블록을 최적화 시켜야함
	//같은 타입의 블록이 대각선, 세로,가로로 3칸이 되었는지 확인하고
	//조건이 만족되었다면 점수를 올리고, 블록의 위치를 최적화한다.
	private class GameManageThread extends Thread{
		private int indexX,indexY;
		
		private GameRunningPanel parent;

		
		public GameManageThread(GameRunningPanel parent){
			this.parent = parent;
		}
		
		
		//좌표를 매개변수로 입력받아 해당 좌표까지 위의 블록들을 아래로 당겨줌
		public void pullBlocks(int indexX,int indexY) {
			//x좌표가 행을 의미, y좌표가 열을 의미
				
				if(blockArray[indexX][indexY]==null) {
					System.out.println("블록제거됨"+indexX+",");
					for(int i=indexY-1;i>=0;i--) {
						blockArray[indexX][i+1] = blockArray[indexX][i];
						
					}
					for(int i=indexY;i>=0;i--) {
						if(blockArray[indexX][i]!=null) {
							blockArray[indexX][i].setLocation(indexX*50,i*50);
						}
					}
					//block
				}
		}
		
		//게임 매니저
		
		@Override
		public void run() {
			while(true) {
				boolean check = false;
				//블록이 맨 위 인덱스에 생긴다면 => 모든 스레드 작동중지, 게임종료
				
				//맨윗칸 검사
				for(int i=0;i<blockArray.length;i++) {
					//블록배열의 맨 윗칸이 래퍼랜스를 갖게된다면 => 게임 종료
					if(blockArray[i][0]!=null) {
						//떨어지
						fallingThread.interrupt();
						
						//블록 생성 중지
						spawnThread.interrupt();
						
						///
						
						//게임 오버 이미지가 뒤에 가려지는 상황을 방지하기 위한 방법 고안할것
						//LayeredPane을 쓰는방법?
						
//						Game gameOver = parent.gameOverLabel;
//						
						parent.gameOverLabel.setSize(500,300);
						parent.gameOverLabel.setLocation(0,160);
						
//						gameOver = new JLabel(parent.gameOverImageIcon);
//						gameOver.setSize(parent.gameOverImageIcon.getIconWidth(),parent.gameOverImageIcon.getIconHeight());
//						gameOver.setLocation(0,160);
//						parent.add(gameOver);
						///
						
						check = true;
						//게임 관리 스레드 종료
						this.interrupt();	
						break;	
					}
				}
				

				//브루트포스 알고리즘
				//조건을 만족하는 블록을 eraseVector에 삽입
				checkAndModifyArray();
				
				//erase벡터안에 있는 블록들 지우기
				for(int i=0;i<eraseVector.size();i++) {
					Point p = eraseVector.get(i);
					//블록이 존재하는 경우에만 블록 삭/제
					if(blockArray[p.x][p.y]!=null) {
						blockArray[p.x][p.y].setVisible(false);
						
						//해당좌표의 블록에 대한 참조를 없앰
						blockArray[p.x][p.y] = null;
						parent.plusScore(50);
						
					}
				}
				
				for(int i=0;i<eraseVector.size();i++) {
					Point p = eraseVector.get(i);
					pullBlocks(p.x,p.y);	
					eraseVector.remove(i);
				}	
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}
	}
	
	//블록을 랜덤으로 생성하여 랜덤한 위치에 보여주는 스레드 => 영역을 나누어 표현해줘야함
	private class BlockSpawnThread extends Thread{
		//생성한 블록에 대한 타입을 지정 => 타입과 함께 맵으로 작성하여 넘겨주기위함
		private int blockType;
		
		//delay시간 지정 => 레벨에 따라~ => 초기값은 500으로
		private int delayTime;
		
		private GameRunningPanel parent;
		
//		//다음에 보여지게 할 블럭
//		private int nextBlockType;
		
//		//wait-notify 관련 코드
//		private boolean stopFlag = false; //블록이 생성되는것을 멈추게 하기 위해
//		
//		//현재 stopFlag상태를 리턴 => wait에 활용
//		public boolean getStopFlag() {return stopFlag;} 
//		
//		//블록의 생성을 멈추도록 함
//		public void stopSpawn() {
//			stopFlag = true;
//		}
//		
		
//		//다음에 생성할 블록의 타입을 매개변수로 보내어 생성하는 경우
//		public BlockSpawnThread(GameRunningPanel parent,int delayTime,int nextBlockType){
//			System.out.println("현재 레벨의 delayTime="+delayTime);
//			this.delayTime = delayTime;
//			this.parent = parent;
//			this.nextBlockType = nextBlockType;
//			System.out.println("스폰쓰레드 생성됨");
//			//아직 블록 생성전이므로 block존재상태를 false로 초기화
//			setBlockStatus(false);
//		}
//		

		public BlockSpawnThread(GameRunningPanel parent,int delayTime){
			System.out.println("현재 레벨의 delayTime="+delayTime);
			this.delayTime = delayTime;
			this.parent = parent;
			System.out.println("스폰쓰레드 생성됨");
			//아직 블록 생성전이므로 block존재상태를 false로 초기화
			setBlockStatus(false);
		}
		
//		//블록이 다시 생성되도록 함
//		synchronized public void resumeSpawn() {
//			stopFlag = false; //블록이 내리는것을 멈추도록
//			System.out.println("블록생성시작");
//			this.notify(); //이 객체를 무한대기하는 쓰레드 깨우기
//		}
//		
//		//flag가 false가 될때까지 기다리는 함수
//		//wait함수를 쓰기 위해선 synchronized 키워드를 사용해야함jh
//		synchronized private void waitFlag() { 
//			try {
//				this.wait(); //쓰레드 무한대기 상태로 변경=>notify()가 불려지기전까지
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} //기다리면서 중단된 상태로
//		}
//		/////////////////
//		
		
		//스레드가 동작하는 공간
		@Override
		public void run() {
			
			while(true) {
				//랜덤 위치 생성 => 구간을 나누어 생성해야함
				
				//가로로 10칸을 나누고 0~9사이의 랜덤인덱스 뽑기
				//이후 50을 곱해서 x의 위치 지정
				//변경포인트
				int x = (int)(Math.random()*10)*50;
				int y = -50; //위에서 부터 떨어져야 하므로 0에서 부터 시작
			
				this.blockType = setBlockType();
				
				//블록 생성
				block = new Block(this.blockType);
				block.setSize(50,50);
				//블록의 초기 위치 설정
				block.setLocation(x,y);
				add(block);
				
				//아래에 블록이 존재한다면
				if(blockArray[x/50][(y+50)/50]!=null) {
					System.out.println("게임 종료");
					break;
				}
				
				//블록을 생성할때 밑에 이미 블록이 존재한다면 벡터에 삽입하지않고 종료
				
				//벡터에 삽입
				//blockVector.add(block);
				
				//블록이 존재하는 상태로 변경
				setBlockStatus(true);
				
//				stopSpawn();
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
				
				//현재 점수에 따라 레벨과, 새로이 떨어질 블록의 delay속도를 결정
				
				//현재 점수와 현재 레벨 측정
				int currentScore = Integer.parseInt(parent.scoreLabel.getText());
				int currentLevel = Integer.parseInt(parent.levelLabel.getText());
				
				
				//레벨*1000이상이 될때마다 레벨을 올리고, 블록이 떨어지는 속도를 증가시킨다.
				if(currentScore>=1000*currentLevel) {
					
					//블록이 떨어지는 시간을 증가시킨다.
					delayTime -= 100;
					System.out.println("속도 증가 : "+delayTime);
					
					//레벨 증가
					parent.levelLabel.setText(Integer.toString(currentLevel+1));
				}
				
				
				//생성한 블록라벨을 관리하는 스레드 작동(떨어지게 하는)
				//딜레이 타임을 매개변수로 넘겨서생성
				fallingThread = new FallingThread(block,delayTime,parent);
				fallingThread.start(); //블록이 떨어지기 시작

				break;
			}
		}
		

		//블록의 타입을 지정하여 리턴한다.
		public int setBlockType() {
			//생성될 블록의 타입 지정? => 빨강,파랑,노랑,초록
			int blockType = (int)(Math.random()*4); // 0~3까지의 타입 지정
			return blockType;
		}

	}
	
	//생성된 블록이 아래로 떨어지게 하는 쓰레드
	private class FallingThread extends Thread{
		private Block block;
		//블록이 떨어지는 시간을 조절하기 위해 => 레벨에 따라 빨라져야함
		private int delayTime;
		
		//스레드가 현재 작동중인지 여부를 저장
		private boolean check = false;
		
		//fallingThread를 종료하기 전, 2차원배열을 조사할지 여부에 대한 flag
		private boolean checkFlag = false;
		
		private GameRunningPanel parent;
		
		public FallingThread(Block block,int delayTime,GameRunningPanel parent) {
			this.parent = parent;
			check = true;
			this.block = block; //매개변수로 참조를 가져옴
			this.delayTime = delayTime;
		}
		
			
		
		@Override
		public void run() {
			while(true) {
				
//				if(stopFlag) { //stop명령이 있다면 블록이동x
//					waitFlag();
//				}
				
				//블록 라벨의 x,y 좌표를 가져옴
				int x = block.getX(); 
				int y = block.getY();
				

				//System.out.println(x+" "+y);
				//블록의 위치를 아래로 조금씩 옮긴다.
				block.setLocation(x,y+50); //5픽셀씩 아래로 이동

//				System.out.println(x+" "+y);
				
				//현재 블록의 위치를 2차원배열상의 인덱스로 변환
				
				int indexX = x/50; //인덱스 상의 위치는 50으로 나눈 몫
				int indexY = y/50+1; //인덱스 상의 위치는 50으로 나눈 몫에 1을 더한값
				
				//System.out.println(indexX+","+indexY);
				
				//블록이 정해진 위치에 도달하였다면 fallingThread를 종료시켜야함
				if(block.getY()>=450) {
					//해당 블록을 삽입하고
					blockArray[indexX][indexY] = block;
					
					//체크 플래그 활성화
					checkFlag = true;
					
//					spawnThread = new BlockSpawnThread();
//					spawnThread.start();
//					
//					break;
				}
				
				//2차원 배열의 인덱스 범위를 벗어나지 않는 조건에 대해서만 조사
				if(indexY+1<10) {
					//블록을 아래로 이동하기전에 해당블록 바로 아래칸에 이미 블록이 존재하는지 확인
					if(blockArray[indexX][indexY+1]!=null) {
						
						//
						
						//System.out.println("타입은" + blockArray[indexX][indexY+1].getType());
						
						//해당 인덱스의 위치에 블록을 삽입하고
						blockArray[indexX][indexY] = block;
						//fallingThread를 종료시킨다.
						
						//체크 플래그 활성화
						checkFlag = true;
						
//						spawnThread = new BlockSpawnThread();
//						spawnThread.start();
//						
//						break;
					}
				}
				
				
				//체크 플래그가 활성화된 상태라면 배열검사를 시작함
				if(checkFlag) {
					//새로운 스폰 스레드 작동시킴
					spawnThread = new BlockSpawnThread(parent,delayTime);
					spawnThread.start();
					
					break;
				}
				
				
				//최고 속도는 100ms
				if(delayTime<=100)
					delayTime = 100;
				
				//딜레이 타임을 걸어줘야함
				try {
					Thread.sleep(this.delayTime/2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
				
			}
		}
		
	}
	
	
	
}
