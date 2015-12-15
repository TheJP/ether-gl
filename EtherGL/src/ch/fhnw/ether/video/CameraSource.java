/*
 * Copyright (c) 2013 - 2015 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2015 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.ether.video;

import java.awt.Dimension;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.Webcam;

import ch.fhnw.ether.image.RGB8Frame;
import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.IRenderTarget;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.util.ClassUtilities;
import ch.fhnw.util.IDisposable;

public class CameraSource extends AbstractFrameSource implements IVideoSource, IDisposable {
	private static final AtomicBoolean kill = new AtomicBoolean();

	private static final Method getFPS = ClassUtilities.getMethod(Webcam.class, "getFPS");

	private final Webcam     cam;
	private AtomicBoolean    disposed = new AtomicBoolean(false);
	private final CameraInfo info;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			try {
				Thread.sleep(1000);
				kill.set(true);
			} catch (InterruptedException e) {}
		}));
		new Thread("Camera watchdog") {
			@Override
			public void run() {
				try {
					while(!(kill.get()))
						Thread.sleep(1000);
					Runtime.getRuntime().halt(0);
				} catch (InterruptedException e) {}
			};
		}.start();
	}

	private CameraSource(CameraInfo info) {
		this.info     = info;
		this.cam      = info.getNativeCamera();
		this.cam.open(true);
		Dimension max = cam.getViewSize();
		for(Dimension dim : this.cam.getViewSizes())
			if(dim.width > max.width && dim.height > max.height)
				max = dim;
		setSize(max.width, max.height);
	}

	@Override
	public void dispose() {
		if(!(disposed.getAndSet(true)))
			cam.close();
	}

	@Override
	protected void run(IRenderTarget<?> target) throws RenderCommandException {
		if(!(cam.isOpen())) return;
		Dimension size       = cam.getViewSize();
		RGB8Frame frame      = new RGB8Frame(size.width, size.height);
		final ByteBuffer src = cam.getImageBytes();
		final ByteBuffer dst = frame.pixels;
		src.clear();
		dst.clear();
		final int rowLength = frame.width * frame.pixelSize;
		for(int y = frame.height; --y >= 0;) {
			src.position(y * rowLength);
			src.limit(y * rowLength + rowLength);
			dst.put(src);
		}
		try {
			((IVideoRenderTarget)target).setFrame(this, new VideoFrame(frame));
		} catch(Throwable t) {
			throw new RenderCommandException(t);
		}
	}

	public void setSize(int width, int height) {
		cam.close();
		cam.setViewSize(new Dimension(width, height));
		cam.open(true);
	}

	@Override
	public float getFrameRate() {
		try {
			return Math.max(10.0f, ((Double)getFPS.invoke(cam)).floatValue());
		} catch(Throwable t) {
			return FRAMERATE_UNKNOWN;
		}
	}

	@Override
	public long getLengthInFrames() {
		return FRAMECOUNT_UNKNOWN;
	}

	@Override
	public double getLengthInSeconds() {
		return LENGTH_INFINITE;
	}

	@Override
	public int getWidth() {
		return cam.getViewSize().width;
	}

	@Override
	public int getHeight() {
		return cam.getViewSize().height;
	}

	//--- utilities

	@Override
	public String toString() {
		return info.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public static CameraSource create(CameraInfo cameraInfo) {
		return new CameraSource(cameraInfo);
	}
}
