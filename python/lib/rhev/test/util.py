#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import re
import sys
import random
import struct
import socket
import logging


def setup_logging(debug):
    """Set up logging."""
    if debug:
        level = logging.DEBUG
    else:
        level = logging.INFO
    logger = logging.getLogger()
    handler = logging.StreamHandler(sys.stdout)
    format = '%(levelname)s: %(message)s'
    formatter = logging.Formatter(format)
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(level)


class store(object):
    """key/value store."""

    def __init__(self, **kwargs):
        for kw in kwargs:
            setattr(self, kw, kwargs[kw])


_re_uuid = re.compile('^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$')
_re_int = re.compile('^-?[0-9]+$')
_re_float = re.compile('^-?[0-9]+(\\.[0-9]+([eE][0-9]+)?)?$')
_re_ip = re.compile('^(\d|\d\d|[01]\d\d|2[0-4]\d|25[0-5])'
                    '(\.(\d|\d\d|[01]\d\d|2[0-4]\d|25[0-5])){3}$')
_re_host = re.compile('^[a-z0-9-]+(\.[a-z0-9-]+)+$', re.I)
_re_bool = re.compile('^(true|false)$', re.I)
_re_mac = re.compile('^[0-9a-f]{2}(:[0-9a-f]{2}){5}$', re.I)
_re_href = re.compile('^/[a-z0-9_-]*api[a-z0-9_-]*(/[a-z0-9_-]+)+$', re.I)
_re_path = re.compile('^(/[a-z0-9_-]+)+$', re.I)
_re_date = re.compile('^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(.\d{3})?[a-z]+$', re.I)

def is_str(s):
    return isinstance(s, str) or isinstance(s, unicode)

def is_str_uuid(s):
    return isinstance(s, basestring) and bool(_re_uuid.match(s))

def is_str_int(s):
    return isinstance(s, basestring) and bool(_re_int.match(s))

def is_str_float(s):
    return isinstance(s, basestring) and bool(_re_float.match(s))

def is_str_ip(s):
    return isinstance(s, basestring) and bool(_re_ip.match(s))

def is_str_host(s):
    return isinstance(s, basestring) and \
            (bool(_re_ip.match(s)) or bool(_re_host.match(s)))

def is_str_bool(s):
    return isinstance(s, basestring) and bool(_re_bool.match(s))

def is_str_mac(s):
    return isinstance(s, basestring) and bool(_re_mac.match(s))

def is_str_href(s):
    return isinstance(s, basestring) and bool(_re_href.match(s))

def is_str_path(s):
    return isinstance(s, basestring) and bool(_re_path.match(s))

def is_str_date(s):
    return isinstance(s, basestring) and bool(_re_date.match(s))

def is_int(i):
    return isinstance(i, int) or isinstance(i, long)

def is_float(f):
    return isinstance(f, float)

def is_bool(b):
    return isinstance(b, bool) or isinstance(b, int)


def random_name(id='obj'):
    return 'tst-%s-%06d' % (id, random.randint(0, 1000000))

def contains_id(list, id):
    for el in list:
        if el.id == id:
            return True
    return False

def ip_from_net(address, netmask, seq):
    addr = struct.unpack('!L', socket.inet_aton(address))[0]
    mask = struct.unpack('!L', socket.inet_aton(netmask))[0]
    ip = socket.inet_ntoa(struct.pack('!L', (addr & mask) + seq))
    return ip
