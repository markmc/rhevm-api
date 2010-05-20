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
package com.redhat.rhevm.api.common.resource;

import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CPUs;
import com.redhat.rhevm.api.resource.CpusResource;

public class DefaultCpusResource implements CpusResource {

    private CPUs cpus = new CPUs();

    {
        addCpu("Intel Xeon w/o XD/NX",  2, "vmx", "sse2");
        addCpu("Intel Xeon",            3, "vmx", "sse2", "nx");
        addCpu("Intel Xeon Core2",      4, "vmx", "sse2", "nx", "cx16", "ssse3");
        addCpu("Intel Xeon 45nm Core2", 5, "vmx", "sse2", "nx", "cx16", "ssse3", "sse4_1");
        addCpu("Intel Xeon Core i7",    6, "vmx", "sse2", "nx", "cx16", "ssse3", "sse4_1", "sse4_2", "popcnt");

        addCpu("AMD Opteron G1 w/o NX", 2, "svm", "sse2");
        addCpu("AMD Opteron G1",        3, "svm", "sse2", "nx");
        addCpu("AMD Opteron G2",        4, "svm", "sse2", "nx", "cx16");
        addCpu("AMD Opteron G3",        5, "svm", "sse2", "nx", "cx16", "sse4a", "misalignsse", "popcnt", "abm");
    }

    private void addCpu(String id, int level, String... flags)
    {
        CPU cpu = new CPU();

        cpu.setId(id);
        cpu.setLevel(level);

        cpu.setFlags(new CPU.Flags());
        for (String flag : flags) {
            cpu.getFlags().getFlags().add(flag);
        }

        cpus.getCPUs().add(cpu);
    }

    public CPUs list() {
        return cpus;
    }
}
