/*
  Amarino - A prototyping software toolkit for Android and Arduino
  Copyright (c) 2010 Bonifaz Kaufmann.  All right reserved.
  
  This application and its library is free software; you can redistribute
  it and/or modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package at.abraxas.amarino;

import android.content.Intent;
import at.abraxas.amarino.log.Logger;

/**
 * The MessageBuilder class converts different data types to
 * a String message which is later sent to Arduino.
 * 
 * <p>The last character of the String message is always an {@link #ACK_FLAG}.
 * If the data is given as an array, the resulting String will separate
 * the single values of the array with the {@link #DATA_DELIMITER}.</p>
 * 
 * $Id: MessageBuilder.java 444 2010-06-10 13:11:59Z abraxas $
 */
public class MessageBuilder {
	
	public static final String TAG = "MessageBuilder";
	
	public static final int STRING_FLAG = 1;
	public static final int DOUBLE_FLAG = 2;
	public static final int BYTE_FLAG = 3;
	public static final int INT_FLAG = 4;
	public static final int SHORT_FLAG = 5;
	public static final int FLOAT_FLAG = 6;
	public static final int BOOLEAN_FLAG = 7;
	public static final int CHAR_FLAG = 8;
	public static final int LONG_FLAG = 9;
	
	public static final char ALIVE_FLAG = 17;
	public static final char ARDUINO_MSG_FLAG = 18;
	public static final char ACK_FLAG = 19;
	public static final char FLUSH_FLAG = 27;
	public static final char DATA_DELIMITER = ';'; // used to separate data strings
	public static final char FLAG_DELIMITER = ' ';
	
	// alive msg is happens very often, we optimize it to be a constant
	// instead of constructing it always from ground
	public static final String ALIVE_MSG = ALIVE_FLAG + "" + ACK_FLAG;
	
	

	public static String getMessage(Intent intent){
		final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
		if (dataType == -1) {
			Logger.d(TAG, "EXTRA_DATA_TYPE not found");
			return null;
		}
		
		final char flag = intent.getCharExtra(AmarinoIntent.EXTRA_FLAG, '-');
		if (flag  == -1 ){
			Logger.d(TAG, "EXTRA_FLAG not found");
			return null;
		}
		
		
		//TODO: maybe use delimiter after length of data
		switch (dataType){
		case AmarinoIntent.STRING_EXTRA:
			String s = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
			//Logger.d(TAG, "plugin says: " + s);
			if (s==null) return "0" + ACK_FLAG;
			return flag + STRING_FLAG + "1" + FLAG_DELIMITER + s + ACK_FLAG;
			
		/* double is too large for Arduinos, better not to use this datatype */
		case AmarinoIntent.DOUBLE_EXTRA:
			double d = intent.getDoubleExtra(AmarinoIntent.EXTRA_DATA, -1);
			//Logger.d(TAG, "plugin says: " + d);
			return flag + DOUBLE_FLAG + "1" + FLAG_DELIMITER + (d + String.valueOf(ACK_FLAG));
			
		/* byte is byte. In Arduino a byte stores an 8-bit unsigned number, from 0 to 255. */
		case AmarinoIntent.BYTE_EXTRA:
			byte by = intent.getByteExtra(AmarinoIntent.EXTRA_DATA, (byte)-1);
			//Logger.d(TAG, "plugin says: " + by);
			return flag + BYTE_FLAG + "1" + FLAG_DELIMITER + (by + String.valueOf(ACK_FLAG));
			
		/* int in Android is long in Arduino (4 bytes) */
		case AmarinoIntent.INT_EXTRA:
			int i = intent.getIntExtra(AmarinoIntent.EXTRA_DATA, -1);
			//Logger.d(TAG, "plugin says: " + i);
			return flag + INT_FLAG + "1" + FLAG_DELIMITER + (i + String.valueOf(ACK_FLAG));
			
		/* short in Android is like int in Arduino (2 bytes) 2^15 */
		case AmarinoIntent.SHORT_EXTRA:
			short sh = intent.getShortExtra(AmarinoIntent.EXTRA_DATA, (short)-1);
			//Logger.d(TAG, "plugin says: " + sh);
			return flag + SHORT_FLAG + "1" + FLAG_DELIMITER + (sh + String.valueOf(ACK_FLAG));

		/* float in Android is float in Arduino (4 bytes) */
		case AmarinoIntent.FLOAT_EXTRA:
			float f = intent.getFloatExtra(AmarinoIntent.EXTRA_DATA, -1f);
			//Logger.d(TAG, "plugin says: " + f);
			return flag + FLOAT_FLAG + "1" + FLAG_DELIMITER + (f + String.valueOf(ACK_FLAG));
		
		/* boolean in Android is in Arduino 0=false, 1=true */
		case AmarinoIntent.BOOLEAN_EXTRA:
			boolean b = intent.getBooleanExtra(AmarinoIntent.EXTRA_DATA, false);
			//Logger.d(TAG, "plugin says: " + b);
			return flag + BOOLEAN_FLAG + "1" + FLAG_DELIMITER + (((b) ? 1 : 0) + String.valueOf(ACK_FLAG));
			
		/* char is char. In Arduino stored in 1 byte of memory */
		case AmarinoIntent.CHAR_EXTRA:
			char c = intent.getCharExtra(AmarinoIntent.EXTRA_DATA, 'x');
			//Logger.d(TAG, "plugin says: " + c);
			return flag + CHAR_FLAG + "1" + FLAG_DELIMITER + (c + String.valueOf(ACK_FLAG));
		
		/* long in Android does not fit in Arduino data types, better not to use it */
		case AmarinoIntent.LONG_EXTRA:
			long l = intent.getLongExtra(AmarinoIntent.EXTRA_DATA, -1l);
			//Logger.d(TAG, "plugin says: " + l);
			return flag + LONG_FLAG + "1" + FLAG_DELIMITER + (l + String.valueOf(ACK_FLAG));

		case AmarinoIntent.INT_ARRAY_EXTRA:
			int[] ints = intent.getIntArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (ints != null){
				String msg = new String();
				for (int integer : ints){
					msg += String.valueOf(integer) + DATA_DELIMITER;
				}
				return flag + INT_FLAG + ints.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.CHAR_ARRAY_EXTRA:
			char[] chars = intent.getCharArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (chars != null){
				String msg = new String();
				for (char character : chars){
					msg += String.valueOf(character) + DATA_DELIMITER;
				}
				return flag + CHAR_FLAG + chars.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.BYTE_ARRAY_EXTRA:
			byte[] bytes = intent.getByteArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (bytes != null){
				String msg = new String();
				for (byte oneByte : bytes){
					msg += String.valueOf(oneByte) + DATA_DELIMITER;
				}
				return flag + BYTE_FLAG + bytes.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.SHORT_ARRAY_EXTRA:
			short[] shorts = intent.getShortArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (shorts != null){
				String msg = new String();
				for (short shorty : shorts){
					msg += String.valueOf(shorty) + DATA_DELIMITER;
				}
				return flag + SHORT_FLAG + shorts.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.STRING_ARRAY_EXTRA:
			String[] strings = intent.getStringArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (strings != null){
				String msg = new String();
				for (String str : strings){
					msg += String.valueOf(str) + DATA_DELIMITER;
				}
				return flag + STRING_FLAG + strings.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.DOUBLE_ARRAY_EXTRA:
			double[] doubles = intent.getDoubleArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (doubles != null){
				String msg = new String();
				for (double singleDouble : doubles){ // :-)
					msg += String.valueOf(singleDouble) + DATA_DELIMITER;
				}
				return flag + DOUBLE_FLAG + doubles.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.FLOAT_ARRAY_EXTRA:
			float[] floats = intent.getFloatArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (floats != null){
				String msg = new String();
				for (float fl : floats){
					msg += String.valueOf(fl) + DATA_DELIMITER;
				}
				return flag + FLOAT_FLAG + floats.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.BOOLEAN_ARRAY_EXTRA:
			boolean[] booleans = intent.getBooleanArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (booleans != null){
				String msg = new String();
				for (boolean bool : booleans){
					msg += String.valueOf((bool) ? 1 : 0) + DATA_DELIMITER;
				}
				return flag + BOOLEAN_FLAG + booleans.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;
			
		case AmarinoIntent.LONG_ARRAY_EXTRA:
			long[] longs = intent.getLongArrayExtra(AmarinoIntent.EXTRA_DATA);
			if (longs != null){
				String msg = new String();
				for (long longo : longs){
					msg += String.valueOf(longo) + DATA_DELIMITER;
				}
				return flag + LONG_FLAG + longs.length + FLAG_DELIMITER  + finishingMessage(msg);
			}
			break;

		}
		return null;
	}
	
	private static String finishingMessage(String msg){
		int length = msg.length();
		if (length > 0)
			return msg.substring(0, length-1) + ACK_FLAG;
		else
			return msg + ACK_FLAG;
	}
	
	/**
	 * Returns array values, in a line by line matter (each value one in a separate line)
	 * 
	 * @param dataType
	 * @param array
	 * @return
	 */
	public static String getMessage(int dataType, Object array){
		String s = new String();
		switch(dataType){
			case AmarinoIntent.INT_ARRAY_EXTRA:
				int[] ints = (int[]) array;
				for (int i2 : ints){
					s += String.valueOf(i2);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.FLOAT_ARRAY_EXTRA:
				float[] floats = (float[]) array;
				for (float f : floats){
					s += String.valueOf(f);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.STRING_ARRAY_EXTRA:
				String[] strings = (String[]) array;
				for (String str : strings){
					s += str;
					s += "\n";
				}
			break;
			
			case AmarinoIntent.SHORT_ARRAY_EXTRA:
				short[] shorts = (short[]) array;
				for (short shorty : shorts){
					s += String.valueOf(shorty);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.BYTE_ARRAY_EXTRA:
				byte[] bytes = (byte[]) array;
				for (byte b : bytes){
					s += String.valueOf(b);
					s += "\n";
				}
			break;
		
			case AmarinoIntent.BOOLEAN_ARRAY_EXTRA:
				boolean[] booleans = (boolean[]) array;
				for (boolean bool : booleans){
					s += String.valueOf(bool);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.CHAR_ARRAY_EXTRA:
				char[] chars = (char[]) array;
				for (char c : chars){
					s += String.valueOf(c);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.DOUBLE_ARRAY_EXTRA:
				double[] doubles = (double[]) array;
				for (double d : doubles){
					s += String.valueOf(d);
					s += "\n";
				}
			break;
			
			case AmarinoIntent.LONG_ARRAY_EXTRA:
				long[] longs = (long[]) array;
				for (long l : longs){
					s += String.valueOf(l);
					s += "\n";
				}
			break;
		}
		return s;
	}
	
}


