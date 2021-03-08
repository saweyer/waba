import waba.io.*;
import waba.sys.*;
import waba.fx.*;

public class AmigoBot
    {
    public SerialPort port;
    public boolean connected;
    public Graphics mainGraphics;
      
    
    /** calculate the checksum of the data */
    public static short checksum(byte[] data)
        {
        int c=0, data1, data2;
        for(int x=0;x<data.length-1;x+=2)
            {
            data1 = data[x];  if (data1 < 0) data1 += 256;
            data2 = data[x+1];  if (data2 < 0) data2 += 256;
            c += ( (data1 << 8) | data2);
            c = c & 0xffff;
            }
        if (data.length % 2 == 1)  // odd
            {
            data1 = data[data.length-1];  if (data1 < 0) data1 += 256;
            c = c ^ data1;
            }
        return (short)c;
        }

    /** the header of the communication packets */
    public final byte[] HEADER = { (byte)(0xFA), (byte)(0xFB) };

    /**
       send the data to the robot.  the data contains only the instruction number and parameters.
       the header if automatically added, the length of the packet is calculated and the checksum is
       appended at the end of the packet, so that the user's job is easier.
    */
    public void submit(byte[] data) 
        {
	    short checksum = checksum(data);
	    byte[] temp = new byte[ data.length + 5 ];
	    Vm.copyArray(data,0,temp,3,data.length);
	    temp[0] = HEADER[0];
	    temp[1] = HEADER[1];
	    temp[2] = (byte)(data.length+2); // remember this cannot exceed 200!
	    temp[temp.length-2] = (byte)(checksum >> 8);
	    temp[temp.length-1] = (byte)(checksum & 0x00ff);
	    // write out the data
	    port.writeBytes(temp,0,temp.length);
        }
static int ypos = 15;

    public void communicationMessage(String s) 
	{	
         mainGraphics.drawText(s,0,ypos);
         ypos += 12; }

    /** Connect to the robot */
    public void connect() 
	{
        if (connected) return;
        
	byte[] buf = new byte[7];
	String s;

      try
      {	
        port = new SerialPort(0,9600,8,false,1);

        if (!port.isOpen()) 
          throw new ConnectException("Port did not open!");
	  
        port.setFlowControl(false);
        port.setReadTimeout(1000);

	byte[] temp = new byte[5];

	communicationMessage( "Sending WMS2 message" );
	temp[0] = (byte)('W');
	temp[1] = (byte)('M');
	temp[2] = (byte)('S');
	temp[3] = (byte)('2');
	temp[4] = 13;

        if (port.writeBytes(temp,0,5) < 5)
          throw new ConnectException("Could not write WSM2!" );
	
        Vm.sleep( 100 );

        // empty the buffer

        int tmp;
        while( (tmp = port.readCheck()) > 0 )
            {
            byte dele[] = new byte[ tmp ];
            if (port.readBytes( dele, 0, tmp ) <= 0)
             throw new ConnectException("Could not read WSM2 reply!" );
            }

        // SYNC0  -- Liviu's magick triple-do do do!
        
	do
	    {
	    communicationMessage( "Sending SYNC0" );
	    sync0();
	    communicationMessage( "Waiting for answer...." );
            Vm.sleep( 100 );
	    do
		{
		do
		    {
                    // read until I see a 250
		      if (port.readBytes(buf, 0, 1) <= 0)
                        throw new ConnectException("Could not read SYNC0 reply!" );
		    } while( buf[0]!=(byte)250 );
                // read another byte -- do I now see a 251?
		  if (port.readBytes(buf, 1, 1) <= 0)
                    throw new ConnectException("Could not read end of SYNC0 reply!" );
		} while( buf[0]!=(byte)250 || buf[1]!=(byte)251 );
    
            // read the remaining bytes
	    if (port.readBytes(buf,2,4) <= 0)
              throw new ConnectException("Could not clean up SYNC0 reply!" );
	    } while( buf[2]!=3 || buf[3]!=0 || buf[4]!=0 || buf[5]!=0 );

        // SYNC 1
        
	do
	    {
	    communicationMessage( "Sending SYNC1" );
	    sync1();
	    communicationMessage( "Waiting for answer...." );

            Vm.sleep( 100 );

	    if (port.readBytes(buf, 0, 6) <= 0)
              throw new ConnectException("Could not read SYNC1 reply!" );
	    } while( buf[0]!=(byte)250 || buf[1]!=(byte)251 || buf[2]!=3 ||
		     buf[3]!=1 || buf[4]!=0 || buf[5]!=1 );


        // SYNC 2
        communicationMessage( "Sending SYNC2" );
        sync2();
	communicationMessage( "Waiting for answer...." );

        Vm.sleep( 100 );

	if (port.readBytes(buf, 0, 4) <= 0)
          throw new ConnectException("Could not read SYNC2 reply!" );

        communicationMessage( "Waiting for autoconfig..." );

	// read autoconfiguration strings (3)
	s = "";
	do
	    {
	      if (port.readBytes(buf,0,1) <= 0)
                throw new ConnectException("Could not read autoconfig str!" );
	    if( buf[0]!=0 )
		s = s + (char)(buf[0]);
	    }
	while( buf[0]!=0 );
	s = "";
	do
	    {
            if (port.readBytes(buf,0,1) <= 0)
               throw new ConnectException("Could not read autoconfig str!" );
	    if( buf[0]!=0 )
		s = s + (char)(buf[0]);
	    }
	while( buf[0]!=0 );
	s = "";
	do
	    {
	      if (port.readBytes(buf,0,1) <= 0)
                throw new ConnectException("Could not read autoconfig str!" );
	    if( buf[0]!=0 )
		s = s + (char)(buf[0]);
	    }
	while( buf[0]!=0 );
      }
      catch (Exception e1)
      {
	communicationMessage(e1.toString());
	port.close();
	return;
      }
      

	// start the controller
	open();
        
        communicationMessage( "Connection established!" );	

        Vm.sleep( 1000 );

        connected = true;
	}

    protected byte lowByte( short arg )
	{
	return (byte)( arg & 0xff );
	}

    protected byte highByte( short arg )
	{
	return (byte)( (arg >> 8) & 0xff );
	}

    /** send sync0 message (for connection to robot) */
    public void sync0() 
	{
	submit(new byte[ ] { 0 });
	}

    /** send sync1 message (for connection to robot) */
    public void sync1() 
	{
	submit(new byte[ ] { 1 });
	}

    /** send sync2 message (for connection to robot) */
    public void sync2() 
	{
	submit(new byte[ ] { 2 });
	}

    /** client pulse resets watchdog and prevents the robot from disconnecting */
    public void pulse() 
	{
	submit(new byte[ ] { 0 });
	}

    /** starts the controller */
    public void open() 
	{
	submit(new byte[ ] { 1 });
	}

    /** close client-server connection */
    public void close() 
	{
	submit(new byte[ ] { 2 });
	}

    /** set sonar polling sequence */
    public void polling( String arg ) 
	{
	byte[] dele = arg.getBytes();
	byte[] temp = new byte[ 3 + dele.length ];
	temp[0] = 3; // command number
	temp[1] = 0x2B; // the parameter is a string
	temp[2] = (byte)(dele.length); // length of string
	for( int i = 0 ; i < dele.length ; i++ )
	    temp[3+i] = dele[i];
	submit( temp);
	}

    /** enable/disable the motors */
    public void enable( boolean arg ) 
	{
	if( arg )
	    submit( new byte[] { 4, 0x3B, 1, 0 });
	else
	    submit( new byte[] { 4, 0x3B, 0, 0 });
	}

    /** sets translation acc/deceleration; in mm/sec^2 */
    public void seta( short arg ) 
	{
	if( arg >= 0 )
	    submit( new byte[] { 5, 0x3B, lowByte(arg), highByte(arg) });
	else
	    submit( new byte[] { 5, 0x1B, lowByte((short)(-arg)), highByte((short)(-arg)) });
	}

    /** set maximum translation velocity; in mm/sec */
    public void setv( short arg ) 
	{
	submit( new byte[] { 6, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** resets server to 0,0,0 origin */
    public void seto() 
	{
	submit( new byte[] { 7 });
	}

    /** sets maximum rotational velocity; in degrees/sec */
    public void setrv( short arg ) 
	{
	submit( new byte[] { 10, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** move forward (+) or reverse (-); in mm/sec */
    public void vel( short arg ) 
	{
	submit( new byte[] { 11, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** turn to absolute heading; 0-359 degress */
    public void head( short arg ) 
	{
	submit( new byte[] { 12, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** turn relative to current heading */
    public void dhead( short arg ) 
	{
	submit( new byte[] { 13, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** sound duration (20 ms increments)/tone (half-cycle) pairs; <i>int</i> is string length */
    public void say( String arg ) 
	{
	byte[] dele = arg.getBytes();
	byte[] temp = new byte[ 3 + dele.length ];
	temp[0] = 15; // command number
	temp[1] = 0x2B; // the parameter is a string
	temp[2] = (byte)(dele.length); // length of string
	for( int i = 0 ; i < dele.length ; i++ )
	    temp[3+i] = dele[i];
	submit( temp);
	}

    /** request configuration SIP */
    public void config( short arg ) 
	{
	submit( new byte[] { 18, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** request continuous (>0) or stop sending (=0) encoder SIPs */
    public void encoder( short arg ) 
	{
	submit( new byte[] { 19, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** rotate at +/- degrees/sec */
    public void rvel( short arg ) 
	{
	if( arg >= 0 )
	    submit( new byte[] { 21, 0x3B, lowByte(arg), highByte(arg) });
	else
	    submit( new byte[] { 21, 0x1B, lowByte((short)(-arg)), highByte((short)(-arg)) });
	}

    /** colbert relative heading setpoint; +/- degrees */
    public void dchead( short arg ) 
	{
	submit( new byte[] { 22, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** sets rotational (+/-)de/acceleration; in mm/sec^2 */
    public void setra( short arg ) 
	{
	submit( new byte[] { 23, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** enable/disable the sonars */
    public void sonar( boolean arg ) 
	{
	if( arg )
	    submit( new byte[] { 28, 0x3B, 1, 0 });
	else
	    submit( new byte[] { 28, 0x3B, 0, 0 });
	}

    /** stops the robot (motors remain enabled) */
    public void stop() 
	{
	submit( new byte[] { 29 });
	}

    /** msbits is a byte mask that selects output port(s) for changes; lsbits set (1) or reset (0) the selected port */
    public void digout( short arg ) 
	{
	submit( new byte[] { 30, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** independent wheel velocities; lsb=right wheel; msb=left wheel; PSOS is in +/- 4mm/sec; POS/AmigOS is in 2 cm/sec increments */
    public void vel2( short arg ) 
	{
	submit( new byte[] { 32, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** Pioneer Gripper server command.  see the Pioneer Gripper manuals for details */
    public void gripper( short arg ) 
	{
	submit( new byte[] { 33, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** select the A/D port number for analog value in SIP.  selected port reported in SIP timer value */
    public void adsel( short arg ) 
	{
	submit( new byte[] { 35, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** Pioneer Gripper server value.  see P2 Gripper manual for details */
    public void gripperval( short arg ) 
	{
	submit( new byte[] { 36, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** msb is the port number (1-4) and lsb is the pulse width in 100 microsec units PSOS or 10 microsec units P2OS */
    public void ptupos( short arg ) 
	{
	submit( new byte[] { 41, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** send string argument to serial device connected to AUX port on microcontroller */
    public void tty2( String arg ) 
	{
	byte[] dele = arg.getBytes();
	byte[] temp = new byte[ 3 + dele.length ];
	temp[0] = 42; // command number
	temp[1] = 0x2B; // the parameter is a string
	temp[2] = (byte)(dele.length); // length of string
	for( int i = 0 ; i < dele.length ; i++ )
	    temp[3+i] = dele[i];
	submit( temp);
	}

    /** request to retrieve 1-200 bytes from the aux serial channel; 0 flusshes the AUX serial input buffer */
    public void getaux( short arg ) 
	{
	submit( new byte[] { 43, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** stop and register a stall in front (1), rear (2) or either (3) bump-ring contacted. Off (default) is 0 */
    public void bumpstall( short arg ) 
	{
	submit( new byte[] { 44, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** TCM2 module commands; see P2 TCM2 manual for details */
    public void tcm2( short arg ) 
	{
	submit( new byte[] { 45, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** emergency stop, overrides deceleration */
    public void e_stop() 
	{
	submit( new byte[] { 55 });
	}

    /** single-step mode (simulator only) */
    public void step() 
	{
	submit( new byte[] { 64 });
	}

    /** play stored sound */
    public void sound( short arg ) 
	{
	submit( new byte[] { 90, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** request playlist packet for sound number or 0 for all user sounds */
    public void playlist( short arg ) 
	{
	submit( new byte[] { 91, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** mute (0) or enable (1) sounds */
    public void soundtog( short arg ) 
	{
	submit( new byte[] { 92, 0x3b, lowByte(arg), highByte(arg) });
	}

    /** shuts down the communication to the robot */
    public void disconnect() 		// "Heeeey, disconnected!"
	{
        if (!connected) return;
        
	// stopping sonars
	sonar( false );

	enable( false );

	close();

	byte[] temp = new byte[4];
	temp[0] = (byte)('|');
	temp[1] = (byte)('|');
	temp[2] = (byte)('|');
	temp[3] = 13;

        port.writeBytes(temp,0,4);

	temp[0] = (byte)('W');
	temp[1] = (byte)('M');
	temp[2] = (byte)('D');
	temp[3] = 13;

        port.writeBytes(temp,0,4);
        port.close();
        connected = false;
	}

    }
