/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: majella (among others)
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2016, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/
package org.cs3.pdt.graphicalviews.focusview;

import org.cs3.pdt.graphicalviews.main.PDTGraphView;
import org.cs3.pdt.graphicalviews.main.PDTGraphViewJ;

/**
 * TODO: Add a short comment to describe the class!
 */
public class NewContextViewCoordinator extends ContextViewCoordinator {

	public NewContextViewCoordinator(ViewBase focusView) {
		super(focusView);
	}

	@Override
	public void swichFocusView(String path) {
		currentFocusView = views.get(path);
		if (currentFocusView == null) {
			PDTGraphView pdtGraphView = new PDTGraphViewJ(focusView, path); 
			GraphProcessLoaderBase loader = focusView.createGraphProcessLoader(pdtGraphView);
			loader.setCurrentPath(path);
			
			currentFocusView = focusView.createFocusViewControl(pdtGraphView, loader);

			refreshCurrentView();
			
			views.put(path, currentFocusView);
		}
		
		currentFocusView.recalculateMode();
		focusView.setCurrentFocusView(currentFocusView);
	}
}
