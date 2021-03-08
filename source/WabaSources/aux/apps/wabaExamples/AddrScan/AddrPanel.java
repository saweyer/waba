/*

AddrPanel.java

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

/**
 * A container displaying an address record.
 */

public class AddrPanel extends Container
{
AddrCatalog cat;
Font font;

/** Creates an AddrPanel associated with an address catalog. */
public AddrPanel(AddrCatalog cat)
	{
	this.cat = cat;
	font = new Font("Helvetica", Font.PLAIN, 12);    
	}

public void onPaint(Graphics g)
	{
	g.setFont(font);
	g.setColor(0, 0, 0);

	if (!cat.isOpen())
		{
		g.drawText("Address book not found", 0, 0);
		return;
		}

	// draw record number
	int recPos = cat.getCurrentPos() + 1;
	g.drawText(recPos + "/" + cat.getRecordCount(), this.width - 25, 0);

	AddrRec rec = cat.getCurrentRec();
	if (rec == null)
		{
		g.drawText("Record does not exist", 0, 0);
		return;
		}

	g.setFont(font);
	g.setColor(0, 0, 0);
	g.drawText("Name: " + rec.firstName + " " + rec.name, 0, 0);
	g.drawText("Company: " + rec.company, 0, 10);
	g.drawText(rec.phoneLabels[rec.phoneLabelId[0]] + ": " + rec.phones[0], 0, 20);
	g.drawText(rec.phoneLabels[rec.phoneLabelId[1]] + ": " + rec.phones[1], 0, 30);
	g.drawText(rec.phoneLabels[rec.phoneLabelId[2]] + ": " + rec.phones[2], 0, 40);
	g.drawText(rec.phoneLabels[rec.phoneLabelId[3]] + ": " + rec.phones[3], 0, 50);
	g.drawText(rec.phoneLabels[rec.phoneLabelId[4]] + ": " + rec.phones[4], 0, 60);
	g.drawText("Address: " + rec.address, 0, 70);
	g.drawText("City: " + rec.city, 0, 80);
	g.drawText("State: " + rec.state, 0, 90);
	g.drawText("ZipCode: " + rec.zipCode, 0, 100);
	g.drawText("Country: " + rec.country, 0, 110);
	}
}
