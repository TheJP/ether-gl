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

package ch.fhnw.ether.scene.mesh;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.EnumSet;

import javax.media.opengl.GL;

import ch.fhnw.ether.render.IRenderable;
import ch.fhnw.ether.render.IRenderer;
import ch.fhnw.ether.render.IRenderer.Pass;
import ch.fhnw.ether.render.attribute.IArrayAttribute;
import ch.fhnw.ether.render.attribute.IAttribute.PrimitiveType;
import ch.fhnw.ether.render.attribute.builtin.PositionArray;
import ch.fhnw.ether.render.attribute.builtin.TexCoordArray;
import ch.fhnw.ether.render.gl.Texture;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.builtin.MaterialShader;
import ch.fhnw.ether.render.shader.builtin.MaterialShader.ShaderInput;
import ch.fhnw.ether.scene.mesh.geometry.VertexGeometry;
import ch.fhnw.ether.scene.mesh.material.TextureMaterial;
import ch.fhnw.util.math.geometry.Primitives;

public class TextMesh extends GenericMesh {
	public static final Font FONT = new Font("SansSerif", Font.BOLD, 12);

	private static final Color CLEAR_COLOR = new Color(0, 0, 0, 0);

	private final BufferedImage image;
	private final Graphics2D graphics;
	private final Texture texture = new Texture();
	private int x;
	private int y;
	private int w;
	private int h;
	private IRenderable renderable = null;
	private final EnumSet<IRenderer.Flag> interactiveOnlyFlag;

	public TextMesh(int x, int y, int w, int h, boolean interactiveOnly) {
		super(PrimitiveType.TRIANGLE);
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		graphics = image.createGraphics();
		graphics.setFont(FONT);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

		float[] position = new float[] { x, y, 0, x + w, y, 0, x + w, y + h, 0, x, y, 0, x + w, y + h, 0, x, y + h, 0 };
		float[] tex_coords = Primitives.DEFAULT_QUAD_TEX_COORDS;
		IArrayAttribute[] attribs = new IArrayAttribute[] { new PositionArray(), new TexCoordArray() };
		setGeometry(new VertexGeometry(new float[][] { position, tex_coords }, attribs, PrimitiveType.TRIANGLE));
		setMaterial(new TextureMaterial(texture));
		interactiveOnlyFlag = interactiveOnly ? EnumSet.of(IRenderer.Flag.INTERACTIVE_VIEW_ONLY) : EnumSet.noneOf(IRenderer.Flag.class);
	}

	public final Texture getTexture() {
		return texture;
	}

	public final int getX() {
		return x;
	}

	public final int getY() {
		return y;
	}

	public final int getWidth() {
		return w;
	}

	public final int getHeight() {
		return h;
	}

	public IRenderable getRenderable(IRenderer renderer) {
		if (renderable == null) {
			IShader s = new MaterialShader(EnumSet.of(ShaderInput.TEXTURE));
			renderable = renderer.createRenderable(Pass.SCREEN_SPACE_OVERLAY, interactiveOnlyFlag, s, getMaterial(), Collections.singletonList(getGeometry()));
		}
		return renderable;
	}

	public final void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		requestUpdate();
	}

	public void clear() {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		fillRect(CLEAR_COLOR, x, y, w, h);
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}

	public void fillRect(Color color, int x, int y, int w, int h) {
		graphics.setColor(color);
		graphics.fillRect(x, y, w, h);
		requestUpdate();
	}

	public void drawString(String string, int x, int y) {
		drawString(Color.WHITE, string, x, y);
		requestUpdate();
	}

	public void drawString(Color color, String string, int x, int y) {
		graphics.setColor(color);
		graphics.drawString(string, x, y);
		requestUpdate();
	}

	public void drawStrings(String[] strings, int x, int y) {
		drawStrings(Color.WHITE, strings, x, y);
	}

	public void drawStrings(Color color, String[] strings, int x, int y) {
		graphics.setColor(color);
		int dy = 0;
		for (String s : strings) {
			graphics.drawString(s, x, y + dy);
			dy += FONT.getSize();
		}
		requestUpdate();
	}

	private void requestUpdate() {
		texture.setData(w, h, IntBuffer.wrap(((DataBufferInt) image.getRaster().getDataBuffer()).getData()), GL.GL_BGRA);
		if (renderable != null)
			renderable.requestUpdate();
	}
}