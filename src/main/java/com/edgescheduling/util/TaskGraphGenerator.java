package com.edgescheduling.util;

import com.edgescheduling.model.TaskNode;
import java.util.*;

public class TaskGraphGenerator {
    private final Random random;

    public TaskGraphGenerator(long seed) {
        this.random = new Random(seed);
    }

    public List<TaskNode> generateDAG(int numTasks, int numEdges) {
        List<TaskNode> tasks = new ArrayList<>();

        // Create tasks with random computation costs
        for (int i = 0; i < numTasks; i++) {
            double computationCost = 100 + random.nextDouble() * 900; // 100-1000 MIPS
            double dataSize = 1 + random.nextDouble() * 9; // 1-10 MB
            tasks.add(new TaskNode(i, computationCost, dataSize));
        }

        // Add dependencies to create DAG structure
        int edgesAdded = 0;
        while (edgesAdded < numEdges) {
            int from = random.nextInt(numTasks);
            int to = random.nextInt(numTasks);

            // Ensure DAG property (no cycles) by only allowing edges from lower to higher indices
            if (from < to && !tasks.get(to).getDependencies().contains(tasks.get(from))) {
                tasks.get(to).addDependency(tasks.get(from));
                edgesAdded++;
            }
        }

        return tasks;
    }

    public void printDAGStatistics(List<TaskNode> tasks) {
        System.out.println("DAG Statistics:");
        System.out.println("Number of tasks: " + tasks.size());

        int totalEdges = tasks.stream()
                .mapToInt(task -> task.getDependencies().size())
                .sum();
        System.out.println("Number of edges: " + totalEdges);

        double avgComputationCost = tasks.stream()
                .mapToDouble(TaskNode::getComputationCost)
                .average()
                .orElse(0.0);
        System.out.println("Average computation cost: " + String.format("%.2f MIPS", avgComputationCost));
    }
}
