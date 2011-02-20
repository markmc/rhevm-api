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

class ParseError(Error):
    """Parse error"""

class ConnectionError(Error):
    """Could not connect to the API."""

class RemoteError(Error):
    """Remote error"""

class IllegalMethod(RemoteError):
    """The method is not supported on the collection or resource provided."""

class NotFound(RemoteError):
    """The collection or resource was not found."""

class IllegalAction(RemoteError):
    """The action is not supported on the resource provided."""

class ResponseError(RemoteError):
    """Got a response but it's not what we're expecting."""

class Fault(RemoteError):
    """The API returned a Fault."""

class ActionError(RemoteError):
    """An action failed."""
