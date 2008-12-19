/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package org.geogurus.gas.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geogurus.data.Datasource;
import org.geogurus.data.Factory;
import org.geogurus.data.webservices.WfsDatasource;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 * Strategy for processing WFS into Datasources
 * 
 * @author jeichar
 */
public final class WfsFactoryStrategy implements
        Factory<List<Datasource>, HostDescriptorBean> {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    public boolean canCreateFrom(HostDescriptorBean host) {
        return "WFS".equalsIgnoreCase(host.getType());
    }

    public List<Datasource> create(HostDescriptorBean host) {
        List<Datasource> res = new ArrayList<Datasource>();
        WfsDatasource ds = new WfsDatasource(host.getPath(), host.getName());
        if (ds.load()) {
            // a valid datasource containing geo data: either file or geo tables
            res.add(ds);
            logger.fine("datasource added, contains: "
                    + ds.getDataList().size());
        } else {
            logger.warning("getDataInformation failed on datasource: "
                    + host.getName() + "\n\tmessage is: " + ds.errorMessage);
        }
        return res;
    }

}
