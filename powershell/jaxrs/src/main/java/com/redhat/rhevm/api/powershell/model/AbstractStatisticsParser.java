package com.redhat.rhevm.api.powershell.model;

import java.math.BigDecimal;

import com.redhat.rhevm.api.powershell.util.UUID;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.Value;
import com.redhat.rhevm.api.model.ValueType;
import com.redhat.rhevm.api.model.Values;

public abstract class AbstractStatisticsParser {

    protected static final BigDecimal CENT = new BigDecimal(100);
    protected static final long Mb = 1024 * 1024L;

    public static Statistic create(String name,
                                   String description,
                                   StatisticType type,
                                   StatisticUnit unit,
                                   ValueType valueType) {
            Statistic statistic = new Statistic();
            statistic.setId(UUID.asId(name));
            statistic.setName(name);
            statistic.setDescription(description);
            statistic.setType(type);
            statistic.setUnit(unit);
            Value value = new Value();
            statistic.setValues(new Values());
            statistic.getValues().setType(valueType);
            statistic.getValues().getValues().add(value);
            return statistic;
    }

    protected static Statistic setDatum(Statistic statistic, long datum) {
        return setDatum(statistic, new BigDecimal(datum));
    }

    protected static Statistic setDatum(Statistic statistic, BigDecimal datum) {
        statistic.getValues().getValues().get(0).setDatum(datum);
        return statistic;
    }

}
