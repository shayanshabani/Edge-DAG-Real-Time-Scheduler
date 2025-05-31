package com.edgescheduling.algorithms;

import com.edgescheduling.model.Task;
import org.cloudsimplus.vms.Vm;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

/**
 * Implementation of the Critical Path on a Processor (CPOP) algorithm
 */
public class CPOPScheduler {
    private final Graph<Task, DefaultEdge> taskGraph;
    private final List<Vm> availableVMs;
    private final Map<Task, Double> upwardRank;
    private final Map<Task, Double> downwardRank;
    private final Map<Task, Vm> taskToVmMapping;
    private final Map<Task, Double> taskStartTime;
    private final Map<Task, Double> taskFinishTime;
    private final Map<Vm, Double> vmAvailableTime; // Track when each VM becomes available

    public CPOPScheduler(Graph<Task, DefaultEdge> taskGraph, List<Vm> availableVMs) {
        this.taskGraph = taskGraph;
        this.availableVMs = new ArrayList<>(availableVMs);
        this.upwardRank = new HashMap<>();
        this.downwardRank = new HashMap<>();
        this.taskToVmMapping = new HashMap<>();
        this.taskStartTime = new HashMap<>();
        this.taskFinishTime = new HashMap<>();
        this.vmAvailableTime = new HashMap<>();

        // Initialize VM available times
        for (Vm vm : availableVMs) {
            vmAvailableTime.put(vm, 0.0);
        }
    }

    /**
     * Executes the CPOP scheduling algorithm
     */
    public Map<Task, Vm> schedule() {
        if (taskGraph.vertexSet().isEmpty()) {
            return new HashMap<>();
        }

        // Step 1: Calculate upward rank for all tasks
        calculateUpwardRank();

        // Step 2: Calculate downward rank for all tasks
        calculateDownwardRank();

        // Step 3: Find critical path
        List<Task> criticalPath = findCriticalPath();

        // Step 4: Find critical processor (VM with minimum execution time for critical path)
        Vm criticalProcessor = findCriticalProcessor(criticalPath);

        // Step 5: Schedule critical path tasks on critical processor
        scheduleCriticalPath(criticalPath, criticalProcessor);

        // Step 6: Schedule remaining tasks
        scheduleRemainingTasks();

        return new HashMap<>(taskToVmMapping);
    }

    /**
     * Calculates upward rank (longest path from task to exit)
     */
    private void calculateUpwardRank() {
        // Use reverse topological order
        List<Task> reverseTopologicalOrder = new ArrayList<>();
        TopologicalOrderIterator<Task, DefaultEdge> iterator =
                new TopologicalOrderIterator<>(taskGraph);

        while (iterator.hasNext()) {
            reverseTopologicalOrder.add(iterator.next());
        }
        Collections.reverse(reverseTopologicalOrder);

        for (Task task : reverseTopologicalOrder) {
            double maxSuccessorRank = 0.0;

            for (DefaultEdge edge : taskGraph.outgoingEdgesOf(task)) {
                Task successor = taskGraph.getEdgeTarget(edge);
                double communicationCost = calculateAverageCommunicationCost(task, successor);
                double successorRank = upwardRank.getOrDefault(successor, 0.0);
                maxSuccessorRank = Math.max(maxSuccessorRank,
                        communicationCost + successorRank);
            }

            double avgComputationCost = calculateAverageComputationCost(task);
            upwardRank.put(task, avgComputationCost + maxSuccessorRank);
        }
    }

    /**
     * Calculates downward rank (longest path from entry to task)
     */
    private void calculateDownwardRank() {
        TopologicalOrderIterator<Task, DefaultEdge> iterator =
                new TopologicalOrderIterator<>(taskGraph);

        while (iterator.hasNext()) {
            Task task = iterator.next();
            double maxPredecessorRank = 0.0;

            for (DefaultEdge edge : taskGraph.incomingEdgesOf(task)) {
                Task predecessor = taskGraph.getEdgeSource(edge);
                double communicationCost = calculateAverageCommunicationCost(predecessor, task);
                double predecessorRank = downwardRank.getOrDefault(predecessor, 0.0);
                maxPredecessorRank = Math.max(maxPredecessorRank,
                        predecessorRank + communicationCost);
            }

            double avgComputationCost = calculateAverageComputationCost(task);
            downwardRank.put(task, maxPredecessorRank + avgComputationCost);
        }
    }

    /**
     * Finds the critical path (tasks with highest priority)
     */
    private List<Task> findCriticalPath() {
        // Sort all tasks by priority (upward + downward rank) in descending order
        List<Task> allTasks = new ArrayList<>(taskGraph.vertexSet());
        allTasks.sort((t1, t2) -> {
            double priority1 = upwardRank.get(t1) + downwardRank.get(t1);
            double priority2 = upwardRank.get(t2) + downwardRank.get(t2);
            return Double.compare(priority2, priority1); // Descending order
        });

        // Find tasks that form the actual critical path
        List<Task> criticalPath = new ArrayList<>();
        Set<Task> visited = new HashSet<>();

        // Start with the highest priority task and trace the path
        if (!allTasks.isEmpty()) {
            Task startTask = allTasks.get(0);
            buildActualCriticalPath(startTask, criticalPath, visited);
        }

        return criticalPath;
    }

    /**
     * Builds the actual critical path by following dependencies
     */
    private void buildActualCriticalPath(Task currentTask, List<Task> path, Set<Task> visited) {
        if (visited.contains(currentTask)) {
            return;
        }

        visited.add(currentTask);
        path.add(currentTask);

        // Find the successor with highest priority that hasn't been visited
        Task nextTask = null;
        double maxPriority = Double.NEGATIVE_INFINITY;

        for (DefaultEdge edge : taskGraph.outgoingEdgesOf(currentTask)) {
            Task successor = taskGraph.getEdgeTarget(edge);
            if (!visited.contains(successor)) {
                double priority = upwardRank.get(successor) + downwardRank.get(successor);
                if (priority > maxPriority) {
                    maxPriority = priority;
                    nextTask = successor;
                }
            }
        }

        if (nextTask != null) {
            buildActualCriticalPath(nextTask, path, visited);
        }
    }

    /**
     * Finds the critical processor (VM) for the critical path
     */
    private Vm findCriticalProcessor(List<Task> criticalPath) {
        if (criticalPath.isEmpty()) {
            return availableVMs.get(0); // Return first VM if no critical path
        }

        Vm bestVm = null;
        double minExecutionTime = Double.MAX_VALUE;

        for (Vm vm : availableVMs) {
            double totalExecutionTime = 0.0;

            for (Task task : criticalPath) {
                totalExecutionTime += calculateExecutionTime(task, vm);
            }

            if (totalExecutionTime < minExecutionTime) {
                minExecutionTime = totalExecutionTime;
                bestVm = vm;
            }
        }

        return bestVm != null ? bestVm : availableVMs.get(0);
    }

    /**
     * Schedules critical path tasks on the critical processor
     */
    private void scheduleCriticalPath(List<Task> criticalPath, Vm criticalProcessor) {
        double currentTime = vmAvailableTime.get(criticalProcessor);

        for (Task task : criticalPath) {
            // Calculate earliest start time considering dependencies
            double earliestStart = calculateEarliestStartTime(task, criticalProcessor);
            double actualStartTime = Math.max(currentTime, earliestStart);

            taskToVmMapping.put(task, criticalProcessor);
            taskStartTime.put(task, actualStartTime);

            double executionTime = calculateExecutionTime(task, criticalProcessor);
            double finishTime = actualStartTime + executionTime;

            taskFinishTime.put(task, finishTime);
            currentTime = finishTime;
        }

        // Update VM available time
        vmAvailableTime.put(criticalProcessor, currentTime);
    }

    /**
     * Schedules remaining tasks using earliest finish time heuristic
     */
    private void scheduleRemainingTasks() {
        Set<Task> scheduledTasks = new HashSet<>(taskToVmMapping.keySet());

        // Get tasks in topological order
        List<Task> remainingTasks = new ArrayList<>();
        TopologicalOrderIterator<Task, DefaultEdge> iterator =
                new TopologicalOrderIterator<>(taskGraph);

        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (!scheduledTasks.contains(task)) {
                remainingTasks.add(task);
            }
        }

        // Sort remaining tasks by priority (upward rank)
        remainingTasks.sort((t1, t2) ->
                Double.compare(upwardRank.get(t2), upwardRank.get(t1)));

        for (Task task : remainingTasks) {
            Vm bestVm = null;
            double earliestFinishTime = Double.MAX_VALUE;

            for (Vm vm : availableVMs) {
                double finishTime = calculateEarliestFinishTime(task, vm);
                if (finishTime < earliestFinishTime) {
                    earliestFinishTime = finishTime;
                    bestVm = vm;
                }
            }

            if (bestVm != null) {
                double startTime = calculateEarliestStartTime(task, bestVm);
                double actualStartTime = Math.max(startTime, vmAvailableTime.get(bestVm));
                double executionTime = calculateExecutionTime(task, bestVm);
                double finishTime = actualStartTime + executionTime;

                taskToVmMapping.put(task, bestVm);
                taskStartTime.put(task, actualStartTime);
                taskFinishTime.put(task, finishTime);

                // Update VM available time
                vmAvailableTime.put(bestVm, finishTime);
            }
        }
    }

    /**
     * Calculates average computation cost across all VMs
     */
    private double calculateAverageComputationCost(Task task) {
        double totalCost = 0.0;
        for (Vm vm : availableVMs) {
            totalCost += calculateExecutionTime(task, vm);
        }
        return totalCost / availableVMs.size();
    }

    /**
     * Calculates average communication cost between two tasks
     */
    private double calculateAverageCommunicationCost(Task source, Task target) {
        // Use a more realistic communication cost model
        double dataSize = source.getOutputSize(); // bytes
        double bandwidth = 1_000_000; // 1 MB/s (more realistic for edge computing)
        return dataSize / bandwidth; // seconds
    }

    /**
     * Calculates communication cost between two tasks on specific VMs
     */
    private double calculateCommunicationCost(Task source, Task target, Vm sourceVm, Vm targetVm) {
        if (sourceVm.equals(targetVm)) {
            return 0.0; // No communication cost if on same VM
        }
        return calculateAverageCommunicationCost(source, target);
    }

    /**
     * Calculates execution time of a task on a specific VM
     */
    private double calculateExecutionTime(Task task, Vm vm) {
        double mips = vm.getMips();
        double instructions = task.getLength(); // Assume task.getLength() is already in instructions
        return instructions / mips; // seconds
    }

    /**
     * Calculates earliest finish time for a task on a VM
     */
    private double calculateEarliestFinishTime(Task task, Vm vm) {
        double startTime = calculateEarliestStartTime(task, vm);
        double vmAvailable = vmAvailableTime.get(vm);
        double actualStartTime = Math.max(startTime, vmAvailable);
        double executionTime = calculateExecutionTime(task, vm);
        return actualStartTime + executionTime;
    }

    /**
     * Calculates earliest start time for a task on a VM
     */
    private double calculateEarliestStartTime(Task task, Vm vm) {
        double readyTime = 0.0;

        // Consider dependencies
        for (DefaultEdge edge : taskGraph.incomingEdgesOf(task)) {
            Task predecessor = taskGraph.getEdgeSource(edge);
            if (taskFinishTime.containsKey(predecessor)) {
                double predecessorFinishTime = taskFinishTime.get(predecessor);
                double communicationTime = 0.0;

                // Add communication time if tasks are on different VMs
                Vm predecessorVm = taskToVmMapping.get(predecessor);
                if (predecessorVm != null && !predecessorVm.equals(vm)) {
                    communicationTime = calculateCommunicationCost(predecessor, task, predecessorVm, vm);
                }

                readyTime = Math.max(readyTime, predecessorFinishTime + communicationTime);
            }
        }

        return readyTime;
    }

    // Getters for analysis
    public Map<Task, Double> getUpwardRank() { return new HashMap<>(upwardRank); }
    public Map<Task, Double> getDownwardRank() { return new HashMap<>(downwardRank); }
    public Map<Task, Double> getTaskStartTime() { return new HashMap<>(taskStartTime); }
    public Map<Task, Double> getTaskFinishTime() { return new HashMap<>(taskFinishTime); }
    public Map<Vm, Double> getVmAvailableTime() { return new HashMap<>(vmAvailableTime); }
}
