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

import java.io.File;

import javax.imageio.ImageIO;

import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.util.TextUtilities;

public class FileTarget extends AbstractVideoTarget {
	private final String name;
	private final String extU;
	private final String extL;
	private final File   path;
	private       long   count;

	public FileTarget(File file) {
		super(Thread.MIN_PRIORITY, AbstractVideoFX.FRAMEFX, false);
		this.path = file.getParentFile();
		this.name = TextUtilities.getFileNameWithoutExtension(file);
		this.extU = TextUtilities.getFileExtensionWithoutDot(file.getName()).toUpperCase();
		this.extL = TextUtilities.getFileExtensionWithoutDot(file.getName()).toLowerCase();
	}

	@Override
	public void render() throws RenderCommandException {
		if(count == 0) {
			if(getVideoSource().getLengthInFrames() < 0)
				count = 100000;
			else
				count = (long) Math.pow(10.0, Math.ceil(Math.log10(getVideoSource().getLengthInFrames())));
		}
		sleepUntil(getFrame().playOutTime);
		try {
			ImageIO.write(getFrame().getFrame().toBufferedImage(), extU, new File(path, name + "_" + count + "." + extL));
		} catch (Throwable e) {
			throw new RenderCommandException(e);
		}
	}
}
