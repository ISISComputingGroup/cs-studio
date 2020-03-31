package org.csstudio.opibuilder.widgets.figures;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.Objects;

import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;

public class IsisMotorFigure extends Figure implements Introspectable {
	
	private final RectangleFigure backgroundRectangle;
	
	private Color fillColour = CustomMediaFactory.getInstance().getColor(
            CustomMediaFactory.COLOR_RED);
	
	public IsisMotorFigure() {
		System.out.println("In constructor");
		
		setLayoutManager(new XYLayout());
		
		backgroundRectangle = new RectangleFigure() {
			@Override
			protected void fillShape(Graphics graphics) {
				graphics.fillRectangle(IsisMotorFigure.this.getClientArea());
				super.fillShape(graphics);
			}
		};
		
        add(backgroundRectangle);
        repaint();
    }
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		System.out.println("in paintClientArea");
		graphics.pushState();
		
		backgroundRectangle.paint(graphics);
		
		graphics.popState();
		super.paintClientArea(graphics);
	}
	
	public void repaint() {
		System.out.println("In repaint");
		super.repaint();
		backgroundRectangle.repaint();
	}
	
	public void setFillColor(Color color) {
		if (!Objects.equals(color, fillColour)) {
			fillColour = color;
			repaint();
		}
	}

	@Override
	public BeanInfo getBeanInfo() throws IntrospectionException {
		return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
	}

}
