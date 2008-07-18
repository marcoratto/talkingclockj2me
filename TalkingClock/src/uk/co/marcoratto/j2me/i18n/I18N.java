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
package uk.co.marcoratto.j2me.i18n;

import java.util.*;
import java.io.*;

public class I18N {

    private static I18N istanza = null;

    private Hashtable tableOfLocales = new Hashtable();

	 private final static String locale = System.getProperty("microedition.locale");

    private I18N() {
    }

    public static I18N getInstance() {
      if (istanza == null) {
        try {
          istanza = new I18N();
          istanza.init();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } 
      return istanza;
    }

    private void init() throws Exception {
	   System.out.println("I18N.getInstance():" + locale);
		if (!this.tableOfLocales.containsKey(locale)) {
			 InputStream is = null;
      	 try {
				String file = "/i18n/" + locale + ".properties";
				System.out.println("I18N.getInstance(): trying read " + file);
			   try {	
            	is = this.getClass().getResourceAsStream(file);
				} catch(Exception e) {
					System.err.println("I18N.getInstance(): ERRORE! File " + file + " not found.");
				} 
				if (is == null) {
				      file = "/i18n/en.properties";
						System.out.println("I18N.getInstance(): trying read " + file);
              		is = this.getClass().getResourceAsStream(file);
				}
            StringBuffer str = new StringBuffer();
            byte b[] = new byte[1];

				Hashtable table = new Hashtable();
            while (is.read(b) != -1 ) {
                if (b[0] == 10) {
						String s = str.toString();
						if ((s.indexOf("=") >= 0) && (!s.startsWith("#"))) {
							String key = s.substring(0, s.indexOf("="));
							String value = s.substring(s.indexOf("=") + 1, s.length());
							System.out.println("I18N.getInstance(): key=" + key);
							System.out.println("I18N.getInstance(): value=" + value);
							
							//
							if (table.containsKey(key)) {
								String tmp = (String) table.get(key);
								value = tmp + "\n" + value;
							}
							table.put(key, value);
						}
                  str = new StringBuffer();
                } else {
                  str.append(new String(b));
                }
            }
				this.tableOfLocales.put(locale, table);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (IOException e) {
               }
            }
        }
		}
   }

	public String translate(String key) {
	  String out = key;
	  if (this.tableOfLocales.containsKey(locale)) {
		 Hashtable ht = (Hashtable) this.tableOfLocales.get(locale);
		 if (ht.containsKey(key)) {
			out = (String) ht.get(key);
		 }
     }
	  System.out.println("I18N.translate(): key=" + key);
	  System.out.println("I18N.translate(): value=" + out);
	  return out;
	}
}

