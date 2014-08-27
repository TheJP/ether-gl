/*
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich (Stefan Muller Arisona & Simon Schubiger)
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona & Simon Schubiger
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

package ch.fhnw.ether.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.fhnw.ether.geom.BoundingBox;
import ch.fhnw.ether.scene.IScene;

/**
 * Created by radar on 05/12/13.
 */
public abstract class AbstractModel implements IModel {
	protected final IScene scene;
	private BoundingBox bounds;
	private final List<IGeometry> geometries = new ArrayList<>();

	protected AbstractModel(IScene scene) {
		this.scene = scene;
	}

	@Override
	public IScene getScene() {
		return scene;
	}
	
	@Override
	public BoundingBox getBounds() {
		if (bounds == null) {
			bounds = new BoundingBox();
			for (IGeometry geometry : geometries)
				bounds.add(geometry.getBounds());
		}
		return bounds;
	}

	protected void invalidateBounds() {
		bounds = null;
	}

	@Override
	public List<IGeometry> getGeometries() {
		return Collections.unmodifiableList(geometries);
	}

	protected void addGeometry(IGeometry geometry) {
		geometries.add(geometry);
	}

	protected void removeGeometry(IGeometry geometry) {
		geometries.remove(geometry);
	}
}