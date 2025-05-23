package com.edgescheduling;

import com.edgescheduling.algorithm.CPOPScheduler;
import com.edgescheduling.environment.EdgeEnvironment;
import com.edgescheduling.model.TaskNode;
import com.edgescheduling.util.TaskGraphGenerator;

import java.util.List;
import java.util.Locale;

public class EdgeSchedulingSimulation {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        System.out.println("=== Edge Computing Task Scheduling Simulation ===\n");

        // Test configurations as specified in requirements
        int[] taskCounts = {100, 200, 300, 400, 500};
        int[] edgeCounts = {10, 20, 30};

        System.out.println("CPOP Algorithm Performance Analysis");
        System.out.println("=" + "=".repeat(60));

        // Create edge environment with 10 devices
        EdgeEnvironment environment = new EdgeEnvironment(10, 42);
        environment.printEnvironmentInfo();
        System.out.println();

        for (int edges : edgeCounts) {
            System.out.printf("DAG Configuration: %d edges\n", edges);
            System.out.println("-".repeat(70));
            System.out.printf("%-6s | %-10s | %-12s | %-8s\n",
                    "Tasks", "Makespan", "Energy (J)", "QoS (%)");
            System.out.println("-".repeat(70));

            for (int tasks : taskCounts) {
                runCPOPSimulation(environment, tasks, edges);
            }
            System.out.println();
        }

        System.out.println("=== Basic Modeling and Implementation Complete ===");
        System.out.println("Next: Implement PSO-based algorithm for comparison");
    }

    private static void runCPOPSimulation(EdgeEnvironment environment, int numTasks, int numEdges) {
        // Generate task graph
        TaskGraphGenerator generator = new TaskGraphGenerator(42);
        List<TaskNode> tasks = generator.generateDAG(numTasks, numEdges);

        // Run CPOP algorithm
        CPOPScheduler scheduler = new CPOPScheduler(environment);
        CPOPScheduler.SchedulingResult result = scheduler.schedule(tasks);

        // Print results in table format
        System.out.printf("%-6d | %-10.2f | %-12.2f | %-8.2f\n",
                numTasks,
                result.getMakespan(),
                result.getEnergyConsumption(),
                result.getQos() * 100);
    }
}
