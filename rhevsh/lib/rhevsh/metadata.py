#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhev import schema
from rhevsh.field import *


_mapping_data = {
    schema.BaseResource: [
        StringField('id', 'A unique ID for this object', 'S'),
        StringField('name', 'A unique name for this object', 'SL'),
        StringField('description', 'A textual description', 'S'),
        StringField('type', 'The object type', 'SL'),
        StringField('status', 'The object status', 'SL')
    ],
    schema.DataCenter: [
        StringField('id', 'A unique ID for this datacenter', 'S'),
        StringField('name', 'The name of this datacenter', 'SLUC'),
        StringField('description', 'A description for this datacenter', 'SUC'),
        StringField('status', 'The status of this datacenter', 'SL'),
        StringField('storage_type', 'The type of storage for this datacenter', 'SLC'),
        VersionField('version', 'The current compatibility version', 'SLC'),
        VersionListField('supported_versions', 'Available compatiblity version', 'S')
    ],
    schema.VM: [
        StringField('id', 'The unique ID for this VM', 'S'),
        StringField('name', 'A unique name for this VM', 'CULS'),
        StringField('description', 'A textual description', 'CUS'),
        StringField('status', 'The VM status', 'LS'),
        IntegerField('memory', 'Memory size in MiB', 'CUS', scale=1024**2),
        StringField('os', 'The operating system', 'CUS', attribute='os.type'),
        BooleanField('ha', 'Restart this VM in case it fails', 'CUS',
                     attribute='highly_available'),
        StringField('display', 'The display type', 'CUS',
                  attribute='display.type'),
        IntegerField('monitors', 'The number of monitors', 'CUS',
                     attribute='display.monitors', min=1, max=4),
        BooleanField('stateless', 'Do not save state', 'CUS'),
        ReferenceField('template', 'The template this VM is based on', 'CS'),
        ReferenceField('cluster', 'The cluster this VM resides in', 'CUS')
    ],
    schema.Action:  [
        StringField('status', 'The action status', 'S'),
        BooleanField('pause', 'Start the VM in paused mode', 'C',
                     scope='VM:start'),
        ReferenceField('host', 'Start the VM on this host', 'C',
                       scope='VM:start', attribute='vm.host'),
        BooleanField('stateless', 'Start the VM in stateless mode', 'C',
                     scope='VM:start', attribute='vm.stateless'),
        StringField('display', 'Display protocol', 'C',
                     scope='VM:start', attribute='vm.display.type'),
        StringField('ticket', 'The ticket value', 'S',
                     scope='VM:ticket', attribute='ticket.value_'),
        IntegerField('expiry', 'The ticket expiration time', 'SC',
                     scope='VM:ticket', attribute='ticket.expiry')
    ],
    schema.Disk: [
        StringField('interface', 'The disk interface ("IDE" or "VIRTIO")', 'CLS'),
        IntegerField('size', 'The disk size in MiB', 'CLS', scale=1024**2),
        StringField('format', 'The disk format ("COW" or "RAW")', 'CLS'),
        StringField('status', 'The disk status', 'LS'),
        StringField('type', 'The disk type', 'CS'),
        BooleanField('sparse', 'This is a sparse disk', 'CS'),
        BooleanField('bootable', 'This is a bootable disk', 'CS'),
        BooleanField('wipe', 'This is a bootable disk', 'CS',
                     attribute='wipe_after_delete'),
        BooleanField('errors', 'Propagate errors', 'CS',
                     attribute='propagate_errors')
    ],
    schema.Statistic: [
        StringField('id', 'A unique ID for this statistic', 'S'),
        StringField('name', 'The statistic name', 'SL'),
        StringField('description', 'A description for this statistic', 'S'),
        StringField('type', 'The statistic type', 'SL'),
        StatisticValueField('value', 'The current value', 'SL',
                            attribute='values'),
        StringField('unit', 'The unit for this statistic', 'SL')
    ]
}

def get_fields(typ, flags, scope=None):
    """Return the list of fields for a type/action."""
    if typ not in _mapping_data:
        return _mapping_data[schema.BaseResource]
    fields = _mapping_data[typ]
    for flag in flags:
        fields = filter(lambda f: flag in f.flags, fields)
    fields = filter(lambda f: f.scope in (None, scope) , fields)
    return fields
