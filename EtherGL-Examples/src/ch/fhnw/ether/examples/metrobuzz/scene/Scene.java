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

package ch.fhnw.ether.examples.metrobuzz.scene;

import java.awt.event.KeyEvent;

import ch.fhnw.ether.examples.metrobuzz.model.Model;
import ch.fhnw.ether.examples.metrobuzz.tool.AreaTool;
import ch.fhnw.ether.scene.AbstractScene;
import ch.fhnw.ether.tool.ITool;
import ch.fhnw.ether.ui.Button;
import ch.fhnw.ether.ui.Slider;

public class Scene extends AbstractScene {
	private final ITool areaTool = new AreaTool(this);
    
    public Scene() {
        addUI();
    }

    @Override
    public Model getModel() {
        return (Model) super.getModel();
    }
    
    private void addUI() {
        getUI().addWidget(new Button(0, 0, "PICK", "Pick Tool (1)", KeyEvent.VK_1, (button, view) -> setCurrentTool(null)));
        getUI().addWidget(new Button(1, 0, "AREA", "AREA Tool (2)", KeyEvent.VK_2, (button, view) -> setCurrentTool(areaTool)));
		getUI().addWidget(new Button(0, 1, "F", "Frame Scene (F)", KeyEvent.VK_F, (button, view) -> {
			view.getCamera().frame(getModel().getBounds());
			repaintViews();
		}));
        getUI().addWidget(new Button(1, 1, "Quit", "Quit", KeyEvent.VK_ESCAPE, (button, view) -> System.exit(0)));
        getUI().addWidget(new Slider(2, 0, "Slider", "Slider", 0.3f, (slider, view) -> System.out.println("I slide... " + slider.getValue())));
    }
    
}