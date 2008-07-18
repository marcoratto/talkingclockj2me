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

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import uk.co.marcoratto.j2me.log.Logger;
 
public class SoundManager implements Runnable {

	private static Logger log = Logger.getLogger(SoundManager.class);
	
	private boolean enabled = true;
	private boolean terminated = true;
	private static SoundManager istanza;
	
	private Vector list = null;
	
	private Player player = null;
	private Thread thread = null;
	
	private SoundManager() {
		log.trace("SoundManager()");
		this.clearList();
	}
	
    public static SoundManager getInstance() {
        if (istanza == null) {
          try {
            istanza = new SoundManager();
          } catch (Exception e) {
            e.printStackTrace();
			log.fatal(e.getMessage());
          }
        } 
        return istanza;
    }
    
	public void clearList()  {
		log.trace("clearList()");
		this.list = new Vector();
	}

	public void addSound(String sound)  {
		log.trace("addSound(String)");
		log.debug("addSound():" + sound);
		this.list.addElement(sound);
	}
	
	public void playBackground()  {
		log.trace("playBackground()");
		if (!enabled) {
			return;
		}
		if (!terminated) {
			return;
		}
		synchronized (this) {
			terminated = false;
			this.thread = new Thread(this);
    		this.thread.start();
		}
	}

	public void playSound(String sound)  {
		log.trace("playSound(String)");
		if (!this.enabled) {
			return;
		}
		
		InputStream is = null;
		try {
			is = SoundManager.class.getResourceAsStream(sound);
			if (sound.endsWith(".wav")) {
				player = Manager.createPlayer(is, "audio/x-wav");
			} if (sound.endsWith(".amr")) {
				player = Manager.createPlayer(is, "audio/amr");
			}
			player.setLoopCount(1);
            player.prefetch();
           
			if (player.getState() == Player.STARTED) {
				return;
			}
			player.start();
            
			try {
                player.realize();
                long dur = player.getDuration();
                player.start();
                sleep(dur);
            } catch (Exception e) {
            	e.printStackTrace();
				log.warn(e.getMessage());
            }
            
            // IMPORTANT!!!
            // You MUST deallocate the player (just one)
            player.deallocate();
		} catch (Throwable e) {
			e.printStackTrace();
			log.fatal(e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Ignore
					e.printStackTrace();
					log.warn(e.getMessage());
				}
			}
		}
	}
 
	public void run()  {
		log.trace("run()");
		for (int i = 0; i < this.list.size(); i++) {
			String s = (String) this.list.elementAt(i); 
			this.playSound(s);
		}
		this.stop();
	}

	private void stop() {
		log.trace("stop()");
		synchronized (this) {
			this.thread = null;
			this.terminated = true;
		}
	}
	
	public void setEnabled(boolean enabled) {
		log.trace("setEnabled(boolean)");
		this.enabled = enabled;
	}
	
    public void sleep(long val) {
		log.trace("sleep()");
        int mval = (int)(val / 1000);
		log.debug("sleep(): " + mval + " ms.");
        try {
			Thread.sleep(mval);
		} catch (InterruptedException e) {
			// Ignore
			log.warn(e.toString());
			e.printStackTrace();
		}
    }
    
    public void changeVolume(int diff) {
		log.trace("changeVolume(int)");
        VolumeControl vc;

        if (player != null) {
            vc = (VolumeControl) player.getControl("VolumeControl");
            
            if (vc != null) {
                int cv = vc.getLevel();
                cv += diff;
                cv = vc.setLevel(cv);
            }
        }
    }

    public void stopSound() {
		log.trace("stopSound()");
    	if ((this.player != null) && 
    		(this.player.getState() == Player.STARTED)) {
    		try {
				this.player.stop();
			} catch (MediaException e) {
				// Ignore
				e.printStackTrace();
				log.warn(e.toString());
			}
    	}
    }
}