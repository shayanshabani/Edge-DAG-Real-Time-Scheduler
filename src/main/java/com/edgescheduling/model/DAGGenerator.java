package com.edgescheduling.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Generates DAG structures for task scheduling
 */
public class DAGGenerator {
    private final Random random;

    public DAGGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generates a DAG with specified number of tasks and edges
     */
    public Graph<Task, DefaultEdge> generateDAG(int numTasks, int numEdges) {
        Graph<Task, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);

        // Create tasks
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            long length = 100 + random.nextInt(1000); // 1000-10000 MI
            long fileSize = 100 + random.nextInt(1000); // 100-1000 bytes
            long outputSize = 50 + random.nextInt(500); // 50-500 bytes
            int priority = random.nextInt(10) + 1; // 1-10

            Task task = new Task(i, length, fileSize, outputSize, priority);
            tasks.add(task);
            dag.addVertex(task);
        }

        // Add edges while maintaining DAG property
        int edgesAdded = 0;
        int attempts = 0;
        int maxAttempts = numEdges * 10;

        while (edgesAdded < numEdges && attempts < maxAttempts) {
            int sourceIdx = random.nextInt(numTasks);
            int targetIdx = random.nextInt(numTasks);

            if (sourceIdx != targetIdx && sourceIdx < targetIdx) { // Ensure no cycles
                Task source = tasks.get(sourceIdx);
                Task target = tasks.get(targetIdx);

                if (!dag.containsEdge(source, target)) {
                    dag.addEdge(source, target);
                    edgesAdded++;
                }
            }
            attempts++;
        }

        return dag;
    }

    /**
     * Gets entry tasks (tasks with no predecessors)
     */
    public Set<Task> getEntryTasks(Graph<Task, DefaultEdge> dag) {
        Set<Task> entryTasks = new HashSet<>();
        for (Task task : dag.vertexSet()) {
            if (dag.inDegreeOf(task) == 0) {
                entryTasks.add(task);
            }
        }
        return entryTasks;
    }

    /**
     * Gets exit tasks (tasks with no successors)
     */
    public Set<Task> getExitTasks(Graph<Task, DefaultEdge> dag) {
        Set<Task> exitTasks = new HashSet<>();
        for (Task task : dag.vertexSet()) {
            if (dag.outDegreeOf(task) == 0) {
                exitTasks.add(task);
            }
        }
        return exitTasks;
    }
}
