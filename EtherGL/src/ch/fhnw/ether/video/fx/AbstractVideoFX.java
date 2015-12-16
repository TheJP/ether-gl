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

package ch.fhnw.ether.video.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.render.Renderable;
import ch.fhnw.ether.render.gl.FrameBuffer;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.IShaderUniform;
import ch.fhnw.ether.render.variable.base.BooleanUniform;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.base.IntUniform;
import ch.fhnw.ether.render.variable.base.Mat3FloatUniform;
import ch.fhnw.ether.render.variable.base.Mat4FloatUniform;
import ch.fhnw.ether.render.variable.base.SamplerUniform;
import ch.fhnw.ether.render.variable.base.Vec3FloatUniform;
import ch.fhnw.ether.render.variable.base.Vec4FloatUniform;
import ch.fhnw.ether.render.variable.builtin.ColorMapArray;
import ch.fhnw.ether.render.variable.builtin.ColorMapUniform;
import ch.fhnw.ether.render.variable.builtin.PositionArray;
import ch.fhnw.ether.scene.attribute.AbstractAttribute;
import ch.fhnw.ether.scene.attribute.IAttribute;
import ch.fhnw.ether.scene.attribute.ITypedAttribute;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.AbstractMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.video.AbstractVideoTarget;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.VideoFrame;
import ch.fhnw.ether.view.gl.GLContextManager;
import ch.fhnw.ether.view.gl.GLContextManager.IGLContext;
import ch.fhnw.util.ClassUtilities;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.UpdateRequest;
import ch.fhnw.util.math.IVec3;
import ch.fhnw.util.math.IVec4;
import ch.fhnw.util.math.Mat3;
import ch.fhnw.util.math.Mat4;

public abstract class AbstractVideoFX extends AbstractRenderCommand<IVideoRenderTarget> {
	private static final Log log = Log.create();
	
	public static final Class<?>     GLFX        = IVideoGLFX.class;
	public static final Class<?>     FRAMEFX     = IVideoFrameFX.class;
	public static final Class<?>[]   FX_CLASSES  = {GLFX, FRAMEFX};
	public static final Uniform<?>[] NO_UNIFORMS = new Uniform<?>[0];
	public static final String[]     NO_INOUT    = ClassUtilities.EMPTY_StringA;

	public static final class Uniform<T> extends AbstractAttribute<T> {
		private T value;

		public Uniform(String id, T value) {
			super(id);
			set(value);
		}

		public String glType() {
			if(value instanceof Integer)
				return "int";
			else if(value instanceof Boolean)
				return "bool";
			else if(value instanceof Frame || value instanceof VideoFrame)
				return "sampler2D";
			return TextUtilities.getShortClassName(value).toLowerCase();
		}

		public IShaderUniform<?> toUniform() {
			return toUniform(-1);
		}
		
		@SuppressWarnings("unchecked")
		public IShaderUniform<?> toUniform(int unit) {
			if(value instanceof Boolean)
				return new BooleanUniform((ITypedAttribute<Boolean>)this, id());
			else if(value instanceof Float)
				return new FloatUniform((ITypedAttribute<Float>)this, id());
			else if(value instanceof Integer)
				return new IntUniform((ITypedAttribute<Integer>)this, id());
			else if(value instanceof Mat3)
				return new Mat3FloatUniform((ITypedAttribute<Mat3>)this, id());
			else if(value instanceof Mat4)
				return new Mat4FloatUniform((ITypedAttribute<Mat4>)this, id());
			else if(value instanceof IVec3)
				return new Vec3FloatUniform((ITypedAttribute<IVec3>)this, id());
			else if(value instanceof IVec4)
				return new Vec4FloatUniform((ITypedAttribute<IVec4>)this, id());
			else if(value instanceof Frame)
				return new SamplerUniform(id(), id(), unit, GL3.GL_TEXTURE_2D);
			else if(value instanceof VideoFrame)
				return new SamplerUniform(id(), id(), unit, GL3.GL_TEXTURE_2D);
			else
				throw new IllegalArgumentException("Unsupported unifrom type:" + value.getClass().getName());
		}

		@SuppressWarnings("unchecked")
		public void set(Object value) {
			this.value = (T) value;
		}

		public Object get() {
			if(value instanceof Frame)
				return ((Frame)value).getTexture();
			else if(value instanceof VideoFrame)
				return ((VideoFrame)value).getTexture();
			return value;
		}
	}

	class FxShader extends AbstractShader {
		private int unit;
		
		public FxShader() {
			super(FxShader.class, AbstractVideoFX.this.getClass().getName(), getVertexCode(), getFragmentCode(), "", Primitive.TRIANGLES);

			addArray(new PositionArray());
			addArray(new ColorMapArray());

			Parameter[] params = getParameters();
			for(int i = 0; i < params.length; i++)
				addUniform(new FloatUniform(params[i].getName(), params[i].getName()));
			for(Uniform<?> u : uniformsvert)
				addUniform(u.toUniform());
			addUniform(new ColorMapUniform("frame", unit++));
			for(Uniform<?> u : uniformsfrag)
				addUniform(u.toUniform(unit++));
		}
	}

	class FxMaterial extends AbstractMaterial implements ICustomMaterial {
		private final IShader     shader;
		private FrameBuffer       fbo;
		private Texture           srcTexture;
		private Texture           dstTexture;
		private Texture           lastDstTexture;

		protected FxMaterial(IAttribute[] attrs) {
			super(attrs, new IGeometryAttribute[] {IGeometry.POSITION_ARRAY, IGeometry.COLOR_MAP_ARRAY});
			shader = new FxShader();
		}

		public void prepare(GL3 gl, AbstractVideoTarget target) throws RenderCommandException {
			this.srcTexture = target.getSrcTexture(gl, AbstractVideoFX.this);
			this.dstTexture = target.getDstTexture(gl, AbstractVideoFX.this);
			if(fbo == null)
				fbo = new FrameBuffer(gl);
			fbo.bind(gl);

			if(dstTexture != lastDstTexture) {
				fbo.attach(gl, GL.GL_COLOR_ATTACHMENT0, dstTexture);
				lastDstTexture = dstTexture;
				int status = fbo.checkStatus(gl);
				if(status != GL3.GL_FRAMEBUFFER_COMPLETE)
					throw new RenderCommandException("createFBO:" + FrameBuffer.toString(status));
			}
		}

		@Override
		public IShader getShader() {
			return shader;
		}

		@Override
		public Object[] getData() {
			Parameter[] params = getParameters();
			Object[] result = new Object[params.length + 1 + uniformsvert.length + uniformsfrag.length];
			int idx = 0;
			for(int i = 0; i < params.length; i++)
				result[i] = Float.valueOf(getVal(params[idx++]));
			for(Uniform<?> u : uniformsvert)
				result[idx++] = u.get();
			result[idx++] = srcTexture;
			for(Uniform<?> u : uniformsfrag)
				result[idx++] = u.get();
			return result;
		}
	}

	public static final Uniform<?>[] EMPTY_UniformA = new Uniform<?>[0];

	protected long                          frame;
	protected Class<? extends Frame>[]      frameTypes;
	protected Set<Class<? extends Frame>>   preferredTypes;
	private   String                        name = getClass().getName();
	private   final FxMaterial              material;
	private   final IMesh                   quad;
	private   final Renderable              renderable;
	private   final int[]                   viewport = new int[4];
	private   final Uniform<?>[]            uniformsvert;
	private   final String[]                outIn;
	private   final Uniform<?>[]            uniformsfrag;
	private   final Map<String, Uniform<?>> name2uniform = new HashMap<>();

	protected AbstractVideoFX(Parameter ... parameters) {
		this(EMPTY_UniformA, ClassUtilities.EMPTY_StringA, EMPTY_UniformA, parameters);
	}

	protected AbstractVideoFX(Uniform<?>[] uniformsvert, String[] outIn, Uniform<?>[] uniformsfrag, Parameter ... parameters) {
		super(parameters);
		if(this instanceof IVideoGLFX) {
			this.uniformsvert = uniformsvert;
			this.outIn        = outIn;
			this.uniformsfrag = uniformsfrag;
			for(Uniform<?> u : uniformsvert)
				name2uniform.put(u.id(), u);
			for(Uniform<?> u : uniformsfrag)
				name2uniform.put(u.id(), u);
			IAttribute[] attrs = new IAttribute[parameters.length + 1 + uniformsvert.length + uniformsfrag.length];
			int idx = 0;
			for(int i = 0; i < parameters.length; i++)
				attrs[idx++] = parameters[i];
			for(Uniform<?> u : uniformsvert)
				attrs[idx++] = u;
			attrs[idx++] = IMaterial.COLOR_MAP;
			for(Uniform<?> u : uniformsfrag)
				attrs[idx++] = u;
			this.material   = new FxMaterial(attrs);
			this.quad       = new DefaultMesh(material, DefaultGeometry.createVM(Primitive.TRIANGLES, MeshUtilities.DEFAULT_QUAD_TRIANGLES, MeshUtilities.DEFAULT_QUAD_TEX_COORDS));
			this.renderable = new Renderable(quad, null);
		} else if(this instanceof IVideoFrameFX) {
			this.uniformsvert = null;
			this.outIn        = null;
			this.uniformsfrag = null;
			this.material     = null;
			this.quad         = null;
			this.renderable   = null;
		} else {
			this.uniformsvert = null;
			this.outIn        = null;
			this.uniformsfrag = null;
			this.material     = null;
			this.quad         = null;
			this.renderable   = null;
			log.severe("'" + this + "' must implement at least one of " + TextUtilities.toString(FX_CLASSES));
			System.exit(0);
		}
	}

	public static float toFloat(final byte v) {
		return (v & 0xFF) / 255f;
	}

	public static byte toByte(final float v) {
		if(v < 0f) return 0;
		if(v > 1f) return -1;
		return (byte) (v * 255f);
	}

	public static byte toByte(final double v) {
		if(v < 0.0) return 0;
		if(v > 1.0) return -1;
		return (byte) (v * 255.0);
	}

	public static float wrap(final float v) {
		float result = v % 1f;
		return result < 0 ? result + 1 : result;
	}

	public static float mix(final float val0, final float val1, float w) {
		return val1 * w + (1f-w) * val0;
	}

	public void processFrame(GL3 gl, double playOutTime, IVideoRenderTarget target) {}

	@Override
	protected final void run(IVideoRenderTarget target) throws RenderCommandException {
		if(target instanceof AbstractVideoTarget && ((AbstractVideoTarget)target).runAs() == GLFX) {
			try(IGLContext ctx = GLContextManager.acquireContext()) {
				final GL3 gl = ctx.getGL();
				processFrame(gl, target.getFrame().playOutTime, target);
				material.prepare(gl, (AbstractVideoTarget)target);
				renderable.update(gl, material.getData(), quad.getTransformedGeometryData());
				material.fbo.bind(gl);
				gl.glGetIntegeri_v(GL3.GL_VIEWPORT, 0, viewport, 0);
				gl.glViewport(0, 0, material.dstTexture.getWidth(), material.dstTexture.getHeight());
				renderable.render(gl);
				FrameBuffer.unbind(gl);
				gl.glBindTexture(GL.GL_TEXTURE_2D, material.dstTexture.getGlObject().getId());
				gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
				target.getFrame().setTexture(material.dstTexture);
				gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
				gl.glFinish();
			} catch(RenderCommandException e) {
				throw e;
			} catch(Throwable t) {
				throw new RenderCommandException(t);
			}
		} else if(target instanceof AbstractVideoTarget && ((AbstractVideoTarget)target).runAs() == FRAMEFX) {
			VideoFrame frame = target.getFrame();
			((IVideoFrameFX)this).processFrame(frame.playOutTime, target, frame.getFrame());
		}
	}

	public Texture getDstTexture() {
		return material.dstTexture;
	}

	@Override
	public String toString() {
		return TextUtilities.getShortClassName(this);
	}

	public IShader getShader() {
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object[] getData() {
		return material.getData();
	}

	public Primitive getType() {
		return Primitive.TRIANGLES;
	}

	public IGeometryAttribute[] getGeometryAttributes() {
		return material.getGeometryAttributes();
	}

	public UpdateRequest getUpdater() {
		return material.getUpdater();
	}

	public IAttribute[] getProvidedAttributes() {
		return material.getProvidedAttributes();
	}

	public String mainFrag() {
		return ClassUtilities.EMPTY_String;
	}

	public String mainVert() {
		return ClassUtilities.EMPTY_String;
	}

	public String[] functionsFrag() {
		return ClassUtilities.EMPTY_StringA;
	}

	public String[] functionsVert() {
		return ClassUtilities.EMPTY_StringA;
	}

	protected void setUniform(String name, Object value) {
		name2uniform.get(name).set(value);
	}

	protected static String lines(String ... lines) {
		return TextUtilities.cat(lines, '\n');
	}

	protected static Uniform<?>[] uniforms(Object ...uniforms) {
		Uniform<?>[] result = new Uniform<?>[uniforms.length / 2];
		for(int i = 0; i < result.length; i++)
			result[i] = new Uniform<>(uniforms[i*2].toString(), uniforms[i*2+1]);
		return result;
	}
	
	private String getVertexCode() {
		StringBuilder uniformsStr = new StringBuilder();
		for(Uniform<?> u : uniformsvert)
			uniformsStr.append("uniform ").append(u.glType()).append(' ').append(u.id()).append(";\n");

		StringBuilder outInStr = new StringBuilder();
		for(String out : outIn)
			outInStr.append("out ").append(out).append(";\n");

		StringBuilder functionStr = new StringBuilder();
		for(String function : functionsVert())
			functionStr.append('\n').append(function).append('\n');

		String main = mainVert();
		if(main.length() > 0 && !(main.endsWith(";")))
			main += ";";

		return lines("#version 330",
				"in vec4 vertexPosition;",
				"in vec2 vertexTexCoord;",
				uniformsStr.toString(),
				"out vec2 uv;",
				outInStr.toString(), 
				functionStr.toString(),
				"void main() {",
				main,
				"  uv = vertexTexCoord;",
				"gl_Position = vertexPosition;",
				"}");
	}

	private String getFragmentCode() {
		Parameter[]   params   = getParameters();
		StringBuilder paramStr = new StringBuilder();
		for(int i = 0; i < params.length; i++)
			paramStr.append("uniform float ").append(params[i].getName()).append(";\n");

		StringBuilder outInStr = new StringBuilder();
		for(String in : outIn)
			outInStr.append("in ").append(in).append(";\n");

		StringBuilder functionStr = new StringBuilder();
		for(String function : functionsFrag())
			functionStr.append('\n').append(function).append('\n');

		String main = mainFrag();
		if(main.length() > 0 && !(main.endsWith(";")))
			main += ";";

		StringBuilder uniformsStr = new StringBuilder();
		for(Uniform<?> u : uniformsfrag)
			uniformsStr.append("uniform ").append(u.glType()).append(' ').append(u.id()).append(";\n");

		return lines(
				"#version 330",
				"uniform sampler2D frame;",
				"in vec2 uv;",
				outInStr.toString(),
				paramStr.toString(),
				uniformsStr.toString(),
				"out vec4 result;",
				functionStr.toString(),
				"void main() {",
				"result = texture(frame, uv);",
				main.toString(),
				"}\n");
	}
}
