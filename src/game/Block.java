package game;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Block extends JLabel {
	private int blockType;
	
	private JLabel block;
	
	private ImageIcon cyanBlockIcon = new ImageIcon("cyanBlock.png");
	private ImageIcon redBlockIcon = new ImageIcon("redBlock.png");
	private ImageIcon greenBlockIcon = new ImageIcon("greenBlock.png");
	private ImageIcon yellowBlockIcon = new ImageIcon("yellowBlock.png");
	
	private ImageIcon selectedIcon;
	
	public Block(int blockType) {
		this.blockType = blockType;
		
		switch(blockType) {
		case 0:
			selectedIcon = cyanBlockIcon;
			break;
		case 1:
			selectedIcon = redBlockIcon;
			break;
		case 2:
			selectedIcon = greenBlockIcon;
			break;
		case 3:
			selectedIcon = yellowBlockIcon;
			break;
		}
		
		
		block = new JLabel(selectedIcon);
		// TODO Auto-generated constructor stub
//		block.setIcon(cyanBlockIcon);
		block.setSize(cyanBlockIcon.getIconWidth(),cyanBlockIcon.getIconWidth());
		block.setLocation(0,0);
		
		add(block);
	}
	
	public void fall() {
		
	}
	
	//블록의 타입을 리턴
	public int getType() {
		return blockType;
	}

}
