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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Iso;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Snapshot;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.AssignedNetworkResource;
import com.redhat.rhevm.api.resource.AssignedNetworksResource;
import com.redhat.rhevm.api.resource.AssignedTagResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.resource.ClustersResource;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.resource.DeviceResource;
import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.RolesResource;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;
import com.redhat.rhevm.api.resource.HostStorageResource;
import com.redhat.rhevm.api.resource.HostNicResource;
import com.redhat.rhevm.api.resource.HostNicsResource;
import com.redhat.rhevm.api.resource.IsoResource;
import com.redhat.rhevm.api.resource.IsosResource;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.resource.NetworksResource;
import com.redhat.rhevm.api.resource.RoleResource;
import com.redhat.rhevm.api.resource.AssignedRolesResource;
import com.redhat.rhevm.api.resource.SnapshotResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;
import com.redhat.rhevm.api.resource.StorageResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.resource.TagResource;
import com.redhat.rhevm.api.resource.TagsResource;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.resource.UsersResource;
import com.redhat.rhevm.api.resource.AttachedUsersResource;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;

/**
 * Contains static methods related to Link addition.
 */
public class LinkHelper {

    private static Map<Class<? extends BaseResource>, Collection> TYPES =
        new HashMap<Class<? extends BaseResource>, Collection>();

    static {
        TYPES.put(Attachment.class,    new Collection(AttachmentResource.class,      AttachmentsResource.class,      StorageDomain.class));
        TYPES.put(CdRom.class,         new Collection(DeviceResource.class,          DevicesResource.class,          VM.class));
        TYPES.put(Cluster.class,       new Collection(ClusterResource.class,         ClustersResource.class));
        TYPES.put(DataCenter.class,    new Collection(DataCenterResource.class,      DataCentersResource.class));
        TYPES.put(Disk.class,          new Collection(DeviceResource.class,          DevicesResource.class,          VM.class));
        TYPES.put(Host.class,          new Collection(HostResource.class,            HostsResource.class));
        TYPES.put(HostNIC.class,       new Collection(HostNicResource.class,         HostNicsResource.class,         Host.class));
        TYPES.put(Iso.class,           new Collection(IsoResource.class,             IsosResource.class,             DataCenter.class));
        TYPES.put(Network.class,       new Collection(AssignedNetworkResource.class, AssignedNetworksResource.class, Cluster.class, NetworksResource.class));
        TYPES.put(NIC.class,           new Collection(DeviceResource.class,          DevicesResource.class,          VM.class));
        TYPES.put(Role.class,          new Collection(RoleResource.class,            AssignedRolesResource.class,    User.class,    RolesResource.class));
        TYPES.put(Snapshot.class,      new Collection(SnapshotResource.class,        SnapshotsResource.class,        VM.class));
        TYPES.put(Storage.class,       new Collection(StorageResource.class,         HostStorageResource.class,      Host.class));
        TYPES.put(StorageDomain.class, new Collection(StorageDomainResource.class,   StorageDomainsResource.class));
        TYPES.put(Tag.class,           new Collection(AssignedTagResource.class,     AssignedTagsResource.class,     VM.class,      TagsResource.class));
        TYPES.put(Template.class,      new Collection(TemplateResource.class,        TemplatesResource.class));
        // REVISIT: will need the concept of multiple parent types, both VM and VmPool for User
        TYPES.put(User.class,          new Collection(UserResource.class,            AttachedUsersResource.class,    VM.class,      UsersResource.class));
        TYPES.put(VM.class,            new Collection(VmResource.class,              VmsResource.class));
        TYPES.put(VmPool.class,        new Collection(VmPoolResource.class,          VmPoolsResource.class));
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

    private static List<BaseResource> getInlineResources(Object obj) {
        ArrayList<BaseResource> ret = new ArrayList<BaseResource>();

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
        Collection type = TYPES.get(model.getClass());

        UriBuilder uriBuilder;
        if (type.getParentType() != null) {
            String path = getPath(type.getCollectionType(),
                                  TYPES.get(type.getParentType()).getResourceType(),
                                  model.getClass());
            BaseResource parent = getParentModel(model, type.getParentType());
            if (parent == null) {
                if (type.getAltCollectionType() != null) {
                    path = getPath(type.getAltCollectionType());
                    uriBuilder = UriBuilder.fromPath(path);
                } else {
                    return null;
                }
            } else {
                uriBuilder = getUriBuilder(parent).path(path);
            }
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
        Collection type = TYPES.get(model.getClass());
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

    private static class Collection {
        private final Class<?> resourceType;
        private final Class<?> collectionType;
        private final Class<?> parentType;
        private final Class<?> alternativeCollectionType;

        public Collection(Class<?> resourceType, Class<?> collectionType, Class<?> parentType, Class<?> alternativeCollectionType) {
            this.resourceType = resourceType;
            this.collectionType = collectionType;
            this.parentType = parentType;
            this.alternativeCollectionType = alternativeCollectionType;
        }
        public Collection(Class<?> resourceType, Class<?> collectionType, Class<?> parentType) {
            this(resourceType, collectionType, parentType, null);
        }
        public Collection(Class<?> resourceType, Class<?> collectionType) {
            this(resourceType, collectionType, null);
        }

        public Class<?> getResourceType()      { return resourceType; }
        public Class<?> getCollectionType()    { return collectionType; }
        public Class<?> getParentType()        { return parentType; }
        public Class<?> getAltCollectionType() { return alternativeCollectionType; }
    }
}
