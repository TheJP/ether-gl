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

import java.util.concurrent.BlockingQueue;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.AbstractFrame;
import ch.fhnw.ether.scene.mesh.material.Texture;

public class VideoFrame extends AbstractFrame {
	private final FrameAccess            framea;
	private       Frame                  frame;
	private       Texture                texture;
	private final BlockingQueue<float[]> audioData;

	public VideoFrame(Frame frame) {
		this(new FrameAccess(frame), null);
	}

	public VideoFrame(FrameAccess framea) {
		this(framea, null);
	}
	
	public VideoFrame(FrameAccess framea, BlockingQueue<float[]> audioData) {
		super(framea.getPlayOutTimeInSec());
		this.framea    = framea;
		this.audioData = audioData;
	}

	public synchronized Frame getFrame() {
		if(frame == null) {
			if(texture != null) {
				frame = Frame.create(texture);
			} else {
				frame = framea.getFrame(audioData);
			}
		}
		return frame;
	}

	public synchronized Texture getTexture() {
		if(texture == null) {
			if(frame != null) {
				setTexture(frame.getTexture());
			} else {
				setTexture(framea.getTexture(audioData));
			}
		}
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public boolean isKeyframe() {
		return framea.isKeyframe();
	}
}
