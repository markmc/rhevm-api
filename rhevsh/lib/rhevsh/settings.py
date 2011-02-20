#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import textwrap
from cli.settings import Settings, enum, boolean


class RhevshSettings(Settings):

    name = 'rhevsh'
    validators = Settings.validators + [
        ('url', str),
        ('username', str),
        ('password', str),
        ('verbose', boolean),
        ('input_format', enum('xml')),
        ('output_format', enum('xml', 'text')),
        ('wide', boolean),
        ('header', boolean),
        ('fields', str),
        ('fields.*', str)
    ]
    defaults = Settings.defaults.copy()
    defaults.update({
        'url': '',
        'username': '',
        'password': '',
        'verbose': False,
        'input_format': 'xml',
        'output_format': 'text',
        'wide': False,
        'header': True,
        'fields': 'name,id,status'
    })
    example = textwrap.dedent("""\
        [main]
        #url = <url>
        #username = <username>
        #password = <password>
        #input_format = %(input_format)s
        #output_format = %(output_format)s
        #wide = %(wide)s
        #header = %(header)s
        #fields = %(fields)s
    """) % defaults
