/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

package java.lang;

/**
 <hr>
 <b><font color=RED size=5>ATTENTION:</font></b><br>
 When compiling Waba programs, you should use the normal String class, not this one.
 However, this class shows you all the methods that Waba supports out of Object
 (a subset of the full collection).  Do not use any unsupported methods in your Waba code.
 <hr>
 <br>
 <br>
 <p>
 * String is an array of characters.
 * <p>
 * As with all classes in the waba.lang package, you can't reference the
 * String class using the full specifier of waba.lang.String.
 * The waba.lang package is implicitly imported.
 * Instead, you should simply access the String class like this:
 * <pre>
 * String s = new String("Hello");
 * </pre>
 */

public class String
{
char chars[];

/** Creates an empty string. */
public String()
	{
	chars = new char[0];
	}

/**
 * Creates a string from an array of strings. This method is declared protected and
 * for internal use only. The StringBuffer class should be used to create a string
 * from an array of strings.
 */
protected String(String s[], int count)
	{
	int len = 0;
	for (int i = 0; i < count; i++)
		len += s[i].chars.length;
	char c[] = new char[len];
	int j = 0;
	for (int i = 0; i < count; i++)
		{
		int slen = s[i].chars.length;
		waba.sys.Vm.copyArray(s[i].chars, 0, c, j, slen);
		j += slen;
		}
	chars = c;
	}

/** Creates a copy of the given string. */
public String(String s)
	{
	chars = s.chars;
	}

/** Creates a string from the given character array. */
public String(char c[])
	{
	this(c, 0, c.length);
	}

/**
 * Creates a string from a portion of the given character array.
 * @param c the character array
 * @param offset the position of the first character in the array
 * @param count the number of characters
 */
public String(char c[], int offset, int count)
	{
	char chars[] = new char[count];
	waba.sys.Vm.copyArray(c, offset, chars, 0, count);
	this.chars = chars;
	}

/** Returns the length of the string in characters. */
public int length()
	{
	return chars.length;
	}

/** Returns the character at the given position. */
public char charAt(int i)
	{
	return chars[i];
	}

/** Concatenates the given string to this string and returns the result. */
public String concat(String s)
	{
	if (s == null || s.chars.length == 0)
		return this;
	return this + s;
	}

/**
  * Returns this string as a character array. The array returned is
  * allocated by this method and is a copy of the string's internal character
  * array.
  */
public char[] toCharArray()
	{
	int length = length();
	char chars[] = new char[length];
	waba.sys.Vm.copyArray(this.chars, 0, chars, 0, length);
	return chars;
	}

/** Converts the given boolean to a String. */
public static String valueOf(boolean b)
	{
	return waba.sys.Convert.toString(b);
	}

/** Converts the given char to a String. */
public static String valueOf(char c)
	{
	return waba.sys.Convert.toString(c);
	}

/** Converts the given int to a String. */
public static String valueOf(int i)
	{
	return waba.sys.Convert.toString(i);
	}

/** Converts the given float to a String. */
public static String valueOf(float f)
	{
	return waba.sys.Convert.toString(f);
	}

/** Returns this string. */
public String toString()
	{
	return this;
	}

/**
 * Returns the string representation of the given object.
 */
public static String valueOf(Object obj)
	{
	if (obj == null)
		return "null";
	if (obj instanceof String)
		return (String)obj;
	return "?";
	}

/**
 * Returns a substring of the string. The start value is included but
 * the end value is not. That is, if you call:
 * <pre>
 * string.substring(4, 6);
 * </pre>
 * a string created from characters 4 and 5 will be returned.
 * @param start the first character of the substring
 * @param end the character after the last character of the substring
 @version 2.0
 */
public String substring(int start, int end)
	{
	return new String(chars, start, end - start);
	}

/** <b>WABA FOR NEWTON ONLY</b> Returns a hash code value for the object. */
public int hashCode()
    {
    int len = chars.length;
    int hash = 0;
    for(int i=0;i<len;i++)
        hash = chars[i] + 31 * hash;
    return hash;
    }

/**
 * Returns true if the given string is equal to this string and false
 * otherwise. If the object passed is not a string, false is returned.
 */
public boolean equals(Object obj)
	{
	if (obj instanceof String)
		{
		String s = (String)obj;
		if (chars.length != s.chars.length)
			return false;
		for (int i = 0; i < chars.length; i++)
			if (chars[i] != s.chars[i])
				return false;
                return true;
		}
        else return false;
	}
}
