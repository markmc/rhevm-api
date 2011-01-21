package com.redhat.rhevm.api.powershell.resource;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Statistic;

/**
 * Subclasses encapsulate the subject-specific aspects of a statistical query
 */
public abstract class AbstractStatisticalQuery<R extends BaseResource> {

    protected Class<R> parentType;

    public AbstractStatisticalQuery(Class<R> parentType) {
        this.parentType = parentType;
    }

    public Class<R> getParentType() {
        return parentType;
    }

    public abstract String resolve();

    public abstract List<Statistic> getStatistics(String output);

    public abstract Statistic adopt(Statistic statistic);

    public static Map<String, Statistic> asMap(Statistic...statistics) {
        Map<String, Statistic> map = new HashMap<String, Statistic>();
        for (Statistic statistic : statistics) {
            map.put(statistic.getId(), statistic);
        }
        return map;
    }

    public static List<Statistic> asList(Statistic...statistics) {
        List<Statistic> list = new ArrayList<Statistic>();
        for (Statistic statistic : statistics) {
            list.add(statistic);
        }
        return list;
    }
}
