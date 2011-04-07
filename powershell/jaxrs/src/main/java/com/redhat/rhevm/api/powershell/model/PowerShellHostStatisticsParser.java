package com.redhat.rhevm.api.powershell.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.ValueType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellHostStatisticsParser extends AbstractStatisticsParser {

    private static final String MEMORY_STATS_TYPE = "RhevmCmd.CLIMemory";
    private static final String CPU_STATS_TYPE = "RhevmCmd.CLICpu";
    private static final String KSM_STATS_TYPE = "RhevmCmd.CLIKsm";

    public static final String GET_HOST_STATS =
        "$h = get-host {0} ; $h.getmemorystatistics() ; $h.getcpustatistics(); $h.getksmstatistics()";

    static boolean isMemory(PowerShellParser.Entity entity) {
        return MEMORY_STATS_TYPE.equals(entity.getType());
    }

    static boolean isCpu(PowerShellParser.Entity entity) {
        return CPU_STATS_TYPE.equals(entity.getType());
    }

    static boolean isKsm(PowerShellParser.Entity entity) {
        return KSM_STATS_TYPE.equals(entity.getType());
    }

    public static List<Statistic> parse(PowerShellParser parser, String output) {
        List<Statistic> statistics = new ArrayList<Statistic>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (isMemory(entity)) {
                statistics.addAll(parseMemoryStats(entity));
            } else if (isCpu(entity)) {
                statistics.addAll(parseCpuStats(entity));
            } else if (isKsm(entity)) {
                statistics.addAll(parseKsmStats(entity));
            }
        }

        return statistics;
    }

    static List<Statistic> parseMemoryStats(PowerShellParser.Entity entity) {
        List<Statistic> statistics = new ArrayList<Statistic>();
        Statistic statistic = null;

        statistic = create("memory.installed",
                           "Total physical memory",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        long mem = entity.get("physicalmemory", Integer.class)*Mb;
        setDatum(statistic, mem);
        statistics.add(statistic);

        statistic = create("memory.total",
                           "Total guest memory",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        setDatum(statistic, entity.get("commitedmemory", Integer.class)*Mb);
        statistics.add(statistic);

        statistic = create("memory.used",
                           "Memory used",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        long memUsedByCent = mem*entity.get("usagepercents", Integer.class);
        setDatum(statistic, new BigDecimal(memUsedByCent).divide(CENT));
        statistics.add(statistic);

        statistic = create("memory.free",
                           "Memory free",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        setDatum(statistic, new BigDecimal((mem*100) - memUsedByCent).divide(CENT));
        statistics.add(statistic);

        statistic = create("swap.total",
                           "Total swap",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        setDatum(statistic, entity.get("swaptotal", Long.class)*Mb);
        statistics.add(statistic);

        statistic = create("swap.free",
                           "Free swap",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        setDatum(statistic, entity.get("swapfree", Long.class)*Mb);
        statistics.add(statistic);

        return statistics;
    }

    static List<Statistic> parseCpuStats(PowerShellParser.Entity entity) {
        List<Statistic> statistics = new ArrayList<Statistic>();
        Statistic statistic = null;

        statistic = create("cpu.current.user",
                           "User+nic CPU usage",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("user", BigDecimal.class));
        statistics.add(statistic);

        statistic = create("cpu.current.system",
                           "System CPU usage",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("system", BigDecimal.class));
        statistics.add(statistic);

        statistic = create("cpu.current.idle",
                           "Idle CPU usage",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("idle", BigDecimal.class));
        statistics.add(statistic);

        statistic = create("cpu.load.avg.5m",
                           "5min CPU load average",
                           StatisticType.GAUGE,
                           StatisticUnit.NONE,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("load", BigDecimal.class));
        statistics.add(statistic);

        return statistics;
    }

    static List<Statistic> parseKsmStats(PowerShellParser.Entity entity) {
        List<Statistic> statistics = new ArrayList<Statistic>();
        Statistic statistic = null;

        statistic = create("ksm.cpu.current",
                           "KSM CPU usage",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("cpu", Integer.class));
        statistics.add(statistic);

        return statistics;
    }

}
