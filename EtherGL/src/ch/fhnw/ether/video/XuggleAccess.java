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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;

import ch.fhnw.ether.audio.AudioUtilities;
import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.image.RGB8Frame;
import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.IScheduler;
import ch.fhnw.ether.media.ITimebase;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.util.BufferUtilities;
import ch.fhnw.util.Log;
import ch.fhnw.util.SortedLongMap;

public final class XuggleAccess extends FrameAccess implements Runnable {
	private static final Log log = Log.create();

	private static final int QUEUE_SZ = 8;

	private final IContainer                      container;
	private       IStreamCoder                    videoCoder;
	private       IStream                         videoStream;
	private       IStreamCoder                    audioCoder;
	private       IStream                         audioStream;
	private       IVideoResampler                 resampler;
	private       AudioFormat                     audioFormat;
	private       AtomicReference<IVideoPicture>  currentPicture = new AtomicReference<>();
	private       double                          playOutTime    = ITimebase.ASAP;
	private       boolean                         isKeyframe;
	private       long                            lastTimeStamp;
	private       long                            maxTimeStamp;
	private       BlockingQueue<float[]>          audioData      = new LinkedBlockingQueue<>();
	private       double                          baseTime;
	private       Thread                          decoderThread;
	private       SortedLongMap<IVideoPicture>    pictureQueue   = new SortedLongMap<>();
	private       Semaphore                       pictures       = new Semaphore(0);
	private       Semaphore                       queueSize      = new Semaphore(QUEUE_SZ);

	public XuggleAccess(URLVideoSource src, int numPlays) throws IOException {
		super(src, numPlays);
		container = IContainer.make();
		open(src);
	}

	@SuppressWarnings("deprecation")
	private void open(URLVideoSource src) throws IOException {
		if (container.open(src.getURL().toExternalForm(), IContainer.Type.READ, null) < 0)
			throw new IOException("could not open " + src);

		// query how many streams the call to open found
		int numStreams = container.getNumStreams();
		// and iterate through the streams to find the first audio stream
		int videoStreamId = -1;
		int audioStreamId = -1;
		for(int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoStream   = stream;
				videoCoder    = coder;
			}
			else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				audioStreamId = i;
				audioStream   = stream;
				audioCoder    = coder;
				audioFormat   = new AudioFormat(
						audioCoder.getSampleRate(),
						(int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
						audioCoder.getChannels(),
						true, /* xuggler defaults to signed 16 bit samples */
						false);
			}
		}
		if (videoStreamId == -1 && audioStreamId == -1)
			throw new IOException("could not find audio or video stream in container in " + src);

		/*
		 * Check if we have a video stream in this file.  If so let's open up our decoder so it can
		 * do work.
		 */
		if (videoCoder != null) {
			if(videoCoder.open() < 0)
				throw new IOException("could not open audio decoder for container " + src);

			if (videoCoder.getPixelType() != IPixelFormat.Type.RGB24) {
				resampler = IVideoResampler.make(
						videoCoder.getWidth(), videoCoder.getHeight(), 
						IPixelFormat.Type.RGB24,
						videoCoder.getWidth(), videoCoder.getHeight(), 
						videoCoder.getPixelType());
				if (resampler == null)
					throw new IOException("could not create color space resampler for " + src);
			}
		}

		if (audioCoder != null) {
			if (audioCoder.open() < 0)
				throw new IOException("could not open audio decoder for container: " + src);
		}
		decoderThread = new Thread(this, src.getURL().toString());
		decoderThread.setPriority(Thread.MIN_PRIORITY);
		decoderThread.setDaemon(true);
		decoderThread.start();
	}

	public void dispose() {
		container.close();
	}

	@Override
	public double getDuration() {
		IRational timeBase = videoStream.getTimeBase();
		long      duration = videoStream.getDuration();
		return duration == Global.NO_PTS ? AbstractFrameSource.LENGTH_UNKNOWN : (duration * timeBase.getNumerator()) / (double)timeBase.getDenominator();
	}

	@Override
	public float getFrameRate() {
		if(getDuration() == AbstractFrameSource.LENGTH_UNKNOWN) {
			IRational rate     = videoStream.getFrameRate();
			IRational timeBase = videoStream.getTimeBase();
			rate = rate.multiply(timeBase);
			return (float)rate.getNumerator() / (float)rate.getDenominator();
		}
		return (float) (getFrameCount() / getDuration());
	}

	@Override
	public long getFrameCount() {
		return videoStream.getNumFrames();
	}

	@Override
	public int getWidth() {
		return videoCoder.getWidth();
	}

	@Override
	public int getHeight() {
		return videoCoder.getHeight();
	}

	@Override
	public String toString() {
		return src.getURL() + " (d=" + getDuration() + " fr=" + getFrameRate() + " fc=" + getFrameCount() + " w=" + getWidth() + " h=" + getHeight() + ")";
	}

	public void rewind() throws IOException {
		double tmp  = playOutTime;
		numPlays--;
		open(getSource());
		playOutTime   = 0;
		lastTimeStamp = 0;
		maxTimeStamp  = 0;
		baseTime      = tmp;
	}

	@Override
	public void run() {
		try {
			final IPacket currentPacket = IPacket.make();
			while(container.readNextPacket(currentPacket) >= 0) {
				if (currentPacket.getStreamIndex() == videoStream.getIndex()) {
					IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
					int bytesDecoded = videoCoder.decodeVideo(picture, currentPacket, 0);
					if (bytesDecoded < 0)
						break;
					if (picture.isComplete()) {
						// terrible hack for fixing up screwed timestamps
						maxTimeStamp = Math.max(maxTimeStamp, picture.getTimeStamp());
						long correction = Math.min((maxTimeStamp - lastTimeStamp) / 2, (long)(IScheduler.SEC2US / getFrameRate()));
						picture.setTimeStamp(lastTimeStamp + correction);
						lastTimeStamp = picture.getTimeStamp();
						queueSize.acquire();
						synchronized (pictureQueue) {
							if(pictureQueue.put(picture.getTimeStamp(), picture) == null)
								pictures.release();
							else
								queueSize.release();
						}
					}
				} else if (currentPacket.getStreamIndex() == audioStream.getIndex()) {
					IAudioSamples samples = IAudioSamples.make(2048, audioCoder.getChannels());
					int offset = 0;
					while(offset < currentPacket.getSize()) {
						int bytesDecoded = audioCoder.decodeAudio(samples, currentPacket, offset);
						if (bytesDecoded < 0) {
							log.warning("got error decoding audio");
							break;
						}
						offset += bytesDecoded;
						if (samples.isComplete())
							audioData.add(AudioUtilities.pcmBytes2float(audioFormat, samples.getData().getByteArray(0, samples.getSize()), samples.getSize()));
					}
				}
			}
		} catch (Throwable t) {
			log.severe(t);
		} finally {
			container.close();
		}
	}

	@Override
	public boolean decodeFrame() {
		try {
			IVideoPicture picture = null;
			if(!(pictures.tryAcquire(1000, TimeUnit.MILLISECONDS))) {
				if(decoderThread.isAlive())
					return false;
				rewind();
				decodeFrame();
				return numPlays <= 0;
			}
			synchronized (pictureQueue) {
				picture = pictureQueue.firstValue();
				pictureQueue.remove(picture.getTimeStamp());
				queueSize.release();
			}
			playOutTime = baseTime + (picture.getTimeStamp() / IScheduler.SEC2US);
			isKeyframe  = picture.isKeyFrame();
			this.currentPicture.set(picture);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	@Override
	protected boolean skipFrame() {
		return decodeFrame();
	}

	IVideoPicture tmpPicture;
	@Override
	protected Frame getFrame(BlockingQueue<float[]> audioData) {
		Frame result = null;
		try {
			final int w = getWidth();
			final int h = getHeight();
			IVideoPicture newPic = currentPicture.get();
			if (resampler != null) {
				if(tmpPicture == null)
					tmpPicture = IVideoPicture.make(resampler.getOutputPixelFormat(), w, h); 
				newPic = tmpPicture;
				if (resampler.resample(newPic, currentPicture.get()) < 0) {
					log.warning("could not resample video");
					return null;
				}
			}
			if (newPic.getPixelType() != IPixelFormat.Type.RGB24) {
				log.warning("could not decode video as RGB24 bit data");
				return null;
			}
			ByteBuffer dstBuffer = BufferUtilities.createDirectByteBuffer(w * h * 3);
			flip(newPic.getByteBuffer(), dstBuffer, w, h);
			result = new RGB8Frame(getWidth(), getHeight(), dstBuffer);

			if(!(this.audioData.isEmpty())) {
				while(audioData.size() > (2  * this.audioData.size()) + 128)
					audioData.take();

				while(!(this.audioData.isEmpty())) 
					audioData.add(this.audioData.take());
			}
		} catch(Throwable t) {
			log.warning(t);
		}
		return result;
	}

	private void flip(ByteBuffer src, ByteBuffer dst, int width, int height) {
		dst.clear();
		final int rowLength = width * 3;
		for(int y = height; --y >= 0;) {
			src.position(y * rowLength);
			src.limit(y * rowLength + rowLength);
			dst.put(src);
		}
	}

	@Override
	public double getPlayOutTimeInSec() {
		return playOutTime;
	}

	@Override
	public boolean isKeyframe() {
		return isKeyframe;
	}

	@Override
	public Texture getTexture(BlockingQueue<float[]> audioData) {
		return getFrame(audioData).getTexture();
	}

	@Override
	protected int getNumChannels() {
		return audioCoder == null ? 2 : audioCoder.getChannels();
	}

	@Override
	protected float getSampleRate() {
		return audioCoder == null ? 48000 : audioCoder.getSampleRate();
	}
}
