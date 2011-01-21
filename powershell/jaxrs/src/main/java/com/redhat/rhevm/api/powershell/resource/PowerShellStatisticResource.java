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

package com.redhat.rhevm.api.powershell.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.Statistics;
import com.redhat.rhevm.api.resource.StatisticResource;
import com.redhat.rhevm.api.resource.StatisticsResource;

public class PowerShellStatisticResource<R extends BaseResource> implements StatisticResource {

    private String id;
    private StatisticsResource collection;

    protected PowerShellStatisticResource(String id, StatisticsResource collection) {
        this.id = id;
        this.collection = collection;
    }

    @Override
    public Statistic get() {
        Statistics statistics = collection.list();
        for (Statistic statistic : statistics.getStatistics()) {
            if (id.equals(statistic.getId())) {
                return statistic;
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
