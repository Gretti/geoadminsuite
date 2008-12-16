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

package org.geogurus.data.webservices;

import org.geogurus.data.AbstractDataAccessFactory;
import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccess;
import org.geogurus.data.Datasource;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;

/**
 * Creates {@link WmsDataAccess} objects
 * 
 * @author jesse
 */
public class WmsAccessFactory extends AbstractDataAccessFactory {

    public boolean canCreateFrom(ConnectionParams host) {
        return "wms".equalsIgnoreCase(host.type);
    }

    public DataAccess createOne(ConnectionParams bean) {
        DataAccess wms = new WmsDataAccess(bean.host, bean.layer, bean.owner);
        return wms;
    }

    public Option<ConnectionParams> createConnectionParameters(Map map,
            Layer layer, Datasource owner) {
        // COMPLETE so wms layers can be loaded from a MapFile
        return Option.none();
    }

}
