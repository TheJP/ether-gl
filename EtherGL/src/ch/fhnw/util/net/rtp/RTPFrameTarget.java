package ch.fhnw.util.net.rtp;

import ch.fhnw.ether.video.AbstractVideoTarget;
import ch.fhnw.ether.video.VideoFrame;
import ch.fhnw.ether.video.fx.AbstractVideoFX;

public class RTPFrameTarget extends AbstractVideoTarget {
	private final RTPServer server;
	
	public RTPFrameTarget(int port) throws Exception {
		super(Thread.MIN_PRIORITY, AbstractVideoFX.FRAMEFX, true);
		server = new RTPServer(port);
	}

	@Override
	public void render() {
		VideoFrame frame = getFrame();
		server.setFrame(frame.getFrame());
		sleepUntil(frame.playOutTime);		
	}	
}
