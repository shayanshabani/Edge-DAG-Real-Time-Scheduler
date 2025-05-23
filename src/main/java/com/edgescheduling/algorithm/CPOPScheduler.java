package com.edgescheduling.algorithm;

import com.edgescheduling.environment.EdgeEnvironment;
import com.edgescheduling.model.EdgeDevice;
import com.edgescheduling.model.TaskNode;

import java.util.*;

public class CPOPScheduler {
    private final EdgeEnvironment environment;

    public CPOPScheduler(EdgeEnvironment environment) {
        this.environment = environment;
    }

    public SchedulingResult schedule(List<TaskNode> tasks) {
        // Reset all tasks
        tasks.forEach(task -> {
            task.setScheduled(false);
            task.setAssignedDeviceId(-1);
            task.setStartTime(0);
            task.setFinishTime(0);
        });

        // Reset all devices
        environment.getDevices().forEach(device -> {
            device.setAvailableTime(0.0);
            device.getScheduledTasks().clear();
        });

        // Step 1: Calculate upward rank for all tasks
        calculateUpwardRank(tasks);

        // Step 2: Sort tasks by priority (descending order)
        List<TaskNode> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> Double.compare(t2.getPriority(), t1.getPriority()));

        // Step 3: Schedule tasks in priority order
        for (TaskNode task : sortedTasks) {
            scheduleTask(task);
        }

        // Calculate results
        return calculateResults(tasks);
    }

    private void calculateUpwardRank(List<TaskNode> tasks) {
        Map<TaskNode, Double> rankCache = new HashMap<>();

        for (TaskNode task : tasks) {
            calculateUpwardRankRecursive(task, rankCache);
        }
    }

    private double calculateUpwardRankRecursive(TaskNode task, Map<TaskNode, Double> cache) {
        if (cache.containsKey(task)) {
            return cache.get(task);
        }

        // Average computation time across all devices
        double avgComputationTime = environment.getDevices().stream()
                .mapToDouble(device -> device.calculateExecutionTime(task))
                .average()
                .orElse(0.0);

        double maxSuccessorRank = 0.0;
        for (TaskNode successor : task.getSuccessors()) {
            double avgCommTime = calculateAverageCommunicationTime(task, successor);
            double successorRank = calculateUpwardRankRecursive(successor, cache);
            maxSuccessorRank = Math.max(maxSuccessorRank, avgCommTime + successorRank);
        }

        double rank = avgComputationTime + maxSuccessorRank;
        cache.put(task, rank);
        task.setPriority(rank);
        return rank;
    }

    private double calculateAverageCommunicationTime(TaskNode from, TaskNode to) {
        double totalCommTime = 0.0;
        int count = 0;

        for (int i = 0; i < environment.getNumDevices(); i++) {
            for (int j = 0; j < environment.getNumDevices(); j++) {
                totalCommTime += environment.getCommunicationTime(i, j, from.getDataSize());
                count++;
            }
        }

        return count > 0 ? totalCommTime / count : 0.0;
    }

    private void scheduleTask(TaskNode task) {
        EdgeDevice bestDevice = null;
        double earliestFinishTime = Double.MAX_VALUE;
        double bestStartTime = 0.0;

        for (EdgeDevice device : environment.getDevices()) {
            double readyTime = Math.max(device.getAvailableTime(),
                    calculateDataReadyTime(task, device.getId()));
            double executionTime = device.calculateExecutionTime(task);
            double finishTime = readyTime + executionTime;

            if (finishTime < earliestFinishTime) {
                earliestFinishTime = finishTime;
                bestDevice = device;
                bestStartTime = readyTime;
            }
        }

        if (bestDevice != null) {
            bestDevice.scheduleTask(task, bestStartTime);
            task.setScheduled(true);
        }
    }

    private double calculateDataReadyTime(TaskNode task, int deviceId) {
        double maxDataReadyTime = 0.0;

        for (TaskNode dependency : task.getDependencies()) {
            if (dependency.getAssignedDeviceId() != deviceId) {
                // Data transfer needed
                double transferTime = environment.getCommunicationTime(
                        dependency.getAssignedDeviceId(), deviceId, dependency.getDataSize());
                maxDataReadyTime = Math.max(maxDataReadyTime,
                        dependency.getFinishTime() + transferTime);
            } else {
                // Same device, no transfer needed
                maxDataReadyTime = Math.max(maxDataReadyTime, dependency.getFinishTime());
            }
        }

        return maxDataReadyTime;
    }

    private SchedulingResult calculateResults(List<TaskNode> tasks) {
        double makespan = tasks.stream()
                .mapToDouble(TaskNode::getFinishTime)
                .max()
                .orElse(0.0);

        double totalEnergyConsumption = 0.0;
        for (EdgeDevice device : environment.getDevices()) {
            for (TaskNode task : device.getScheduledTasks()) {
                totalEnergyConsumption += device.calculateEnergyConsumption(task);
            }
        }

        double qos = calculateQoS(tasks, makespan);

        return new SchedulingResult(makespan, totalEnergyConsumption, qos);
    }

    private double calculateQoS(List<TaskNode> tasks, double makespan) {
        // QoS based on deadline satisfaction and load balancing
        double deadlineSatisfaction = tasks.stream()
                .mapToDouble(task -> {
                    double deadline = (task.getId() + 1) * 15.0; // Flexible deadline
                    return task.getFinishTime() <= deadline ? 1.0 : 0.0;
                })
                .average()
                .orElse(0.0);

        // Load balancing factor
        double[] deviceLoads = new double[environment.getNumDevices()];
        for (TaskNode task : tasks) {
            if (task.getAssignedDeviceId() >= 0) {
                deviceLoads[task.getAssignedDeviceId()] += task.getFinishTime() - task.getStartTime();
            }
        }

        double avgLoad = Arrays.stream(deviceLoads).average().orElse(0.0);
        double loadVariance = Arrays.stream(deviceLoads)
                .map(load -> Math.pow(load - avgLoad, 2))
                .average()
                .orElse(0.0);

        double loadBalancing = avgLoad > 0 ? 1.0 / (1.0 + loadVariance / (avgLoad * avgLoad)) : 1.0;

        return 0.7 * deadlineSatisfaction + 0.3 * loadBalancing;
    }

    public static class SchedulingResult {
        private final double makespan;
        private final double energyConsumption;
        private final double qos;

        public SchedulingResult(double makespan, double energyConsumption, double qos) {
            this.makespan = makespan;
            this.energyConsumption = energyConsumption;
            this.qos = qos;
        }

        public double getMakespan() { return makespan; }
        public double getEnergyConsumption() { return energyConsumption; }
        public double getQos() { return qos; }

        @Override
        public String toString() {
            return String.format("Result[makespan=%.2f, energy=%.2f, qos=%.2f]",
                    makespan, energyConsumption, qos);
        }
    }
}
