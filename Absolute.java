// Absolute Space

import java.awt.*;
import java.net.*;
import java.applet.Applet;
import java.applet.AudioClip;

public class Absolute extends Applet implements Runnable
{
  Dimension	d;
  Font 		largefont = new Font("Helvetica", Font.PLAIN, 24);
  Font		smallfont = new Font("Helvetica", Font.PLAIN, 14);

  FontMetrics	fmsmall, fmlarge;
  Graphics	goff;
  Image		ii;
  Thread	thethread;

  boolean	ingame=false;

  int		x, y, mousex, mousey, oldx, oldy, dx=0, dy=0, count, shield=0;
  boolean	showtitle=true;
  Image		ship;
  Image[]	fire;
  int		firecnt=0;

  // Bullet variables
  Image		bullet;
  int[]		bx;
  int[]		by;
  final int	bmy=16, bul_xs=54, bul_ys=8;

  // Meteor variables
  Image		meteor;
  int		maxmet, metcount, mtotal, mrenew, metmy;
  int[]		metx;
  int[]		mety;
  int[]		metf;
  boolean[] 	metr;
  final int	sxmet=80, symet=84;

  // These are for the star field
  public int starsX[];
  public int starsY[];
  public Color starsC[];
  public int numStars = 30;
  public int speed = 6, xSize, ySize;

  // Variables for big boom
  Image[]	boom;
  int		rndbx, rndby, rndcnt=777;
  final int	sxbom=71, sybom=100, bframes=4;

  // Global Variables
  int		distance=0, maxdist=2000;
  int		slevel, blevel, difflev, bosslevel;
  int		smax, bmax;
  int		scur, bcur, renew, rcnt=0, sstretch, txtalign=100;
  long		score;

  // Sounds
  AudioClip	blast, crash, kill;

  // Bosses here
  // Sunbird
  boolean	sunbird, sbefore, safter;
  int		sbx, sby, sbmove, maxtribe, tribe;
  int[]		sbfx, sbfy;

  final int	maxshield=9;
  final int	backcol=0x102040;
  final int	fireframe=2;
  final int	borderwidth=0;
  final int	sxsize=90, sysize=39, sxfire=11, syfire=6;
  final int	movex=10, movey=5;
  final int	scoreheight=45;
  final int	screendelay=300;

  public String getAppletInfo()
  {
    return("Absolute Space - by nicks707");
  }

  public void init()
  {
    Graphics g;
    int n;
    d = size();
    setBackground(Color.black);
    g=getGraphics();
    g.setFont(smallfont);
    fmsmall = g.getFontMetrics();
    g.setFont(largefont);
    fmlarge = g.getFontMetrics();
    ship = getImage(getCodeBase(), "ship.gif");
    bullet = getImage(getCodeBase(), "bullet.gif");
    fire = new Image[fireframe];
    for (n=0; n<fireframe; n++) {
	fire[n] = getImage(getCodeBase(), "fire"+n+".gif");
    }
    boom = new Image[bframes+1];
    for (n=0; n<=bframes; n++) {
	boom[n] = getImage(getCodeBase(), "boom"+n+".gif");
    }

    xSize = d.width - borderwidth*2;
    ySize = d.height - borderwidth*2 - scoreheight;

    x = (xSize - sxsize) / 2;
    y = ySize - sysize - scoreheight - borderwidth;
    mousex = -1;

    // will be override by command line parameters
    blevel = 3;
    slevel = 3;

    bx = new int[blevel*10];
    by = new int[blevel*10];

    for (n=0; n<blevel*10; n++) {
	bx[n] = -1;
    }

    // Meteor initiliaze
    meteor = getImage(getCodeBase(), "meteor.gif");
    maxmet = d.height / symet + 1;
    maxmet = maxmet * 10;
    metx = new int[maxmet];
    mety = new int[maxmet];
    metf = new int[maxmet];
    metr = new boolean[maxmet];

    // Audio
    try {
      blast = getAudioClip(new URL(getDocumentBase(), "blast.au"));
      crash = getAudioClip(new URL(getDocumentBase(), "collisn.au"));
      kill = getAudioClip(new URL(getDocumentBase(), "mdestr.au"));
    }
    catch (MalformedURLException e) {}

    blast.play();  blast.stop();
    crash.play();  crash.stop();
    kill.play();   kill.stop();

    initStars();

    rndcnt = 777;

    // Bosses
    // Sunbird
    sbfx = new int[11];
    sbfy = new int[11];
    sbfx[0] = 10;
    sbfy[0] = 0;
    sbfx[1] = 15;
    sbfy[1] = 10;
    sbfx[2] = 0;
    sbfy[2] = 10;
    sbfx[3] = 3;
    sbfy[3] = 15;
    sbfx[4] = 17;
    sbfy[4] = 15;
    sbfx[5] = 20;
    sbfy[5] = 20;
    sbfx[6] = 23;
    sbfy[6] = 15;
    sbfx[7] = 37;
    sbfy[7] = 15;
    sbfx[8] = 40;
    sbfy[8] = 10;
    sbfx[9] = 25;
    sbfy[9] = 10;
    sbfx[10] = 30;
    sbfy[10] = 0;

  }

// This creates the starfield in the background
        public void initStars () {
         starsX = new int[numStars];
         starsY = new int[numStars];
	 starsC = new Color[numStars];
          for (int i = 0; i < numStars; i++) {
           starsX[i] = (int) ((Math.random() * xSize - 1) + 1);
           starsY[i] = (int) ((Math.random() * ySize - 1) + 1);
	   starsC[i] = NewColor();
          }
        }

  public boolean keyDown(Event e, int key)
  {
    if (ingame)
    {
      mousex = -1;
      if (key == Event.LEFT)
          dx=-1;
      if (key == Event.RIGHT)
        dx=1;
      if (key == Event.UP)
          dy=-1;
      if (key == Event.DOWN)
        dy=1;
      if (key == ' ')
	if (bcur>0) FireGun();
      if (key == Event.ESCAPE)
        ingame=false;
    }
    else
    {
      if (key == 's' || key == 'S')
      {
        ingame=true;
	GameStart();
      }
    }
    return true;
  }

  public boolean keyUp(Event e, int key)
  {
    System.out.println("Key: "+key);
    if (key == Event.LEFT || key == Event.RIGHT)
       dx=0;
    if (key == Event.UP || key == Event.DOWN)
       dy=0;
    return true;
  }

  public void paint(Graphics g)
  {
    String s;
    Graphics gg;

    if (goff==null && d.width>0 && d.height>0)
    {
      ii = createImage(d.width, d.height);
      goff = ii.getGraphics();
    }
    if (goff==null || ii==null)
      return;

    goff.setColor(Color.black);
    goff.fillRect(0, 0, d.width, d.height);

    if (ingame)
      PlayGame();
    else
      ShowIntroScreen();
    g.drawImage(ii, 0, 0, this);
  }


  public void PlayGame()
  {
    NewMeteor();
    MoveShip();
    DrawPlayField();

    // Big bosses here
    if (sunbird) SunBird();

    ShowScore();
    distance++;
    score+=100;
    if (distance % maxdist == 0) {
	difflev++;
	if (difflev>2 & difflev<10) {
	  renew-=20;
	  bmax+=1;
	  smax+=1;
	  metmy++;
	  mrenew--;
	}
	if (difflev>3 & difflev<11) {
	  maxtribe++;
	  sbmove++;
	}
	if (difflev>3) {
	  sunbird = true;
	  tribe = maxtribe;
	}
    }

    // Renew Ship Energy
    rcnt++;
    if (rcnt % (renew / blevel) == 0) {
	bcur++;
	if (bcur>bmax) bcur=bmax;
    }
    if (distance % 500 == 0) {
	scur++;
	if (scur>smax) scur=smax;
    }
    if (rcnt>renew) rcnt=0;
  }

  public void ShowIntroScreen()
  {
    String s;

    DrawPlayField();
    goff.setFont(largefont);

    if (rndcnt > bframes) {
	rndbx = (int) (Math.random() * (xSize - sxbom) + 1);
	rndby = (int) (Math.random() * (ySize - sybom) + 1);
	rndcnt = 0;
    }

    goff.drawImage(boom[rndcnt], rndbx, rndby, this);
    rndcnt++;
    for (int i=0; i<xSize/bul_xs; i++) {
	goff.drawImage(bullet, i*bul_xs, 0, this);
	goff.drawImage(bullet, i*bul_xs, ySize-bul_ys, this);
    }

    if (showtitle)
    {
      goff.setColor(new Color(0xff0000));
      s="Absolute Space";
      goff.drawString(s,(d.width-fmlarge.stringWidth(s)) / 2, (d.height-scoreheight-borderwidth)/2 - 20);
      goff.setColor(new Color(0xff00ff));
      s="(c)2000 by Navjot Singh";
      goff.setFont(smallfont);
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,(d.height-scoreheight-borderwidth)/2 + 10);
      s="navjot.usit@gmail.com";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,(d.height-scoreheight-borderwidth)/2 + 30);
    }
    else
    {
      goff.setFont(smallfont);
      goff.setColor(new Color(0xffff00));
      s="Leftclick to start game";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,(d.height-scoreheight-borderwidth)/2 - 10);
      goff.setColor(new Color(0x00ff00));
      s="Use cursor keys move, click or press SPACE to fire";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,(d.height-scoreheight-borderwidth)/2 + 20);
      goff.setFont(largefont);
      goff.setColor(new Color(0xff00ff));
      s="LAST SCORE: "+score;
      goff.drawString(s,(d.width-fmlarge.stringWidth(s))/2,(d.height-scoreheight-borderwidth)/2 + 120);
    }
    count--;
    if (count<=0)
    { count=screendelay; showtitle=!showtitle; }
  }

  public void DrawPlayField()
  {

    // Show stars
    moveStars();
    for (int a = 0; a < numStars; a++) {
      goff.setColor(starsC[a]);
      goff.drawRect(starsX[a], starsY[a], 1, 1);
    }

    ShowMeteors();
    KillEmAll();
    goff.drawImage(ship, x, y, this); // paint ship
    if (firecnt != 0) {
      goff.drawImage(fire[firecnt-1], x+( (sxsize-sxfire) / 2 ), y+sysize, this); // engine fire
    }
    firecnt++;
    if (firecnt > 2) firecnt=0;
    Collisions();

    if (shield>0) {
	goff.setColor(new Color(0x00ffff));
	goff.drawOval(x-shield, y-shield, sxsize+shield*2, sysize+shield*2);
	shield--;
    }

  }

  public void ShowScore()
  {
    String s;
    int my;
    sstretch = (xSize-txtalign*2)/Math.max(bmax,smax);
    // Laser bar
    my = d.height-scoreheight+10;
    goff.setColor(new Color(0x00ff96));
    goff.drawRect(txtalign, my-10, bmax*sstretch, 10);
    goff.setFont(smallfont);
    s="laser: "+bcur+"/"+bmax;
    goff.fillRect(txtalign, my-10, bcur*sstretch, 10);
    goff.drawString(s,10,my);
    // Shield bar
    my += 15;
    goff.setColor(new Color(0x00ffff));
    goff.drawRect(txtalign, my-10, smax*sstretch, 10);
    goff.setFont(smallfont);
    s="shield: "+scur+"/"+smax;
    goff.fillRect(txtalign, my-10, scur*sstretch, 10);
    goff.drawString(s,10,my);
    // Score
    my += 20;
    goff.setColor(new Color(0xffffff));
    goff.setFont(largefont);
    s="score: "+score;
    goff.drawString(s,10,my);
  }

  public void MoveShip()
  {
    int xx, yy;
    oldx = x;
    oldy = y;

    xx = mousex;
    if (xx>0) {
	yy = mousey;
	if (xx<x) dx=-1;
	if (xx>x+sxsize) dx=1;
	if (yy<y) dy=-1;
	if (yy>y+sysize) dy=1;
	if (xx>x & xx<x+sxsize & yy>y & yy<y+sysize) {
	  dx = 0;
	  dy = 0;
	  mousex = -1;
	}
    }

    x+=dx*movex;
    y+=dy*movey;

    if (y<=borderwidth || y>=(d.height-sysize-scoreheight))
    {
      dy=0;
      y=oldy;
    }
    if (x>=(d.width-borderwidth-sxsize) || x<=borderwidth)
    {
      dx=0;
      x=oldx;
    }
  }

  public void FireGun()
  {
    int n=0, f=-1;
    while (n<blevel*10 && bx[n]>=0) n++;
    if (n<blevel*10) f = n;
    if (f>=0) {
	bx[f] = x+( (sxsize-bul_xs) / 2);
	by[f] = y;
	bcur--;
	blast.play();
    }
  }

  public void KillEmAll()
  {
    int f;
    for (int n=0; n<blevel*10; n++) {
      if (bx[n]>0) {
	by[n] -= bmy;
	if ( by[n] < borderwidth | MetHit(n) | BirdHit(bx[n], by[n]) ) {
	  bx[n] = -1;
	} else {
	  goff.drawImage(bullet, bx[n], by[n], this); // paint bullet
	}
      }
    }
  }

  public boolean MetHit(int f)
  {
    for (int n=0; n<maxmet; n++) {
      if (metx[n]>=0) {
	if (metr[n] & bx[f]+bul_xs>metx[n] & bx[f]<metx[n]+sxmet & by[f]+bul_ys>mety[n] & by[f]<mety[n]+symet) {
	  DelMeteor(n);
	  kill.play();
	  return true;
	} 
      }
    }
    return false;
  }

  public void ShowMeteors()
  {
    int n;
    mtotal = 0;
    for (n=0; n<maxmet; n++) {
      if (metx[n]>=0) {
	mtotal++;
	mety[n] += metmy;
	if (mety[n] > d.height-borderwidth-scoreheight) {
	  DelMeteor(n);
	} else {
	  if (metr[n]) {
		goff.drawImage(meteor, metx[n], mety[n], this); // paint meteor
	  } else {
		goff.drawImage(boom[bframes-metf[n]], metx[n]+(sxmet-sxbom)/2, mety[n]+(symet-sybom)/2, this); // paint boom
		metf[n]--;
		if (metf[n]<0) DelMeteor(n);
	  }
	}
      }
    }
  }

  public void NewMeteor()
  {
    int n=0, f=-1;
    metcount++;
    if (metcount > mrenew/metmy) {
	metcount = 0;
	while (n<maxmet & metx[n]>=0) n++;
	if (n<maxmet) f = n;
	if (f>=0) {
		metx[f] = (int) (Math.random() * (xSize - sxmet) + 1);
		mety[f] = borderwidth-symet;
		metr[f] = true;
		metf[f] = bframes;
	}
    }
  }

// If a star in the background reaches the bottome then it will go back to the top
        public void moveStars () {
         for (int i = 0; i < numStars; i++) {
          if (starsY[i] + 1 > ySize - (speed * 2 )) {
           starsY[i] = 0;
           starsX[i] = (int) ((Math.random() * xSize - 1) + 1);
	   starsC[i] = NewColor();
          }
          else {
           starsY[i] += speed;
          }
         }
        }

  public void Collisions()
  {

    for (int n=0; n<maxmet; n++) {
      if (metx[n]>=0) {
	if (metr[n] & x+sxsize>metx[n] & x<metx[n]+sxmet & y+sysize>mety[n] & y<mety[n]+symet) {
	  HitShip();
	  DelMeteor(n);
	} 
      }
    }

  }

  public void HitShip()
  {
    crash.play();
    shield=maxshield;
    scur--;
    if (scur<0) GameOver();
  }

  public void DelMeteor(int n)
  {
    if (metr[n]) {
	  metr[n] = false;
	  metf[n] = bframes;
    } else {
          metx[n] = -1;
	  metr[n] = true;
	  metf[n] = 0;
    }
  }

  public Color NewColor()
  {
   int[] rgb;
   int t;
   rgb = new int[3];
   for (int i=0; i<3; i++) rgb[i] = 0;
   t = (int) (Math.random()*3);
   rgb[t] = (int) (Math.random()*128 + 1) + 127;
   return new Color(rgb[0], rgb[1], rgb[2]);
  }

  public void run()
  {
    long  starttime;
    Graphics g;

    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    g=getGraphics();

    while(true)
    {
      starttime=System.currentTimeMillis();
      try
      {
        paint(g);
        starttime += 30;
        Thread.sleep(Math.max(0, starttime-System.currentTimeMillis()));
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
  }

  public void start()
  {
    if (thethread == null) {
      thethread = new Thread(this);
      thethread.start();
    }
  }

  public void stop()
  {
    if (thethread != null) {
      thethread.stop();
      thethread = null;
    }
  }

// This class handles mouse clicking
	public boolean mouseDown(Event e,int xx,int yy)
	{
          if (ingame) {
	    mousex = xx;
	    mousey = yy;
	    keyDown(e, 32);
	  } else {
	    keyDown(e, 'S');
	  }
	  return true;
	}

  // Game Start
  public void GameStart()
  {
    // Set Up Ship variables
    bmax = blevel*blevel;
    bcur = bmax;
    smax = slevel*slevel;
    scur = smax;
    difflev = 3;
    distance=0;
    score=0;
    renew=250;
    for (int n=0; n<maxmet; n++) {
	metx[n] = -1;
	metf[n] = 0;
	metr[n] = true;
    }
    metcount=0;
    metmy=2;
    mrenew=60;

    // Bosses init
    // #1 - SunBird;
    sbx = -1;
    sbmove = 2;
    maxtribe = 1;
    sunbird=false;
    sbefore = true;
    safter = false;
  }

  // Game Over
  public void GameOver()
  {
	ingame=false;
  }

  // Boss #1 - Sunbird's pack
  public void SunBird()
  {
    int[] xcur, ycur;
    xcur = new int[11];
    ycur = new int[11];
    if (sbx<0) {
	sbx = (int) ((Math.random() * xSize - 40) + 1);
	sby = -5;
	sbefore = true;
	safter = false;
    }
    sby += sbmove;
    if (y+sysize/2<sby) safter = true;
    goff.setColor(new Color(0xffff00));
    if (sbefore & safter) {
	// hit ship
	goff.fillRect(0, sby+15, xSize, 2);
	HitShip();
    }
    for (int i=0; i<11; i++) {
	xcur[i] = sbfx[i] + sbx;
	ycur[i] = sbfy[i] + sby;
    }
    goff.fillPolygon(xcur, ycur, 11);
    if (sby>xSize+20) {
	sbx=-1;
	sbefore = true;
	safter = false;
    }
    sbefore=false;
    if (y+sysize/2>sby) sbefore = true;
  }

  public boolean BirdHit(int blx, int bly) {
    if (sunbird) {
 	if (blx+bul_xs>sbx & blx<sbx+40 & bly+bul_ys>sby & bly<sby+20) {
	  tribe--;
	  if (tribe<0) sunbird=false;
	  sbx=-1;
	  sbefore = true;
	  safter = false;
	  return true;
	}
    }
    return false;
  }

}
