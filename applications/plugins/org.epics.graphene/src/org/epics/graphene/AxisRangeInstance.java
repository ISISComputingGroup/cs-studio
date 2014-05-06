/**
 * Copyright (C) 2012-14 graphene developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.graphene;

/**
 *
 * @author carcassi
 */
public interface AxisRangeInstance {
    public Range axisRange(Range dataRange, Range displayRange);
    public AxisRange getAxisRange();
}
