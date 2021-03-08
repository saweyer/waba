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
 When compiling Waba programs, you should use the normal Object class, not this one.
 However, this class shows you all the methods that Waba supports out of Object
 (a subset of the full collection).  Do not use any unsupported methods in your Waba code.
 <hr>
 <br>
 <br>
 <p>
 * Object is the the base class for all objects.
 * <p>
 * The number of methods in this class is
 * small since each method added to this class is added to all other classes
 * in the system.
 * <p>
 * As with all classes in the waba.lang package, you can't reference the
 * Object class using the full specifier of waba.lang.Object.
 * The waba.lang package is implicitly imported.
 * Instead, you should simply access the Object class like this:
 * <pre>
 * Object obj = (Object)value;
 * </pre>
 @version 2.0
 */

public class Object
{
/** <b>WABA FOR NEWTON ONLY</b> Returns true if this object equals the other object.   Serindipdously,
the version 1.0 of String already has the correct equals(...) method
built in, so we don't need to make one for that.*/
public boolean equals(Object obj)
    {
    return (this==obj);
    }

/** <b>WABA FOR NEWTON ONLY</b> Returns a hash code value for the object.  The hash code is not
guaranteed to remain the same over the life of the object, so don't
store it and assume it continues to be valid. */
public native int hashCode();

/** <b>MODIFIED FOR WABA FOR NEWTON</b> Returns the string representation of the object.  The standard Waba
implementation (not the Waba for Newton implementation) returns "". */
public String toString()
    {
    // not hexified like in Java, but that's probably a
    // *good* thing.  :-)
    return waba.sys.Vm.getClassName(this) + "@" + hashCode();
    }
}

