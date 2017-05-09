package com.pedroedrasousa.cutlistoptimizer.model;

import java.util.List;

public class Configuration {

    private String taskId;

    private int cutThickness;

    private boolean allowTileRotation;

    private boolean forceOneBaseTile;

    private List<String> priorities;

    private int accuracyFactor;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getCutThickness() {
        return cutThickness;
    }

    public void setCutThickness(int cutThickness) {
        this.cutThickness = cutThickness;
    }

    public boolean getAllowTileRotation() {
        return allowTileRotation;
    }

    public void setAllowTileRotation(boolean allowTileRotation) {
        this.allowTileRotation = allowTileRotation;
    }

    public boolean getForceOneBaseTile() {
        return forceOneBaseTile;
    }

    public void setForceOneBaseTile(boolean forceOneBaseTile) {
        this.forceOneBaseTile = forceOneBaseTile;
    }

    public boolean isAllowTileRotation() {
        return allowTileRotation;
    }

    public List<String> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<String> priorities) {
        this.priorities = priorities;
    }

    public int getAccuracyFactor() {
        return accuracyFactor;
    }

    public void setAccuracyFactor(int accuracyFactor) {
        this.accuracyFactor = accuracyFactor;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "taskId='" + taskId + '\'' +
                ", cutThickness=" + cutThickness +
                ", allowTileRotation=" + allowTileRotation +
                ", forceOneBaseTile=" + forceOneBaseTile +
                ", priorities=" + priorities +
                ", accuracyFactor=" + accuracyFactor +
                '}';
    }
}
