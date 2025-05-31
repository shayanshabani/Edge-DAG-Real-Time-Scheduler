package com.edgescheduling;

import com.edgescheduling.algorithms.CPOPScheduler;
import com.edgescheduling.environment.EdgeEnvironment;
import com.edgescheduling.model.DAGGenerator;
import com.edgescheduling.model.Task;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.Vm;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Main class for Edge Computing Task Scheduling Simulation
 */
public class EdgeSchedulingSimulation {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        System.out.println("=== Edge Computing Task Scheduling Simulation ===");
        System.out.println("Phase 1: CPOP Algorithm Implementation\n");

        // Test with different DAG sizes
        int[] taskCounts = {100, 200, 300};
        int[] edgeCounts = {10, 20, 30};

        for (int tasks : taskCounts) {
            for (int edges : edgeCounts) {
                System.out.printf("Testing with %d tasks and %d edges:\n", tasks, edges);
                runSimulation(tasks, edges);
                System.out.println();
            }
        }
    }

    private static void runSimulation(int numTasks, int numEdges) {
        try {
            // Create edge environment
            EdgeEnvironment environment = new EdgeEnvironment();

            // Generate DAG
            DAGGenerator dagGenerator = new DAGGenerator(42); // Fixed seed for reproducibility
            Graph<Task, DefaultEdge> taskGraph = dagGenerator.generateDAG(numTasks, numEdges);

            System.out.printf("Generated DAG: %d tasks, %d edges\n",
                    taskGraph.vertexSet().size(), taskGraph.edgeSet().size());

            // Get available VMs
            List<Vm> availableVMs = environment.getEdgeVMs();
            System.out.printf("Available VMs: %d\n", availableVMs.size());

            // Run CPOP scheduling
            CPOPScheduler cpopScheduler = new CPOPScheduler(taskGraph, availableVMs);
            long startTime = System.currentTimeMillis();
            Map<Task, Vm> scheduling = cpopScheduler.schedule();
            long endTime = System.currentTimeMillis();

            // Create and submit cloudlets based on scheduling
            createAndSubmitCloudlets(taskGraph, scheduling, environment);

            // Run simulation
            environment.getSimulation().start();

            // Analyze results
            analyzeResults(cpopScheduler, scheduling, endTime - startTime);

        } catch (Exception e) {
            System.err.println("Error in simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAndSubmitCloudlets(Graph<Task, DefaultEdge> taskGraph,
                                                 Map<Task, Vm> scheduling,
                                                 EdgeEnvironment environment) {
        for (Task task : taskGraph.vertexSet()) {
            CloudletSimple cloudlet = new CloudletSimple(
                    task.getId(),
                    task.getLength(),
                    1 // Number of PEs required
            );

            cloudlet.setFileSize(task.getFileSize());
            cloudlet.setOutputSize(task.getOutputSize());
            cloudlet.setUtilizationModelCpu(new UtilizationModelDynamic(0.1));
            cloudlet.setUtilizationModelRam(new UtilizationModelDynamic(0.1));
            cloudlet.setUtilizationModelBw(new UtilizationModelDynamic(0.1));

            task.setCloudlet(cloudlet);

            // Submit to broker
            environment.getBroker().submitCloudlet(cloudlet);

            // Bind to specific VM
            Vm assignedVm = scheduling.get(task);
            environment.getBroker().bindCloudletToVm(cloudlet, assignedVm);
        }
    }

    private static void analyzeResults(CPOPScheduler scheduler, Map<Task, Vm> scheduling, long executionTime) {
        System.out.println("=== CPOP Algorithm Results ===");
        System.out.printf("Scheduling computation time: %d ms\n", executionTime);

        // Calculate makespan
        double makespan = 0.0;
        for (double finishTime : scheduler.getTaskFinishTime().values()) {
            makespan = Math.max(makespan, finishTime);
        }
        System.out.printf("Makespan: %.2f seconds\n", makespan);

        // VM utilization
        Map<Vm, Integer> vmTaskCount = new java.util.HashMap<>();
        for (Vm vm : scheduling.values()) {
            vmTaskCount.put(vm, vmTaskCount.getOrDefault(vm, 0) + 1);
        }

        System.out.println("VM Task Distribution:");
        for (Map.Entry<Vm, Integer> entry : vmTaskCount.entrySet()) {
            System.out.printf("  VM %d: %d tasks\n", entry.getKey().getId(), entry.getValue());
        }

        // Average task execution time
        double totalExecutionTime = 0.0;
        int taskCount = 0;
        for (Map.Entry<Task, Double> startEntry : scheduler.getTaskStartTime().entrySet()) {
            Task task = startEntry.getKey();
            double startTime = startEntry.getValue();
            double finishTime = scheduler.getTaskFinishTime().get(task);
            totalExecutionTime += (finishTime - startTime);
            taskCount++;
        }

        if (taskCount > 0) {
            System.out.printf("Average task execution time: %.2f seconds\n",
                    totalExecutionTime / taskCount);
        }

        System.out.println("=== End Results ===");
    }
}
