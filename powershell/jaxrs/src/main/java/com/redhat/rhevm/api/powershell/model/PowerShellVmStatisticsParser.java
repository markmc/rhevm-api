package com.redhat.rhevm.api.powershell.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.ValueType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellVmStatisticsParser extends AbstractStatisticsParser {

    private static final String MEMORY_STATS_TYPE = "RhevmCmd.MemoryStatistics";
    private static final String CPU_STATS_TYPE = "RhevmCmd.CLICpu";

    public static final String GET_VMS_STATS = "$vm = get-vm {0} ; $vm.getmemorystatistics() ; $vm.getcpustatistics()";

    static boolean isMemory(PowerShellParser.Entity entity) {
        return MEMORY_STATS_TYPE.equals(entity.getType());
    }

    static boolean isCpu(PowerShellParser.Entity entity) {
        return CPU_STATS_TYPE.equals(entity.getType());
    }

    public static List<Statistic> parse(PowerShellParser parser, String output) {
        List<Statistic> statistics = new ArrayList<Statistic>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (isMemory(entity)) {
                statistics.addAll(parseMemoryStats(entity));
            } else if (isCpu(entity)) {
                statistics.addAll(parseCpuStats(entity));
            }
        }

        return statistics;
    }

    static List<Statistic> parseMemoryStats(PowerShellParser.Entity entity) {
        List<Statistic> statistics = new ArrayList<Statistic>();
        Statistic statistic = null;

        statistic = create("memory.installed",
                           "Total mem configured",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        long mem = entity.get("memsizemb", Integer.class)*Mb;
        setDatum(statistic, mem);
        statistics.add(statistic);

        statistic = create("memory.used",
                           "Memory used (agent)",
                           StatisticType.GAUGE,
                           StatisticUnit.BYTES,
                           ValueType.INTEGER);
        setDatum(statistic, mem*entity.get("usagemempercent", Integer.class)/100D);
        statistics.add(statistic);

        return statistics;
    }

    static List<Statistic> parseCpuStats(PowerShellParser.Entity entity) {
        List<Statistic> statistics = new ArrayList<Statistic>();
        Statistic statistic = null;

        statistic = create("cpu.current.guest",
                           "CPU used by guest",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("user", BigDecimal.class).doubleValue());
        statistics.add(statistic);

        statistic = create("cpu.current.hypervisor",
                           "CPU overhead",
                           StatisticType.GAUGE,
                           StatisticUnit.PERCENT,
                           ValueType.DECIMAL);
        setDatum(statistic, entity.get("system", BigDecimal.class).doubleValue());
        statistics.add(statistic);

        return statistics;
    }
}
