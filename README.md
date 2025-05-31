# 🚦 Edge Computing DAG Scheduling with CPOP

<div align="center">

<img src="https://img.shields.io/badge/CloudSimPlus-8.0.0-blue?logo=java&logoColor=white" alt="CloudSimPlus">
<img src="https://img.shields.io/badge/JGraphT-1.5.1-brightgreen?logo=java&logoColor=white">
<img src="https://img.shields.io/badge/Status-Course%20Project-yellow">
<img src="https://img.shields.io/badge/License-MIT-blue.svg">

</div>

---

**A scalable edge computing simulation toolkit for static DAG workflow scheduling — featuring a clean, reproducible Java implementation of the classic [CPOP](https://doi.org/10.1109/71.877944) (Critical Path On a Processor) heuristic, with focus on load balancing, makespan minimization, and extensibility for future QoS/energy-aware research.**

---

## ✨ Project Highlights

- 📉 **Static scheduling of dependent tasks (DAGs) across heterogeneous edge devices**
- 🕸️ **CPOP scheduling heuristic**: upward/downward rank calculation, critical path binding, and load-aware non-critical task allocation
- ⚡ **CloudSimPlus** and **JGraphT** for realistic simulation and reproducibility
- 📊 Modular, research-friendly code; easy to extend for metaheuristics (e.g., PSO), heterogeneous links, or energy/QoS models

---

## 🧩 Problem Statement

Given a DAG of dependent tasks and a pool of edge VMs with diverse performance:

- ⏱️ Assign tasks to VMs to **minimize the total makespan**
- 🔗 **Respect precedence and data-dependency constraints**
- 🎯 Seek good **load balancing**; minimize bottlenecks
- 🚀 Lay groundwork for **energy-/QoS-aware scheduling** research

---

## 🚦 Why CPOP?

CPOP is a well-known baseline for DAG scheduling on heterogeneous resources. Its main features:

- **Ranks tasks** by their criticality using upward / downward propagation
- **Binds critical-path tasks** to the fastest VM, minimizing the chain latency
- **Allocates remaining tasks** by intelligent priority and earliest finish time
- **Static, light-weight, and produces high parallelism when VMs are abundant**

---

## 🏗️ Simulation Modules

- **Task.java**\  
  ☑️ Represents a DAG node with unique ID, computational workload, and references to predecessors/successors

- **DAGGenerator.java**\  
  ⚙️ Procedurally generates random acyclic graphs (with user-defined size/density) for rigorous testing

- **EdgeEnvironment.java**\  
  💻 Initializes CloudSimPlus hosts, brokers, and a heterogenous pool of edge VMs

- **CPOPScheduler.java**\  
  🧠 Core scheduling implementation — computes ranks, extracts critical path, assigns tasks, computes mapping/timing

- **EdgeSchedulingSimulation.java**\  
  🚀 Main loop to generate DAGs, run CPOP scheduling, launch CloudSimPlus, and record/print results

---

## 📈 Example Results (CPOP Baseline)

- **Scheduling Overhead:**  
  Under 12ms for up to 300 tasks (on 27 VMs)
- **Makespan:**  
  Grows linearly with task count  
  *E.g.,* 100 tasks ≈ 0.59s, 300 tasks ≈ 1.73s
- **Load Balance:**  
  Tasks largely balanced; critical-path VM (~20 tasks), others 10–12
- **DAG Density:**  
  Deeper DAGs (less dense) produce longer critical paths and marginally increased scheduling time. Communication delay negligible in current model.

![CPOP scheduling concept diagram](https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Dag-cpop-criticalpath.png/350px-Dag-cpop-criticalpath.png)

---

## ⚗️ Extending This Project

> This codebase is meant to be hacked, extended, and benchmarked!

Want to prototype metaheuristics (e.g. PSO, ACO), introduce realistic network topologies, or test SLA/Energy-QoS objectives?  
Just fork and start building!

---

## 🚀 Quick Start

1. **Clone and build:**
   ```bash
   git clone https://github.com/your-username/edge-dag-cpop
   cd edge-dag-cpop
   mvn clean package
   ```

2. **Run sample simulation:**
   ```bash
   java -jar target/edge-dag-cpop-1.0-SNAPSHOT.jar
   ```
   *(Requires JDK 11+ and Maven; dependencies auto-resolved)*

---

## 📎 Related Papers

- Topcuoglu, H., Hariri, S., & Wu, M.-Y. (2002). [Performance-Effective and Low-Complexity Task Scheduling for Heterogeneous Computing](https://doi.org/10.1109/71.877944). *IEEE Transactions on Parallel and Distributed Systems*, 13(3), 260-274.

---

## 🤝 Contributing

Pull requests are welcome — please open an issue first to discuss any major design changes or research ideas.  
If you use/adapt this project for your research, please cite or acknowledge appropriately!

---

## 👤 Maintainers
| [Dorsa Ghobadi](https://github.com/dorsaaaa) |  
|:--:|  
| Undergraduate Computer Engineering Students |
| [Mohammadshayan Shabani](https://github.com/shayanshabani) |  
| Undergraduate Computer Engineering Students |  
---

*Happy Scheduling! 🚦✨*