#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

def plural_noun(s):
    """Inflect a single noun to a plural."""
    # This is not a full inflection engine (see python-inflect for that).
    # This merely supports the relationship names we have defined.
    if s == 'host_storage':
        p = 'storage'
    elif s[-1] != 's':
        p = s + 's'
    else:
        p = None
    return p
