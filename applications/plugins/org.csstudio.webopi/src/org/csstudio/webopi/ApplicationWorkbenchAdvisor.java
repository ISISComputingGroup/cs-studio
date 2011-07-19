package org.csstudio.webopi;

import javax.servlet.http.HttpServletRequest;

import org.csstudio.opibuilder.runmode.OPIRunnerPerspective;
import org.csstudio.opibuilder.runmode.StandaloneRuntimePerspective;
import org.eclipse.rwt.RWT;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		HttpServletRequest request = RWT.getRequest();
		 String opiPath = request.getParameter( "mode" );
		 if(opiPath!=null && opiPath.equals("standalone"))
			 return StandaloneRuntimePerspective.ID;
		return OPIRunnerPerspective.ID;
	}
}
