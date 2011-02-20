#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

# We try to keep all compatiblity related functions in this module, so
# that the main body of code can be relatively clean.

# Python 2.4 does not seem to understand that our collection type
# has a __setslice__ (via CollectionMinxin)

def set_slice(list, new):
    try:
        list[:] = new
    except TypeError:
        list.__setslice__(0, len(list), new)


# In Python 2.4, the result of urlparse() was a plain tuple. In 2.5, it
# became a hybrid that also allows its elements to be accessed by the.
# attribute name.

from urlparse import urlparse as _urlparse

class ParsedUrl(object):
    """urlparse() result for compatiblity with Python 2.4."""

    def __init__(self, parsed):
        self.scheme = parsed[0]
        self.netloc = parsed[1]
        self.path = parsed[2]

def urlparse(url):
    parsed = _urlparse(url)
    if not hasattr(parsed, 'scheme'):
        parsed = ParsedUrl(parsed)
    return parsed


# In Python 2.4, Exception is not derived from object, but we'd like to use
# super() on it.

_super = super

def super(type, self):
    if issubclass(type, object):
        return _super(type, self)
    class proxy(object):
        def __init__(self, type, obj):
            object.__setattr__(self, '__type__', type)
            object.__setattr__(self, '__obj__', obj)
        def __getattribute__(self, name):
            def bind(func, self):
                def _f(*args):
                    func(self, *args)
                return _f
            type = object.__getattribute__(self, '__type__')
            obj = object.__getattribute__(self, '__obj__')
            return bind(getattr(type, name), obj)
    base = type.__bases__[0]
    return proxy(base, self)
