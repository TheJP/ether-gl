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

package ch.fhnw.ether.examples.video.fx;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.ether.video.fx.IVideoFrameFX;
import ch.fhnw.ether.video.fx.IVideoGLFX;

public class FadeToColor extends AbstractVideoFX implements IVideoFrameFX, IVideoGLFX {
	private static final Parameter FADE  = new Parameter("fade",  "Fade",  0, 1, 1);
	private static final Parameter RED   = new Parameter("red",   "Red",   0, 1, 0);
	private static final Parameter GREEN = new Parameter("green", "Green", 0, 1, 0);
	private static final Parameter BLUE  = new Parameter("blue",  "Blue",  0, 1, 0);

	public FadeToColor() {
		super(FADE, RED, GREEN, BLUE);
	}

	@Override
	public String mainFrag() {
		return lines(
				"result.r = result.r * fade + (1.-fade) * red;",
				"result.g = result.g * fade + (1.-fade) * green;",
				"result.b = result.b * fade + (1.-fade) * blue;"
				);
	}
	
	@Override
	public void processFrame(final double playOutTime, final IVideoRenderTarget target, final Frame frame) {
		final float fade  = getVal(FADE);
		final float red  = getVal(RED);
		final float geen = getVal(GREEN);
		final float blue = getVal(BLUE);

		if(frame.pixelSize == 4) {
			frame.processLines((pixels, j)->{
				int idx = pixels.position();
				for(int i = 0; i < frame.width; i++) {
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), red,  fade)));
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), geen, fade)));
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), blue, fade)));
					pixels.get();
					idx++;
				}
			});
		} else {
			frame.processLines((pixels, j)->{
				int idx = pixels.position();
				for(int i = 0; i < frame.width; i++) {
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), red,  fade)));
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), geen, fade)));
					pixels.put(toByte(mix(toFloat(pixels.get(idx++)), blue, fade)));
				}
			});
		}
	}
}
