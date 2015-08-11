package Game;
/*
 * <APPLET CODE=Poker.class WIDTH=1000 HEIGHT=1000>
 * </APPLET>
 * */
import java.applet.Applet;
import java.awt.*;
//import java.util.*;
import java.awt.event.*;

public class Poker extends Applet implements Runnable, MouseListener{

	MediaTracker mt;
	
	Image back;
	Image cards[] = new Image[52];
	
	/* �����̎�D�̏�� */
	int mycards[] =  new int[5];
	int myvalue;
	int mysuit;
	int myuseful = 0;
	String myhighcard ="";
	
	/* �e�̏�� */
	int dealercard[] = new int[5]; 
	int dealervalue;
	int dealersuit;
	int dealeruseful = 0;
	String dealerhighcard ="";
	
	boolean change[] = new boolean[5];
	boolean use[] = new boolean[52];
	boolean call = false;
	boolean youlose = false;

	int money=50;
	int bet=0;

	String mes[]={"","One Pair","Two Pair","Three Card",
	                "Straight","Flush","Full House",
	                "Four Card","Straight Flush","Royal Flush"};

	Thread kicker = null;
	Dimension dim;
	Image  offscr;
	Graphics grf;

	public void init() {
	    int i ,j;
	    String value[]={"2","3","4","5","6","7","8","9","10","11","12","13","1"};
	    String symbol[]={"club","diamond","heart","spade"};

	    mt = new MediaTracker(this);

	     /* �C���[�W�t�@�C���̓ǂݍ��� */
	    back=getImage(getCodeBase(),"trump_back.png");
	    mt.addImage(back,0);
	    for(i=0;i<4;i++){
	      for(j=0;j<13;j++){
	        cards[i*13+j]=getImage(getCodeBase(),""+symbol[i]+value[j]+".png");
	        mt.addImage(cards[i*13+j],0);
	      }
	    }

	    /* �I�t�X�N���[���̐ݒ� */
	    dim = getSize(); // �\���̈�̎擾
	    offscr = createImage( dim.width, dim.height);
	    grf  = offscr.getGraphics();

	    /* �}�E�X���X�i�Ƃ��Ď������g��o�^ */
	    addMouseListener(this);

	    /* �J�[�h��z�� */		
	    deal();
	  }

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g){

		int i;

		/* �ǂݍ��ݒ����b�Z�[�W�̕\�� */
		if (mt.checkID(0) == false) {
			g.setColor(Color.black);
			g.fillRect(0,0,dim.width, dim.height);
			g.setColor(Color.yellow);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			g.drawString("Loading...", 40, 50);
			return;
		}

		/* �o�b�N��΂œh�� */
		grf.setColor(new Color(0, 100,30 ));
		grf.fillRect(0,0,dim.width, dim.height);

		/* �^�C�g���̕`�� */
		grf.setFont(new Font("BoldItaric", Font.ITALIC|Font.BOLD, 24)); 
		grf.setColor(Color.white);
		grf.drawString("Poker",50,35);

		/* �q���̕`�� */
		grf.setFont(new Font("TimesRoman", Font.PLAIN, 12)); 
		grf.setColor(Color.yellow);
		grf.drawString("BET $"+bet,170,35);

		/* �������̕`�� */
		grf.setColor(Color.white);
		grf.drawString("Money $"+money,240,35);

		/* DEAL�{�^���̕`�� */
		grf.setColor(Color.green);
		grf.fillRect( 38, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("DEAL", 52, 288);

		/* BET�{�^���̕`�� */
		grf.setColor(Color.pink);
		grf.fillRect( 138, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("BET $1", 145, 288);

		/* CHANGE�{�^���̕`�� */
		grf.setColor(Color.orange);
		grf.fillRect( 238, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("CHANGE", 245, 288);

		/* �J�[�h�̕`�� */
		grf.setColor(Color.orange);
		grf.drawString("Dealer", 50, 155);    
		grf.drawString("You", 50, 170);
		for(i=0; i<5; i++ ){
			if (call){
				/* �e�̎�D�̕`�� */
				grf.drawImage(cards[dealercard[i]],25+(55+5)*i,56,this);
			} else{
				/* �e�̎�D�i���j�̕`�� */
				grf.drawImage(back,25+(55+5)*i,56,this);
			}
			/* ��D�̕`�� */
			grf.drawImage(cards[mycards[i]],25+(55+5)*i,176,this);
			grf.setColor(Color.yellow);
			if (change[i]&&!call){
				/* �`�F���W����J�[�h�͉��F������� */
				grf.drawRect(23+(55+5)*i, 174, 58, 84);
				grf.drawRect(24+(55+5)*i, 175, 56, 82);
			}
		}

		/* �� */
		grf.setColor(Color.yellow);
		if ( dealeruseful ==0 && call){
			/* �e�̖��̕`��i�n�C�J�[�h�j */
			grf.drawString(dealerhighcard, 150, 155);
		} else{
			/* �e�̖��̕`�� */    
			grf.drawString(mes[dealeruseful], 150, 155);
		}
		if ( myuseful ==0 && call ){
			/* ���̕`��i�n�C�J�[�h�j */
			grf.drawString(myhighcard, 150, 170);
		} else{
			/* ���̕`�� */   
			grf.drawString(mes[myuseful], 150, 170);
		}

		/* ���������̃��b�Z�[�W */
		grf.setFont(new Font("TimesRoman", Font.PLAIN, 24)); 
		if (youlose&&call){
			grf.drawString("�N�̕�����wwwww", 220, 170);
		}
		if (!youlose&&call){
			grf.drawString("�N�̏������E�E�E", 220, 170);
		}
		
		/* �I�t�X�N���[���̃C���[�W���ꋓ�Ɏ��ۂ̕\���̈�ɕ`�� */
		g.drawImage(offscr, 0, 0, this);
			
	}
	
	public void deal(){
		int i,card;

		/* �ϐ��̏����� */
		for (i=0;i<5;i++){
			mycards[i]=0;
			change[i]=false;
			dealercard[i]=0;
		}
		for (i=0;i<52;i++){
			use[i]=false;
		}
		myuseful=0;
		dealeruseful=0;
		call=false;
		bet=0;

		/* �J�[�h�z�z */
		for (i=0;i<5;i++){
			while(true){
				card=(int)(Math.random() * 52); 
				if(!use[card]){ 
					break;
				}
			}
			mycards[i]=card; 
			use[card]=true; 
		}
		
		/* �e�̎�D */
		for (i=0;i<5;i++){
			while(true){
				card=(int)(Math.random() * 52);
				if(!use[card]){ 
					break;
				}
			}
			dealercard[i]=card;
			use[card]=true; 
		}
	}
	
	public void change(){
		int i,card;

		/* �J�[�h�̃`�F���W */
		for (i=0;i<5;i++){
			if(change[i]){ 
				while(true){
					card=(int)(Math.random() * 52); 
					if(!use[card]){ 
						break;
					}
				}
				mycards[i]=card;
				use[card]=true; 
			}
		}
		for (i=0;i<5;i++){
			change[i]=false; 
		}	
		/* ���̃`�F�b�N */
		check(0);

		/* �e�̖��̃`�F�b�N */
		check(1);

		/* �����̔��� */
		judge();

		call=true;
	}

	public void mousePressed(MouseEvent e){
		int i;
		int ix, iy; 
		
		/* �}�E�X�������ꂽ���W�𓾂� */
		ix = e.getX();
		iy = e.getY();
		
		if ( ix > 38 && ix < 103 && iy > 270 && iy < 295 ) {
			/* �}�E�X�̍��W(ix,iy)��DEAL�{�^������������J�[�h��z�z�������@�@�@�@*/
			deal();
			repaint();	
			return;
		} 
		
		if ( ix > 138 && ix < 203 && iy > 270 && iy < 295 && !call ) {
			/* �}�E�X�̍��W(ix,iy)��BET�{�^������������q���𑝂₷ */
			betmoney();
			repaint();
			return; 
		} 
		if ( ix > 238 && ix < 303 && iy > 270 && iy < 295 && !call && bet > 0) {
			/* �}�E�X�̍��W(ix,iy)��CHANGE�{�^������������J�[�h���`�F���W����   �@�@ */
			change();
			repaint();
			return; 
		} 
		for(i=0; i<5; i++ ){
			if ( ix > 25+(55+5)*i && ix < 80+(55+5)*i && iy > 176 && iy < 257 ) {
				/* i�Ԗڂ̎�D�� */
				if(change[i]){
					change[i]=false; 
				} else{
					change[i]=true;
				}
				repaint();
				return; 
			}
		} 
	}
	/* �}�E�X�{�^�������ꂽ�� */
	public void mouseReleased(MouseEvent e){}
	/* �}�E�X�{�^�����N���b�N���ꂽ */
	public void mouseClicked(MouseEvent e){}
	/* �}�E�X�J�[�\���������Ă��� */
	public void mouseEntered(MouseEvent e){}
	/* �}�E�X�J�[�\�����o�Ă����� */
	public void mouseExited(MouseEvent e){}

	public void check(int turn){
		int i,j; 
		int work;
		int threeVal; 
		boolean four=false; 
		boolean flush=false; 
		boolean straight=false; 
		boolean three=false; 
		boolean pair=false; 

		int hand[] = new int[5]; 
		int value[] = new int[5]; 
		int suit[] = new int[5]; 
		int usefulhigh = 0; 
		int usefulsuit = 0; 
		int useful; 
		String highcard=""; 

		if (turn == 0 ) {
			/* ���[�U�̔� */	
			for (i=0;i<5;i++){
				hand[i]=mycards[i]; 
			}	
		} else{
			/* �e�̔� */
			for (i=0;i<5;i++){
				hand[i]=dealercard[i]; 
			}
		}

		threeVal=0;
		useful=0;

		/* ��D�̃X�[�g�Ɛ����𒲂ׂ� */
		for (i=0;i<5;i++){
			suit[i]=(int)(hand[i]/13);
			value[i]=hand[i]-suit[i]*13+2;
		}
		/* ��D���\�[�g���� */
		for(i=0;i<4;i++){
			for(j=1;j<5-i;j++){
				if(value[j-1]>value[j]){
					work=value[j-1];
					value[j-1]=value[j];
					value[j]=work;
					work=suit[j-1];
					suit[j-1]=suit[j];
					suit[j]=work;
				}
			}
		}

		/* �n�C�J�[�h�̃��b�Z�[�W */
		highcard=(value[4])+"th High";
		if(value[4]==2){
			highcard=(value[4])+"nd High";
		}
		if(value[4]==3){
			highcard=(value[4])+"rd High";
		}
		if(value[4]>10){
			highcard="";
			if(value[4]==11){
				highcard="Jack High";
			}
			if(value[4]==12){
				highcard="Queen High";
			}
			if(value[4]==13){
				highcard="King High";
			}
			if(value[4]==14){
				highcard="Ace High";
			}
		}
		usefulhigh=value[4];
		usefulsuit=suit[4];

		/* �t���b�V�� */
		if((suit[0]==suit[1])&&(suit[0]==suit[2])&&(suit[0]==suit[3])&&(suit[0]==suit[4])){
			flush=true;
			useful=5;
		}

		/* �X�g���[�g */
		straight=true;
		for(i=1;i<5;i++){
			if(value[i]!=value[i-1]+1){
				straight=false;
				break;	
			}
		}

		/* �X�g���[�g�t���b�V�� */	
		if(straight){
			useful=4;
			if(flush){
				useful=8; 
				/* ���C�����t���b�V�� */ 
				if(value[0]>9){
					useful=9;
				}
			}
		}

		/* �t�H�[�J�[�h */
		if(((value[0]==value[1])&&(value[0]==value[2])&&(value[0]==value[3]))||
				((value[1]==value[2])&&(value[1]==value[3])&&(value[1]==value[4]))){
			four=true;
			useful=7;
			if (value[0]==value[1]) {
				usefulhigh=value[0];
				usefulsuit=suit[0];
			} 
		}
		
		if(!four){
			/* �X���[�J�[�h */
			for(i=2;i<5;i++){
				if((value[i-2]==value[i-1])&&(value[i-1]==value[i])){
					three=true;
					useful=3;
					threeVal=value[i];
					usefulhigh=threeVal;
					usefulsuit=suit[i];
				}
			}

			/* �c�[�y�A�A�����y�A */
			for(i=1;i<5;i++){
				/* �X���[�J�[�h�ɂȂ��Ă��鐔���ȊO�ł̃y�A��{�� */ 
				if((value[i-1]==value[i])&&(value[i]!=threeVal)) {
					if(pair){
						useful=2; 
						if ( usefulhigh <= value[i] ) {
							usefulhigh = value[i];
							if ( suit[i-1] < suit[i] ) {
								usefulsuit = suit[i];
							} else {
								usefulsuit = suit[i-1];
							}
						}
					} else{
						pair=true;
						useful=1;
						if ( threeVal <= value[i] ) {
							usefulhigh = value[i];
							if ( suit[i-1] < suit[i] ) {
								usefulsuit = suit[i];
							} else {
								usefulsuit = suit[i-1];
							}
						}
					}
				}
			}
		}

		/* �t���n�E�X */
		if((pair)&&(three)) {
			useful=6;
		}

		if (turn == 0 ) {
			// �����̔Ԃ̏ꍇ //
			myvalue=usefulhigh;
			mysuit=usefulsuit;
			myuseful=useful;
			if ( myuseful== 0 ) {
				myhighcard=highcard;
			}
		} else{
			// �e�̔Ԃ̏ꍇ //
			dealervalue=usefulhigh;
			dealersuit=usefulsuit;
			dealeruseful=useful;
			if ( dealeruseful== 0 ) {
				dealerhighcard=highcard;
			}
		}
	}
	
	public void judge(){

		youlose = true;

		/* ���̔�r */
		if ( dealeruseful < myuseful ) {
			youlose = false;
		} else {
			if ( dealeruseful == myuseful ) {
				/* ���������ꍇ�����̔�r   */
				if ( dealervalue < myvalue ) {
					youlose = false;
				} else {
					if ( dealervalue == myvalue ) {
						/* �����������ꍇ�X�[�g�̔�r   */
						if ( dealersuit < mysuit ) {
							youlose = false;
						}
					}
				}
			}
		}
		
		/* �����̏ꍇ�̊������� */
		if (!youlose){
			money=money+bet*2;
		}
	}

	public void betmoney(){

		if ( money >=1 ) {
			money=money-1;
			bet=bet+1; 
		}
	}

	public void start() {
		if (kicker == null) {    
			/* �X���b�h�����s������ */
			kicker = new Thread(this);        
			kicker.start();                 
		}
	}

	public void stop() {
		/* �X���b�h���~�߂� */
		if ( kicker != null ) {
			kicker = null;
		}
	}

	public void run() {
		/* �S�ẴC���[�W�̓ǂݍ��݂�҂� */
		try{
			mt.waitForID(0);
		} catch (InterruptedException e) {
			return;
		}
		repaint();
	}

}
