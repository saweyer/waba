/*

WebPage.java

Copyright (c) 1998, 1999 Wabasoft 

Wabasoft grants you a non-exclusive license to use, modify and re-distribute
this program provided that this copyright notice and license appear on all
copies of the software.

Software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR
IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
HEREBY EXCLUDED. THE ENTIRE RISK ARISING OUT OF USING THE SOFTWARE IS ASSUMED
BY THE LICENSEE. 

WABASOFT AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING SOFTWARE.
IN NO EVENT WILL WABASOFT OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL
OR PUNITIVE DAMAGES, HOWEVER CAUSED AN REGARDLESS OF THE THEORY OF LIABILITY,
ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF WABASOFT HAS
BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

*/

import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import waba.sys.*;

public class WebPage extends Container
{
String host;
String page;
Font font = new Font("Helvetica", Font.PLAIN, 12);
FontMetrics fontMetrics;

byte text[] = new byte[2500]; // displayed text
boolean drawnOnce = false;
int ntext;
int pageNum = 0; // current page number

Timer timer;
Socket socket;

static final int NOT_CONNECTED = 0;
static final int CANT_CONNECT = 1;
static final int LOADING = 2;
static final int LOADED = 3;

int status = NOT_CONNECTED;

public void load(String url)
	{
	stopLoading();
	if (fontMetrics == null)
		fontMetrics = getFontMetrics(font);
	int len = url.length();
	if (len > 7)
		{
		String proto = url.substring(0, 7);
		if (proto.equals("http://"))
			{
			url = url.substring(7, len);
			len -= 7;
			}
		}
	String host = null;
	String page = null;
	for (int i = 0; i < len; i++)
		if (url.charAt(i) == '/')
			{
			host = url.substring(0, i);
			page = url.substring(i, len);
			break;
			}
	if (host == null)
		{
		host = url;
		page = "/";
		}

	this.host = host;
	this.page = page;
	pageNum = 0;
	ntext = 0;
	socket = new Socket(host, 80);
	socket.setReadTimeout(10);
	if (!socket.isOpen())
		{
		status = CANT_CONNECT;
		socket.close();
		socket = null;
		repaint();
		return;
		}
	status = LOADING;
	socketWriteln(socket, "GET " + page + " HTTP/1.0");
	socketWriteln(socket, "");
	socketWriteln(socket, "");
	parseInit();
	repaint();
	timer = addTimer(50);
	}

private void stopLoading()
	{
	drawnOnce = false;
	if (timer != null)
		{
		removeTimer(timer);
		timer = null;
		}
	if (socket != null)
		{
		socket.close();
		socket = null;
		}
	}

private void loadMore()
	{
	boolean doRepaint = false;
	byte data[] = new byte[512];
	int ndata = socket.readBytes(data, 0, 512);
	status = LOADED;
	if (ndata >= 0)
		{
		parseHTML(data, ndata);
		if (ntext > 160 && !drawnOnce)
			{
			// when we get more than 160 characters, draw what
			// we've got to display something quick
			doRepaint = true;
			drawnOnce = true;
			}
		}
	else
		{
		stopLoading();
		if (ntext == 0)
			status = CANT_CONNECT;
		doRepaint = true;
		}
	if (doRepaint)
		repaint();
	}

private static boolean socketWriteln(Socket socket, String s)
	{
	int len = s.length();
	byte buf[] = new byte[len + 2];
	int i = 0;
	for (; i < len; i++)
		buf[i] = (byte)s.charAt(i);
	buf[i++] = 10;
	buf[i++] = 13;
	if (socket.writeBytes(buf, 0, i) != i)
		return false;
	return true;
	}

public void pageUp()
	{
	if (pageNum == 0)
		return;
	pageNum--;
	repaint();
	}

public void pageDown()
	{
	pageNum++;
	repaint();
	}

public void onEvent(Event event)
	{
	if (event.type == ControlEvent.TIMER)
		loadMore();
	}

char cBuf[] = new char[40];
private void drawAscii(Graphics g, byte ascii[], int start, int len, int x, int y)
	{
	while (len > 0)
		{
		int n = 40;
		if (n > len)
			n = len;
		for (int i = 0; i < n; i++)
			cBuf[i] = (char)ascii[start++];
		g.drawText(cBuf, 0, n, x, y);
		x += fontMetrics.getTextWidth(cBuf, 0, n);
		len -= n;
		}
	}

public void onPaint(Graphics g)
	{
	// draw background in white
	g.setColor(255, 255, 255);
	g.fillRect(0, 0, this.width, this.height);

	g.setColor(0, 0, 0);
	if (status == NOT_CONNECTED)
		{
		g.drawText("Not connected...", 0, 2);
		return;
		}
	if (status == LOADING)
		{
		g.drawText("Loading " + page + "...", 0, 2);
		return;
		}
	if (status == CANT_CONNECT)
		{
		g.drawText("Can't connect to " + host, 0, 2);
		return;
		}
	int y = 0;
	for (int i = 0; i < 20; i++)
		{
		int charsPerLine = 30;
		int charsPerPage = 9 * charsPerLine;
		int pos = (pageNum * charsPerPage) + (i * charsPerLine);
		int len = charsPerLine;
		if (pos + len > ntext)
			len = ntext - pos;
		if (len > 0)
			drawAscii(g, text, pos, len, 0, y);
		y += 15;
		}
	}

// these variables are cleared at each call to parseHTML()
byte buf[];
int bufPos;
int bufCount;

// these variables are retained between calls to parseHTML() to perform
// incremental parsing
boolean needsSpace;
boolean inTag;
boolean inFirstTagChar;
boolean inTagComment;
boolean inTagQuote;
char prevCommentChar;

void parseInit()
	{
	needsSpace = false;
	inTag = false;
	inFirstTagChar = false;
	inTagComment = false;
	inTagQuote = false;
	}

void parseHTML(byte buf[], int bufCount)
	{
	if (bufCount == 0)
		return;
	this.buf = buf;
	this.bufCount = bufCount;
	this.bufPos = 0;
	while (true)
		{
		if (!inTag)
			parseHTMLText();
		if (ntext >= text.length - 1)
			return; // out of space
		if (bufPos >= bufCount)
			return;
		parseHTMLTag();
		if (bufPos >= bufCount)
			return;
		}
	}

void parseHTMLText()
	{
	while (true)
		{
		// parse text
		char c = (char)buf[bufPos++];
		if (c == '<')
			{
			inTag = true;
			inFirstTagChar = true;
			return;
			}
		if (c == ' ' || c < 32) //== '\n' || c == '\t' || c == 13)
			needsSpace = true;
		else
			{
			if (ntext >= text.length - 1)
				return; // out of space
			if (needsSpace)
				text[ntext++] = (byte)' ';
			text[ntext++] = (byte)c;
			needsSpace = false;
			}
		if (bufPos >= bufCount)
			return;
		}
	}

void parseHTMLTag()
	{
	// determine if normal tag or comment tag
	if (inFirstTagChar)
		{
		inFirstTagChar = false;
		char c = (char)buf[bufPos];
		if (c == '!')
			{
			inTagComment = true;
//			comment[ncomment++] = '<';
//			comment[ncomment++] = c;
			prevCommentChar = c;
			bufPos++;
			if (bufPos >= bufCount)
				return;
			}
//		else
//			tag[ntag++] = '<';
		}

	// parse tag or comment
	while (true)
		{
		char c = (char)buf[bufPos++];
		if (!inTagComment)
			{
//			if (c != '\n' && c != '\t')
//			tag[ntag++] = c;
			if (c == '"')
				inTagQuote = !inTagQuote;
			else if (c == '>' && !inTagQuote)
				{
				inTag = false;
				return;
				}
			}
		else
			{
//			if (c != '\n' && c != '\t')
//			comment[ncomment++] = c;
			if (c == '>' && prevCommentChar == '-')
				{
				inTagComment = false;
				inTag = false;
				return;
				}
			prevCommentChar = c;
			}
		if (bufPos >= bufCount)
			return;
		}
	}
}
