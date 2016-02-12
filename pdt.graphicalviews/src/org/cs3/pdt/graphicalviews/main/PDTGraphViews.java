/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.pdt.graphicalviews.main;

import org.cs3.pdt.graphicalviews.focusview.ViewBase;
import org.cs3.pdt.graphicalviews.view.modes.MouseHandler;
import org.cs3.pdt.graphicalviews.view.modes.OpenInEditorViewMode;

public class PDTGraphViews {
	
	private PDTGraphViewJ pdtGraphViewJ;
	private PDTGraphView pdtGraphViewY;

	public PDTGraphViews(ViewBase focusView, String path) {
		this.pdtGraphViewJ = new PDTGraphViewJ(focusView, path);
		this.pdtGraphViewY = new PDTGraphView(focusView);
	}

	public void updateLayout() {
		//this.pdtGraphViewJ.updateLayout();
		this.pdtGraphViewY.updateLayout();
	}

	public void addViewMode(OpenInEditorViewMode openInEditorViewMode) {
		pdtGraphViewY.addViewMode(openInEditorViewMode);
		pdtGraphViewJ.addViewMode(openInEditorViewMode);
	}

	public void addViewMode(MouseHandler mouseHandler) {
		pdtGraphViewY.addViewMode(mouseHandler);
		pdtGraphViewJ.addViewMode(mouseHandler);
	}

	public void recalculateMode() {
		pdtGraphViewY.recalculateMode();
		pdtGraphViewJ.recalculateMode();
	}

	public boolean isEmpty() {
		return pdtGraphViewY.isEmpty(); //TODO: make sure PdtGraphViewJ always returns the same as original implementation
	}

}
