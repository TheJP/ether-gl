package ch.fhnw.ether.video;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.IRenderTarget;
import ch.fhnw.ether.media.IScheduler;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.RenderProgram;

public class ArrayVideoSource extends AbstractFrameSource implements IVideoSource {
	private final int              width;
	private final int              height;
	private final float            frameRate;
	private final long             lengthInFrames;
	private final double           lengthInSeconds;
	private final List<VideoFrame> frames = new ArrayList<>();
	private int                    frameIdx;

	class Target extends AbstractVideoTarget {
		protected Target() {
			super(Thread.MAX_PRIORITY, null, false);
		}

		@Override
		public void render() throws RenderCommandException {
			VideoFrame frame = getFrame();
			frame.getFrame();
			frames.add(frame);
		}
	}

	public ArrayVideoSource(IVideoSource source) throws RenderCommandException {
		if(source.getLengthInFrames() <= 0)
			throw new RenderCommandException("Source '" + source + "' has an invalid frame count (" + source.getLengthInFrames() +")");
		width           = source.getWidth();
		height          = source.getHeight();
		frameRate       = source.getFrameRate();
		lengthInFrames  = source.getLengthInFrames();
		lengthInSeconds = source.getLengthInSeconds();

		Target t = new Target();
		t.useProgram(new RenderProgram<>(source));
		t.start();
		t.sleepUntil(IScheduler.NOT_RENDERING);
		t.stop();
		frames.get(frames.size()-1).setLast(false);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public long getLengthInFrames() {
		return lengthInFrames;
	}

	@Override
	public double getLengthInSeconds() {
		return lengthInSeconds;
	}

	@Override
	public float getFrameRate() {
		return frameRate;
	}

	@Override
	protected void run(IRenderTarget<?> target) throws RenderCommandException {
		if(frameIdx >= frames.size()) frameIdx = 0;
		if(frameIdx == 0) {
			double now = target.getTime();
			int idx = 0;
			for(VideoFrame f : frames) {
				f.playOutTime = now + idx / getFrameRate();
				idx++;
			}
		}
		VideoFrame frame = frames.get(frameIdx);
		((IVideoRenderTarget)target).setFrame(this, frame);
		frameIdx++;
	}
}
