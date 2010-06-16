/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 *
 * $Id$
 */
package org.csstudio.alarm.service.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.log4j.Logger;
import org.csstudio.alarm.service.declaration.AlarmPreference;
import org.csstudio.alarm.service.declaration.IAlarmResource;
import org.csstudio.platform.logging.CentralLogger;

/**
 * This is just a bag of parameters, which are necessary for the alarm service and alarm connection implementations.
 *
 * @author jpenning
 * @author $Author$
 * @version $Revision$
 * @since 16.06.2010
 */
public class AlarmResource implements IAlarmResource {
    private static final Logger LOG = CentralLogger.getInstance().getLogger(AlarmResource.class);

    private final List<String> _topics;
    private final List<String> _facilities;
    private final String _filepath;

    public AlarmResource(@CheckForNull final List<String> topics,
                         @CheckForNull final List<String> facilities,
                         @CheckForNull final String filepath) {
        _topics = topics == null ? AlarmPreference.getTopicNames() : topics;
        _facilities = facilities == null ? AlarmPreference.getFacilityNames() : facilities;

        String configFilename = null;
        try {
            configFilename = AlarmPreference.getConfigFilename();
        } catch (IOException e) {
            LOG.error("Error creating alarm resource. Could not retrieve configuration file name.", e);
            // TODO (jpenning) promote to caller?
        }
        _filepath = filepath == null ? configFilename : filepath;
    }

    public List<String> getTopics() {
        return Collections.unmodifiableList(_topics);
    }

    public List<String> getFacilities() {
        return Collections.unmodifiableList(_facilities);
    }

    public String getFilepath() {
        return _filepath;
    }

    @Override
    public String toString() {
        return "topics: " + _topics + ", facilities: " + _facilities + ", file: " + _filepath;
    }
}
