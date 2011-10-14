/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.util.ResourceUtilSSHelper;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Xihui Chen
 *
 */
public class ResourceUtilSSHelperImpl extends ResourceUtilSSHelper {

	private static InputStream inputStream;
	private static Object lock = new Boolean(true);
	
	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.util.ResourceUtilSSHelper#pathToInputStream(org.eclipse.core.runtime.IPath, boolean)
	 */
	@Override
	public InputStream pathToInputStream(IPath path, boolean runInUIJob)
			throws Exception {

	    // Try workspace location
	    final IFile workspace_file = getIFileFromIPath(path);
	    // Valid file should either open, or give meaningful exception
	    if (workspace_file != null  &&  workspace_file.exists())
	        return workspace_file.getContents();

	    // Not a workspace file. Try local file system
        File local_file = path.toFile();
        // Path URL for "file:..." so that it opens as FileInputStream
        if (local_file.getPath().startsWith("file:"))
            local_file = new File(local_file.getPath().substring(5));
        String urlString;
        try
        {
            return new FileInputStream(local_file);
        }
        catch (Exception ex)
        {
            // Could not open as local file.
            // Does it look like a URL?
            // Eclipse Path collapses "//" into "/", revert that:
            urlString = path.toString();
            if(!urlString.contains("://"))
                urlString = urlString.replaceFirst(":/", "://");
            // Does it now look like a URL? If not, report the original local file problem
            if (! ResourceUtil.isURL(urlString))
                throw new Exception("Cannot open " + ex.getMessage(), ex);
        }

        // Must be a URL
        final URL url = new URL(urlString);
        inputStream = null;
        
		if (runInUIJob) {
			synchronized (lock) {
				IRunnableWithProgress openURLTask = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						try {
							monitor.beginTask("Connecting to " + url,
									IProgressMonitor.UNKNOWN);
							inputStream = ResourceUtil.openURLStream(url);
						} catch (IOException e) {
							throw new InvocationTargetException(e,
									"Timeout while connecting to " + url);
						} finally {
							monitor.done();
						}
					}

				};
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.run(true, false, openURLTask);
			}
		}else
			return ResourceUtil.openURLStream(url);
       
		return inputStream;        
	}

	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.util.ResourceUtilSSHelper#getInputStreamFromEditorInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	public InputStream getInputStreamFromEditorInput(IEditorInput editorInput) {
		InputStream result = null;
		if (editorInput instanceof FileEditorInput) {
			try {
				result = ((FileEditorInput) editorInput).getFile()
						.getContents();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if (editorInput instanceof FileStoreEditorInput) {
			IPath path = URIUtil.toPath(((FileStoreEditorInput) editorInput)
					.getURI());
			try {
				result = new FileInputStream(path.toFile());
			} catch (FileNotFoundException e) {
				result = null;
			}
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.util.ResourceUtilSSHelper#isExistingWorkspaceFile(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean isExistingWorkspaceFile(IPath path) {
		return getIFileFromIPath(path) != null;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.util.ResourceUtilSSHelper#getPathInEditor(org.eclipse.ui.IEditorInput)
	 */
	@Override
	public IPath getPathInEditor(IEditorInput input) {
		if(input instanceof FileEditorInput)
			return ((FileEditorInput)input).getFile().getFullPath();
		else if(input instanceof IPathEditorInput)
			return ((IPathEditorInput)input).getPath();
		else if(input instanceof FileStoreEditorInput) {
			IPath path = URIUtil.toPath(((FileStoreEditorInput) input)
					.getURI());
			return path;
		}
			return null;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.util.ResourceUtilSSHelper#workspacePathToSysPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IPath workspacePathToSysPath(IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	IWorkspaceRoot root = workspace.getRoot();
    	IResource resource = root.findMember(path);
    	if(resource != null)
    		return resource.getLocation();
    	else
    		 return null;    	
	}
	
	

	/**Get the IFile from IPath.
	 * @param path Path to file in workspace
	 * @return the IFile. <code>null</code> if no IFile on the path, file does not exist, internal error.
	 */
	public static IFile getIFileFromIPath(final IPath path)
	{
	    try
	    {
    		final IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(
    				path, false);
    		if (r!= null && r instanceof IFile)
		    {
    		    final IFile file = (IFile) r;
    		    if (file.exists())
    		        return file;
		    }
	    }
	    catch (Exception ex)
	    {
	        // Ignored
	    }
	    return null;
	}
	
	/**Get screenshot image from GraphicalViewer
	 * @param viewer the GraphicalViewer
	 * @return the screenshot image
	 */
	public static Image getScreenshotImage(GraphicalViewer viewer){
		LayerManager lm = (LayerManager)viewer.getEditPartRegistry().get(LayerManager.ID);
		IFigure f = lm.getLayer(LayerConstants.PRIMARY_LAYER);

		Rectangle bounds = f.getBounds();
		Image image = new Image(null, bounds.width + 6, bounds.height + 6);
		GC gc = new GC(image);
		SWTGraphics graphics = new SWTGraphics(gc);
		graphics.translate(-bounds.x + 3, -bounds.y + 3);
		graphics.setBackgroundColor(viewer.getControl().getBackground());
		graphics.fillRectangle(bounds);
		f.paint(graphics);
		gc.dispose();

		return image;
	}

	@SuppressWarnings("nls")
    public static String getScreenshotFile(GraphicalViewer viewer) throws Exception{
		File file;
		 // Get name for snapshot file
        try
        {
            file = File.createTempFile("opi", ".png"); //$NON-NLS-1$ //$NON-NLS-2$
            file.deleteOnExit();
        }
        catch (Exception ex)
        {
            throw new Exception("Cannot create tmp. file:\n" + ex.getMessage());
        }

        // Create snapshot file
        try
        {
            final ImageLoader loader = new ImageLoader();

            final Image image = getScreenshotImage(viewer);
            loader.data = new ImageData[]{image.getImageData()};
            image.dispose();
            loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
        }
        catch (Exception ex)
        {
            throw new Exception(
                    NLS.bind("Cannot create snapshot in {0}:\n{1}",
                            file.getAbsolutePath(), ex.getMessage()));
        }
        return file.getAbsolutePath();
    }
}