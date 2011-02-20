#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.


class Formatter(object):
    """Base class for formatter objects."""

    name = None

    def format(self, context, result):
        raise NotImplementedError
