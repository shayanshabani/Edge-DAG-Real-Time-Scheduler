# 🚦 Comparative Analysis of CPOP and PSO for DAG Task Scheduling in Edge Computing

<div align="center">



### A comprehensive simulation toolkit for scheduling dependent workflows on heterogeneous edge infrastructure. This project implements and contrasts a classic heuristic (CPOP) with a metaheuristic (PSO) to explore the trade-offs between makespan, energy, and Quality of Service.

<br>

<p>
  <img src="https://img.shields.io/badge/Language-Java_11+-F89820?style=for-the-badge&logo=java&logoColor=white" alt="Language: Java">
  <img src="https://img.shields.io/badge/Simulation-CloudSimPlus-1E90FF?style=for-the-badge" alt="CloudSimPlus">
  <img src="https://img.shields.io/badge/Status-Complete-28A745?style=for-the-badge" alt="Status: Complete">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License: MIT">
</p>
</div>

---

## 🌟 Project Overview

In edge computing, efficiently scheduling complex workflows—represented as **Directed Acyclic Graphs (DAGs)**—is a critical challenge. The goal is to assign computational tasks to a diverse set of edge devices (VMs) to optimize for multiple, often conflicting, objectives.

This project provides a robust, simulation-based framework to tackle this problem by implementing and evaluating two distinct scheduling philosophies:

1.  **CPOP (Critical-Path On a Processor):** A fast, deterministic heuristic that prioritizes minimizing the longest execution path in the workflow.
2.  **PSO (Particle Swarm Optimization):** A powerful metaheuristic that explores a vast solution space to co-optimize for a blend of objectives, including **makespan**, **energy consumption**, and **Quality of Service (QoS)**.

Our findings reveal a crucial trade-off: the "best" algorithm is entirely dependent on the specific performance goals of the edge application.

---

## 📊 Key Findings at a Glance

Our experiments, conducted using the **CloudSimPlus** framework, yielded a nuanced and insightful comparison. The choice of the "best" algorithm is not straightforward and depends entirely on the optimization goal.

| Metric | CPOP (Heuristic) | PSO (Metaheuristic) | Insight |
| :--- | :---: | :---: | :--- |
| **Makespan** (Speed) | 🏆 **Winner** | Slower | CPOP's rigid, speed-focused strategy is superior for raw completion time. |
| **Energy Consumption** | Higher | 🏆 **Winner** | PSO intelligently assigns tasks to power-efficient VMs, even if they are slower. |
| **Quality of Service (QoS)** | Lower | 🏆 **Winner** | PSO excels at finding balanced solutions that improve load distribution and stability. |
| **Scheduling Overhead** | ⚡ **Negligible** | High | CPOP is near-instantaneous, while PSO's iterative search is computationally expensive. |

<br>

<details>
<summary><strong>Click to view the comparative performance charts</strong></summary>

| Makespan Comparison (Lower is Better) | Energy Comparison (Lower is Better) |
| :---: | :---: |
| <img src="/results/charts/makespan_comparison_ieee.png" alt="Makespan Comparison Chart"> | <img src="/results/charts/energy_comparison_ieee.png" alt="Energy Comparison Chart"> |

| QoS Comparison (Higher is Better) | Scalability (Lower is Better) |
| :---: | :---: |
| <img src="/results/charts/qos_comparison_ieee.png" alt="QoS Comparison Chart"> | <img src="/results/charts/scalability_analysis_ieee.png" alt="Scalability Chart"> |

</details>

<br>

---

## 🏗️ Architectural Design

The project is built with a modular and extensible architecture, making it ideal for future research and experimentation.

-   **`Core Models`**: `Task.java` and `DAGGenerator.java` define the fundamental workflow structures and procedurally generate random DAGs for testing.
-   **`EdgeEnvironment.java`**: Configures the CloudSimPlus simulation, creating hosts and a heterogeneous pool of VMs to model the edge infrastructure.
-   **`Scheduler Implementations`**: The heart of the project.
    -   `CPOPScheduler.java`: Implements the rank calculation, critical path identification, and task-to-VM mapping logic for the CPOP heuristic.
    -   `PSOScheduler.java`: Implements the swarm initialization, iterative fitness evaluation, and particle update loop for the PSO metaheuristic.
-   **`EdgeSchedulingSimulation.java`**: The main driver that orchestrates the experiments—running both schedulers on a common DAG, launching the simulations, and aggregating the results for comparison.

---

## 🚀 Getting Started

### Prerequisites
*   Java Development Kit (JDK) 11 or higher
*   Apache Maven

### Installation & Execution

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/edge-dag-scheduling.git
    cd edge-dag-scheduling
    ```

2.  **Build the project using Maven:**
    This command will download all dependencies (like CloudSimPlus) and compile the source code.
    ```bash
    mvn clean package
    ```

3.  **Run the simulation:**
    Execute the packaged JAR file to run the full comparative simulation.
    ```bash
    java -jar target/EdgeScheduling-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```
    The console will output the progress and final results of the comparison.

---

## 🧪 Extending This Project

This codebase is designed to be a foundation for further research. Here are some ideas:

-   **Tune PSO Parameters:** Conduct a sensitivity analysis on PSO's population size, inertia weight, and fitness function weights to see if its makespan performance can be improved.
-   **Develop a Hybrid Algorithm:** Create a new scheduler that uses CPOP to generate a high-quality initial solution for the PSO swarm. This could offer the best of both worlds!
-   **Implement Other Algorithms:** Add other classic heuristics (e.g., HEFT) or metaheuristics (e.g., Genetic Algorithm, Ant Colony Optimization) for a broader comparison.
-   **Introduce Realistic Network Models:** Enhance `EdgeEnvironment.java` to simulate network latency and bandwidth constraints between different edge locations.

---

## 🤝 Contributing

Pull requests are warmly welcome! For major changes or new features, please open an issue first to discuss what you would like to change. If you use or adapt this project for your research, a citation or acknowledgment would be greatly appreciated.

---

## 👤 Maintainers

| [<img src="https://avatars.githubusercontent.com/u/81723821?v=4" width="100px;"/><br /><sub>Dorsa Ghobadi</sub>](https://github.com/dorsaaaa) | [<img src="https://avatars.githubusercontent.com/u/61823522?v=4" width="100px;"/><br /><sub>Mohammadshayan Shabani</sub>](https://github.com/shayanshabani) |
| :---: | :---: |
| *Computer Engineering Student* | *Computer Engineering Student* |

---

## 📚 References

-   Topcuoglu, H., Hariri, S., & Wu, M.-Y. (2002). [Performance-Effective and Low-Complexity Task Scheduling for Heterogeneous Computing](https://doi.org/10.1109/71.877944). *IEEE Transactions on Parallel and Distributed Systems*, 13(3), 260-274.

---

<div align="center">
  <em>Happy Scheduling! ⚡</em>
</div>
