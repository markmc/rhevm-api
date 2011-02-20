#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhevsh.format.format import Formatter
from rhevsh.format.xml_ import XmlFormatter
from rhevsh.format.text import TextFormatter


def get_formatter(format):
    """Return the formatter class for `format', or None if it doesn't exist."""
    for sym in globals():
        obj = globals()[sym]
        if isinstance(obj, type) and issubclass(obj, Formatter) \
                and obj.name == format:
            return obj
