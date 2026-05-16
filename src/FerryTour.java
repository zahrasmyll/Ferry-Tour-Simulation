package com.mycompany.ferrytour;

import java.util.*;
import java.util.concurrent.*;

class Vehicle extends Thread {
    enum Type { CAR, MINIBUS, TRUCK }

    int id;
    Type type;
    int startSide;
    Ferry ferry;

    public Vehicle(int id, Type type, int startSide, Ferry ferry) {
        this.id = id;
        this.type = type;
        this.startSide = startSide;
        this.ferry = ferry;
    }

    @Override
    public void run() {
        try {
            ferry.enterToll(this);
            ferry.waitInSquare(this);
            ferry.loadOntoFerry(this);
            ferry.waitAndReturn(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getLoadValue() {
        return switch (type) {
            case CAR -> 1;
            case MINIBUS -> 2;
            case TRUCK -> 3;
        };
    }

    @Override
    public String toString() {
        return type + "-" + id;
    }
}

class Ferry {
    private final int MAX_CAPACITY = 20;
    private int currentCapacity = 0;
    private int side = 0; 
    private final Object lock = new Object();

    private final Queue<Vehicle>[] waitingVehicles = new LinkedList[]{new LinkedList<>(), new LinkedList<>()};

    public void enterToll(Vehicle v) throws InterruptedException {
        int tollId = new Random().nextInt(2);
        System.out.println(v + " passed toll " + tollId + " on side " + v.startSide);
        Thread.sleep(100); 
    }

    public void waitInSquare(Vehicle v) {
        synchronized (waitingVehicles[v.startSide]) {
            waitingVehicles[v.startSide].add(v);
            System.out.println(v + " is waiting in square on side " + v.startSide);
        }
    }

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

    private void depart() throws InterruptedException {
        System.out.println("Ferry departing from side " + side + " with " + currentCapacity + " capacity used.");
        Thread.sleep(500); 
        currentCapacity = 0;
        side = 1 - side;
        System.out.println("Ferry arrived at side " + side);
        lock.notifyAll();
    }

    public void waitAndReturn(Vehicle v) throws InterruptedException {
        Thread.sleep(1000); 
        v.startSide = 1 - v.startSide; 
        System.out.println(v + " is returning to original side.");
        enterToll(v);
        waitInSquare(v);
        loadOntoFerry(v);
    }
}

public class FerryTour {
    public static void main(String[] args) {
        Ferry ferry = new Ferry();
        Random rand = new Random();
        List<Thread> allVehicles = new ArrayList<>();

        int id = 1;
        for (int i = 0; i < 12; i++)
            allVehicles.add(new Vehicle(id++, Vehicle.Type.CAR, rand.nextInt(2), ferry));
        for (int i = 0; i < 10; i++)
            allVehicles.add(new Vehicle(id++, Vehicle.Type.MINIBUS, rand.nextInt(2), ferry));
        for (int i = 0; i < 8; i++)
            allVehicles.add(new Vehicle(id++, Vehicle.Type.TRUCK, rand.nextInt(2), ferry));

        allVehicles.forEach(Thread::start);
        allVehicles.forEach(vehicle -> {
            try {
                vehicle.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("All vehicles have returned to their starting side.");
    }
}
