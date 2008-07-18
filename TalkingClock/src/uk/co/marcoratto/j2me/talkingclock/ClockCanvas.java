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

import java.io.*;

import javax.microedition.lcdui.*;

import uk.co.marcoratto.util.Utility;

public class ClockCanvas extends Canvas implements Runnable {
    private Thread  thread;

    private boolean interrupted;
    private boolean paused;
    private Display parentDisplay;

    private Image imageNumeri[];
    private Image imageDuePunti;
    private String dirImage = "/gif/";

    public ClockCanvas(Display display) {
        super();
        this.parentDisplay = display;
        initialize();
    }

    private void initialize() {
       this.loadImages();
    	        
        // start new player
        // synchronized (thread) {
            interrupted = false;
            paused = false;
            thread = new Thread(this);
            thread.start();
        // }
        
        this.parentDisplay.setCurrent(this);

    }

    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        g.setColor(0);
        g.fillRect(0, 0, w, h);

        int x = 10;
        g.drawImage(imageNumeri[Utility.getHour() / 10], x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageNumeri[Utility.getHour() % 10], x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageDuePunti, x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageNumeri[Utility.getMin() / 10], x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageNumeri[Utility.getMin() % 10], x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageDuePunti, x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageNumeri[Utility.getSec() / 10], x, 0, Graphics.TOP | Graphics.HCENTER);
        x += 14;
        g.drawImage(imageNumeri[Utility.getSec() % 10], x, 0, Graphics.TOP | Graphics.HCENTER);
    }

    public void run() {
        // mtime update loop
        while (!interrupted) {
            try {
                this.repaint();
                Thread.sleep(1000);
            } catch (Exception ex) {
            	// Ignore
            }
        }
    }

    protected void keyPressed(int keycode) {
        switch (keycode) {
        case KEY_STAR:
        	SoundManager.getInstance().changeVolume(-10);

            break;

        case KEY_POUND:
        	SoundManager.getInstance().changeVolume(10);

            break;
        }
    }
    
    private void loadImages(){
        imageNumeri = new Image[10];

        for (int j=0;j<10;j++) {
           try {
			imageNumeri[j] = Image.createImage(dirImage + j + ".gif");
	        imageDuePunti = Image.createImage(dirImage + "duepunti.gif");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }

     }

}
