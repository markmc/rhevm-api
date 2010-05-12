package com.redhat.rhevm.api.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
