package ch.fhnw.ether.video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.image.ImageScaler;
import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.video.fx.AbstractVideoFX;

public class PreviewTarget extends AbstractVideoTarget {
	private static final int BORDER = 2;

	private       BufferedImage preview;
	private       Frame         previewf;
	private       Graphics2D    g;
	private       double        length;
	private       double        start;
	private       boolean       init = true;
	private       double        next;
	private       int           prvN;
	private       int           prvHeight;
	private       int           prvWidth;
	private       int           x;

	public PreviewTarget(int width, int height) {
		this(width, height, 0, AbstractFrameSource.LENGTH_UNKNOWN);
	}

	public PreviewTarget(int width, int height, double startInSeconds, double lengthInSeconds) {
		super(Thread.MIN_PRIORITY, AbstractVideoFX.FRAMEFX, false);
		preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g       = (Graphics2D) preview.getGraphics();
		g.setBackground(new Color(0, true));
		g.clearRect(0, 0, width, height);
		length  = lengthInSeconds;
		start   = startInSeconds;
	}

	@Override
	public void render() throws RenderCommandException {
		Frame frame = null;
		if(length == AbstractFrameSource.LENGTH_UNKNOWN) 
			length = getVideoSource().getLengthInSeconds();
		if(init) {
			init      = false;
			frame     = getFrame().getFrame();
			prvHeight = preview.getHeight();
			prvWidth  = (prvHeight * frame.width) / frame.height; 
			prvN      = Math.max(preview.getWidth() / (prvWidth + BORDER), 1); 
			next      = start + (length / prvN);
		} else if(getTime() >= next) {
			frame     = getFrame().getFrame();
			next     += (length / prvN);
		} else
			getFrame().skip();

		if(frame != null) {
			g.drawImage(ImageScaler.getScaledInstance(frame.toBufferedImage(), prvWidth, prvHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false), x, 0, ImageScaler.AWT_OBSERVER);
			x += prvWidth + BORDER;
		}
		if(getTime() >= start + length)
			stop();
	}

	public Frame getPreview() {
		if(previewf == null) {
			g.dispose();
			previewf = Frame.create(preview);
			g       = null;
			preview = null;
		}
		return previewf;
	}
	
	@Override
	public double getTime() {
		if(timebase != null) return timebase.getTime();
		if(isRealTime())     return super.getTime();
		AbstractFrameSource src = program.getFrameSource();
		return start + (getTotalElapsedFrames() / src.getFrameRate());
	}

}
