package com.edgescheduling.model;

import java.util.ArrayList;
import java.util.List;

public class TaskNode {
    private final int id;
    private final double computationCost; // MIPS required
    private final double dataSize; // MB of data to transfer
    private final List<TaskNode> dependencies;
    private final List<TaskNode> successors;
    private double priority;
    private boolean scheduled;
    private int assignedDeviceId;
    private double startTime;
    private double finishTime;

    public TaskNode(int id, double computationCost, double dataSize) {
        this.id = id;
        this.computationCost = computationCost;
        this.dataSize = dataSize;
        this.dependencies = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.priority = 0.0;
        this.scheduled = false;
        this.assignedDeviceId = -1;
        this.startTime = 0.0;
        this.finishTime = 0.0;
    }

    public void addDependency(TaskNode dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
            dependency.successors.add(this);
        }
    }

    public boolean isReady() {
        return dependencies.stream().allMatch(TaskNode::isScheduled);
    }

    // Getters and setters
    public int getId() { return id; }
    public double getComputationCost() { return computationCost; }
    public double getDataSize() { return dataSize; }
    public List<TaskNode> getDependencies() { return dependencies; }
    public List<TaskNode> getSuccessors() { return successors; }
    public double getPriority() { return priority; }
    public void setPriority(double priority) { this.priority = priority; }
    public boolean isScheduled() { return scheduled; }
    public void setScheduled(boolean scheduled) { this.scheduled = scheduled; }
    public int getAssignedDeviceId() { return assignedDeviceId; }
    public void setAssignedDeviceId(int deviceId) { this.assignedDeviceId = deviceId; }
    public double getStartTime() { return startTime; }
    public void setStartTime(double startTime) { this.startTime = startTime; }
    public double getFinishTime() { return finishTime; }
    public void setFinishTime(double finishTime) { this.finishTime = finishTime; }

    @Override
    public String toString() {
        return String.format("Task[id=%d, cost=%.2f, data=%.2f, deps=%d]",
                id, computationCost, dataSize, dependencies.size());
    }
}
