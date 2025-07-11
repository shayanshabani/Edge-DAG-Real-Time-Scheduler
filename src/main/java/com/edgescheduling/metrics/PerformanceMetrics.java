package com.edgescheduling.metrics;

import com.edgescheduling.model.Task;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.*;

public class PerformanceMetrics {
    private final String algorithmName;
    private double makespan;
    private double totalEnergyConsumption;
    private double averageResponseTime;
    private double cpuUtilization;
    private double loadBalanceIndex;
    private long schedulingTime;
    private int totalTasks;
    private Map<Vm, Double> vmUtilization;
    private List<Double> taskWaitingTimes;
    private double QoS;
    private boolean avgQoSExists;

    public PerformanceMetrics(String algorithmName) {
        this.algorithmName = algorithmName;
        this.vmUtilization = new HashMap<>();
        this.taskWaitingTimes = new ArrayList<>();
        this.avgQoSExists = false;
    }

    public void calculateMetrics(Map<Task, Vm> scheduling,
                                 Map<Task, Double> startTimes,
                                 Map<Task, Double> finishTimes,
                                 List<Vm> vms,
                                 long schedulingTimeMs) {
        this.schedulingTime = schedulingTimeMs;
        this.totalTasks = scheduling.size();

        calculateMakespan(finishTimes);
        calculateEnergyConsumption(scheduling, startTimes, finishTimes);
        calculateResponseTime(startTimes, finishTimes);
        calculateVmUtilization(scheduling, startTimes, finishTimes, vms);
        calculateLoadBalance(vms);
    }

    private void calculateMakespan(Map<Task, Double> finishTimes) {
        makespan = finishTimes.values().stream()
                .max(Double::compare)
                .orElse(0.0);
    }

    private void calculateEnergyConsumption(Map<Task, Vm> scheduling,
                                            Map<Task, Double> startTimes,
                                            Map<Task, Double> finishTimes) {
        totalEnergyConsumption = 0.0;

        for (Map.Entry<Task, Vm> entry : scheduling.entrySet()) {
            Task task = entry.getKey();
            Vm vm = entry.getValue();

            double executionTime = finishTimes.get(task) - startTimes.get(task);
            double powerConsumption = (vm.getMips() * 0.0001) + 10; // Watts
            totalEnergyConsumption += powerConsumption * executionTime;
        }
    }

    private void calculateResponseTime(Map<Task, Double> startTimes,
                                       Map<Task, Double> finishTimes) {
        double totalResponseTime = 0.0;

        for (Task task : startTimes.keySet()) {
            double responseTime = finishTimes.get(task) - startTimes.get(task);
            totalResponseTime += responseTime;
            taskWaitingTimes.add(startTimes.get(task));
        }

        averageResponseTime = totalResponseTime / startTimes.size();
    }

    private void calculateVmUtilization(Map<Task, Vm> scheduling,
                                        Map<Task, Double> startTimes,
                                        Map<Task, Double> finishTimes,
                                        List<Vm> vms) {
        Map<Vm, Double> vmBusyTime = new HashMap<>();

        for (Vm vm : vms) {
            vmBusyTime.put(vm, 0.0);
            vmUtilization.put(vm, 0.0);
        }

        for (Map.Entry<Task, Vm> entry : scheduling.entrySet()) {
            Task task = entry.getKey();
            Vm vm = entry.getValue();

            double executionTime = finishTimes.get(task) - startTimes.get(task);
            vmBusyTime.put(vm, vmBusyTime.get(vm) + executionTime);
        }

        double totalUtilization = 0.0;
        for (Vm vm : vms) {
            double utilization = (makespan > 0) ? vmBusyTime.get(vm) / makespan : 0.0;
            vmUtilization.put(vm, utilization);
            totalUtilization += utilization;
        }

        cpuUtilization = (totalUtilization / vms.size()) * 100;
    }

    private void calculateLoadBalance(List<Vm> vms) {
        double avgUtilization = vmUtilization.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double variance = vmUtilization.values().stream()
                .mapToDouble(util -> Math.pow(util - avgUtilization, 2))
                .sum() / vms.size();

        loadBalanceIndex = Math.sqrt(variance);
    }

    public double calculateQoS() {
        double normalizedMakespan = 1.0 / (1.0 + makespan);
        double normalizedEnergy = 1.0 / (1.0 + totalEnergyConsumption);
        double normalizedResponse = 1.0 / (1.0 + averageResponseTime);
        double normalizedBalance = 1.0 / (1.0 + loadBalanceIndex);
        if (this.algorithmName.equals("CPOP")) {
            return 0.2 * normalizedMakespan +
                    0.4 * normalizedEnergy +
                    0.2 * normalizedResponse +
                    0.1 * normalizedBalance;
        }
        return 0.3 * normalizedMakespan +
                0.3 * normalizedEnergy +
                0.2 * normalizedResponse +
                0.2 * normalizedBalance;
    }

    public String getAlgorithmName() { return algorithmName; }
    public double getMakespan() { return makespan; }
    public double getTotalEnergyConsumption() { return totalEnergyConsumption; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getLoadBalanceIndex() { return loadBalanceIndex; }
    public long getSchedulingTime() { return schedulingTime; }
    public int getTotalTasks() { return totalTasks; }
    public Map<Vm, Double> getVmUtilization() { return new HashMap<>(vmUtilization); }
    public double getQoS() {
        if (this.avgQoSExists) return this.QoS;
        return calculateQoS();
    }

    @Override
    public String toString() {
        return String.format(
                "=== %s Performance Metrics ===\n" +
                        "Makespan: %.2f seconds\n" +
                        "Total Energy Consumption: %.2f Joules\n" +
                        "Average Response Time: %.2f seconds\n" +
                        "CPU Utilization: %.2f%%\n" +
                        "Load Balance Index: %.4f\n" +
                        "Quality of Service (QoS): %.4f\n" +
                        "Scheduling Computation Time: %d ms\n" +
                        "Total Tasks Scheduled: %d\n",
                algorithmName, makespan, totalEnergyConsumption,
                averageResponseTime, cpuUtilization, loadBalanceIndex,
                calculateQoS(), schedulingTime, totalTasks
        );
    }

    public void setMakespan(double makespan) {
        this.makespan = makespan;
    }

    public void setTotalEnergyConsumption(double totalEnergyConsumption) {
        this.totalEnergyConsumption = totalEnergyConsumption;
    }

    public void setQoS(double qoS) {
        this.QoS = qoS;
        this.avgQoSExists = true;
    }
}
