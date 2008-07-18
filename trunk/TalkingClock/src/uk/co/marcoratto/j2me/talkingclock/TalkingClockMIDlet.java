/*
 * Copyright (C) 2008 Marco Ratto
 *
 * This file is part of the project Talking Clock J2ME.
 *
 * Talking Clock J2ME is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Talking Clock J2ME is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talking Clock J2ME; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package uk.co.marcoratto.j2me.talkingclock;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import uk.co.marcoratto.util.Utility;
import uk.co.marcoratto.j2me.i18n.I18N;
import uk.co.marcoratto.j2me.log.Logger;
import uk.co.marcoratto.j2me.about.About;

public class TalkingClockMIDlet extends MIDlet implements CommandListener {

	private static Logger log;
	
	private static ClockCanvas clockGUI = null;
	
    // display manager
    private Display display;
    private Displayable currentDisplayable; 

    private final Command cmdLog;
    private final Command cmdExit;
    private final Command cmdPlay;
    private final Command cmdAbout;
        
    // pause/resume support
    private boolean restartOnResume = false;

    public TalkingClockMIDlet() {
    	super();  	   
    	
        this.display = Display.getDisplay(this);

    	cmdLog = new Command(I18N.getInstance().translate("button.log"), Command.BACK, 1);
    	cmdExit = new Command(I18N.getInstance().translate("button.exit"), Command.BACK, 2);
    	cmdPlay = new Command(I18N.getInstance().translate("button.play"), Command.ITEM, 1);
    	cmdAbout = new Command(I18N.getInstance().translate("button.about"), Command.ITEM, 1);
    	
    	clockGUI = new ClockCanvas(display);
    	clockGUI.addCommand(cmdLog);
    	clockGUI.addCommand(cmdExit);
    	clockGUI.addCommand(cmdPlay);
    	clockGUI.addCommand(cmdAbout);
    	clockGUI.setCommandListener(this);
    	clockGUI.setTicker(new Ticker(I18N.getInstance().translate("appl.title")));
    	currentDisplayable = clockGUI;	    

    	log = Logger.getLogger(this);    	
    	log.trace("TalkingClockMIDlet(): end");
	}

    /**
     * Called when this MIDlet is started for the first time,
     * or when it returns from paused mode.
     * If a player is visible, and it was playing
     * when the MIDlet was paused, call its playSound method.
     */
    public void startApp() {
    	log.trace("TalkingClockMIDlet.startApp()");
        display.setCurrent(currentDisplayable);        	
    }

   /*
    * simple implementation, not reflected actual state
    * of player.
    */
   public void commandAction(Command c, Displayable s) {
	   if (c == cmdExit) {
           destroyApp(true);
           notifyDestroyed();
       } else if (c == cmdLog) {
    	   log.show(this);
       } else if (c == cmdAbout) {
    	   About.getInstance().show(this);
       } else if (c == cmdPlay) {
    	   this.speak();
       } 
   }

   public void destroyApp(boolean b) {
	   SoundManager.getInstance().stopSound();	   
	   display.setCurrent(null);
	   notifyDestroyed();
	 }

   /**
    * Called when this MIDlet is paused.
    * If the player GUI is visible, call its pauseSound method.
    * For consistency across different VM's
    * it's a good idea to stop the player if it's currently
    * playing.
    */
   public void pauseApp() {
	   SoundManager.getInstance().setEnabled(false);
   }

   private void speak() {
  	   int hour  = Utility.getHour();
  	   int min   = Utility.getMin();  	  

  	   
  	 SoundManager.getInstance().clearList();
  	   SoundManager.getInstance().addSound("/audio/sonoleore.wav");  	   
       if (min == 0) {
     	   SoundManager.getInstance().addSound("/audio/00.wav");  	   
       } else {
     	  SoundManager.getInstance().addSound("/audio/" + Utility.intTwoDigit(hour) + ".wav");  	   
     	  SoundManager.getInstance().addSound("/audio/e.wav");  	   
     	  SoundManager.getInstance().addSound("/audio/" + Utility.intTwoDigit(min) + ".wav");  	   
          if (min == 1) {
         	  SoundManager.getInstance().addSound("/audio/minuto.wav");  	   
          } else {
         	  SoundManager.getInstance().addSound("/audio/minuti.wav");  	   
          }
       }
       SoundManager.getInstance().playBackground();
   }
}
