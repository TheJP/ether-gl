package ch.fhnw.ether.video;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.IScheduler;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.util.IDisposable;

public class FrameAccess implements IDisposable {
	protected final URLVideoSource src;
	protected int                  numPlays;
	private         Frame          frame;

	FrameAccess(URLVideoSource src) throws IOException {
		this.frame    = Frame.create(src.getURL());
		this.src      = src;
		this.numPlays = 0;
	}

	FrameAccess(Frame frame) {
		this.frame    = frame;
		this.src      = null;
		this.numPlays = 0;
	}

	protected FrameAccess(URLVideoSource src, int numPlays) {
		this.frame    = null;
		this.src      = src;
		this.numPlays = numPlays;
	}

	protected Frame getFrame(BlockingQueue<float[]> audioData) {
		return frame;
	}
	
	public Texture getTexture(BlockingQueue<float[]> audioData) {
		return frame.getTexture();
	}

	protected int getWidth() {
		return frame.width;
	}
	protected int getHeight() {
		return frame.height;
	}
	
	protected float getFrameRate() {
		return AbstractFrameSource.FRAMERATE_UNKNOWN;
	}

	protected float getSampleRate() {
		return 0;
	}
	
	protected int getNumChannels() {
		return 0;
	}
	
	protected long getFrameCount() {
		return 1;
	}

	protected double getDuration() {
		return AbstractFrameSource.LENGTH_INFINITE;
	}

	public double getPlayOutTimeInSec() {
		return IScheduler.ASAP;
	}
	
	public URLVideoSource getSource() {
		return src;
	}

	public boolean decodeFrame() {return true;}

	public boolean isKeyframe() {
		return true;
	}

	@Override
	public void dispose() {
		frame = null; // help GC
	}
}
