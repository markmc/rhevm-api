package com.redhat.rhevm.api.resteasy.yaml;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ho.yaml.wrapper.DefaultBeanWrapper;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class CustomBeanWrapper extends DefaultBeanWrapper
{
    private Set<String> extraKeys = new HashSet<String>();

    public CustomBeanWrapper(Class type) {
        super(type);
        try {
            for (PropertyDescriptor prop: Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (prop.getReadMethod() != null &&
                    prop.getWriteMethod() == null &&
                    prop.getPropertyType().isAssignableFrom(List.class)) {
                    extraKeys.add(prop.getName());
                }
            }
        } catch (IntrospectionException e) {
        }
    }

    @Override
    public Collection keys() {
        Collection<String> keys = super.keys();
        keys.addAll(extraKeys);
        return keys;
    }
}
