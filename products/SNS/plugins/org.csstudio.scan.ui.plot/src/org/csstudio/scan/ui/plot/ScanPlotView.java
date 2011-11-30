/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.scan.ui.plot;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.scan.server.ScanInfo;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/** Eclipse View for the scan plot
 *  @author Kay Kasemir
 */
public class ScanPlotView extends ViewPart
{
    /** View ID defined in plugin.xml */
    final public static String ID = "org.csstudio.scan.ui.plot.view"; //$NON-NLS-1$
    
    private PlotDataModel model;

    private Plot plot;

    private ScanSelectorAction scan_selector;

    private DeviceSelectorAction y_selector;

    private DeviceSelectorAction x_selector;
    
    /** {@inheritDoc} */
    @Override
    public void createPartControl(final Composite parent)
    {
        try
        {
            model = new PlotDataModel(parent.getDisplay());
            model.start();
        }
        catch (Exception ex)
        {
            final Label l = new Label(parent, 0);
            l.setText("Error getting scan info: " + ex.getMessage()); //$NON-NLS-1$
            Logger.getLogger(getClass().getName()).
                log(Level.WARNING, "Error getting scan info", ex); //$NON-NLS-1$
            return;
        }
        
        parent.addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                model.stop();
                model = null;
            }
        });
        
        createComponents(parent);
    }

    /** @param parent Parent composite under which to create GUI elements */
    private void createComponents(final Composite parent)
    {
        plot = new Plot(parent);
        plot.addTrace(model.getPlotData());
        
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        scan_selector = new ScanSelectorAction(model, plot);
        x_selector = DeviceSelectorAction.forXAxis(model, plot);
        y_selector = DeviceSelectorAction.forYAxis(model, plot);
        toolbar.add(scan_selector);
        toolbar.add(x_selector);
        toolbar.add(y_selector);
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus()
    {
        // NOP
    }

    /** Select a scan to display in the plot
     *  @param name  Scan name  (might be replaced with the actual scan name)
     *  @param id    ID of scan to select
     */
    public void selectScan(final String name, final long id)
    {
        final String option;
        // Try to use the 'correct' scan information for the ID,
        // but if the model has not (yet) obtained that,
        // use the name and ID as given
        final ScanInfo scan = model.getScan(id);
        if (scan != null)
            option = ScanSelectorAction.encode(scan);
        else
            option = ScanSelectorAction.encode(name, id);
        scan_selector.setSelection(option);
        scan_selector.handleSelection(option);
    }

    /** Select the devices to use for the plot's axes
     *  @param xdevice Name of X axis device
     *  @param ydevice .. Y axis ...
     */
    public void selectDevices(final String xdevice, final String ydevice)
    {
        x_selector.setSelection(xdevice);
        x_selector.handleSelection(xdevice);
        
        y_selector.setSelection(ydevice);
        y_selector.handleSelection(ydevice);
    }
}
