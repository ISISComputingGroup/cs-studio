/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.scan.ui.plot;

import java.rmi.RemoteException;
import java.util.List;

import org.csstudio.scan.client.ScanInfoModel;
import org.csstudio.scan.data.ScanData;
import org.csstudio.scan.server.ScanInfo;
import org.eclipse.swt.widgets.Display;

/** Model of the Plot's data
 *  <ul>
 *  <li>Maintains underlying {@link ScanInfoModel}
 *  <li>Currently selected scan and its scan data
 *  <li>Current X, Y axis assignments
 *  </ul>
 *  @author Kay Kasemir
 */
public class PlotDataModel implements Runnable
{
    /** Scan model */
    final private ScanInfoModel model;

    // TODO Check synchronization
    
    /** @see #run() */
    private volatile Thread update_thread;

    /** Currently selected scan */
    private volatile ScanInfo selected_scan = null;

    private volatile List<String> devices = null;
    
    private volatile String x_axis_device = null;

    private volatile String y_axis_device = null;
    
    final private PlotDataProvider plot_data;
    
    /** Initialize
     *  @throws Exception on error connecting to scan server
     *  @see #dispose()
     */
    public PlotDataModel(final Display display) throws Exception
    {
        model = ScanInfoModel.getInstance();
        plot_data = new PlotDataProvider(display);
    }
    
    /** Start the model */
    public void start()
    {
        update_thread = new Thread(this, "PlotDataModel");
        update_thread.setDaemon(true);
        update_thread.start();
    }
    
    /** Must be called to release resources */
    public void stop()
    {
        final Thread thread = update_thread;
        update_thread = null;
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            // Ignore, shutting down anyway
        }
        model.release();
    }

    /** Runnable of the update thread.
     * 
     *  Fetches scan data from selected scan,
     *  then picks the X and Y data
     *  and notifies listeners.
     */
    @Override
    public void run()
    {
        while (update_thread != null)
        {
            final ScanInfo scan = selected_scan;
            final String x_device = x_axis_device;
            final String y_device = y_axis_device;
            if (scan == null)
            {
                devices = null;
                plot_data.clear();
            }
            else
            {   // Get data for scan
                final ScanData scan_data;
                try
                {
                    scan_data = model.getScanData(scan);
                    devices = scan_data.getDevices();
                    // Get data for selected devices from plot
                    if (x_device == null  ||  y_device == null)
                        plot_data.clear();
                    else
                        plot_data.update(scan_data, x_device, y_device);
                }
                catch (RemoteException ex)
                {
                    plot_data.clear();
                    devices = null;
                }
            }                
            
            // Wait for next update period
            // or early wake from waveUpdateThread()
            synchronized (this)
            {
                final long start = System.currentTimeMillis();
                try
                {
                    wait(1000);
                }
                catch (InterruptedException e)
                {
                    // Ignore
                }
                final long time = System.currentTimeMillis() - start;
                System.out.println("Update thread woke after " + time + "ms");
            }
        }
    }

    /** Wake update thread ASAP because
     *  configuration has been changed
     */
    private void waveUpdateThread()
    {
        synchronized (this)
        {
            notifyAll();
        }
    }

    /** @return Most recent infos obtained from server */
    public List<ScanInfo> getScanInfos()
    {
        return model.getInfos();
    }

    /** Select scan for monitoring data
     *  @param id Scan ID
     */
    public void selectScan(final long id)
    {
        selected_scan = getScan(id);
        waveUpdateThread();
    }

    /** Get scan info by ID
     *  @param id Scan ID
     *  @return {@link ScanInfo} or <code>null</code>
     */
    private ScanInfo getScan(final long id)
    {
        final List<ScanInfo> infos = model.getInfos();
        for (ScanInfo info : infos)
            if (info.getId() == id)
                return info;
        return null;
    }

    /** @return Devices used by currently selected Scan */
    public List<String> getDevices()
    {
        return devices;
    }
    
    /** @param device_name Device to use for "X" axis */
    public void selectXDevice(final String device_name)
    {
        x_axis_device = device_name;
        waveUpdateThread();
    }

    /** @param device_name Device to use for "Y" axis */
    public void selectYDevice(final String device_name)
    {
        y_axis_device = device_name;
        waveUpdateThread();
    }
    
    /** @return Data of selected scan */
    public PlotDataProvider getPlotData()
    {
        return plot_data;
    }
}