package org.cs3.pdt.graphicalviews.focusview;

public class NewFocusView extends FocusView {
	
	@Override
	protected ViewCoordinatorBase createViewCoordinator() {
		return new NewContextViewCoordinator(this);
	}

}
