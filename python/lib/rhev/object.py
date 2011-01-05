#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import os
from rhev.error import Error


def create(type, *args, **kwargs):
    """Create rhev-python objects."""
    from rhev import schema
    from rhev.connection import Connection
    if issubclass(type, Connection):
        for key in ('url', 'username', 'password'):
            if key not in kwargs:
                name = 'RHEV_%s' % key.upper()
                value = os.environ.get(name)
                if value is None:
                    raise Error, '$%s not set' % name
                kwargs[key] = value
        obj = type(*args, **kwargs)
    elif type in schema._type_mapping:
        obj = schema.new(type, *args, **kwargs)
    else:
        obj = type(*args, **kwargs)
    return obj

def singleton(type, *args, **kwargs):
    """Create a singleton instance for a type."""
    obj = create(type, *args, **kwargs)
    type.instance = obj
    return obj

def instance(type):
    """Return a singleton instance of a type."""
    if not hasattr(type, 'instance'):
        raise RuntimeError, 'Singleton not initialized.'
    return type.instance
