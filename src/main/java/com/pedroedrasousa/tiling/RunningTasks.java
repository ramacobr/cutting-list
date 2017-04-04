package com.pedroedrasousa.tiling;

import com.pedroedrasousa.tiling.model.TillingResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RunningTasks {

    private List<Task> tasks = new ArrayList<>();

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
    }
}
