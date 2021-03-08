/*

AddrRec.java

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

/**
 * An address book record.
 */

public class AddrRec
{
public static final String phoneLabels[] =
	{ "Work", "Home", "Fax", "Other", "E-mail", "Main", "Pager", "Mobile" };

public int phoneLabelId[] = { 0, 0, 0, 0, 0 };
public String name;
public String firstName;
public String company;
public String phones[] = { null, null, null, null, null };
public String address;
public String city;
public String state;
public String zipCode;
public String country;
}
