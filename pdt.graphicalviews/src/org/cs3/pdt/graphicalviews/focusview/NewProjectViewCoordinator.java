/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: zahn (among others)
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

import java.io.IOException;

import org.cs3.pdt.connector.util.FileUtils;
import org.cs3.pdt.graphicalviews.main.PDTGraphView;
import org.cs3.pdt.graphicalviews.main.PDTGraphViewJ;
import org.eclipse.core.resources.IProject;

/**
 * TODO: Add a short comment to describe the class!
 */
public class NewProjectViewCoordinator extends ProjectViewCoordinator {

	public NewProjectViewCoordinator(ViewBase focusView) {
		super(focusView);
	}

	@Override
	public void swichFocusView(String path) {
		try {
			IProject project = FileUtils.findFileForLocation(path).getProject();
		
			currentFocusView = views.get(project.getName());
			
			if (currentFocusView == null) {
				PDTGraphView pdtGraphView = new PDTGraphViewJ(focusView, path);
				GraphProcessLoaderBase loader = focusView.createGraphProcessLoader(pdtGraphView);
				loader.setCurrentPath(path);
				
				currentFocusView = focusView.createFocusViewControl(pdtGraphView, loader);
	
				refreshCurrentView();
				
				views.put(project.getName(), currentFocusView);
			}
	
			currentFocusView.recalculateMode();
			focusView.setCurrentFocusView(currentFocusView);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
