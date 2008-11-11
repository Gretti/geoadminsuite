/**
 * 
 */
package org.geogurus.data.files;

import java.io.File;
import java.io.IOException;

import org.geogurus.data.ConnectionParams;
import org.geogurus.data.DataAccessHelper;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Datasource;
import org.geogurus.data.GTDataStoreDataAccess;
import org.geogurus.data.Option;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Access a Shapefile
 * 
 * @author jesse
 */
public class ShpDataAccess extends GTDataStoreDataAccess {

	private static final long serialVersionUID = 1L;
	private File file;

	public ShpDataAccess(String name, String fullpath, Datasource owner) {
		super(name, owner, DataAccessType.SHP);

		file = new File(fullpath);
	}

	protected synchronized Option<FeatureSource<SimpleFeatureType,SimpleFeature>> createFeatureSource() {
		try {
			ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI()
					.toURL());
			String typename = dataStore.getTypeNames()[0];
			return Option.some(dataStore.getFeatureSource(typename));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Option.none();
	}

	@Override
	protected Layer createMSLayerInner(RGB color) {
		return DataAccessHelper.createMapServerLayer(geomTypeCode, file, color);
	}

	protected String getFileName() {
		return file.getAbsolutePath();
	}

	@Override
	public String getConnectionURI() {
		return "file://" + getFileName();
	}

	@Override
	public <T> Option<T> doGet(Class<T> request) {
		if (File.class.isAssignableFrom(request)) {
			return Option.some(request.cast(file));
		}
		return Option.none();
	}

	@Override
	public ConnectionParams getConnectionParams() {
		ConnectionParams params = new ConnectionParams(owner);
		params.name = this.name;
		params.path = file.getAbsolutePath();
		params.type = datasourceType.name();
		return params;
	}
}
