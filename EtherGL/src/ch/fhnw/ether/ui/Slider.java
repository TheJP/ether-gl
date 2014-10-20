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

package ch.fhnw.ether.ui;

import java.awt.Color;

import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.MathUtil;

import com.jogamp.newt.event.MouseEvent;

public class Slider extends AbstractWidget {
	public interface ISliderAction extends IWidgetAction<Slider> {
		@Override
		void execute(Slider slider, IView view);
	}

	private static final int SLIDER_WIDTH = 96;
	private static final int SLIDER_HEIGHT = 24;

	private static final int SLIDER_GAP = 8;

	private static final Color SLIDER_BG = new Color(1f, 1f, 1f, 0.25f);
	private static final Color SLIDER_FG = new Color(0.6f, 0, 0, 0.75f);

	private boolean sliding;
	private float value;

	public Slider(int x, int y, String label, String help) {
		this(x, y, label, help, 0, null);
	}

	public Slider(int x, int y, String label, String help, float value) {
		this(x, y, label, help, value, null);
	}

	public Slider(int x, int y, String label, String help, float value, ISliderAction action) {
		super(x, y, label, help, action);
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	@Override
	public boolean hit(int x, int y, IView view) {
		UI ui = getUI();
		float bx = ui.getX() + getX() * (SLIDER_GAP + SLIDER_WIDTH);
		float by = ui.getY() + getY() * (SLIDER_GAP + SLIDER_HEIGHT);
		return x >= bx && x <= bx + SLIDER_WIDTH && y >= by && y <= by + SLIDER_HEIGHT;
	}

	@Override
	public void draw(GraphicsPlane surface) {
		int bw = Slider.SLIDER_WIDTH;
		int bh = Slider.SLIDER_HEIGHT;
		int bg = Slider.SLIDER_GAP;
		int bx = getX() * (bg + bw);
		int by = getY() * (bg + bh);
		surface.fillRect(SLIDER_BG, bx, by, bw, bh);
		surface.fillRect(SLIDER_FG, bx, by, (int) (value * bw), bh);
		String label = getLabel();
		if (label != null)
			surface.drawString(TEXT_COLOR, label, bx + 2, by + bh - 4);

	}

	@Override
	public void fire(IView view) {
		if (getAction() == null)
			throw new UnsupportedOperationException("button '" + getLabel() + "' has no action defined");
		((ISliderAction) getAction()).execute(this, view);
	}

	@Override
	public boolean mousePressed(MouseEvent e, IView view) {
		if (hit(e.getX(), e.getY(), view)) {
			sliding = true;
			updateValue(e, view);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(MouseEvent e, IView view) {
		if (sliding) {
			sliding = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent e, IView view) {
		if (sliding) {
			updateValue(e, view);
			return true;
		}
		return false;
	}

	private void updateValue(MouseEvent e, IView view) {
		UI ui = getUI();
		float bx = ui.getX() + getX() * (SLIDER_GAP + SLIDER_WIDTH);
		value = MathUtil.clamp((e.getX() - bx) / SLIDER_WIDTH, 0, 1);
		requestUpdate();
		fire(view);
	}
}
