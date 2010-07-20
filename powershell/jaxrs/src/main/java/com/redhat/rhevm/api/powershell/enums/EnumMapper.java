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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

public class EnumMapper {

    private final HashMap<String, Class<? extends Enum<?>>> mapping;

    {
        mapping = new HashMap<String, Class<? extends Enum<?>>>();
        mapping.put("RhevmCmd.HostSelectionAlgorithm",                      PowerShellHostSelectionAlgorithm.class);
        mapping.put("VdcCommon.BusinessEntities.BootSequence",              PowerShellBootSequence.class);
        mapping.put("VdcCommon.BusinessEntities.DiskType",                  PowerShellDiskType.class);
        mapping.put("VdcCommon.BusinessEntities.HypervisorType",            PowerShellHypervisorType.class);
        mapping.put("VdcCommon.BusinessEntities.ImageStatus",               PowerShellImageStatus.class);
        mapping.put("VdcCommon.BusinessEntities.NetworkStatus",             PowerShellNetworkStatus.class);
        mapping.put("VdcCommon.BusinessEntities.OperationMode",             PowerShellOperationMode.class);
        mapping.put("VdcCommon.BusinessEntities.OriginType",                PowerShellOriginType.class);
        mapping.put("VdcCommon.BusinessEntities.PropagateErrors",           PowerShellPropagateErrors.class);
        mapping.put("VdcCommon.BusinessEntities.StorageDomainSharedStatus", PowerShellStorageDomainSharedStatus.class);
        mapping.put("VdcCommon.BusinessEntities.StorageDomainStatus",       PowerShellStorageDomainStatus.class);
        mapping.put("VdcCommon.BusinessEntities.StorageType",               PowerShellStorageType.class);
        mapping.put("VdcCommon.BusinessEntities.UsbPolicy",                 PowerShellUsbPolicy.class);
        mapping.put("VdcCommon.BusinessEntities.VdsSpmStatus",              PowerShellVdsSpmStatus.class);
        mapping.put("VdcCommon.BusinessEntities.VmTemplateStatus",          PowerShellVmTemplateStatus.class);
        mapping.put("VdcCommon.BusinessEntities.VmType",                    PowerShellVmType.class);
        mapping.put("VdcCommon.BusinessEntities.VolumeFormat",              PowerShellVolumeFormat.class);
        mapping.put("VdcCommon.BusinessEntities.VolumeType",                PowerShellVolumeType.class);
    };

    public boolean isEnum(String type) {
        return mapping.containsKey(type);
    }

    public Enum<?> parseEnum(String type, Integer value) {
        Class<? extends Enum<?>> clz = mapping.get(type);
        Class<?>[] paramTypes = new Class<?>[] { int.class };
        try {
            return clz.cast(clz.getMethod("forValue", paramTypes).invoke(null, value));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing " + clz + " enum", e);
        }
    }
}
