# Real-Time Scheduling of Dependent Tasks in Edge Computing Environments: Project Report

## 1. Problem Definition and Approach

### 1.1 Problem Statement

Edge computing environments require efficient scheduling of complex, interdependent tasks to meet real-time constraints while optimizing energy consumption and Quality of Service (QoS). The challenge lies in scheduling task graphs (Directed Acyclic Graphs - DAGs) across heterogeneous edge devices with varying computational capabilities, power consumption characteristics, and communication delays.

### 1.2 Task Model

Tasks are modeled as a **Directed Acyclic Graph (DAG)** where:
- **Nodes** represent computational tasks with specific resource requirements
- **Edges** represent data dependencies between tasks
- Each task has computational cost (MIPS required) and data transfer requirements
- Tasks must be executed in dependency order (precedence constraints)

### 1.3 CPOP Algorithm Overview

The **Critical Path on a Processor (CPOP)** algorithm is a list-based heuristic scheduling algorithm that works in two phases:

1. **Priority Assignment Phase**: 
   - Calculates upward rank for each task using the formula:
   $$\text{rank}_u(n_i) = \overline{w_i} + \max_{n_j \in \text{succ}(n_i)} (\overline{c_{i,j}} + \text{rank}_u(n_j))$$
   - Where $\overline{w_i}$ is average computation time and $\overline{c_{i,j}}$ is average communication time

2. **Task Selection and Assignment Phase**:
   - Tasks are sorted by priority (descending upward rank)
   - Each task is assigned to the processor that minimizes its Earliest Finish Time (EFT)

### 1.4 Edge Computing Environment Model

The edge environment consists of:
- **Heterogeneous edge devices** with different processing capabilities (MIPS), cores, power consumption profiles
- **Communication network** with varying delays and bandwidth between devices
- **Energy model** considering both idle and active power consumption
- **QoS metrics** based on deadline satisfaction and load balancing

## 2. Code Architecture and Implementation

### 2.1 Package Structure

The implementation is organized into four main packages:

#### 2.1.1 `com.edgescheduling.model` Package

**TaskNode Class:**
- **Purpose**: Represents individual tasks in the DAG
- **Key Functions**:
  - `addDependency(TaskNode dependency)`: Establishes precedence relationships
  - `isReady()`: Checks if all dependencies are satisfied
  - `getComputationCost()`: Returns MIPS requirement
  - `getDataSize()`: Returns data transfer requirement

**EdgeDevice Class:**
- **Purpose**: Models heterogeneous edge computing devices
- **Key Functions**:
  - `calculateExecutionTime(TaskNode task)`: Computes execution time using $\text{time} = \frac{\text{computationCost}}{\text{MIPS}}$
  - `calculateEnergyConsumption(TaskNode task)`: Estimates energy using power model:
    $$E = (P_{\text{idle}} + (P_{\text{max}} - P_{\text{idle}}) \times \text{utilization}) \times \text{time}$$
  - `scheduleTask(TaskNode task, double startTime)`: Assigns task to device

#### 2.1.2 `com.edgescheduling.environment` Package

**EdgeEnvironment Class:**
- **Purpose**: Manages the complete edge computing infrastructure
- **Key Functions**:
  - `createEdgeDevices(int numDevices)`: Generates heterogeneous devices with random characteristics
  - `initializeCommunicationMatrix()`: Sets up communication delays between devices
  - `getCommunicationTime(int from, int to, double dataSize)`: Calculates data transfer time:
    $$\text{commTime} = \text{delay} + \frac{\text{dataSize}}{\min(\text{BW}_{\text{from}}, \text{BW}_{\text{to}})}$$

#### 2.1.3 `com.edgescheduling.algorithm` Package

**CPOPScheduler Class:**
- **Purpose**: Implements the CPOP scheduling algorithm
- **Key Functions**:
  - `calculateUpwardRank(List<TaskNode> tasks)`: Computes priority for all tasks
  - `calculateUpwardRankRecursive(TaskNode task, Map<TaskNode, Double> cache)`: Recursive rank calculation with memoization
  - `scheduleTask(TaskNode task)`: Finds optimal device assignment using EFT heuristic
  - `calculateDataReadyTime(TaskNode task, int deviceId)`: Determines when input data is available
  - `calculateResults(List<TaskNode> tasks)`: Computes performance metrics

#### 2.1.4 `com.edgescheduling.util` Package

**TaskGraphGenerator Class:**
- **Purpose**: Creates synthetic DAGs for testing
- **Key Functions**:
  - `generateDAG(int numTasks, int numEdges)`: Creates random task graphs with specified characteristics
  - Ensures DAG properties (no cycles) and realistic task parameters

### 2.2 Core Algorithm Implementation

The CPOP implementation follows these steps:

1. **Initialization**: Reset all tasks and devices to clean state
2. **Rank Calculation**: Compute upward rank for priority assignment
3. **Task Sorting**: Order tasks by decreasing priority
4. **Scheduling Loop**: For each task, find device with minimum EFT
5. **Result Calculation**: Compute makespan, energy, and QoS metrics

## 3. Results Analysis and Interpretation

### 3.1 Environment Configuration

The simulation uses 10 heterogeneous edge devices with varying characteristics:
- **Processing Power**: 1,181 - 2,597 MIPS (heterogeneous capabilities)
- **Cores**: 2-4 cores per device
- **Power Consumption**: 10.3-18.2 W (idle) to 60.5-97.1 W (maximum)
- **Bandwidth**: 54.0-97.5 MB/s (network capabilities)

### 3.2 Performance Metrics Analysis

#### 3.2.1 Makespan Analysis

**DAG with 10 edges:**
- Makespan increases linearly with task count: 4.11s (100 tasks) → 15.30s (500 tasks)
- Growth rate: ~0.022s per additional task
- Indicates good scalability for sparse DAGs

**DAG with 20 edges:**
- Similar performance to 10 edges for most configurations
- Slight increase for 100 tasks (5.92s vs 4.11s) due to increased dependencies

**DAG with 30 edges:**
- Notable increase for smaller task sets (100 tasks: 9.23s)
- Converges to similar performance for larger task sets
- Suggests dependency bottlenecks in dense DAGs with fewer tasks

#### 3.2.2 Energy Consumption Analysis

**Energy scaling pattern:**
- Nearly linear increase with task count across all configurations
- 100 tasks: ~640-660 J
- 500 tasks: ~3,200-3,220 J
- Energy efficiency: ~6.4 J per task (consistent across scales)

**Edge density impact:**
- Minimal variation in total energy consumption
- Slight fluctuations due to different task-to-device mappings
- CPOP's EFT heuristic maintains energy efficiency

#### 3.2.3 Quality of Service (QoS) Analysis

**Excellent QoS performance:**
- 97.88% - 100.00% QoS across all configurations
- Higher edge density slightly reduces QoS for smaller task sets
- QoS improvement with larger task sets (better load distribution)

**QoS components:**
- **Deadline satisfaction**: Tasks complete within flexible deadlines
- **Load balancing**: Even distribution across devices prevents bottlenecks

### 3.3 Algorithm Performance Insights

#### 3.3.1 Scalability

The CPOP algorithm demonstrates excellent scalability:
- **Linear makespan growth** indicates predictable performance
- **Consistent energy efficiency** across different scales
- **Maintained QoS** even with increased complexity

#### 3.3.2 Edge Density Impact

- **Sparse DAGs (10 edges)**: Optimal performance due to minimal dependencies
- **Medium DAGs (20 edges)**: Slight performance degradation
- **Dense DAGs (30 edges)**: More pronounced impact on smaller task sets

#### 3.3.3 Load Distribution

The heterogeneous device utilization shows:
- Effective use of high-performance devices (Device 2: 2,566 MIPS, Device 3: 2,597 MIPS)
- Balanced assignment considering both computation and communication costs
- Power-aware scheduling maintaining energy efficiency

### 3.4 Key Findings

1. **CPOP Effectiveness**: The algorithm successfully handles real-time constraints while maintaining energy efficiency

2. **Heterogeneity Benefits**: Diverse device capabilities allow for optimized task-device matching

3. **Dependency Management**: The upward rank calculation effectively handles complex task dependencies

4. **Scalability**: Linear performance scaling makes the approach suitable for large-scale edge deployments

5. **QoS Achievement**: Consistently high QoS scores demonstrate the algorithm's ability to meet service requirements

## 4. Conclusion

The implementation successfully demonstrates the CPOP algorithm's effectiveness for task scheduling in edge computing environments. The results show excellent scalability, energy efficiency, and QoS performance across various DAG configurations. The foundation is now ready for Part 2, where PSO-based optimization will be implemented for comparative analysis and potential performance improvements.

The next phase will focus on developing the PSO algorithm to potentially achieve better energy-QoS trade-offs and handling more complex optimization scenarios in edge computing environments.