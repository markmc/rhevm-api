/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.redhat.rhevm.api.common.security.auth;

public interface Validator {

    /**
     * By default principal validation is lazy, with the assumption
     * that this will be initiated by the resource later on the
     * dispatch path. This method allows subclasses to pursue an
     * alternate strategy based on eager validation.  The injected
     * validator, if present, will be called immediately after the
     * credentials have been decoded.
     *
     * @param principal  the decoded principal
     * @return           true iff dispatch should continue
     */
    boolean validate(Principal principal);
}
