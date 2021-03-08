// =========== Header ===========// File:				NIE.h// Project:				(Library)// Written by:			Paul Guyot (pguyot@kallisys.net)//// Created on:			06/02/2001// Internal version:	1//// Copyright:			� 2001 by Paul Guyot.// 						All rights reserved worldwide.// ===========// =========== Change History ===========// 06/02/2001	v1	[PG]	Creation of the file// ===========#ifndef __NIE__#define __NIE__// NIE APIs are defined in this file (and the .cp file)// We have://	Various constants for NIE//	Options objects for NIE// Based on NIE 2.0 APIs#ifndef __OPTIONARRAY_H	#include <OptionArray.h>#endif// ============================== ////   ����� NIE Constants �����    //// ============================== //// I'm using a class as a name space for all constants.class KNIEConsts{public:	enum {		kNIEServiceID								=	'inet'	// For use with the service option.	};	// Transport Service Type Constants		enum {		kTCP										=	1,	// Use TCP transport service.		kUDP										=	2	// Use UDP transport service.	};		// Domain Name Server Result Type	enum {		kDNSAddressType								=	1,	// The operation that generated the results frame resulted															// in an IP address. For example, the															// DNSGetAddressFromName function.		kDNSDomainNameType							=	2	// The operation that generated the result frame resulted															// in a domain name string. For example, the															// DNSGetNameFromAddress function.	};	// Link Controller Error Codes		enum {		kInetErrNoSuchLinkID						=	-60501,	// The specified link identifier does not exist.		kInetErrLinkDisconnected					=	-60504,	// The link has been disconnected.		kInetErrConnectLinkFailed					=	-60505,	// The link could not connect.		kInetErrTooManyISPLinks						=	-60506,	// A request to open a new link occurred when a link																// was already active.		kInetErrNoSuchPhysicalLayerService			=	-60507,	// The physical layer service specified in																// the link entry was not available.		kInetErrNoSuchLinkLayerService				=	-60508,	// The link layer service specified in the link																// entry was not available.		kInetErrBadIPAddress						=	-60509,	// The link entry contained an invalid IP address.		kInetErrPowerOff							=	-60510,	// Power off was in progress or had already occurred.		kInetErrScriptTimeout						=	-60511,	// A step in the link script timed out.		kInetErrScriptCommand						=	-60512	// There was a problem with a link script command	};	// DNS Error Codes		enum {		kDNSErrNoAnswerFoundYet						=	-60751,	// The answer for the question has not yet been found.		kDNSErrInternalErr							=	-60752,	// Internal DNS tool error.		kDNSErrNameSyntaxErr						=	-60791,	// The name in the DNS request is not valid.		kDNSErrNoNameServer							=	-60794,	// The option specifiation does not contain a name server.		kDNSErrAuthNameErr							=	-60795,	// The domain name does not exist.		kDNSErrNoAnswerErr							=	-60796,	// No answers available for request; this could be due to a																// domain that does not exist.				kDNSErrNonexistentDomain					=	-60797,	// The domain name does not exist.		kDNSErrOutOfMemory							=	-60798,	// DNS tool out of memory.		kDNSErrCouldNotContactServer				=	-60800,	// Could not connect to the current DNS server.		kDNSErrNoServersAvailable					=	-60801,	// Could not connect to any of the listed DNS																// servers.		kDNSErrRequestFormatErr						=	-60802,	// The DNS server did not like the format of the																// request, which could indicate an invalid domain name.		kDNSErrServerInternalErr					=	-60803,	// An internal error occurred in the DNS server.		kDNSErrServerNotImplemented					=	-60804,	// The DNS server does not support the specified																// type of request.		kDNSErrServerRefused						=	-60805,	// The DNS server refused to answer the client's query.		kDNSErrUnknownServerErr						=	-60806,	// The DNS server returned an error code that is not																// recognized.		kDNSErrNoResponseFromServer					=	-60814,	// No response from the current server		kDNSErrNoResponseFromAnyServer 				=	-60815	// No response from any of the available DNS																// servers.	};		// Newton Internet Enabler PPP Errors		enum {		kInetToolErrPppAuthSetupFailed				=	-60084,	// The authentication type was rejected,																// probably because the remote host does not support PAP																// or CHAP.		kInetToolErrPppAuthReqFailed				=	-60085	// The authentication request failed, probably																// due to incorrect user name and/or password entry.	};	// --- Newton Internet Enabler Lower-Level Tool Errors ---	// Inet Tool Errors	enum {		kInetToolErrBindFailed						=	-60001,	// The bind operation failed at the lowest level.		kInetToolErrIPBindFailed					=	-60002,	// The IP layer bind operation failed.		kInetToolErrPushModule						=	-60004,		kInetToolErrIlink							=	-60005,		kInetToolErrNetActivateReq					=	-60006,		kInetToolErrTCPBind							=	-60007,	// The TCP layer bind operation failed.		kInetToolErrGetRequest						=	-60008,	// The get request resulted in an error.		kInetToolErrPutRequest						=	-60009,	// The put request resulted in an error.		kInetToolErrConnect							=	-60010,	// The connect request resulted in an error.		kInetToolErrDlAttach						=	-60011,		kInetToolErrBind							=	-60012,		kInetToolErrOpenLink						=	-60013,		kInetToolErrUnlink							=	-60014,		kInetToolErrOutOfPhase						=	-60015,	// The stack layers are out of sync.		kInetToolErrAddRoute						=	-60016,		kInetToolErrListen							=	-60017,		kInetToolErrLinkNotOpened					=	-60018,		kInetToolErrDriverNotOpened					=	-60019,		kInetToolErrStreamNotOpened					=	-60020,		kInetToolErrBindReqFailed					=	-60021,	// The bind request failed.		kInetToolErrConnResReqFailed				=	-60022	};	// Application-related Errors	enum {		kInetToolErrMemAlloc						=	-60023,	// Requested memory could not be allocated.		kInetToolErrMsgType							=	-60024,		kInetToolErrNoDevice						=	-60025,	// No stream available.		kInetToolErrIllegalOpenOnStream				=	-60026,		kInetToolErrReqInInvalidState				=	-60027,		kInetToolErrPrimitiveTooSmall				=	-60028,		kInetToolErrPrimitiveOutOfRange				=	-60029,		kInetToolErrPrimitiveOnInvalidStr			=	-60030,		kInetToolErrMessageTooLong					=	-60031,		kInetToolErrNetworkAlreadyActive			=	-60032,		kInetToolErrNetworkNumberInvalid			=	-60033,		kInetToolErrUnsupportedIoctl				=	-60034,		kInetToolErrStreamAlreadyAttached			=	-60035,		kInetToolErrUnknownMuxIndex					=	-60036,		kInetToolErrNetworkIsInactive				=	-60037,		kInetToolErrBogusConnection					=	-60038,		kInetToolErrInvalidBillingMode				=	-60039,		kInetToolErrNoTrigSelectedInAlarm			=	-60040,		kInetToolErrInvalidTrigSize					=	-60041,		kInetToolErrInvalidConnectionRef			=	-60042,		kInetToolErrIlegalMdataInPrim				=	-60043,		kInetToolErrMissingMdataInPrim				=	-60044,		kInetToolErrInvalidSegmentedPrim			=	-60045,		kInetToolErrInvalidNPIVersion				=	-60046,		kInetToolErrInvalidAddress					=	-60047,		kInetToolErrOutOfTCPPortNumbers				=	-60048,		kInetToolErrSocketInUse						=	-60049,		kInetToolErrReservedPortNumber				=	-60050,		kInetToolErrExpDataNotSupported				=	-60051	};		// UDP Errors		enum {		kInetToolErrRedundentRequest				=	-60052,		kInetToolErrUnexpectedDLPrim				=	-60053,		kInetToolErrUnexpectedTPIPrim				=	-60054,		kInetToolErrUnexpectedNPIPrim				=	-60055,		kInetToolErrUnknownTPIErrorCode				=	-60056	};		// Inet Tool-Specific Errors		enum {		kInetErrStreamInoperative					=	-60057,	// The communications connection shut down due to a																// fatal error.		kInetErrUnexpectedMgmtEvent					=	-60058	// The communications connection shut down due to an																// internal error.	};	// PPP Internal Errors		enum {		kInetErrLinkActivationNotConfirmed			=	-60059,	// Link activation not confirmed.		kInetErrLinkDeactivationNotConfirmed		=	-60060,	// Link deactivation not confirmed.		kInetErrLinkTerminateAckNotReceived			=	-60061,	// Termination acknowledgment not																// received.		kInetErrNetworkDifferentFromTheReq			=	-60062,	// Illegal value fo internal network																// parameter.		kInetErrConnLossFromTerminateReq			=	-60063,	// The connection was lost due to a																// terminate request.		kInetErrConnLossFromConfigureRequest		=	-60064,	// The connection was lost due to a																// configuration request.		kInetErrConnLossFromConfigureReject			=	-60065,	// The connection was lost due to a																// configuration rejection.		kInetErrConnLossFromConfigureAck			=	-60066,	// The connection was lost due to a																// configuration acknowledgment.		kInetErrConnLossFromConfigureNack			=	-60067,	// The connection was lost due to a																// configuration NACK.		kInetErrConnLossFromCodeReject				=	-60068,	// The connection was lost due to a code																// rejection.		kInetErrConnLossFromMaxSetUpAttempts		=	-60069,	// The connection was lost due to																// exceeding the maximum number of setup attempts.		kInetErrConnLossFromMandatoryFrameSize		=	-60070,	// The connection was lost due to not																// meeting the mandatory frame size.		kInetErrConnLossFromMandatoryAuthRej		=	-60071,	// The connection was lost due to the																// rejection of a mandatory authentication option																// configuration request.		kInetErrConnLossFromRemoteInitAuthFail		=	-60072,	// The connection was lost due to a																// failure of the authentication initiated by the remote host.		kInetErrConnLossFromLocalInitAuthFail		=	-60073,	// The connection was lost due to a																// failure of the authentication initiated by the local host.		kInetErrConnLossFromLowerLinkFailure		=	-60074,	// The connection was lost due to a																// failure at a lower link level.		kInetErrConnLossUnspecifiedReason			=	-60075,	// The connection was lost for an																// unspecified reason.		kInetErrConnLossFromMaxRemoteInitAuthFail	=	-60076,	// The connection was lost due to																// the remote host exceeding the maximum number of																// authentication initiations.		kInetErrConnLossFromMaxLocalInitAuthFail	=	-60077,	// The connection was lost due to																// the local host exceeding the maximum number of																// authentication initiations.		kInetErrConnLossFromMngmntDeactivation		=	-60078,	// The connection was lost due to an																// internal deactivation request.		kInetErrConnLossFromNoIpAddr				=	-60079,	// The connection was lost because no valid IP																// address was negotiated.		kInetToolErrDataLinkLevel					=	-60080,	// A data link level error occurred.		kInetToolErrSubsBind						=	-60081,	// A subtool binding error occurred.		kInetToolErrNetDeActivateReq				=	-60082,	// An error occurred in the net deactivation																// request.		kInetToolErrNetworkLevel					=	-60083	// A network level error occurred.	};	// Multicast Group Errors	enum {		kInetToolErrJoinMulticast					=	-60086,	// An error occurred on a request to join a multicast group.		kInetToolErrLeaveMulticast					=	-60087	// An error occurred on a request to leave a multicast																// group.	};		// DHCP Errors		enum {		kInetToolErrDhcpReqFailed					=	-60088	// The dynamic host configuration protocol request failed.	};};// ============================ ////   ����� NIE Options �����    //// ============================ //// --- Inet Tool Expedited Next Byte Transfer ('iexn') Option ---// The Inet Tool expedited next byte transfer option allows you to immediately// transfer a byte of data over a TCP connection without specifying the data in// the option; instead, the next byte of data in the output data stream is// expedited.// This option contains no fields. It must accompany an output (PutBytes// option) request. When the expedited next byte option accompanies an output// request, the system software sends the first byte in the output data stream as// TCP expedited data.// This option is new in Newton Internet Enabler version 2.0.// Note// You can only use this option with TCP connections.class TITOExpeditedNextByteTransfer	:		public TOption{public:					TITOExpeditedNextByteTransfer( void );private:	enum {		kOptionID			=	'iexn',		kOptionDataLength	=	0	};};// --- Inet Tool Expedited Data Transfer ('iexp') Option ---// The Inet Tool expedited data transfer option is used for the expedited// transmission of an unsigned data byte. You can use this option with an// Output request to your endpoint to cause the data in that request to be sent// immediately. You typically use this to send a break character or a similar// indicator.// WARNING// This option is still available; however, it has been deprecated// and my not be available in future releases. Use the expedited// next byte option.class TITOExpeditedDataTransfer	:		public TOption{public:					TITOExpeditedDataTransfer( void );	// Accessors	inline	UChar	GetByte( void )		{			return mByte;		}	inline	void	SetByte( UChar inByte )		{			mByte = inByte;		}		private:	enum {		kOptionID			=	'iexp',		kOptionDataLength	=	4,	// sizeof( UChar ),		kByteDefault		=	0	};	// Option data:		UChar			mByte;	// The data byte that was received or is to be sent.};// --- Inet Tool Physical Link Identifier ('ilid') Option ---// The Inet physical link identifier option is used to set or retrieve the physical// link identifier.class TITOPhysicalLinkIdentifier	:		public TOption{public:					TITOPhysicalLinkIdentifier( void );	// Accessors	inline	ULong	GetLinkID( void )		{			return mLinkID;		}	inline	void	SetLinkID( ULong inLinkID )		{			mLinkID = inLinkID;		}		private:	enum {		kOptionID			=	'ilid',		kOptionDataLength	=	sizeof( ULong ),		kLinkIDDefault		=	0	};	// Option data:	ULong			mLinkID;	// The link identifier.};// -- Inet Tool Local Port ('ilpt') Option ---// The Inet Tool local port option is used to set or retrieve the Internet port// number for a transport service. The following rules apply to port number// assignments.// - Use of the port number by the Inet tool -// Connect over TCP link://	TCP picks this port; no need to set.// Listen over TCP link://	The port on which to listen. Specify 0 to indicate//	listening on all ports or use one of the port numbers as//	specified in IEFT Assigned Numbers RFC.// Connect over UDP link://	The port to bind to locally. Specify useDefault:true to//	indicate that Newton Internet Enabler should choose the//	port number for you, in which case the assigned value//	will be returned in the option.// Listen over UDP link://	The port on which to listen. Specify 0 to indicate//	listening on all ports or use one of the port numbers as//	specified in IEFT Assigned Numbers RFC.class TITOLocalPort	:		public TOption{public:					TITOLocalPort( void );	// Accessors	inline	UShort	GetPortNumber( void )		{			return mPortNumber;		}	inline	void	SetPortNumber( UShort inPortNumber )		{			mPortNumber = inPortNumber;		}		// I can't see any reason why you would need to retrieve the use default field.	inline	void	SetUseDefault( Boolean inUseDefault )		{			mUseDefault = inUseDefault;		}		private:	enum {		kOptionID			=	'ilpt',		kOptionDataLength	=	8,	// sizeof( UShort )									// + sizeof( Boolean ),		kPortNumberDefault	=	0,		kUseDefaultDefault	=	false	// Default of useDefault is false	};	// Option data:	UShort			mPortNumber;	// The reserved port number for this service. This									// value is used as previously described.	Boolean			mUseDefault;	// A Boolean value that applies only to connect									// binds for the UDP transport service type. If									// useDefault is true, the default UDP port									// number is used.};// --- Inet Profile ('iprf') Option --- // The Inet profile option is used to retrieve the the local and gateway IP// addresses used by your endpoint.class TITOProfile	:		public TOption{public:					TITOProfile( void );	// Accessors	void			GetLocalHostIP( UChar outLocalHostIP[4] );	void			GetGatewayHostIP( UChar outGatewayHostIP[4] );	private:	enum {		kOptionID			=	'iprf',		kOptionDataLength	=	sizeof( UChar[4] )								+ sizeof( UChar[4] )	};	// Option data:	UChar			mLocalHostIP[4];	// local host (Newton) IP address	UChar			mGatewayHostIP[4];	// gateway host IP address};// --- Inet Tool TCP Remote Socket ('itrs') Option ---// The Inet Tool TCP remote socket option is used to set or retrieve the// parameters of the remote host. If you are sending a Connect request over a// TCP link, you must use this option to set the remote socket address; if// you are sending a Listen request over a TCP link, you can use this option to// retrieve the address of the sender of the data.class TITOTCPRemoteSocket	:		public TOption{public:					TITOTCPRemoteSocket( void );	// Accessors	void			GetRemoteHostIP( UChar outRemoteHostIP[4] );	void			SetRemoteHostIP( const UChar inRemoteHostIP[4] );	inline	UShort	GetRemoteHostPort( void )		{			return mRemoteHostPort;		}	inline	void	SetRemoteHostPort( UShort inRemoteHostPort )		{			mRemoteHostPort = inRemoteHostPort;		}	private:	enum {		kOptionID				=	'itrs',		kOptionDataLength		=	8,	// sizeof( UChar[4] )										// + sizeof( UShort ),		kRemoteHostIPDefault	=	0,	// [0,0,0,0]		kRemoteHostPortDefault	=	0	};	// Option data:	UChar			mRemoteHostIP[4];	// Internet address of remote host IP address,										// expressed as four single-byte values.	UShort			mRemoteHostPort;	// Reserved Internet port identifier.};// --- Inet Tool Transport Service Type ('itsv') Option ---// The Inet Tool transport service type option is used to specify the transport// service type associated with a link.class TITOTransportServiceType	:		public TOption{public:					TITOTransportServiceType( void );	// Accessors	inline			SetServiceType( ULong inServiceType )		{			mServiceType = inServiceType;		}	private:	enum {		kOptionID				=	'itsv',		kOptionDataLength		=	sizeof( ULong ),		kServiceTypeDefault		=	KNIEConsts::kTCP	};	// Option data:	ULong			mServiceType;	// The transport service type. Use one of the									// constants previously defined.};// --- Inet Tool UDP Destination Socket ('iuds') Option ---// The Inet Tool UDP destination socket option is used to set or retrieve the// Internet destination host Internet socket address that is used for data// transmission over a UDP link.class TITOUDPDestinationSocket	:		public TOption{public:					TITOUDPDestinationSocket( void );	// Accessors	void			GetDestinationIP( UChar outDestinationIP[4] );	void			SetDestinationIP( const UChar inDestinationIP[4] );	inline	UShort	GetDestinationPort( void )		{			return mDestinationPort;		}	inline	void	SetDestinationPort( UShort inDestinationPort )		{			mDestinationPort = inDestinationPort;		}	private:	enum {		kOptionID				=	'iuds',		kOptionDataLength		=	8,	// sizeof( UChar[4] )										// + sizeof( UShort ),		kDestinationIPDefault	=	0,	// [0,0,0,0]		kDestinationPortDefault	=	0	};	// Option data:	UChar			mDestinationIP[4];	// The destination IP address, expressed as four										// single-byte values.	UShort			mDestinationPort;	// The reserved Internet port identifier.};// --- Inet Tool UDP Source Socket ('iuss') Option ---// The Inet Tool UDP source socket option is used to retrieve the host Internet// socket address that sent a datagram received by your application.class TITOUDPSourceSocket	:		public TOption{public:					TITOUDPSourceSocket( void );	// Accessors	void			GetSourceIP( UChar outSourceIP[4] );	inline	UShort	GetSourcePort( void )		{			return mSourcePort;		}	private:	enum {		kOptionID				=	'iuss',		kOptionDataLength		=	8	// sizeof( UChar[4] )										// + sizeof( UShort )	};	// Option data:	UChar			mSourceIP[4];	// The source IP address, expressed as four									// single-byte values.	UShort			mSourcePort;	// The reserved Internet port identifier.};// --- Inet Tool UDP Multicast ('iumc') Option ---// You use the Inet Tool UDP multicast option to join or leave a multicast// group. To join a multicast group, specify kJoinMulticastGroup (1) in the// arguments list; to leave a multicast group, specify kLeaveMulticastGroup (2).// Note// You must send both the UDP multicast ('iumc') and UDP// broadcast ('iubc') options if you want to receive multicast// packets.class TITOUDPMulticast	:		public TOption{public:					TITOUDPMulticast( void );	// Accessors	void			SetGroupIP( const UChar inGroupIP[4] );	// It's not clear whether the tool should be set or get.	// Example sets it to nil.	inline	long	GetTool( void )		{			return mTool;		}	inline	void	SetTool( long inTool )		{			mTool = inTool;		}	inline	long	GetExtendedResult( void )		{			return mExtendedResult;		}	inline	void	SetMulticastRequest( ULong inMulticastRequest )		{			mMulticastRequest = inMulticastRequest;		}	enum {		kJoinMulticastGroup		=	1,		kLeaveMulticastGroup	=	2	};private:	enum {		kOptionID					=	'iumc',		kOptionDataLength			=	16,	// sizeof( long )											// + sizeof( long )											// + sizeof( ULong )											// + sizeof( UChar ),		kMulticastRequestDefault	=	kJoinMulticastGroup,		kToolDefault				=	0,		kGroupIPDefault				=	0	// [0,0,0,0]	};	// Option data:	long					mTool;	long					mExtendedResult;		// The result code of the operation; this slot is		// valid when the result of the option request is		// opFailure. The result can be one of the following:		//	kCommErrNotConnected:			The connection is not established.		//	kCommErrBadCommand:				The transport type is not UDP.		//	kCommErrBadParameter:			The kMulticastRequest slot does not contain		//										a valid value.		//	kInetToolErrLeaveMulticast:		You specified kLeaveMulticastGroup for a		//										multicast group that you had not previously		//										joined.	ULong					mMulticastRequest;	// The operation type.	UChar					mGroupIP[4];};// --- Inet Tool UDP Broadcast ('iubc') Option ---// You use the Inet Tool UDP broadcast option to control receiving broadcast// datagrams, receiving multicasted datagrams, and whether multicast// datagrams that you send are also received by your connection end.// Note// You must send both the UDP multicast ('iumc') and UDP// broadcast ('iubc') options if you want to receive multicast// packets.class TITOUDPBroadcast	:		public TOption{public:					TITOUDPBroadcast( void );	// Accessors	inline	void	SetReceiveBroadcast( Boolean inReceiveBroadcast )		{			mReceiveBroadcast = inReceiveBroadcast;		}	inline	void	SetReceiveMulticast( Boolean inReceiveMulticast )		{			mReceiveMulticast = inReceiveMulticast;		}	inline	void	SetEchoMulticast( Boolean inEchoMulticast )		{			mEchoMulticast = inEchoMulticast;		}private:	enum {		kOptionID					=	'iubc',		kOptionDataLength			=	4,	// sizeof( Boolean )											// + sizeof( Boolean )											// + sizeof( Boolean ),		kReceiveBroadcastDefault	=	false,		kReceiveMulticastDefault	=	true,		kEchoMulticastDefault		=	false	};	// Option data:	Boolean				mReceiveBroadcast;	// Set to true to enable receiving of broadcasted											// datagrams. This slot defaults to false.	Boolean				mReceiveMulticast;	// Set to true to enable receiving of multicasted											// datagrams. This slot defaults to true.	Boolean				mEchoMulticast;		// Set to true to enable receiving of multicast											// datagrams that you send. This slot defaults to											// false};#endif		// __NIE__// ============================================================================ //// Build a system that even a fool can use and only a fool will want to use it. //// ============================================================================ //