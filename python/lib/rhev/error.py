#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

class Error(Exception):
    """RHEV error"""
    
    def __init__(self, message='', **kwargs):
        super(Error, self).__init__(message)
        for kw in kwargs:
            setattr(self, kw, kwargs[kw])
