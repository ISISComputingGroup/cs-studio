/*
 * Copyright (c) 2006 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.css.dal.epics;

import gov.aps.jca.dbr.DBRType;

import org.epics.css.dal.RemoteException;

/**
 * Long sequence property proxy implementation.
 * @author msekoranja
 */
public class LongSeqPropertyProxyImpl extends PropertyProxyImpl {

	/**
	 * Constructor.
	 * @param plug plug handling this property.
	 * @param name property name.
	 * @throws RemoteException
	 */
	public LongSeqPropertyProxyImpl(EPICSPlug plug, String name)
			throws RemoteException {
		super(plug, name, long[].class, DBRType.INT);
	}

}
