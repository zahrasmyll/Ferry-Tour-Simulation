# 🚢 Ferry Tour Simulation (Multithreading & Synchronization)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Course](https://img.shields.io/badge/Course-Operating%20Systems-blue.svg)](#)

This project is a concurrent simulation of a ferry transportation system operating between two sides of a city. It demonstrates key **Operating Systems** concepts such as multithreading, mutual exclusion, thread coordination, and shared resource management in Java.

---

## 📌 Project Overview
The simulation involves a total of 30 distinct vehicle threads interacting with a shared single ferry instance:
* 🚗 **12 Cars** (Weight: 1 unit)
* 🚐 **10 Minibuses** (Weight: 2 units)
* 🚛 **8 Trucks** (Weight: 3 units)

Each vehicle is implemented as an independent thread that must pass through a toll booth, wait in a local square, cross to the opposite side via the ferry, spend some time there, and then complete a full return journey back to its starting side.

---

## 🛠️ System Architecture & Workflow

1. **Toll Gate Processing:** Vehicles randomly select one of two toll booths on their current side and experience a simulated processing delay.
2. **Waiting Square:** Vehicles enter a synchronized waiting queue corresponding to their side.
3. **Ferry Loading:** The ferry loads vehicles one-by-one under strict constraints:
   * The vehicle must be on the same side as the ferry.
   * The ferry's maximum capacity ($20$ units) must not be exceeded.
4. **Transit & Return:** Once the ferry is full or no more matching vehicles are waiting, it departs, crosses the river, unloads, and the vehicles execute their return trip.

---

## 💻 Technical Implementation Details

### 🔑 Synchronization Strategy
* **`synchronized(lock)` Blocks:** Used within the core loading logic to ensure mutual exclusion and prevent race conditions during capacity updates.
* **Conditional Waiting (`wait()` & `notifyAll()`):** Vehicle threads gracefully yield CPU execution and wait if the ferry is on the opposite side or if their vehicle weight would overload the ferry's current capacity.
* **Thread Safety:** Separate thread-safe queues are used to manage waiting structures on both Side 0 and Side 1 independently to reduce lock contention.

### Core Simulation Snippet (Ferry Loading)
```java
public void loadOntoFerry(Vehicle v) throws InterruptedException {
    synchronized (lock) {
        while (v.startSide != side || currentCapacity + v.getLoadValue() > MAX_CAPACITY) {
            lock.wait();
        }
        synchronized (waitingVehicles[side]) {
            if (waitingVehicles[side].remove(v)) {
                currentCapacity += v.getLoadValue();
                System.out.println(v + " loaded onto ferry on side " + side);
            }
        }
        Thread.sleep(100); 
        if (currentCapacity >= MAX_CAPACITY || waitingVehicles[side].isEmpty()) {
            depart();
        }
    }
}
