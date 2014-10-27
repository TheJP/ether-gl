/*
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich
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

package ch.fhnw.ether.scene.mesh.material;

import java.util.function.Supplier;

import ch.fhnw.ether.render.gl.Texture;
import ch.fhnw.ether.scene.mesh.IAttribute.ISuppliers;
import ch.fhnw.util.color.RGBA;

public class ColorMapMaterial extends ColorMaterial {
	private Texture texture;
	
	public ColorMapMaterial(Texture texture) {
		this(texture, RGBA.WHITE);
	}

	public ColorMapMaterial(Texture texture, RGBA color) {
		this(texture, RGBA.WHITE, false);
	}

	public ColorMapMaterial(Texture texture, RGBA color, boolean perVertexColor) {
		super(color, perVertexColor);
		this.texture = texture;
	}

	@Override
	public void getAttributeSuppliers(ISuppliers dst) {
		//dst.provide(IMaterial.COLOR_MAP, () -> texture);
		dst.provide(IMaterial.COLOR_MAP, new Supplier<Texture>() {
			@Override
			public Texture get() {
				return texture;
			}
		});
		dst.require(IMaterial.COLOR_MAP_ARRAY);
		super.getAttributeSuppliers(dst);
	}
}