/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.widgets.figures.IsisMotorFigure;
import org.csstudio.opibuilder.widgets.model.IsisMotorModel;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the tank widget. The controller mediates between
 * {@link IsisMotorModel} and {@link IsisMotorFigure}.
 *
 * @author Xihui Chen
 * @author Takashi Nakamoto - support for "FillColor Alarm Sensitive" property
 */
public final class IsisMotorEditPart extends AbstractPVWidgetEditPart {
    /**
     * {@inheritDoc}
     */
    @Override
    protected IFigure doCreateFigure() {
        IsisMotorModel model = getWidgetModel();

        IsisMotorFigure motorFigure = new IsisMotorFigure();
//        initializeBaseFigureProperties(motorFigure, model);
//        motorFigure.setFillColor(model.getFillColor());
//        tank.setEffect3D(model.isEffect3D());
        return motorFigure;

    }

    @Override
    public IsisMotorModel getWidgetModel() {
        return (IsisMotorModel) getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerPropertyChangeHandlers() {
        registerBasePropertyChangeHandlers();

//        //fillColor
//        IWidgetPropertyChangeHandler fillColorHandler = new IWidgetPropertyChangeHandler() {
//            @Override
//            public boolean handleChange(final Object oldValue,
//                    final Object newValue,
//                    final IFigure refreshableFigure) {
//                IsisMotorFigure motorFigure = (IsisMotorFigure) refreshableFigure;
//                motorFigure.setFillColor(((OPIColor) newValue).getSWTColor());
//                return false;
//            }
//        };
//        setPropertyChangeHandler(IsisMotorModel.PROP_FILL_COLOR, fillColorHandler);

//        // Change fill color when "FillColor Alarm Sensitive" property changes.
//        IWidgetPropertyChangeHandler fillColorAlarmSensitiveHandler = new IWidgetPropertyChangeHandler() {
//            @Override
//            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
//                TankFigure figure = (TankFigure) refreshableFigure;
//                boolean sensitive = (Boolean)newValue;
//                figure.setFillColor(
//                        delegate.calculateAlarmColor(sensitive,
//                                                     getWidgetModel().getFillColor()));
//                return true;
//            }
//        };
//        setPropertyChangeHandler(TankModel.PROP_FILLCOLOR_ALARM_SENSITIVE, fillColorAlarmSensitiveHandler);
//
//        // Change fill color when alarm severity changes.
//        delegate.addAlarmSeverityListener(new AlarmSeverityListener() {
//
//            @Override
//            public boolean severityChanged(AlarmSeverity severity, IFigure figure) {
//                if (!getWidgetModel().isFillColorAlarmSensitive())
//                    return false;
//                TankFigure tank = (TankFigure) figure;
//                tank.setFillColor(
//                        delegate.calculateAlarmColor(getWidgetModel().isFillColorAlarmSensitive(),
//                                                     getWidgetModel().getFillColor()));
//                return true;
//            }
//        });
    }
}
