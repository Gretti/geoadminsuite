package org.geogurus.data;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.Map;

/**
 * A specialization of the Factory class for creating DataAccessObjects
 * 
 * @author jesse
 */
public abstract class AbstractDataAccessFactory implements
        Factory<List<DataAccess>, ConnectionParams> {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * A methods for creating {@link ConnectionParams} from a MapFile layer. The
     * parameters create can be used by this factory to create a DataAccess
     * object
     * 
     * @param mapfile
     *            the MapFile object
     * @param layer
     *            the layer
     * @param owner
     *            the Datasource that the new DataAccess object will be part of
     * 
     * @return {@link ConnectionParams} for use by this object or null if this
     *         DataAccess does not make sense for the layer.
     */
    public abstract Option<ConnectionParams> createConnectionParameters(Map mapfile,
            Layer layer, Datasource owner);

    /**
     * Calls createOne and returns the result as a singletonList
     */
    public List<DataAccess> create(ConnectionParams params) {
        return Collections.singletonList(createOne(params));
    }

    /**
     * If the params only create a single DataSource then implement this method.
     * If not return null and override {@link #create(ConnectionParams)}
     */
    public abstract DataAccess createOne(ConnectionParams params);

}