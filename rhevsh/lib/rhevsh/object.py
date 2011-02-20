#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.


def create(cls, *args, **kwargs):
    """Create rhevsh objects."""
    from rhevsh.format import Formatter, get_formatter
    if issubclass(cls, Formatter):
        format = args[0]
        cls = get_formatter(format)
        obj = cls(**kwargs)
    else:
        obj = cls(*args, **kwargs)
    return obj
