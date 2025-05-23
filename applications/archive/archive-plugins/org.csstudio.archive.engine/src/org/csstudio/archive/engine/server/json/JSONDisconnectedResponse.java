/*******************************************************************************
 * Copyright (c) 2017 Science & Technology Facilities Council.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.engine.server.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.csstudio.archive.engine.Messages;
import org.csstudio.archive.engine.model.ArchiveChannel;
import org.csstudio.archive.engine.model.ArchiveGroup;
import org.csstudio.archive.engine.model.EngineModel;
import org.csstudio.archive.engine.server.AbstractResponse;

import java.util.Iterator;

/** Provide web page with list of disconnected channels in JSON
 *  @author Dominic Oram
 */
@SuppressWarnings("nls")
public class JSONDisconnectedResponse extends AbstractResponse
{
    public JSONDisconnectedResponse(final EngineModel model)
    {
        super(model);
    }

    @Override
    public void fillResponse(final HttpServletRequest req,
                    final HttpServletResponse resp) throws Exception
    {
        final JSONRoot json = new JSONRoot(resp);
        JSONList disconnected = new JSONList();
		
		final Iterator<ArchiveGroup> groupsIter = model.getAllGroupsIter();

		while (groupsIter.hasNext())
		{
			final ArchiveGroup group = groupsIter.next();
			final Iterator<ArchiveChannel> channelsIter = group.getAllChannelsIter();
			while (channelsIter.hasNext())
			{
				final ArchiveChannel channel = channelsIter.next();
				if (channel.isConnected())
					continue;

				JSONObject JSONchannel = new JSONObject();

				JSONchannel.writeObjectEntry(Messages.HTTP_Channel, channel.getName());
				JSONchannel.writeObjectEntry(Messages.HTTP_Group, group.getName());
				disconnected.addObjectToList(JSONchannel);

			}
		}
		json.writeObjectEntry(Messages.HTTP_DisconnectedTitle, disconnected);
		json.close();
    }
}
