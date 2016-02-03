package org.cs3.pdt.graphicalviews.focusview;

public class NewGlobalView extends GlobalView {
	
	@Override
	protected ViewCoordinatorBase createViewCoordinator() {
		return new NewProjectViewCoordinator(this);
	}

}
