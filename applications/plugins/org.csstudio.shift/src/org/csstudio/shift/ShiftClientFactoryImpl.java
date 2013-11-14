package org.csstudio.shift;


import static gov.bnl.shiftClient.ShiftApiClientImpl.ShiftClientBuilder.*;
import gov.bnl.shiftClient.ShiftClientCreator;

import org.csstudio.utility.shift.PreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;


public class ShiftClientFactoryImpl implements ShiftClientFactory {
	
    @Override
    public ShiftClient getClient() throws Exception {
	return new ShiftClientImpl(ShiftClientCreator.getClient());
    }

    @Override
    public ShiftClient getClient(String username, String password) throws Exception {
    	final IPreferencesService prefs = Platform.getPreferencesService();
    	final String url = prefs.getString(org.csstudio.utility.shift.Activator.PLUGIN_ID,
			PreferenceConstants.Shift_URL,
			"https://localhost:8181/Shift/resources", null);
    	return new ShiftClientImpl(serviceURL(url).withHTTPAuthentication(true).username(username)
    			.password(password).create());
    }
}
