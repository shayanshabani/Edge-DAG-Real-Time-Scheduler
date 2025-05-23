package com.edgescheduling.environment;

import com.edgescheduling.model.EdgeDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EdgeEnvironment {
    private final List<EdgeDevice> devices;
    private final double[][] communicationMatrix; // Communication delays between devices
    private final Random random;

    public EdgeEnvironment(int numDevices, long seed) {
        this.devices = new ArrayList<>();
        this.communicationMatrix = new double[numDevices][numDevices];
        this.random = new Random(seed);
        createEdgeDevices(numDevices);
        initializeCommunicationMatrix();
    }

    private void createEdgeDevices(int numDevices) {
        for (int i = 0; i < numDevices; i++) {
            // Create heterogeneous edge devices
            double mips = 1000 + random.nextDouble() * 2000; // 1000-3000 MIPS
            int cores = 2 + random.nextInt(3); // 2-4 cores
            double powerIdle = 10 + random.nextDouble() * 10; // 10-20 Watts idle
            double powerMax = 50 + random.nextDouble() * 50; // 50-100 Watts max
            double bandwidth = 50 + random.nextDouble() * 50; // 50-100 MB/s

            devices.add(new EdgeDevice(i, mips, cores, powerIdle, powerMax, bandwidth));
        }
    }

    private void initializeCommunicationMatrix() {
        int n = devices.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    communicationMatrix[i][j] = 0.0; // No communication delay within same device
                } else {
                    // Random communication delay between devices (1-10 ms base + distance factor)
                    communicationMatrix[i][j] = 1.0 + random.nextDouble() * 9.0;
                }
            }
        }
    }

    public double getCommunicationTime(int fromDevice, int toDevice, double dataSize) {
        if (fromDevice == toDevice) return 0.0;

        double delay = communicationMatrix[fromDevice][toDevice];
        double bandwidth = Math.min(devices.get(fromDevice).getBandwidth(),
                devices.get(toDevice).getBandwidth());
        return delay + (dataSize / bandwidth);
    }

    public List<EdgeDevice> getDevices() { return devices; }
    public EdgeDevice getDevice(int id) { return devices.get(id); }
    public int getNumDevices() { return devices.size(); }

    public void printEnvironmentInfo() {
        System.out.println("Edge Environment Configuration:");
        System.out.println("Number of devices: " + devices.size());
        for (EdgeDevice device : devices) {
            System.out.printf("  %s - Power: %.1f-%.1f W, BW: %.1f MB/s\n",
                    device, device.getPowerIdle(), device.getPowerMax(), device.getBandwidth());
        }
    }
}
