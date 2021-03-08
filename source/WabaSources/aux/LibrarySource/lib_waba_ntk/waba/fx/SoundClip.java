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

package waba.fx;


/**
 * SoundClip is a sound clip.
 * <p>
 * Support for sound clips varies between platforms. Under Java, sound clips are
 * only supported by Java applets - not applications. This is primarily because
 * AudioClips in Java weren't supported in Java applications until the JDK 1.2
 * without using undocumented method calls. Even when using applets, some Java
 * virtual machines support .wav and .au sound files and some versions don't
 * seem to support either format.
 * <p>
 * Using a Waba Virtual Machine, .wav format sound clips are supported under
 * Win32 and WinCE. The WabaVM under PalmOS has no support for sound clips.
 * Under Win32 and WinCE, the .wav files for sound clips must exist in a file
 * outside of the programs warp (resource) file.
 * <p>
 * If you're playing a sound clip under a Windows CE device and you don't hear
 * anything, make sure that the device is set to allow programs to play sounds.
 * To check the setting, look at:
 * <p>
 * Start->Settings->Volume & Sounds
 * <p>
 * for the check box:
 * <p>
 * Enable sounds for: Programs
 * <p>
 * If it is not checked on, sound clips won't play.
 * <p>
 * Here is an example that plays a sound:
 *
 * <pre>
 * SoundClip s = new SoundClip("sound.wav");
 * s.play();
 * </pre>
 */
public class SoundClip
{
String path;
boolean loaded;

/**
 * Loads and constructs a sound clip from a file.
 */
public SoundClip(String path)
	{
	this.path = path;
	loaded = false;
	}

/**
 * Plays the sound clip. Returns true if the sound starts playing and false otherwise.
 */
public native boolean play();

}