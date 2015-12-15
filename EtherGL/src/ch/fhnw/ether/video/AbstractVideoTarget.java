package ch.fhnw.ether.video;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.IScheduler;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.RenderProgram;
import ch.fhnw.ether.render.gl.GLObject;
import ch.fhnw.ether.render.gl.GLObject.Type;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.ether.video.fx.IVideoFrameFX;
import ch.fhnw.ether.video.fx.IVideoGLFX;

public abstract class AbstractVideoTarget extends AbstractMediaTarget<VideoFrame, IVideoRenderTarget> implements IVideoRenderTarget, IScheduler {
	private final Class<?> preferredType;

	protected AbstractVideoTarget(int threadPriority, Class<?> preferredType, boolean realTime) {
		super(threadPriority, realTime);
		this.preferredType = preferredType;
	}

	@Override
	public void useProgram(RenderProgram<IVideoRenderTarget> program) throws RenderCommandException {
		AbstractRenderCommand<?>[] cmds = program.getProgram();
		int numGlFx    = 0;
		int numFrameFx = 0;
		for(int i = 1; i < cmds.length; i++) {
			if(!(cmds[i] instanceof AbstractVideoFX))
				throw new ClassCastException("Command '" + cmds[i] + "' not sublcass of " + AbstractVideoFX.class.getName());
			if(cmds[i] instanceof IVideoFrameFX)
				numFrameFx++;
			if(cmds[i] instanceof IVideoGLFX)
				numGlFx++;
		}
		if(numGlFx == cmds.length - 1 && preferredType == AbstractVideoFX.GLFX) {}
		else if(numFrameFx == cmds.length - 1 && preferredType == AbstractVideoFX.FRAMEFX) {}
		else if(numGlFx == 0 && numFrameFx == 0) {}
		else
			throw new IllegalArgumentException("All commands must implement either " + IVideoGLFX.class.getName() + " or " + IVideoFrameFX.class.getName());
		super.useProgram(program);
	}

	@Override
	public IVideoSource getVideoSource() {
		return (IVideoSource)program.getFrameSource();
	}

	public Texture getSrcTexture(GL3 gl, AbstractVideoFX fx) {
		AbstractRenderCommand<?>[] cmds = program.getProgram();
		for(int i = cmds.length; --i >= 1;)
			if(cmds[i] == fx)
				for(int j = i; --j >= 0;)
					if(cmds[j].isEnabled())
						return j == 0 ? getFrame().getTexture() : getDstTexture(gl, (AbstractVideoFX)cmds[j]);
		return null;
	}

	public Texture getDstTexture(GL3 gl, AbstractVideoFX fx) {
		AbstractRenderCommand<?>[] cmds = program.getProgram();
		IVideoSource               src  = (IVideoSource) cmds[0];
		if(cmds[cmds.length - 1] == fx || fx.getDstTexture() == null)
			return createTexture(gl, src);
		return fx.getDstTexture();
	}

	private Texture createTexture(GL3 gl, IVideoSource src) {
		Texture result;
		result = new Texture(new GLObject(gl, Type.TEXTURE), src.getWidth(), src.getHeight());
		gl.glBindTexture(GL3.GL_TEXTURE_2D, result.getGlObject().getId());
		gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, src.getWidth(), src.getHeight(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL3.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameterf(GL3.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		return result;
	}

	public Class<?> runAs() {
		return preferredType;
	}
}
