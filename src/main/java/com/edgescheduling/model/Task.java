package com.edgescheduling.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;

import java.util.*;

/**
 * Represents a task in the DAG with dependencies
 */
public class Task {
    private final int id;
    private final long length; // in MI (Million Instructions)
    private final long fileSize; // in bytes
    private final long outputSize; // in bytes
    private final int priority;
    private Cloudlet cloudlet;
    private double computationCost;
    private double communicationCost;

    public Task(int id, long length, long fileSize, long outputSize, int priority) {
        this.id = id;
        this.length = length;
        this.fileSize = fileSize;
        this.outputSize = outputSize;
        this.priority = priority;
        this.computationCost = 0.0;
        this.communicationCost = 0.0;
    }

    // Getters and setters
    public int getId() { return id; }
    public long getLength() { return length; }
    public long getFileSize() { return fileSize; }
    public long getOutputSize() { return outputSize; }
    public int getPriority() { return priority; }
    public Cloudlet getCloudlet() { return cloudlet; }
    public void setCloudlet(Cloudlet cloudlet) { this.cloudlet = cloudlet; }
    public double getComputationCost() { return computationCost; }
    public void setComputationCost(double computationCost) { this.computationCost = computationCost; }
    public double getCommunicationCost() { return communicationCost; }
    public void setCommunicationCost(double communicationCost) { this.communicationCost = communicationCost; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", length=" + length + ", priority=" + priority + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
