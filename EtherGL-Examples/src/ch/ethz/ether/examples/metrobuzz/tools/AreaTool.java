/*
 * Copyright (c) 2013 - 2014, ETH Zurich & FHNW (Stefan Muller Arisona)
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
 *  Neither the name of ETH Zurich nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
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

package ch.ethz.ether.examples.metrobuzz.tools;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import ch.ethz.ether.geom.ProjectionUtil;
import ch.ethz.ether.geom.Vec3;
import ch.ethz.ether.model.CubeMesh;
import ch.ethz.ether.model.CubeMesh.Origin;
import ch.ethz.ether.model.IPickable;
import ch.ethz.ether.render.AbstractRenderGroup;
import ch.ethz.ether.render.IRenderGroup;
import ch.ethz.ether.render.IRenderGroup.Pass;
import ch.ethz.ether.render.IRenderGroup.Source;
import ch.ethz.ether.render.IRenderGroup.Type;
import ch.ethz.ether.render.IRenderer;
import ch.ethz.ether.scene.AbstractTool;
import ch.ethz.ether.scene.IScene;
import ch.ethz.ether.view.IView;
import ch.ethz.util.IAddOnlyFloatList;

public final class AreaTool extends AbstractTool {
    private static final float[] TOOL_COLOR = {1.0f, 1.0f, 0.0f, 1.0f};
    
    private static final float KEY_INCREMENT = 0.01f;
    
    private CubeMesh mesh = new CubeMesh(Origin.BOTTOM_CENTER);
    
    private boolean moving = false;
    
    private float xOffset = 0;
    private float yOffset = 0;


    private IRenderGroup toolGeometry = new AbstractRenderGroup(Source.TOOL, Type.TRIANGLES, Pass.DEPTH) {
        @Override
        public void getVertices(IAddOnlyFloatList dst) {
        	mesh.getTriangleVertices(dst);
        }

        @Override
        public float[] getColor() {
            return TOOL_COLOR;
        }
    };

    public AreaTool(IScene scene) {
        super(scene);
		mesh.setScale(new Vec3(0.1, 0.1, 0.001));
    }

    @Override
    public void activate() {
        IRenderer.GROUPS.add(toolGeometry);
    }

    @Override
    public void deactivate() {
        IRenderer.GROUPS.remove(toolGeometry);
    }

    @Override
    public void keyPressed(KeyEvent e, IView view) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            	yOffset += KEY_INCREMENT;
                break;
            case KeyEvent.VK_DOWN:
            	yOffset -= KEY_INCREMENT;
                break;
            case KeyEvent.VK_LEFT:
            	xOffset -= KEY_INCREMENT;
                break;
            case KeyEvent.VK_RIGHT:
            	xOffset += KEY_INCREMENT;
                break;
        }
        
        mesh.setTranslation(new Vec3(xOffset, yOffset, 0));
        toolGeometry.requestUpdate();
        view.getScene().repaintViews();
    }

    @Override
    public void mousePressed(MouseEvent e, IView view) {
		int x = e.getX();
		int y = view.getViewport().h - e.getY();
		if (mesh.pick(IPickable.PickMode.POINT, x, y, 0, 0, view, null))
			moving = true;
    }

    @Override
    public void mouseDragged(MouseEvent e, IView view) {
    	if (moving) {
            Line line = ProjectionUtil.getRay(view, e.getX(), view.getViewport().h - e.getY());
            Plane plane = new Plane(new Vector3D(0, 0, 1));
            Vector3D p = plane.intersection(line);
            if (p != null) {
            	xOffset = (float)p.getX();
            	yOffset = (float)p.getY();
                mesh.setTranslation(new Vec3(xOffset, yOffset, 0));
                toolGeometry.requestUpdate();
                view.getScene().repaintViews();    		
            }
    	}
    }
    
    @Override
    public void mouseReleased(MouseEvent e, IView view) {
    	moving = false;
    }
}