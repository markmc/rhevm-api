/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.common.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.model.Iso;
import com.redhat.rhevm.api.model.Isos;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.Snapshot;
import com.redhat.rhevm.api.model.Snapshots;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.resource.ClustersResource;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.resource.DeviceResource;
import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;
import com.redhat.rhevm.api.resource.IsoResource;
import com.redhat.rhevm.api.resource.IsosResource;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.resource.NetworksResource;
import com.redhat.rhevm.api.resource.SnapshotResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;

/**
 * Contains static methods related to Link addition.
 */
public class LinkHelper {

    private static Map<Class<? extends BaseResource>, ResourceType> TYPES =
        new HashMap<Class<? extends BaseResource>, ResourceType>();

    static {
        TYPES.put(Attachment.class,    new ResourceType(AttachmentResource.class,    AttachmentsResource.class, StorageDomain.class));
        TYPES.put(CdRom.class,         new ResourceType(DeviceResource.class,        DevicesResource.class,     VM.class));
        TYPES.put(Cluster.class,       new ResourceType(ClusterResource.class,       ClustersResource.class));
        TYPES.put(DataCenter.class,    new ResourceType(DataCenterResource.class,    DataCentersResource.class));
        TYPES.put(Disk.class,          new ResourceType(DeviceResource.class,        DevicesResource.class,     VM.class));
        TYPES.put(Host.class,          new ResourceType(HostResource.class,          HostsResource.class));
        TYPES.put(Iso.class,           new ResourceType(IsoResource.class,           IsosResource.class,        DataCenter.class));
        TYPES.put(Network.class,       new ResourceType(NetworkResource.class,       NetworksResource.class));
        TYPES.put(NIC.class,           new ResourceType(DeviceResource.class,        DevicesResource.class,     VM.class));
        TYPES.put(Snapshot.class,      new ResourceType(SnapshotResource.class,      SnapshotsResource.class,   VM.class));
        TYPES.put(StorageDomain.class, new ResourceType(StorageDomainResource.class, StorageDomainsResource.class));
        TYPES.put(Template.class,      new ResourceType(TemplateResource.class,      TemplatesResource.class));
        TYPES.put(VM.class,            new ResourceType(VmResource.class,            VmsResource.class));
        TYPES.put(VmPool.class,        new ResourceType(VmPoolResource.class,        VmPoolsResource.class));
    }

    private static String getPath(Class<?> clz) {
        Path pathAnnotation = (Path)clz.getAnnotation(Path.class);

        String path = pathAnnotation.value();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    private static String getPath(Class<?> clz, Class<?> parent, Class<?> type) {
        for (Method method : parent.getMethods()) {
            if (method.getName().startsWith("get") &&
                clz.isAssignableFrom(method.getReturnType()) &&
                method.getName().toLowerCase().contains(type.getSimpleName().toLowerCase())) {
                Path pathAnnotation = (Path)method.getAnnotation(Path.class);
                return pathAnnotation.value();
            }
        }
        return null;
    }

    private static Collection<BaseResource> getInlineResources(Object obj) {
        ArrayList ret = new ArrayList();

        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().startsWith("get") &&
                BaseResource.class.isAssignableFrom(method.getReturnType())) {
                try {
                    BaseResource inline = (BaseResource)method.invoke(obj);
                    if (inline != null) {
                        ret.add(inline);
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple getter
                }
            }
        }

        return ret;
    }

    private static <R extends BaseResource> BaseResource getParentModel(R model, Class<?> parentType) {
        for (BaseResource inline : getInlineResources(model)) {
            if (parentType.isAssignableFrom(inline.getClass())) {
                return inline;
            }
        }
        return null;
    }

    public static <R extends BaseResource> UriBuilder getUriBuilder(R model) {
        ResourceType type = TYPES.get(model.getClass());

        UriBuilder uriBuilder;
        if (type.getParentType() != null) {
            String path = getPath(type.getCollectionType(),
                                  TYPES.get(type.getParentType()).getResourceType(),
                                  model.getClass());
            BaseResource parent = getParentModel(model, type.getParentType());
            if (parent == null) {
                return null;
            }
            uriBuilder = getUriBuilder(parent).path(path);
        } else {
            String path = getPath(type.getCollectionType());
            uriBuilder = UriBuilder.fromPath(path);
        }

        return uriBuilder.path(model.getId());
    }

    private static <R extends BaseResource> void setHref(R model) {
        UriBuilder uriBuilder = getUriBuilder(model);
        if (uriBuilder != null) {
            model.setHref(uriBuilder.build().toString());
        }
    }

    private static <R extends BaseResource> void setActions(R model) {
        ResourceType type = TYPES.get(model.getClass());
        UriBuilder uriBuilder = getUriBuilder(model);
        if (uriBuilder != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, type.getResourceType());
            model.setActions(actionsBuilder.build());
        }
    }

    public static <R extends BaseResource> R addLinks(R model) {
        setHref(model);
        setActions(model);

        for (BaseResource inline : getInlineResources(model)) {
            if (inline.getId() != null) {
                setHref(inline);
            }
        }

        return model;
    }

    private static class ResourceType {
        private final Class<?> resourceType;
        private final Class<?> collectionType;
        private final Class<?> parentType;

        public ResourceType(Class<?> resourceType, Class<?> collectionType, Class<?> parentType) {
            this.resourceType = resourceType;
            this.collectionType = collectionType;
            this.parentType = parentType;
        }
        public ResourceType(Class<?> resourceType, Class<?> collectionType) {
            this(resourceType, collectionType, null);
        }

        public Class<?> getResourceType()   { return resourceType; }
        public Class<?> getCollectionType() { return collectionType; }
        public Class<?> getParentType()     { return parentType; }
    }
}
