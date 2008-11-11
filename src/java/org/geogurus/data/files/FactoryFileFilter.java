package org.geogurus.data.files;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Accept all files that are accepted by one (or more) of the Factories
 * 
 * @author jesse
 */
public class FactoryFileFilter implements FileFilter {

	private final List<AbstractFileAccessFactory> factories;

	/**
	 * Creates a new instance of type FactoryFileFilter
	 *
	 * @param factories
	 */
	public FactoryFileFilter(List<? extends AbstractFileAccessFactory> factories) {
		List<AbstractFileAccessFactory> fileFactories = new ArrayList<AbstractFileAccessFactory>();
		
		for (AbstractFileAccessFactory factoryStrategy : factories) {
				fileFactories.add(factoryStrategy);
		}
		
		this.factories = Collections.unmodifiableList(fileFactories);
	}

	public boolean accept(File file) {
		for (AbstractFileAccessFactory factory : factories) {
			if( factory.canCreateFrom(file)){
				return true;
			}
		}
		return false;
	}

}
