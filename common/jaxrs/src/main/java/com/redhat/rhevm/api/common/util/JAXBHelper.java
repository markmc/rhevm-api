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
package com.redhat.rhevm.api.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

public class JAXBHelper {

    private JAXBHelper() {}

    /**
     * It's unfortunate that's there's no clone already defined on JAXB
     * generated classes. Here we emulate that missing deep copy support
     * by marshalling and unmarshalling (a little heavyweight admittedly).
     *
     * @param <S>    type parameter
     * @param object object to be cloned
     * @param clz    type of that object
     * @return       clone
     */
    public static <S> S clone(JAXBElement<S> element) {
        S ret = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshall(baos, element);
            ret = unmarshall(new ByteArrayInputStream(baos.toString().getBytes()), element.getDeclaredType());
        } catch (Exception e) {
        }
        return ret;
    }

    public static <S> S clone(String localName, Class<S> clz, S s) {
        return clone(new JAXBElement<S>(new QName("", localName), clz, null, s));
    }

    private static <S> void marshall(OutputStream os, JAXBElement<S> element) throws Exception {
        JAXBContext context = JAXBContext.newInstance(element.getDeclaredType());
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(element, os);
    }

    private static <S> S unmarshall(InputStream is, Class<S> clz) throws Exception {
        JAXBContext context = JAXBContext.newInstance(clz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<S> root = unmarshaller.unmarshal(new StreamSource(is), clz);
        return root.getValue();
    }
}
