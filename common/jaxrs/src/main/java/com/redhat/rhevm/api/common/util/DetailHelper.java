package com.redhat.rhevm.api.common.util;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

public class DetailHelper {

    private static final String ACCEPT = "Accept";
    private static final String DETAIL = "detail";
    private static final String PARAM_SEPARATOR = ";";
    private static final String SETTING_SEPARATOR = " ";
    private static final String VALUE_SEPARATOR = "=";
    private static final String DETAIL_SEPARATOR = "\\+";

    public static boolean include(HttpHeaders httpheaders, String relation) {
        List<String> accepts = httpheaders.getRequestHeader(ACCEPT);
        if (!(accepts == null || accepts.isEmpty())) {
            String[] parameters = accepts.get(0).split(PARAM_SEPARATOR);
            for (String parameter : parameters) {
                String[] settings = parameter.split(SETTING_SEPARATOR);
                for (String setting : settings) {
                    String[] includes = setting.split(VALUE_SEPARATOR);
                    if (includes.length > 1 && DETAIL.equalsIgnoreCase(includes[0].trim())) {
                        for (String rel : includes[1].trim().split(DETAIL_SEPARATOR)) {
                            if (relation.equalsIgnoreCase(rel.trim())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
