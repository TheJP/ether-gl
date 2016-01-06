package ch.fhnw.util.net.rtp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Timer;

import ch.fhnw.util.ClassUtilities;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.net.NetworkUtilities;

public class RTPSession implements ActionListener {
	private static final Log log = Log.create();

	private static final String CRLF = "\r\n";

	private static final int FRAMERATE = 25; // fix for now

	//rtsp states
	enum State {
		INIT,
		READY,
		PLAYING,
	}

	//RTP variables:
	//----------------
	private DatagramSocket    socketRTP; //socket to be used to send and receive UDP packets
	private DatagramPacket    senddp; //UDP packet containing the video frames
	private State             state = State.INIT;; //RTSP Server state == INIT or READY or PLAY
	private Timer             timer;    //timer used to send the images at the video frame rate
	private RTCPReceiver      rtcpReceiver;
	private DatagramSocket    socketRTCP;
	private final InetAddress clientIP;   //Client IP address
	private final int         clientRTPport;  //destination port for RTP packets  (given by the RTSP Client) 
	private final int         clientRTPCport; //source port for RTPC packets  (given by the RTSP Client) 
	private final int         sessionKey;
	private final RTPServer   server;
	private final LinkedBlockingQueue<RTCPpacket> rtpcQ = new LinkedBlockingQueue<>(); 

	//Video variables:
	//----------------
	private int               imagenb = 1; //image nb of the image currently transmitted
	private RTSPRequest       lastReq;

	public RTPSession(RTPServer server, RTSPRequest req) throws Exception {
		this.lastReq        = req;
		this.clientIP       = req.getClientIP();
		String transport    = req.get("Transport:");
		this.clientRTPport  = getClientPort(transport, 0);
		this.clientRTPCport = getClientPort(transport, 1);
		this.server         = server;
		this.sessionKey     = req.getSessionKey();
		this.server.addSession(getSessionKey(), this);

		//update RTSP state
		state = State.READY;
		RTPServer.log(this+"New RTSP state: READY");

		if(req.get("Transport:").contains("client_port=")) {
			//init RTP and RTCP sockets
			socketRTP  = new DatagramSocket();
			socketRTCP = new DatagramSocket();
			socketRTP.setTrafficClass(NetworkUtilities.IPTOS_LOWDELAY | NetworkUtilities.IPTOS_THROUGHPUT);

			//Send response
			req.send(RTSP_setup(req, socketRTP.getLocalPort(), socketRTCP.getLocalPort()));
		} else {
			req.send(RTSP_setup(req));
		}

		//init RTP sending Timer
		timer = new Timer(1000/FRAMERATE, this);
		timer.setInitialDelay(0);
		timer.setCoalesce(true);

		//init the RTCP packet receiver
		rtcpReceiver = new RTCPReceiver();
	}

	private int getClientPort(String t, int channel) {
		String udp   = "client_port=";
		String inter = "interleaved=";
		String key   = t.contains(udp) ? udp : inter;
		int start = t.indexOf(key);
		if(start < 0) return -1;
		int end = t.indexOf(start, ';');
		if(end < 0) end = t.length();
		String[] ports = t.substring(start+key.length(), end).split("-");
		return Integer.parseInt(ports[channel]);
	}

	public void play(RTSPRequest req) throws IOException {
		this.lastReq = req;
		if(state == State.READY) {
			//send back response
			req.send(RTSP_response(req));
			//start timer
			timer.start();
			//update state
			state = State.PLAYING;
			RTPServer.log(this + "New RTSP state: PLAYING");
		}
	}

	//------------------------
	//Listener for RTCP packets sent from client
	//------------------------
	class RTCPReceiver extends Thread {
		private byte[]  rtcpBuf = new byte[512];
		private boolean run     = true;

		public RTCPReceiver() {
			super(RTCPReceiver.class.getName());
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while(run) {
				try {
					if(socketRTCP != null) {
						//Construct a DatagramPacket to receive data from the UDP socket
						DatagramPacket dp = new DatagramPacket(rtcpBuf, rtcpBuf.length);
						socketRTCP.receive(dp);   // Blocking
						rtpcQ.put(new RTCPpacket(dp.getData(), dp.getLength()));
					}
					RTPServer.log(this+rtpcQ.take().toString());
				} catch (Throwable t) {
					log.severe(t);
				}
			}
		}

		public void close() {
			run = false;
		}
		
		@Override
		public String toString() {
			return "#" + TextUtilities.getShortClassName(this);
		}
		
	}

	//------------------------
	//Handler for timer
	//------------------------
	@Override
	public void actionPerformed(ActionEvent e) {
		//if the current image nb is less than the length of the video
		//update current imagenb
		imagenb++;

		try {
			//get next frame to send from the video, as well as its size
			BufferedImage buf = server.getImage();

			//Builds an RTPpackets object containing the frame
			RTPmjpg mjpg = new RTPmjpg(buf, imagenb, imagenb*(RTSPRequest.MJPEG_TIMEBASE/FRAMERATE));
			if(socketRTP == null) mjpg.setMTU(63 * 1024);
			List<RTPpacket> rtp_packets = mjpg.createPackets();
			for(RTPpacket rtp_packet : rtp_packets) {
				if(socketRTP != null) {
					//send the packet as a DatagramPacket over the UDP socket 
					senddp = new DatagramPacket(rtp_packet.getPacket(), rtp_packet.size(), clientIP, clientRTPport);
					socketRTP.send(senddp);
				} else {
					lastReq.send(clientRTPport, rtp_packet.getPacket());
				}
			}
			//RTPServer.log(this + "Sent frame #" + imagenb + " as "+rtp_packets.size()+" packets to " + (socketRTP == null ? " channel " : clientIP + ":") + clientRTPport); 
		}
		catch(Throwable t) {
			log.severe(t);
			timer.stop();
			rtcpReceiver.close();
		}
	}

	public void pause(RTSPRequest req) throws IOException {
		this.lastReq = req;
		if(state == State.PLAYING) {
			//send back response
			req.send(RTSP_response(req));
			//stop timer
			timer.stop();
			//update state
			state = State.READY;
			RTPServer.log(this + "New RTSP state: READY");
		}
	}

	public void teardown(RTSPRequest req) throws IOException {
		this.lastReq = req;
		//send back response
		req.send(RTSP_response(req));
		//stop timer
		timer.stop();
		rtcpReceiver.close();
		//close sockets
		if(socketRTP != null)
			socketRTP.close();
		state = State.READY;
		return;
	}

	private String RTSP_response(RTSPRequest req) {
		return
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+req.getCSeq()+CRLF +
				"Session: "+ getSessionKey() +CRLF+CRLF;
	}

	private String RTSP_setup(RTSPRequest req) {
		return
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+req.getCSeq()+CRLF +
				"Transport: " + req.get("Transport:") + CRLF +
				"Session: "+ getSessionKey() +CRLF+CRLF;
	}

	private String RTSP_setup(RTSPRequest req, int rtpPort, int rtcpPort) {
		return
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+req.getCSeq()+CRLF +
				"Transport: " + req.get("Transport:") + ";server_port=" + rtpPort + "-" + rtcpPort + CRLF +
				"Session: "+ getSessionKey() +CRLF+CRLF;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	@Override
	public String toString() {
		return "#Session:" + getSessionKey() + ": ";
	}

	public void recv(int channel, byte[] data) throws InterruptedException {
		if(channel == clientRTPCport)
			rtpcQ.put(new RTCPpacket(data, data.length));
	}
}
