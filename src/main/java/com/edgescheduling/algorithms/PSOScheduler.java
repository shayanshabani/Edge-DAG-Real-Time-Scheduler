package com.edgescheduling.algorithms;

import com.edgescheduling.model.Task;
import org.cloudsimplus.vms.Vm;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PSOScheduler {
    private final Graph<Task, DefaultEdge> taskGraph;
    private final List<Vm> availableVMs;
    private final int swarmSize;
    private final int maxIterations;

    private double inertiaWeight;
    private final double wMax = 0.9;
    private final double wMin = 0.4;
    private final double c1;
    private final double c2;

    private double w1 = 0.7; // makespan weight
    private double w2 = 0.2; // energy weight
    private double w3 = 0.1; // load balance weight

    private List<Particle> swarm;
    private Particle globalBest;
    private final Random random;

    private final Map<Task, Double> taskStartTime;
    private final Map<Task, Double> taskFinishTime;
    private final Map<Vm, Double> vmAvailableTime;
    private final Map<Task, Integer> taskOrder;

    private double maxPossibleMakespan;
    private double maxPossibleEnergy;
    private double maxPossibleLoadBalance;

    private final List<Double> convergenceHistory;
    private int stagnationCounter;
    private double previousBestFitness;

    public PSOScheduler(Graph<Task, DefaultEdge> taskGraph,
                        List<Vm> availableVMs) {
        this(taskGraph, availableVMs, 100, 300, 0.9, 2.0, 2.0);
    }

    public PSOScheduler(Graph<Task, DefaultEdge> taskGraph,
                        List<Vm> availableVMs,
                        int swarmSize,
                        int maxIterations,
                        double initialInertia,
                        double c1,
                        double c2) {
        this.taskGraph      = taskGraph;
        this.availableVMs   = new ArrayList<>(availableVMs);
        this.swarmSize      = swarmSize;
        this.maxIterations  = maxIterations;
        this.inertiaWeight  = initialInertia;
        this.c1             = c1;
        this.c2             = c2;
        this.random         = ThreadLocalRandom.current();

        this.taskStartTime      = new HashMap<>();
        this.taskFinishTime     = new HashMap<>();
        this.vmAvailableTime    = new HashMap<>();
        this.taskOrder          = new HashMap<>();
        this.convergenceHistory = new ArrayList<>();
        this.stagnationCounter  = 0;
        this.previousBestFitness= Double.MAX_VALUE;

        for (Vm vm : availableVMs) {
            vmAvailableTime.put(vm, 0.0);
        }

        TopologicalOrderIterator<Task, DefaultEdge> topoIter =
                new TopologicalOrderIterator<>(taskGraph);
        int idx = 0;
        while (topoIter.hasNext()) {
            Task t = topoIter.next();
            taskOrder.put(t, idx++);
        }

        calculateNormalizationFactors();
    }

    private void calculateNormalizationFactors() {
        double minMips = availableVMs.stream()
                .mapToDouble(Vm::getMips)
                .min()
                .orElse(1.0);
        double totalLength = taskGraph.vertexSet()
                .stream()
                .mapToDouble(Task::getLength)
                .sum();
        maxPossibleMakespan = totalLength / minMips;

        double maxMips = availableVMs.stream()
                .mapToDouble(Vm::getMips)
                .max()
                .orElse(1.0);
        double peakPower = maxMips * 0.0001 + 10.0; // Watts
        maxPossibleEnergy = (totalLength / maxMips) * peakPower;

        maxPossibleLoadBalance = maxPossibleMakespan;
    }

    public Map<Task, Vm> schedule() {
        if (taskGraph.vertexSet().isEmpty()) {
            return Collections.emptyMap();
        }

        initializeSwarm();
        for (int iter = 0; iter < maxIterations; iter++) {
            for (Particle p : swarm) {
                evaluateFitness(p);
                if (p.fitness < p.bestFitness) {
                    p.bestFitness = p.fitness;
                    System.arraycopy(p.position, 0, p.bestPosition, 0, p.position.length);
                }
            }

            Particle bestInSwarm = Collections.min(swarm, Comparator.comparingDouble(x -> x.bestFitness));
            if (bestInSwarm.bestFitness < globalBest.bestFitness) {
                globalBest = bestInSwarm.copy();
            }

            convergenceHistory.add(globalBest.bestFitness);

            if (Math.abs(previousBestFitness - globalBest.bestFitness) < 1e-6) {
                stagnationCounter++;
            } else {
                stagnationCounter = 0;
            }
            previousBestFitness = globalBest.bestFitness;

            if (stagnationCounter > 30) {
                mutateWorstParticles();
                stagnationCounter = 0;
            }

            inertiaWeight = wMax - ((wMax - wMin) * iter / (double) (maxIterations - 1));

            for (Particle p : swarm) {
                for (int d = 0; d < p.position.length; d++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();
                    p.velocity[d] = inertiaWeight * p.velocity[d]
                            + c1 * r1 * (p.bestPosition[d] - p.position[d])
                            + c2 * r2 * (globalBest.bestPosition[d] - p.position[d]);
                    double vmax = availableVMs.size();
                    if (p.velocity[d] > vmax) p.velocity[d] = vmax;
                    if (p.velocity[d] < -vmax) p.velocity[d] = -vmax;

                    p.position[d] += p.velocity[d];
                    if (p.position[d] < 0) {
                        p.position[d] = -p.position[d];
                        p.velocity[d] = -p.velocity[d];
                    }
                    if (p.position[d] >= availableVMs.size()) {
                        p.position[d] = 2 * availableVMs.size() - p.position[d];
                        p.velocity[d] = -p.velocity[d];
                    }
                }
            }

        }

        return convertToMapping(globalBest);
    }

    private void initializeSwarm() {
        List<Task> tasks = new ArrayList<>(taskGraph.vertexSet());
        tasks.sort(Comparator.comparing(taskOrder::get));

        swarm = new ArrayList<>(swarmSize);
        for (int i = 0; i < swarmSize; i++) {
            Particle p = new Particle(tasks.size());
            if (i < swarmSize/3) {
                initEarliestFinishHeuristic(p, tasks);
            } else if (i < 2*swarmSize/3) {
                initLoadBalanceHeuristic(p, tasks);
            } else {
                for (int d = 0; d < tasks.size(); d++) {
                    p.position[d] = random.nextDouble() * availableVMs.size();
                }
            }
            for (int d = 0; d < tasks.size(); d++) {
                p.velocity[d] = (random.nextDouble() - 0.5) * availableVMs.size() * 0.2;
            }
            System.arraycopy(p.position, 0, p.bestPosition, 0, p.position.length);
            swarm.add(p);
        }

        globalBest = swarm.get(0).copy();
        evaluateFitness(globalBest);
        globalBest.bestFitness = globalBest.fitness;
    }

    private void initEarliestFinishHeuristic(Particle p, List<Task> tasks) {
        Map<Vm,Double> vmFinish = new HashMap<>();
        for (Vm vm: availableVMs) vmFinish.put(vm,0.0);

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            double bestTime = Double.MAX_VALUE;
            int bestVmIdx=0;
            for (int v=0; v<availableVMs.size(); v++){
                Vm vm = availableVMs.get(v);
                double exec = t.getLength()/vm.getMips();
                double fin = vmFinish.get(vm)+exec;
                if (fin<bestTime){
                    bestTime=fin; bestVmIdx=v;
                }
            }
            p.position[i] = bestVmIdx + random.nextDouble()*0.05;
            vmFinish.put(availableVMs.get(bestVmIdx), bestTime);
        }
    }

    private void initLoadBalanceHeuristic(Particle p, List<Task> tasks) {
        Map<Vm,Double> vmLoad = new HashMap<>();
        for (Vm vm: availableVMs) vmLoad.put(vm,0.0);

        for (int i=0; i<tasks.size(); i++){
            Task t=tasks.get(i);
            double minLoad = Double.MAX_VALUE;
            int bestVid=0;
            for (int v=0; v<availableVMs.size(); v++){
                Vm vm=availableVMs.get(v);
                if (vmLoad.get(vm)<minLoad){
                    minLoad=vmLoad.get(vm);
                    bestVid=v;
                }
            }
            p.position[i] = bestVid + random.nextDouble()*0.05;
            double exec= t.getLength()/availableVMs.get(bestVid).getMips();
            vmLoad.put(availableVMs.get(bestVid), vmLoad.get(availableVMs.get(bestVid))+exec);
        }
    }

    private void evaluateFitness(Particle p) {
        Map<Task,Vm> mapping = convertToMapping(p);

        double makespan = calculateMakespan(mapping);
        double energy   = calculateEnergy(mapping);
        double balance  = calculateLoadBalance(mapping);

        double nm = makespan / maxPossibleMakespan;
        double ne = energy    / maxPossibleEnergy;
        double nb = balance   / maxPossibleLoadBalance;

        p.fitness = w1*nm + w2*ne + w3*nb;
    }

    private Map<Task,Vm> convertToMapping(Particle p){
        List<Task> tasks = new ArrayList<>(taskGraph.vertexSet());
        tasks.sort(Comparator.comparing(taskOrder::get));
        Map<Task,Vm> map = new HashMap<>(tasks.size());
        for (int i=0; i<tasks.size(); i++){
            int vid = (int)Math.floor(p.position[i]);
            vid = Math.max(0, Math.min(vid, availableVMs.size()-1));
            map.put(tasks.get(i), availableVMs.get(vid));
        }
        updateTimings(map);
        return map;
    }

    private void updateTimings(Map<Task,Vm> map){
        taskStartTime.clear();
        taskFinishTime.clear();
        for (Vm vm: availableVMs) vmAvailableTime.put(vm,0.0);

        List<Task> st = new ArrayList<>(taskGraph.vertexSet());
        st.sort(Comparator.comparing(taskOrder::get));
        for (Task t: st){
            Vm vm = map.get(t);
            double ready = vmAvailableTime.get(vm);
            for (DefaultEdge e: taskGraph.incomingEdgesOf(t)){
                Task pre=taskGraph.getEdgeSource(e);
                double comm = map.get(pre).equals(vm)? 0.0 : pre.getOutputSize()/1_000_000.0;
                ready = Math.max(ready, taskFinishTime.get(pre)+comm);
            }
            double exec = t.getLength()/vm.getMips();
            taskStartTime.put(t, ready);
            taskFinishTime.put(t, ready+exec);
            vmAvailableTime.put(vm, ready+exec);
        }
    }

    private double calculateMakespan(Map<Task,Vm> map){
        return taskFinishTime.values().stream().mapToDouble(x->x).max().orElse(0.0);
    }

    private double calculateEnergy(Map<Task,Vm> map){
        double total=0.0;
        for (Task t: taskGraph.vertexSet()){
            Vm vm=map.get(t);
            double exec=t.getLength()/vm.getMips();
            double power=vm.getMips()*0.0001 + 10.0;
            total += power * exec;
        }
        return total;
    }

    private double calculateLoadBalance(Map<Task,Vm> map){
        double mean = maxPossibleLoadBalance/availableVMs.size(); // not used
        double sum=0.0;
        for (Vm vm: availableVMs){
            double busy = vmAvailableTime.get(vm);
            sum += Math.pow(busy - (calculateMakespan(map)/availableVMs.size()),2);
        }
        return Math.sqrt(sum/availableVMs.size());
    }

    private void mutateWorstParticles(){
        swarm.stream()
                .sorted(Comparator.comparingDouble(p->-p.fitness))
                .limit(swarmSize/5)
                .forEach(p->{
                    int d = random.nextInt(p.position.length);
                    p.position[d] = random.nextDouble() * availableVMs.size();
                });
    }

    public Map<Task,Double> getTaskStartTime(){
        return Collections.unmodifiableMap(taskStartTime);
    }

    public Map<Task,Double> getTaskFinishTime(){
        return Collections.unmodifiableMap(taskFinishTime);
    }

    public List<Double> getConvergenceHistory(){
        return new ArrayList<>(convergenceHistory);
    }

    private static class Particle {
        final double[] position;
        final double[] velocity;
        final double[] bestPosition;
        double fitness;
        double bestFitness;

        Particle(int dim){
            position      = new double[dim];
            velocity      = new double[dim];
            bestPosition  = new double[dim];
            fitness       = Double.MAX_VALUE;
            bestFitness   = Double.MAX_VALUE;
        }

        Particle copy(){
            Particle c = new Particle(position.length);
            System.arraycopy(position,0,c.position,0,position.length);
            System.arraycopy(velocity,0,c.velocity,0,velocity.length);
            System.arraycopy(bestPosition,0,c.bestPosition,0,bestPosition.length);
            c.fitness     = fitness;
            c.bestFitness = bestFitness;
            return c;
        }
    }
}
