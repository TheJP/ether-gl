package ch.fhnw.util.net.rtp;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.image.ImageScaler;
import ch.fhnw.util.Log;

public class RTPServer extends Thread {
	private static final Log log = Log.create();
	
	private final Map<Integer, RTPSession> sessions = new ConcurrentHashMap<>();
		
	private final AtomicReference<BufferedImage> currentImage = new AtomicReference<>(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
	
	private final int port;

	public RTPServer(int port) {
		this(port, true);
	}
	
	public RTPServer(int port, boolean start) {
		this.port = port;
		setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		start();
	}
	
	@Override
	public void run() {
		try(ServerSocket listenSocket = new ServerSocket(port)) {
			System.out.println("# RTSPServer running " +contentBase(InetAddress.getLocalHost(), port));
			for(;;)
				new RTSPRequest(this, listenSocket.accept());
		} catch (Exception e) {
			log.severe(e);
		}
	}
	
	public RTPSession getSession(RTSPRequest req) {
		return sessions.get(Integer.valueOf(req.getSessionKey()));
	}

	static String contentBase(InetAddress addr, int port) {
		return "rtsp://" + addr.getHostName() + ":" + port + "/video.mjpg";
	}

	public void addSession(int sessionKey, RTPSession session) {
		sessions.put(sessionKey, session);
	}
	
	static void log(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String args[]) {
		new RTPServer(Integer.parseInt(args[0])).run();;
	}

	public void setFrame(Frame frame) {
		currentImage.set(ImageScaler.getScaledInstance(frame.toBufferedImage(), frame.width, frame.height, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false));
	}

	public BufferedImage getImage() {
		return currentImage.get();
	}
}