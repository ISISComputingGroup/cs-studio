package org.csstudio.sds.components.ui.internal.editparts;

import org.csstudio.sds.components.model.RectangleElement;
import org.csstudio.sds.components.ui.internal.figures.RefreshableRectangleFigure;
import org.csstudio.sds.ui.editparts.AbstractElementEditPart;
import org.csstudio.sds.ui.figures.IRefreshableFigure;

/**
 * EditPart controller for <code>RectangleElement</code> elements.
 * 
 * @author Sven Wende & Stefan Hofer
 * 
 */
public final class RectangleEditPart extends AbstractElementEditPart {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IRefreshableFigure doCreateFigure() {
		return new RefreshableRectangleFigure();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized boolean doRefreshFigure(final String propertyName,
			final Object newValue, final IRefreshableFigure figure) {
		RefreshableRectangleFigure rectangle = (RefreshableRectangleFigure) figure;
		
		if (propertyName.equals(RectangleElement.PROP_FILL)) {
			rectangle.setFill((Double) newValue);
			return true;
		}

		return false;
	}
}
