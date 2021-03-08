
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
 When compiling Waba programs, you should use the normal StringBuffer class, not this one.
 However, this class shows you all the methods that Waba supports out of StringBuffer
 (a subset of the full collection).  Do not use any unsupported methods in your Waba code.
 <hr>
 <br>
 <br>
 <p>
 * StringBuffer is an array of strings.
 * <p>
 * This class is used in code such as:
 * <pre>
 * String s = "Number: " + i + " Name: " + s;
 * </pre>
 * to concatenate multiple strings together. In the code shown, the
 * compiler generates references to the StringBuffer class to append the
 * objects together.
 * <p>
 * As with all classes in the waba.lang package, you can't reference the
 * StringBuffer class using the full specifier of waba.lang.StringBuffer.
 * The waba.lang package is implicitly imported.
 * Instead, you should simply access the StringBuffer like this:
 * <pre>
 * StringBuffer sb = new StringBuffer(s);
 * </pre>
 */

public class StringBuffer
{
String strings[] = new String[4];
int count = 0;

/** Constructs an empty string buffer. */
public StringBuffer()
	{
	}

/** Constructs a string buffer containing the given string. */
public StringBuffer(String s)
	{
	append(s);
	}

/**
 * Constructs a string buffer containing the string representation of the
 * given boolean value.
 * @see waba.sys.Convert
 */
public StringBuffer append(boolean b)
	{
	return append(waba.sys.Convert.toString(b));
	}

/**
 * Constructs a string buffer containing the string representation of the
 * given char value.
 * @see waba.sys.Convert
 */
public StringBuffer append(char c)
	{
	return append(waba.sys.Convert.toString(c));
	}

/**
 * Constructs a string buffer containing the string representation of the
 * given int value.
 * @see waba.sys.Convert
 */
public StringBuffer append(int i)
	{
	return append(waba.sys.Convert.toString(i));
	}

/**
 * Constructs a string buffer containing the string representation of the
 * given float value.
 * @see waba.sys.Convert
 */
public StringBuffer append(float f)
	{
	return append(waba.sys.Convert.toString(f));
	}

/** Appends the given character array as a string to the string buffer. */
public StringBuffer append(char c[])
	{
	return append(new String(c));
	}

/** Appends the given string to the string buffer. */
public StringBuffer append(String s)
	{
	if (s == null)
		return append(String.valueOf(s));
	if (strings.length == count)
		{
		String newStrings[] = new String[strings.length * 2];
		waba.sys.Vm.copyArray(strings, 0, newStrings, 0, strings.length);
		strings = newStrings;
		}
	strings[count++] = s;
	return this;
	}

/** Appends the string representation of the given object to the string buffer. */
public StringBuffer append(Object obj)
	{
	return append(String.valueOf(obj));
	}

/**
 * Empties the StringBuffer and clears out its contents so it may be reused. The
 * only value that can be passed to this method is 0. The method is called setLength()
 * for compatibility reasons only.
 */
public void setLength(int zero)
	{
	count = 0;
	}

/** Converts the string buffer to its string representation. */
public String toString()
	{
	return new String(strings, count);
	}
}