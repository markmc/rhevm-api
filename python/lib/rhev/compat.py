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


# Python 2.4 does not have ElementTree

try:
    from xml.etree import ElementTree as etree
except ImportError:
    from elementtree import ElementTree as etree


# Compatibility for ElementTree <= 1.2 (Python <= 2.6)

import re
_re_predicate = re.compile(r'\[@([a-z]+)="([a-z_]+)"\]', re.I)

def find(node, expr):
    version = map(int, etree.VERSION.split('.'))
    if version[:2] >= (1,3):
        return node.find(expr)
    # Very course emulation of XPath predicates for ElementTree 1.2
    # This only supports what we actually use.
    while True:
        match = _re_predicate.search(expr)
        if not match:
            break
        subexpr = expr[:match.start(0)]
        for elem in node.findall(subexpr):
            if elem.attrib.get(match.group(1)) == match.group(2):
                break
        else:
            return None
        expr = '.' + expr[match.end(0):]
        node = elem
    element = node.find(expr)
    return element
