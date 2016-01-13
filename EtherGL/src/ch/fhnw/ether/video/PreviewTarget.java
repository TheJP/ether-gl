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

	private BufferedImage preview;
	private Frame         previewf;
	private Graphics2D    g;
	private boolean       init = true;
	private int           prvN;
	private int           prvHeight;
	private int           prvWidth;
	private int           x;
	private double[]      startEnd;
	private double        prvSkip;
	private int           idx;
	
	public PreviewTarget(int width, int height) {
		this(width, height, 0, AbstractFrameSource.LENGTH_UNKNOWN);
	}

	public PreviewTarget(int width, int height, double...durations) {
		this(startEnd(durations), width, height);
	}

	private static double[] startEnd(double[] durations) {
		double[] result = new double[durations.length*2];
		double   start  = 0;
		int      idx    = 0;
		for(double d : durations) {
			result[idx++] = start;
			start += d;
			result[idx++] = start;
		}
		return result;
	}

	public PreviewTarget(int width, int height, double startInSeconds, double lengthInSeconds) {
		this(new double[] {startInSeconds, startInSeconds+lengthInSeconds}, width, height);
	}

	private PreviewTarget(double[] startEnd, int width, int height) {
		super(Thread.MIN_PRIORITY, AbstractVideoFX.FRAMEFX, false);
		this.startEnd = startEnd;
		this.preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.g       = (Graphics2D) preview.getGraphics();
		this.g.setBackground(new Color(0, true));
		this.g.clearRect(0, 0, width, height);
	}
	
	@Override
	public void render() throws RenderCommandException {
		VideoFrame vframe = getFrame();
		Frame      frame  = null;
		
		if(startEnd[1] == AbstractFrameSource.LENGTH_UNKNOWN) 
			startEnd[1] = getVideoSource().getLengthInSeconds();
		if(init) {
			init      = false;
			frame     = vframe.getFrame();
			prvHeight = preview.getHeight();
			prvWidth  = (prvHeight * frame.width) / frame.height; 
			prvN      = Math.max(preview.getWidth() / (prvWidth + BORDER), 1);
			prvSkip   = (startEnd[1]-startEnd[0]) / prvN;
		}
		
		while(vframe.playOutTime < startEnd[idx])
			return;
		
		frame = vframe.getFrame();
		
		if(frame != null) {
			g.drawImage(ImageScaler.getScaledInstance(frame.toBufferedImage(), prvWidth, prvHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false), x, 0, ImageScaler.AWT_OBSERVER);
			x += prvWidth + BORDER;
		}
		
		if(vframe.playOutTime >= startEnd[idx+1]) {
			previewf = null;
			processPreview();
			idx += 2;
			if(idx >= startEnd.length)
				stop();
			else {
				prvSkip = (startEnd[idx+1]-startEnd[idx+0]) / prvN;
				g.clearRect(0, 0, preview.getWidth(), preview.getHeight());
				x = 0;
			}
		} else
			startEnd[idx] += prvSkip;
	}

	@SuppressWarnings("unused")
	protected void processPreview() throws RenderCommandException {}
	
	public Frame getPreview() {
		if(previewf == null)
			previewf = Frame.create(preview);
		return previewf;
	}	
}
