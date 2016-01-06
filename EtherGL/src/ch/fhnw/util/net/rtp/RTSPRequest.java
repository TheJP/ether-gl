package ch.fhnw.util.net.rtp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ch.fhnw.util.Log;

public class RTSPRequest implements Runnable {
	private final static Log log = Log.create();
	final static int MJPEG_TIMEBASE = 90000;
	
	final static String CRLF = "\r\n";

	//----------------
	//rtsp message types
	enum REQType {
		OPTIONS,
		SETUP,
		PLAY,
		PAUSE,
		TEARDOWN,
		DESCRIBE,
	}

	final static Set<String> REQTypes = new HashSet<>();
	static {
		for(REQType t : REQType.values())
			REQTypes.add(t.toString());
	}

	private Socket socketRTSP; //socket used to send/receive RTSP messages
	//input and output stream filters
	private DataInputStream    in;
	private DataOutputStream   out;
	private int                localRTSPport = 0;
	private Map<String,String> request = new HashMap<>();
	private int                cSeq;
	private final String       label;
	private final InetAddress  clientIP;   //Client IP address
	private int                sessionKey = -1;
	private final RTPServer    server;

	//--------------------------------
	//Constructor
	//--------------------------------
	public RTSPRequest(RTPServer server, Socket socket) throws Exception {
		this.server        = server;
		this.label         = socket.toString();
		this.socketRTSP    = socket;
		this.localRTSPport = socket.getLocalPort();
		RTPServer.log(this+"New client connection");

		//Get Client IP address
		clientIP = socketRTSP.getInetAddress();

		//Set input and output stream filters:
		in  = new DataInputStream(socketRTSP.getInputStream());
		out = new DataOutputStream(socketRTSP.getOutputStream());

		Thread t = new Thread(this, label);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		try {
			msg_loop:
				for(;;) {
					//------------------------------------
					//Parse RTSP Request
					//------------------------------------
					REQType reqType = null;
					RTPServer.log(this + "Received from Client:");
					for(;;) {
						//parse request line and extract the request_type:
						String line = readLine(in);
						RTPServer.log(line);
						if(line == null) break msg_loop;
						if(line.length() == 0) break;
						StringTokenizer tokens = new StringTokenizer(line);
						String key = tokens.nextToken();

						if("CSeq:".equals(key))
							cSeq = Integer.parseInt(tokens.nextToken());
						else if("Session:".equals(key))
							sessionKey= Integer.parseInt(tokens.nextToken());

						if(REQTypes.contains(key))
							reqType = REQType.valueOf(key);
						request.put(key, line.substring(key.length() + 1));
					}

					switch(reqType) { 
					case OPTIONS:
						send(RTSP_options());
						break; // msg_loop;
					case SETUP:
						new RTPSession(server, this);
						break;
					case PLAY:
						server.getSession(this).play(this);
						break;
					case PAUSE:
						server.getSession(this).pause(this);
						break;
					case TEARDOWN:
						server.getSession(this).teardown(this);
						break msg_loop;
					case DESCRIBE:
						send(RTSP_describe());
						break;
					default:
						log.warning(this + "Unexpected request:" + request);
						break;
					}
				}
		in.close();
		out.close();
		} catch(Throwable t) {
			log.warning(toString(), t);
		}
	}

	private String readLine(DataInputStream in) throws IOException, InterruptedException {
		StringBuilder result = new StringBuilder();
		for(;;) {
			int ch = in.read();
			if(ch == 0x24) {
				int channel = in.read();
				int size    = in.readChar();
				byte[] data = new byte[size];
				in.readFully(data, 0, size);
				RTPSession session = server.getSession(this);
				if(session != null)
					session.recv(channel, data);
				continue;
			}
			if(ch < 0) return null;
			else if(ch == '\r') continue;
			else if(ch == '\n') return result.toString();
			else result.append((char)ch);
		}
	}

	void send(String msg) throws IOException {
		RTPServer.log(this + "Sent to Client:");
		RTPServer.log(msg);
		synchronized (out) {
			out.write(msg.getBytes());
			out.flush();
		}
	}

	void send(int channel, byte[] data) throws IOException {
		synchronized (out) {
			out.write(0x24);
			out.write(channel);
			out.writeChar(data.length);
			out.write(data);
			out.flush();
		}
	}

	// Creates a DESCRIBE response string in SDP format for current media
	private String describe() {
		// Write the body first so we can get the size later
		String content = 
				"v=0" + CRLF +
				"m=video " + 0 + " RTP/AVP " + RTPpacket.MJPEG_TYPE + CRLF +
				"a=control:streamid=" + getSessionKey() + CRLF +
				"a=rtpmap:26 JPEG/" + MJPEG_TIMEBASE + CRLF +
				"a=mimetype:string;\"video/MJPEG\"" + CRLF;

		String header = 
				"Content-Base: " +RTPServer.contentBase(socketRTSP.getInetAddress(), localRTSPport)+ CRLF +
				"Content-Type: " + "application/sdp" + CRLF +
				"Content-Length: " + content.length() + CRLF + CRLF;

		return header + content;
	}
	
	private String reqTypes() {
		String result = "";
		for(REQType t : REQType.values()) {
			if(t == REQType.OPTIONS) continue;
			result += t.toString() + ", ";
		}
		return result.substring(0, result.length() - 2);
	}

	//------------------------------------
	//RTSP Response
	//------------------------------------

	private String RTSP_options() {
		return 
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+cSeq+CRLF +
				"Public: "+reqTypes()+CRLF+CRLF;
	}

	private String RTSP_describe() {
		return 
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+cSeq+CRLF +
				describe();
	}

	public String get(String key) {
		return request.get(key);
	}

	@Override
	public String toString() {
		return "-------------------------------------------\n#" + label + ": ";
	}

	public int getSessionKey() {
		if(sessionKey < 0)
			sessionKey = (int) (Math.random() * 1000000);
		return sessionKey;
	}

	public int getCSeq() {
		return cSeq;
	}

	public InetAddress getClientIP() {
		return clientIP;
	}
}
