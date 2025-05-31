package com.edgescheduling.environment;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the edge computing environment using CloudSimPlus
 */
public class EdgeEnvironment {
    private final CloudSimPlus simulation;
    private final List<Datacenter> edgeDatacenters;
    private final List<Vm> edgeVMs;
    private final DatacenterBroker broker;

    public EdgeEnvironment() {
        this.simulation = new CloudSimPlus();
        this.edgeDatacenters = new ArrayList<>();
        this.edgeVMs = new ArrayList<>();
        this.broker = new DatacenterBrokerSimple(simulation);

        createEdgeInfrastructure();
    }

    /**
     * Creates edge computing infrastructure with multiple edge nodes
     */
    private void createEdgeInfrastructure() {
        // Create 5 edge datacenters with different capabilities
        for (int i = 0; i < 5; i++) {
            Datacenter datacenter = createEdgeDatacenter(i);
            edgeDatacenters.add(datacenter);
        }

        // Create VMs for each datacenter
        createEdgeVMs();
    }

    /**
     * Creates an edge datacenter with specified characteristics
     */
    private Datacenter createEdgeDatacenter(int id) {
        List<Host> hostList = new ArrayList<>();

        // Create 2-4 hosts per datacenter
        int numHosts = 2 + (id % 3);
        for (int i = 0; i < numHosts; i++) {
            Host host = createEdgeHost(id * 10 + i);
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    /**
     * Creates an edge host with limited resources
     */
    private Host createEdgeHost(int id) {
        List<Pe> peList = new ArrayList<>();

        // Edge devices have 2-8 cores with varying MIPS
        int numPes = 2 + (id % 7);
        long mips = 1000 + (id % 4) * 500; // 1000-2500 MIPS

        for (int i = 0; i < numPes; i++) {
            peList.add(new PeSimple(mips));
        }

        long ram = 2048 + (id % 4) * 1024; // 2-5 GB RAM
        long storage = 10000 + (id % 5) * 5000; // 10-35 GB storage
        long bw = 100 + (id % 10) * 50; // 100-550 Mbps

        return new HostSimple(ram, bw, storage, peList)
                .setVmScheduler(new VmSchedulerTimeShared());
    }

    /**
     * Creates VMs for edge computing
     */
    private void createEdgeVMs() {
        int vmId = 0;

        for (Datacenter datacenter : edgeDatacenters) {
            for (Host host : datacenter.getHostList()) {
                // Create 1-2 VMs per host
                int numVMs = 1 + (vmId % 2);

                for (int i = 0; i < numVMs; i++) {
                    Vm vm = createEdgeVM(vmId++, host);
                    edgeVMs.add(vm);
                }
            }
        }

        broker.submitVmList(edgeVMs);
    }

    /**
     * Creates a VM with edge-appropriate specifications
     */
    private Vm createEdgeVM(int id, Host host) {
        double mips = host.getTotalMipsCapacity() / 2; // Use half of host capacity
        int pesNumber = Math.max(1, host.getPeList().size() / 2);
        long ram = host.getRam().getCapacity() / 2;
        long bw = host.getBw().getCapacity() / 2;
        long storage = host.getStorage().getCapacity() / 2;

        return new VmSimple(id, mips, pesNumber)
                .setRam(ram)
                .setBw(bw)
                .setSize(storage)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }

    // Getters
    public CloudSimPlus getSimulation() { return simulation; }
    public List<Datacenter> getEdgeDatacenters() { return edgeDatacenters; }
    public List<Vm> getEdgeVMs() { return edgeVMs; }
    public DatacenterBroker getBroker() { return broker; }
}
