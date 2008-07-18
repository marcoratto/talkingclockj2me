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
package uk.co.marcoratto.j2me.log;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

import uk.co.marcoratto.j2me.i18n.I18N;

/**
 * @author Marco Ratto
 */
public class Logger implements CommandListener{

    /**
     * Max 2000 characters logged at one time
     */
    private static final int DEFAULT_MAX_SIZE = 2000;

    /**
     * Severe errors that cause premature termination. Expect these to be immediately visible on a status console.
     */
    public static final byte FATAL = 6;
    /**
     * Other runtime errors or unexpected conditions. Expect these to be immediately visible on a status console.
     */
    public static final byte ERROR = 5;
    /**
     * Use of deprecated APIs, poor use of API, 'almost' errors, other runtime situations that are undesirable or unexpected, but not necessarily "wrong". Expect these to be immediately visible on a status console.
     */
    public static final byte WARN = 4;
    /**
     * Interesting runtime events (startup/shutdown). Expect these to be immediately visible on a console, so be conservative and keep to a minimum.
     */
    public static final byte INFO = 3;
    /**
     * Detailed information on flow of through the system. Expect these to be written to logs only.
     */
    public static final byte DEBUG = 2;
    /**
     * Detailed information on flow of through the system. Expect these to be written to logs only.
     */
    public static final byte TRACE = 1;

    private static final String[] LEVEL_NAMES = {"ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};

    public static final byte OFF = 7;
    public static final byte ALL =  0;

    private static Logger logger;

    private final Form form;
    private final Command backCommand;
    private final Command refreshCommand;
    private final Command offCommand;
    private final Command fatalCommand;
    private final Command errorCommand;
    private final Command warnCommand;
    private final Command infoCommand;
    private final Command debugCommand;
    private final Command traceCommand;
    private final Command resetCommand;

    private Displayable previous;
    private Display display;
    private MIDlet parent;
    
    private StringBuffer buffer;
    private int maxSize;

    private long lastLoggedAt=0; // Timestamp of the lastlogged message

    private int loggingLevel;

    private String lastMessage;
    private int lastMessageLevel = -1;
    private int numOfLastMessage;

    private static String classname = null;
    
    public static Logger getLogger(MIDlet m) {
        if(logger == null){
            logger = new Logger(m);
        }
        return logger;
    }

    public static Logger getLogger() throws NullPointerException {
        if(logger == null){
            throw new NullPointerException("Logger.logger is null. The first time you access the Logger, you must use getLogger(MIDlet)");
        }
        return logger;
    }

    public static Logger getLogger(Class instance) throws NullPointerException {
        if(logger == null){
            throw new NullPointerException("Logger.logger is null. The first time you access the Logger, you must use getLogger(MIDlet)");
        }
        classname = instance.getName();
        return logger;
    }    

    private Logger(MIDlet m) {
        this.parent = m;
        
        this.buffer = new StringBuffer();
        this.maxSize = DEFAULT_MAX_SIZE;

        refreshCommand = new Command(I18N.getInstance().translate("log.button_refresh"), Command.OK, 1);
        backCommand = new Command(I18N.getInstance().translate("log.button_back"), Command.BACK, 2);
        offCommand = new Command(I18N.getInstance().translate("log.button_logoff"), Command.BACK, 3);
        fatalCommand = new Command(I18N.getInstance().translate("log.button_fatal"), Command.BACK, 4);
        errorCommand = new Command(I18N.getInstance().translate("log.button_error"), Command.BACK, 5);
        warnCommand = new Command(I18N.getInstance().translate("log.button_warn"), Command.BACK, 6);
        infoCommand = new Command(I18N.getInstance().translate("log.button_info"), Command.BACK, 7);
        debugCommand = new Command(I18N.getInstance().translate("log.button_debug"), Command.BACK, 8);
        traceCommand = new Command(I18N.getInstance().translate("log.button_trace"), Command.BACK, 9);
        resetCommand = new Command(I18N.getInstance().translate("log.button_reset"), Command.BACK, 10);

        // Form related setup;
        form = new Form(I18N.getInstance().translate("log.title"));
        form.addCommand(refreshCommand);
        form.addCommand(backCommand);
        form.addCommand(offCommand);
        form.addCommand(fatalCommand);
        form.addCommand(errorCommand);
        form.addCommand(warnCommand);
        form.addCommand(infoCommand);
        form.addCommand(debugCommand);
        form.addCommand(traceCommand);
        form.addCommand(resetCommand);

        form.setCommandListener(this);
       
        this.setLoggingLevel();
        this.setLoggingSize();
    }

    public void show(MIDlet m) {
        this.display = Display.getDisplay(this.parent);
        this.previous = this.display.getCurrent();
        this.display.setCurrent(form);
    }

    public void log(int level, StringBuffer message){
        System.out.println(level + ") " +
        				   ((classname == null) ? "" : classname) + 
        				   "." +
        				   message);
        if(level < TRACE || level > FATAL){
            throw new IllegalArgumentException("Logging level must be between DEBUG(" + DEBUG + ") and FATAL(" + FATAL + ")");
        }
        if(level < loggingLevel){
            return;
        }
        synchronized(buffer){


            if(level == lastMessageLevel && message.toString().equals(this.lastMessage)){
                numOfLastMessage++;
                if(numOfLastMessage == 2){
                    buffer.append("(x" + numOfLastMessage + ")");
                }else{
                    int numToDelete = 2;// at least one digit, plus ')' char.
                    int temp = numOfLastMessage -1;
                    while((temp = temp/10) > 0){
                        numToDelete++;
                    }
                    buffer.delete(buffer.length()-numToDelete, buffer.length());
                    buffer.append(numOfLastMessage + ")");
                }
            }else{
                lastMessage = message.toString();
                lastMessageLevel = level;
                numOfLastMessage = 1;

                int messageLength = message.length();
                if(messageLength + buffer.length() > maxSize){
                    if(messageLength < maxSize){
                        buffer.delete(0, messageLength);
                    }else{
                        buffer.delete(0, buffer.length());
                    }
                }

                buffer.append(message).append(' ').append('\n');

                lastLoggedAt=System.currentTimeMillis();
            }
        }
    }

    public void log(int level, String message) {
        this.log(level, new StringBuffer(message));
    }

    public void refresh(){
    	form.deleteAll();
        if(buffer.length() == 0){
        	form.append("Nothing Logged Yet");
        }else{
        	form.append(buffer.toString());
        }
    }

    public void commandAction(Command command, Displayable disp) {
            if (command == backCommand) {
            	System.out.println("back");
            	this.display.setCurrent(this.previous);
            } else if(command == refreshCommand){
                this.refresh();
            }else if(command == offCommand){
                this.setLoggingLevel(Logger.OFF);
            }else if(command == fatalCommand){
                this.setLoggingLevel(Logger.FATAL);
            }else if(command == errorCommand){
                this.setLoggingLevel(Logger.ERROR);
            }else if(command == warnCommand){
                this.setLoggingLevel(Logger.WARN);
            }else if(command == infoCommand){
                this.setLoggingLevel(Logger.INFO);
            }else if(command == debugCommand){
                this.setLoggingLevel(Logger.DEBUG);
            }else if(command == traceCommand){
                this.setLoggingLevel(Logger.TRACE);
            }else if(command == resetCommand){
            	this.buffer = new StringBuffer();
            	this.refresh();
            }
    }

    public void parseLoggingLevel(String s){
    	this.setLoggingLevel(Integer.parseInt(s));
    }
    
    public void setLoggingLevel(int level) {
        if(level < ALL || level > OFF){
            throw new IllegalArgumentException("Logging level must be between ALL(" + ALL + ") and OFF(" + OFF + ")");
        }
        this.loggingLevel = level;
        if(loggingLevel == OFF || loggingLevel == ALL){
        	form.setTitle(I18N.getInstance().translate("log.title") + "(" + LEVEL_NAMES[loggingLevel] + ")");
        }else{
        	form.setTitle(I18N.getInstance().translate("log.title") + "(" + LEVEL_NAMES[loggingLevel] + ")");
        }
    }

    public void parseLoggingSize(String s){
    	this.setLoggingSize(Integer.parseInt(s));
    }

    void setLoggingSize(int size){
        if(size < DEFAULT_MAX_SIZE) {
        	throw new IllegalArgumentException("Logging level must be greatergreater or equal to " + DEFAULT_MAX_SIZE);     
        }

    	this.maxSize = size;
    }

    public int getLoggingLevel(){
        return this.loggingLevel;
    }
    /**
     * Returns the last logged message
     * @return The last logged message
     */
    public String getLastMessage(){
        return lastMessage;
    }
    /**
     * Returns the date the last logged message was recorded at
     * @return Date
     */
    public long getTimeOfLastMessage(){
        return lastLoggedAt;
    }
    
    // printing methods:
    public void trace(String message, Throwable t) {
    	t.printStackTrace();
    	this.log(TRACE, message);    	
    	this.log(TRACE, t.getMessage());    	
    }
    public void trace(String message) {
    	this.log(TRACE, message);    	
    }
    public void trace(Hashtable ht) {
    	Enumeration keys = ht.keys();
    	while (keys.hasMoreElements()) {
    		Object key = (Object) keys.nextElement();
    		Object value = (Object) ht.get(key);
    		this.log(TRACE, key + "=" + value);	
    	}
    }
    public void debug(String message) {
    	this.log(DEBUG, message);
    }
    public void debug(Hashtable ht) {
    	Enumeration keys = ht.keys();
    	while (keys.hasMoreElements()) {
    		Object key = (Object) keys.nextElement();
    		Object value = (Object) ht.get(key);
    		this.log(DEBUG, key + "=" + value);	
    	}
    }
    public void info(String message) {
    	this.log(INFO, message);
    }
    public void warn(String message) {
    	this.log(WARN, message);
    }
    public void error(String message) {
    	this.log(ERROR, message);
    }
    public void fatal(String message) {
    	this.log(FATAL, message);
    }   
    
    private void setLoggingLevel() {
        try {
            this.parseLoggingLevel(this.parent.getAppProperty("loggingLevel"));
         } catch (Exception e) {
            // Ignore
        	 this.loggingLevel = ALL;
        	 System.err.println("Logger.setLoggingLevel(): loggingLevel not valid!");
         }
    	
    }

    private void setLoggingSize() {
        try {
            this.parseLoggingSize(this.parent.getAppProperty("loggingSize"));
        } catch (Exception e) {
            // Ignore
        	 this.maxSize = DEFAULT_MAX_SIZE;
        	 System.err.println("Logger.setLoggingSize(): loggingSize not valid!");
         }
    	
    }
}
