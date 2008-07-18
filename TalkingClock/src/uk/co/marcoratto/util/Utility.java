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
package uk.co.marcoratto.util;

import java.util.Calendar;

public class Utility {
	
    public final static String getHourFormatedTwoDigit() {
    	return intTwoDigit(getHour());
    }

    public final static String getMinFormatedTwoDigit() {
    	return intTwoDigit(getMin());
    }

    public final static int getHour() {
   	   return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
     }
     
    public final static int getMin() {
    	return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public final static int getSec() {
    	return Calendar.getInstance().get(Calendar.SECOND);
    }
     
    public final static String intTwoDigit(int i) {
         return ((i < 10) ? "0" : "") + i;
    }
}
