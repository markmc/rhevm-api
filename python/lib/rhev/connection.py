#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import re
import fnmatch
import logging
import socket
import httplib as http

from httplib import HTTPConnection, HTTPSConnection
from httplib import HTTPException
from urlparse import urlparse
from urllib import quote as urlencode
from rhev import schema
from rhev.error import Error


class Connection(object):
    """Access the RHEV API."""

    retries = 5
    verbosity = 1

    def __init__(self, url, username, password):
        """Constructor."""
        self.url = url
        self.username = username
        self.password = password
        self.scheme = None
        self.entrypoint = None
        self._logger = logging.getLogger('rhev.RhevConnection')
        self._parse_url()
        self._connection = None
        self._cache = {}
        self._api = None

    def _parse_url(self):
        """INTERNAL: Parse an URL into its components."""
        if self.url is None:
            raise Error, 'URL not set'
        parsed = urlparse(self.url)
        if parsed.netloc.count(':') == 1:
            host, port = parsed.netloc.split(':')
        elif parsed.scheme == 'http':
            host = parsed.netloc
            port = http.HTTP_PORT
        elif parsed.scheme == 'https':
            host = parsed.netloc
            port = http.HTTPS_PORT
        self.scheme = parsed.scheme
        self.host = host
        self.port = port
        self.entrypoint = parsed.path

    def connect(self):
        """Connect to the REST API. It is safe to call this method multiple
        times, only one connection will be made. It is not required to call
        this method. It will be called automatically when a connection is
        required."""
        if self._connection is not None:
            return
        self._connect()

    def _connect(self):
        """INTERNAL: Connect to the REST API."""
        if self.username is None or self.password is None:
            raise RuntimeError, 'RHEV username/password not set.'
        if self.scheme == 'https':
            factory = HTTPSConnection
        elif self.scheme == 'http':
            factory = HTTPConnection
        else:
            raise Error, 'Unsupported scheme: %s' % self.scheme
        self._connection = factory(self.host, self.port)
        self._logger.debug('connecting to RHEV-M at %s:%s' % (self.host, self.port))
        self._connection.connect()

    def close(self):
        """Close the connection to the API."""
        if self._connection is None:
            return
        self._connection.close()
        self._connection = None
        self._logger.debug('disconnected from RHEV-M')

    def _retry_request(self, method, url, headers, body):
        """INTERNAL: make a HTTP request, and retry if necessary."""
        if self._connection is None:
            self._connect()
        for i in range(self.retries):
            try:
                self._connection.request(method, url, body, headers)
                response = self._connection.getresponse()
                response.body = response.read()
            except (socket.error, HTTPException), e:
                self._logger.debug('Could not complete request, retry %d/%d' %
                                   (i+1, self.retries))
                try:
                    self._connection.close()
                except:
                    pass
                self._connect()
            else:
                break
        else:
            m = 'Could not complete request after %s retries.' % self.retries
            raise Error, m
        return response

    def _make_request(self, method, url, headers=None, body=None):
        """INTERNAL: perform a HTTP request to the API."""
        if headers is None:
            headers = {}
        if body is None:
            body = ''
        elif isinstance(body, unicode):
            body = body.encode('utf-8')
        authval = '%s:%s' % (self.username, self.password)
        authval = authval.encode('base64').rstrip()
        headers['Authorization'] = 'Basic %s' % authval
        headers['Accept'] = 'application/xml'
        headers['Content-Type'] = 'application/xml; charset=utf-8'
        headers['Accept-Charset'] = 'utf-8'
        ctype = headers['Content-Type']
        self._logger.debug('making request: %s %s (%s; %d bytes)'
                           % (method, url, ctype, len(body)))
        if self.verbosity > 5:
            self._logger.debug('request body: %s' % body)
        response = self._retry_request(method, url, headers, body)
        response.body = response.body.decode('utf-8')
        clen = response.getheader('Content-Length')
        if clen:
            length = '%s bytes' % clen
        else:
            length = response.getheader('Transfer-Encoding', 'unknown')
        ctype = response.getheader('Content-Type')
        self._logger.debug('got response: %s %s (%s; %s bytes)'
                           % (response.status, response.reason, ctype, clen))
        if self.verbosity > 5:
            self._logger.debug('response body: %s' % response.body)
        return response

    def _parse_xml(self, response):
        """INTERNAL: parse an XML response"""
        ctype = response.getheader('Content-Type')
        body = response.body
        if ctype not in ('application/xml', 'text/xml'):
            reason = 'Expecting an XML response (got: %s)' % ctype
            raise Error(reason, detail=body)
        try:
            parsed = schema._create_from_xml(body)
        except Exception, e:
            reason = 'Could not parse XML response: %s' % str(e)
            raise Error(reason, detail=body)
        parsed._connection = self
        return parsed

    def _join_path(self, *segments):
        path = []
        for seg in segments:
            path += [ s for s in seg.split('/') if s ]
        joined = '/' + '/'.join(path)
        # XXX: Jetty requires a trailing '/' to the entrypoint:
        if joined == self.entrypoint.rstrip('/'):
            joined += '/'
        return joined

    def _resolve_url(self, typ, base=None, id=None, search=None,
                     special=None, **query):
        """INTERNAL: resolve a relationship `name' under the URL `base'."""
        info = schema._type_info(typ)
        if info is None:
            raise TypeError, 'Unknown binding type: %s' % typ
        rel = info[4]
        if rel is None:
            raise TypeError, 'Can\'t resolve URLs for type: %s' % typ
        elif rel.startswith('/'):
            return self._join_path(self.entrypoint, rel)
        if base is not None:
            if hasattr(base, 'href'):
                base = base.href
            else:
                base = self._resolve_url(type(base))
        else:
            base = self.entrypoint
        if search:
            special = 'search'
        elif query:
            special = 'search'
            search = ' AND '.join([ '%s=%s' % (k, query[k]) for k in query ])
        if special:
            rel = '%s/%s' % (rel, special)
        if base not in self._cache:
            response = self._make_request('GET', base)
            if response.status != http.OK:
                raise self._create_exception(response)
            parsed = self._parse_xml(response)
            self._cache[base] = parsed
        else:
            parsed = self._cache[base]
        for link in parsed.link:
            if link.rel == rel:
                url = link.href
                break
        else:
            reason = 'relationship %s not found under %s' % (rel, base)
            raise Error(reason)
        if id is not None:
            url = self._join_path(url, str(id))
        elif search is not None:
            url = url.replace('{query}', urlencode(search))
        return url

    def _resolve_action(self, url, action):
        """INTERNAL: Return the URL for the action beloning to the
        resource."""
        response = self._make_request('GET', url)
        if response.status != http.OK:
            raise self._create_exception(response)
        resource = self._parse_xml(response)
        for link in resource.actions.link:
            if link.rel == action:
                return link.href

    def _create_exception(self, response):
        """INTERNAL: Create an exception with some useful details."""
        ctype = response.getheader('Content-Type')
        if ctype == 'application/xml':
            fault = self._parse_xml(response)
            if isinstance(fault, schema.Fault):
                reason = fault.reason
                detail = fault.detail
            else:
                reason = 'Unexpected HTTP status code: %s' % response.status
                detail = response.reason
        else:
            reason = 'Unexpected HTTP status code: %s' % response.status
            detail = response.reason
        exception = Error(reason, detail=detail)
        return exception

    def _filter_results(self, result, filter):
        """INTERNAL: filter a set of results."""
        matched = []; compiled = []
        for attr in filter:
            match = filter[attr]
            if isinstance(match, str):
                match = re.compile(fnmatch.translate(match))
            compiled.append((attr.split('.'), match))
        for res in result:
            for attr, match in compiled:
                value = res
                for at in attr:
                    value = getattr(value, at, None)
                    if value is None:
                        break
                if not hasattr(match, 'match') and value != match:
                    break
                if hasattr(match, 'match') and (value is None \
                                or not match.match(str(value))):
                    break
            else:
                matched.append(res)
        result[:] = matched
        return result

    def ping(self):
        """Ping the API, to make sure we have a proper connection."""
        response = self._make_request('OPTIONS', self.entrypoint)
        if response.status != http.OK:
            raise Error, 'RHEV-M returned HTTP status %s' % response.status
        allowed = response.getheader('Allow', '')
        allowed = [ h.strip() for h in allowed.split(',') ]
        if 'GET' not in allowed:
            raise Error, 'GET method not supported on API entry point'

    def getall(self, typ, base=None, search=None, filter=None, special=None,
               **query):
        """Get a list of resources that match the supplied arguments. In
        case of success a binding instance is returned. In case of failure
        a Error is raised."""
        url = self._resolve_url(typ, base=base, special=special,
                                search=search, **query)
        response = self._make_request('GET', url)
        if response.status == http.OK:
            result = self._parse_xml(response)
            if filter:
                result = self._filter_results(result, filter)
        elif response.status == http.NOT_FOUND:
            result = []
        else:
            raise self._create_exception(response)
        return result

    def get(self, typ, id=None, base=None, special=None, search=None,
            filter=None, **query):
        """Get a single resource that matches the supplied arguments.In
        case of success a binding instance is returned. In case of failure a
        Error is raised."""
        url = self._resolve_url(typ, base=base, id=id, search=search,
                                special=special, **query)
        response = self._make_request('GET', url)
        if response.status == http.OK:
            result = self._parse_xml(response)
            if not isinstance(result, schema.BaseResources):
                result = [result]
            if filter:
                result = self._filter_results(result, filter)
            if not issubclass(typ, schema.BaseResources):
                if len(result):
                    result = result[0]
                else:
                    result = None
        elif response.status == http.NOT_FOUND:
            result = None
        else:
            # Trac ticket #121: INTERNAL_SERVER_ERROR should be NOT_FOUND
            raise self._create_exception(response)
        return result

    def reload(self, resource):
        """Reload an existing resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding object.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding object.'
        response = self._make_request('GET', resource.href)
        if response.status == http.OK:
            result = self._parse_xml(response)
        elif response.status == http.NOT_FOUND:
            result = None
        else:
            raise self._create_exception(response)
        return result

    def create(self, resource, base=None):
        """Create a new resource. If base is provided, it must be a
        resource, and the new resource is created subordinate to it.  On
        success, a new instance is returned corresponding to the newly
        created resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if resource.href and base is None:
            raise ValueError, 'Expecting a new binding instance.'
        url = self._resolve_url(type(resource), base)
        response = self._make_request('POST', url, body=resource.toxml())
        if response.status in (http.OK, http.CREATED, http.ACCEPTED):
            result = self._parse_xml(response)
        else:
            raise self._create_exception(response)
        return result

    def update(self, resource):
        """Update a resource. On success a new instance is returned
        corresponding to the updated values."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        response = self._make_request('PUT', resource.href, body=resource.toxml())
        if response.status == http.OK:
            result = self._parse_xml(response)
        elif response.status == http.NOT_FOUND:
            raise KeyError, 'Resource not found.'
        else:
            raise self._create_exception(response)
        return result

    def delete(self, resource, base=None, data=None):
        """Delete a resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        if base is None:
            url = resource.href
        else:
            url = self._resolve_url(type(resource), base=base,
                                    id=resource.id)
        if data is not None:
            data = data.toxml()
        response = self._make_request('DELETE', url, body=data)
        if response.status in (http.OK, http.NO_CONTENT):
            result = None
        elif response.status == http.NOT_FOUND:
            raise KeyError, 'Resource not found.'
        else:
            raise self._create_exception(response)
        return result

    def action(self, resource, action, data=None):
        """Execute an action on a resource. """
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        url = self._resolve_action(resource.href, action)
        if data is None:
            data = schema.new(schema.Action)
        response = self._make_request('POST', url, body=data.toxml())
        if response.status in (http.OK, http.NO_CONTENT):
            result = self._parse_xml(response)
        elif response.status == http.NOT_FOUND:
            raise KeyError, 'Resource not found.'
        else:
            raise self._create_exception(response)
        return result

    def api(self):
        """Return the entry point to the mapping API."""
        if self._api is None:
            self._api = self.get(schema.API)
        return self._api
