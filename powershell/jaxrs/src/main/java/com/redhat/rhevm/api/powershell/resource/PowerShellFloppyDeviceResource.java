package com.redhat.rhevm.api.powershell.resource;

import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.resource.DeviceResource;

public class PowerShellFloppyDeviceResource
    extends PowerShellDeviceResource<Floppy, Floppies>
    implements DeviceResource<Floppy> {

    public PowerShellFloppyDeviceResource(PowerShellFloppiesResource parent, String deviceId) {
        super(parent, deviceId);
    }

    @Override
    public Floppy update(Floppy floppy) {
        return floppy.isSetFile() && floppy.getFile().isSetId()
               ? ((PowerShellFloppiesResource)parent).updateFloppy(floppy.getFile().getId())
               : get();
    }
}
