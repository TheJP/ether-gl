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

import ch.fhnw.ether.scene.attribute.AbstractAttribute;
import ch.fhnw.ether.scene.attribute.IAttributeProvider;
import ch.fhnw.util.IUpdateRequester;

public interface IMaterial extends IAttributeProvider, IUpdateRequester {
	final class MaterialAttribute extends AbstractAttribute {
		public MaterialAttribute(String id) {
			super(id);
		}
	}

	// position array (note that this attribute is mandatory)
	MaterialAttribute POSITION_ARRAY = new MaterialAttribute("builtin.material.position_array");

	// non-shaded objects
	MaterialAttribute COLOR_ARRAY = new MaterialAttribute("builtin.material.color_array");
	MaterialAttribute COLOR = new MaterialAttribute("builtin.material.color");

	// texture
	MaterialAttribute COLOR_MAP_ARRAY = new MaterialAttribute("builtin.material.color_map_array");
	MaterialAttribute COLOR_MAP = new MaterialAttribute("builtin.material.color_map");

	// triangles only: normals & shading
	MaterialAttribute NORMAL_ARRAY = new MaterialAttribute("builtin.material.normal_array");
	MaterialAttribute EMISSION = new MaterialAttribute("builtin.material.shading.emission");
	MaterialAttribute AMBIENT = new MaterialAttribute("builtin.material.shading.ambient");
	MaterialAttribute DIFFUSE = new MaterialAttribute("builtin.material.shading.diffuse");
	MaterialAttribute SPECULAR = new MaterialAttribute("builtin.material.shading.specular");
	MaterialAttribute SHININESS = new MaterialAttribute("builtin.material.shading.shininess");
	MaterialAttribute STRENGTH = new MaterialAttribute("builtin.material.shading.strength");

	// lines only: line width
	MaterialAttribute LINE_WIDTH_ARRAY = new MaterialAttribute("builtin.material.line_width_array");
	MaterialAttribute LINE_WIDTH = new MaterialAttribute("builtin.material.line_width");

	// points only: point size
	MaterialAttribute POINT_SIZE_ARRAY = new MaterialAttribute("builtin.material.point_size_array");
	MaterialAttribute POINT_SIZE = new MaterialAttribute("builtin.material.point_size");
}
