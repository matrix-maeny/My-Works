package com.matrix_maeny.myworks.models;

public class WorkModel {

    String workName;
    int state;

    public WorkModel(String workName,int state) {
        this.workName = workName;
        this.state = state;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
