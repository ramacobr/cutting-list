package com.pedroedrasousa.cutlistoptimizer;

import com.pedroedrasousa.cutlistoptimizer.model.TillingResponseDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RunningTasks {

    private static final RunningTasks instance = new RunningTasks();

    private List<Task> tasks = new ArrayList<>();

    private RunningTasks() {}

    public static RunningTasks getInstance() {
        return instance;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Task getTask(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    public void removeTask(String id) {
        for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext(); ) {
            Task task = iterator.next();
            if (task.getId().equals(id)) {
                iterator.remove();
            }
        }
    }

    public static class Task {

        private String id;

        private TillingResponseDTO solution;

        private String statusMessage;

        private int runningThreads;

        private int nbrTotalThreads;

        private int percentageDone;

        private int iterationsCompleted;

        private int totalIterations;

        private HashMap<Integer, Integer> iterationsProgress;

        public Task(String id) {
            this.id = id;
        }

        public Task(String id, String statusMessage) {
            this.id = id;
            this.statusMessage = statusMessage;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public TillingResponseDTO getSolution() {
            return solution;
        }

        public void setSolution(TillingResponseDTO solution) {
            this.solution = solution;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        public int getRunningThreads() {
            return runningThreads;
        }

        public void setRunningThreads(int runningThreads) {
            this.runningThreads = runningThreads;
        }

        public int getPercentageDone() {
            return percentageDone;
        }

        public void setPercentageDone(int percentageDone) {
            this.percentageDone = percentageDone;
        }

        public int incrementRunningThreads() {
            return ++this.runningThreads;
        }

        public int decrementRunningThreads() {
            return --this.runningThreads;
        }

        public int getNbrTotalThreads() {
            return nbrTotalThreads;
        }

        public void setNbrTotalThreads(int nbrTotalThreads) {
            this.nbrTotalThreads = nbrTotalThreads;
        }

        public int getIterationsCompleted() {
            return iterationsCompleted;
        }

        public void setIterationsCompleted(int iterationsCompleted) {
            this.iterationsCompleted = iterationsCompleted;
        }

        public int incrementNbrTotalThreads() {
            return ++this.nbrTotalThreads;
        }

        public int decrementNbrTotalThreads() {
            return --this.nbrTotalThreads;
        }

        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        public HashMap<Integer, Integer> getIterationsProgress() {
            return iterationsProgress;
        }

        public void setIterationsProgress(HashMap<Integer, Integer> iterationsProgress) {
            this.iterationsProgress = iterationsProgress;
        }
    }
}
