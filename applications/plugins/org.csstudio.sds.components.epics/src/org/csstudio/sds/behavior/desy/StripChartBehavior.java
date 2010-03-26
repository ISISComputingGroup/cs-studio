/*
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron,
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
 */
/*
 * $Id$
 */
package org.csstudio.sds.behavior.desy;

import org.csstudio.sds.components.model.StripChartModel;
import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.BorderStyleEnum;
import org.epics.css.dal.context.ConnectionState;
import org.epics.css.dal.simple.AnyData;
import org.epics.css.dal.simple.MetaData;

/**
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 26.03.2010
 */
public class StripChartBehavior extends AbstractDesyBehavior<StripChartModel> {

    @Override
    protected String[] doGetInvisiblePropertyIds() {
        return new String[] { AbstractWidgetModel.PROP_NAME, StripChartModel.PROP_ACTIONDATA,
                StripChartModel.PROP_CURSOR, StripChartModel.PROP_BORDER_COLOR,
                StripChartModel.PROP_BORDER_STYLE, StripChartModel.PROP_BORDER_WIDTH };
    }

    @Override
    protected void doInitialize(StripChartModel widget) {
        widget.setPropertyValue(StripChartModel.PROP_BORDER_STYLE, BorderStyleEnum.RAISED);
    }

    @Override
    protected void doProcessConnectionStateChange(StripChartModel widget,
            ConnectionState connectionState) {
    }

    @Override
    protected void doProcessMetaDataChange(StripChartModel widget, MetaData metaData) {
    }

    @Override
    protected void doProcessValueChange(StripChartModel model, AnyData anyData) {
    }

}
