package com.edgescheduling.model;

import java.util.ArrayList;
import java.util.List;

public class EdgeDevice {
    private final int id;
    private final double mips; // Processing power in MIPS
    private final int cores;
    private final double powerIdle; // Idle power consumption (Watts)
    private final double powerMax; // Maximum power consumption (Watts)
    private final double bandwidth; // Network bandwidth (MB/s)
    private double availableTime; // When the device becomes available
    private final List<TaskNode> scheduledTasks;

    public EdgeDevice(int id, double mips, int cores, double powerIdle, double powerMax, double bandwidth) {
        this.id = id;
        this.mips = mips;
        this.cores = cores;
        this.powerIdle = powerIdle;
        this.powerMax = powerMax;
        this.bandwidth = bandwidth;
        this.availableTime = 0.0;
        this.scheduledTasks = new ArrayList<>();
    }

    public double calculateExecutionTime(TaskNode task) {
        return task.getComputationCost() / mips;
    }

    public double calculateEnergyConsumption(TaskNode task) {
        double executionTime = calculateExecutionTime(task);
        double utilization = Math.min(1.0, task.getComputationCost() / (mips * cores));
        double power = powerIdle + (powerMax - powerIdle) * utilization;
        return power * executionTime;
    }

    public void scheduleTask(TaskNode task, double startTime) {
        task.setAssignedDeviceId(id);
        task.setStartTime(startTime);
        task.setFinishTime(startTime + calculateExecutionTime(task));
        scheduledTasks.add(task);
        availableTime = task.getFinishTime();
    }

    // Getters
    public int getId() { return id; }
    public double getMips() { return mips; }
    public int getCores() { return cores; }
    public double getPowerIdle() { return powerIdle; }
    public double getPowerMax() { return powerMax; }
    public double getBandwidth() { return bandwidth; }
    public double getAvailableTime() { return availableTime; }
    public void setAvailableTime(double time) { this.availableTime = time; }
    public List<TaskNode> getScheduledTasks() { return scheduledTasks; }

    @Override
    public String toString() {
        return String.format("EdgeDevice[id=%d, mips=%.0f, cores=%d]", id, mips, cores);
    }
}
