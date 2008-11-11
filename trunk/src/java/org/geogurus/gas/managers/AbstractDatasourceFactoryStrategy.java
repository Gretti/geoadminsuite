package org.geogurus.gas.managers;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.geogurus.data.Datasource;
import org.geogurus.data.Factory;
import org.geogurus.gas.objects.HostDescriptorBean;

/**
 * @author jesse
 * 
 */
public abstract class AbstractDatasourceFactoryStrategy implements
        Factory<List<Datasource>, HostDescriptorBean> {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Creates a HostDescriptorBean for the file so that a <T> can be created by
     * calling "create". The values in the template will be copied to the new
     * bean and recurse, path and type will be overridden.
     * 
     * @param file
     *            the file to use as path of the bean
     * @param template
     *            the template object to copy parameters from. It may be null.
     * @param recurse
     *            whether or not to recurse into contained files (if it applies)
     * @return a bean useful for creating a <T>.
     */
    public HostDescriptorBean fileToBean(File file,
            HostDescriptorBean template, boolean recurse) {
        HostDescriptorBean bean = new HostDescriptorBean();
        bean.setType(getTypeString());
        bean.setPath(file.getAbsolutePath());
        bean.setRecurse(String.valueOf(recurse));

        if (template != null) {
            bean.setInstance(template.getInstance());
            bean.setName(template.getName());
            bean.setPort(template.getPort());
            bean.setUname(template.getUname());
            bean.setUpwd(template.getUpwd());
        }
        return bean;
    }

    /**
     * Returns a string that can be used as the "type" indicator for the type.
     * Maybe we will pull to the super class.
     * 
     * @return the type identifier string
     */
    protected abstract String getTypeString();

    public boolean canCreateFrom(HostDescriptorBean host) {
        File file = new File(host.getPath());
        return host.getType().equalsIgnoreCase(getTypeString())
                && canCreateFrom(file);
    }

    protected abstract boolean canCreateFrom(File file);

}
