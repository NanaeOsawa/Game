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
	
	/* 自分の手札の情報 */
	int mycards[] =  new int[5];
	int myvalue;
	int mysuit;
	int myuseful = 0;
	String myhighcard ="";
	
	/* 親の情報 */
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

	     /* イメージファイルの読み込み */
	    back=getImage(getCodeBase(),"trump_back.png");
	    mt.addImage(back,0);
	    for(i=0;i<4;i++){
	      for(j=0;j<13;j++){
	        cards[i*13+j]=getImage(getCodeBase(),""+symbol[i]+value[j]+".png");
	        mt.addImage(cards[i*13+j],0);
	      }
	    }

	    /* オフスクリーンの設定 */
	    dim = getSize(); // 表示領域の取得
	    offscr = createImage( dim.width, dim.height);
	    grf  = offscr.getGraphics();

	    /* マウスリスナとして自分自身を登録 */
	    addMouseListener(this);

	    /* カードを配る */		
	    deal();
	  }

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g){

		int i;

		/* 読み込み中メッセージの表示 */
		if (mt.checkID(0) == false) {
			g.setColor(Color.black);
			g.fillRect(0,0,dim.width, dim.height);
			g.setColor(Color.yellow);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			g.drawString("Loading...", 40, 50);
			return;
		}

		/* バックを緑で塗る */
		grf.setColor(new Color(0, 100,30 ));
		grf.fillRect(0,0,dim.width, dim.height);

		/* タイトルの描画 */
		grf.setFont(new Font("BoldItaric", Font.ITALIC|Font.BOLD, 24)); 
		grf.setColor(Color.white);
		grf.drawString("Poker",50,35);

		/* 賭金の描画 */
		grf.setFont(new Font("TimesRoman", Font.PLAIN, 12)); 
		grf.setColor(Color.yellow);
		grf.drawString("BET $"+bet,170,35);

		/* 所持金の描画 */
		grf.setColor(Color.white);
		grf.drawString("Money $"+money,240,35);

		/* DEALボタンの描画 */
		grf.setColor(Color.green);
		grf.fillRect( 38, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("DEAL", 52, 288);

		/* BETボタンの描画 */
		grf.setColor(Color.pink);
		grf.fillRect( 138, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("BET $1", 145, 288);

		/* CHANGEボタンの描画 */
		grf.setColor(Color.orange);
		grf.fillRect( 238, 270, 65, 25 );  
		grf.setColor(Color.black);
		grf.drawString("CHANGE", 245, 288);

		/* カードの描画 */
		grf.setColor(Color.orange);
		grf.drawString("Dealer", 50, 155);    
		grf.drawString("You", 50, 170);
		for(i=0; i<5; i++ ){
			if (call){
				/* 親の手札の描画 */
				grf.drawImage(cards[dealercard[i]],25+(55+5)*i,56,this);
			} else{
				/* 親の手札（裏）の描画 */
				grf.drawImage(back,25+(55+5)*i,56,this);
			}
			/* 手札の描画 */
			grf.drawImage(cards[mycards[i]],25+(55+5)*i,176,this);
			grf.setColor(Color.yellow);
			if (change[i]&&!call){
				/* チェンジするカードは黄色く縁取り */
				grf.drawRect(23+(55+5)*i, 174, 58, 84);
				grf.drawRect(24+(55+5)*i, 175, 56, 82);
			}
		}

		/* 役 */
		grf.setColor(Color.yellow);
		if ( dealeruseful ==0 && call){
			/* 親の役の描画（ハイカード） */
			grf.drawString(dealerhighcard, 150, 155);
		} else{
			/* 親の役の描画 */    
			grf.drawString(mes[dealeruseful], 150, 155);
		}
		if ( myuseful ==0 && call ){
			/* 役の描画（ハイカード） */
			grf.drawString(myhighcard, 150, 170);
		} else{
			/* 役の描画 */   
			grf.drawString(mes[myuseful], 150, 170);
		}

		/* 勝ち負けのメッセージ */
		grf.setFont(new Font("TimesRoman", Font.PLAIN, 24)); 
		if (youlose&&call){
			grf.drawString("君の負けだwwwww", 220, 170);
		}
		if (!youlose&&call){
			grf.drawString("君の勝ちだ・・・", 220, 170);
		}
		
		/* オフスクリーンのイメージを一挙に実際の表示領域に描く */
		g.drawImage(offscr, 0, 0, this);
			
	}
	
	public void deal(){
		int i,card;

		/* 変数の初期化 */
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

		/* カード配布 */
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
		
		/* 親の手札 */
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

		/* カードのチェンジ */
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
		/* 役のチェック */
		check(0);

		/* 親の役のチェック */
		check(1);

		/* 勝負の判定 */
		judge();

		call=true;
	}

	public void mousePressed(MouseEvent e){
		int i;
		int ix, iy; 
		
		/* マウスが押された座標を得る */
		ix = e.getX();
		iy = e.getY();
		
		if ( ix > 38 && ix < 103 && iy > 270 && iy < 295 ) {
			/* マウスの座標(ix,iy)がDEALボタン内だったらカードを配布し直す　　　　*/
			deal();
			repaint();	
			return;
		} 
		
		if ( ix > 138 && ix < 203 && iy > 270 && iy < 295 && !call ) {
			/* マウスの座標(ix,iy)がBETボタン内だったら賭金を増やす */
			betmoney();
			repaint();
			return; 
		} 
		if ( ix > 238 && ix < 303 && iy > 270 && iy < 295 && !call && bet > 0) {
			/* マウスの座標(ix,iy)がCHANGEボタン内だったらカードをチェンジする   　　 */
			change();
			repaint();
			return; 
		} 
		for(i=0; i<5; i++ ){
			if ( ix > 25+(55+5)*i && ix < 80+(55+5)*i && iy > 176 && iy < 257 ) {
				/* i番目の手札内 */
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
	/* マウスボタンが離れたら */
	public void mouseReleased(MouseEvent e){}
	/* マウスボタンがクリックされた */
	public void mouseClicked(MouseEvent e){}
	/* マウスカーソルが入ってきた */
	public void mouseEntered(MouseEvent e){}
	/* マウスカーソルが出ていった */
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
			/* ユーザの番 */	
			for (i=0;i<5;i++){
				hand[i]=mycards[i]; 
			}	
		} else{
			/* 親の番 */
			for (i=0;i<5;i++){
				hand[i]=dealercard[i]; 
			}
		}

		threeVal=0;
		useful=0;

		/* 手札のスートと数字を調べる */
		for (i=0;i<5;i++){
			suit[i]=(int)(hand[i]/13);
			value[i]=hand[i]-suit[i]*13+2;
		}
		/* 手札をソートする */
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

		/* ハイカードのメッセージ */
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

		/* フラッシュ */
		if((suit[0]==suit[1])&&(suit[0]==suit[2])&&(suit[0]==suit[3])&&(suit[0]==suit[4])){
			flush=true;
			useful=5;
		}

		/* ストレート */
		straight=true;
		for(i=1;i<5;i++){
			if(value[i]!=value[i-1]+1){
				straight=false;
				break;	
			}
		}

		/* ストレートフラッシュ */	
		if(straight){
			useful=4;
			if(flush){
				useful=8; 
				/* ロイヤルフラッシュ */ 
				if(value[0]>9){
					useful=9;
				}
			}
		}

		/* フォーカード */
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
			/* スリーカード */
			for(i=2;i<5;i++){
				if((value[i-2]==value[i-1])&&(value[i-1]==value[i])){
					three=true;
					useful=3;
					threeVal=value[i];
					usefulhigh=threeVal;
					usefulsuit=suit[i];
				}
			}

			/* ツーペア、ワンペア */
			for(i=1;i<5;i++){
				/* スリーカードになっている数字以外でのペアを捜す */ 
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

		/* フルハウス */
		if((pair)&&(three)) {
			useful=6;
		}

		if (turn == 0 ) {
			// 自分の番の場合 //
			myvalue=usefulhigh;
			mysuit=usefulsuit;
			myuseful=useful;
			if ( myuseful== 0 ) {
				myhighcard=highcard;
			}
		} else{
			// 親の番の場合 //
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

		/* 役の比較 */
		if ( dealeruseful < myuseful ) {
			youlose = false;
		} else {
			if ( dealeruseful == myuseful ) {
				/* 役が同じ場合数字の比較   */
				if ( dealervalue < myvalue ) {
					youlose = false;
				} else {
					if ( dealervalue == myvalue ) {
						/* 数字も同じ場合スートの比較   */
						if ( dealersuit < mysuit ) {
							youlose = false;
						}
					}
				}
			}
		}
		
		/* 勝ちの場合の換金処理 */
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
			/* スレッドを実行させる */
			kicker = new Thread(this);        
			kicker.start();                 
		}
	}

	public void stop() {
		/* スレッドを止める */
		if ( kicker != null ) {
			kicker = null;
		}
	}

	public void run() {
		/* 全てのイメージの読み込みを待つ */
		try{
			mt.waitForID(0);
		} catch (InterruptedException e) {
			return;
		}
		repaint();
	}

}
