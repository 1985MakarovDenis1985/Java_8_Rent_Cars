package cars.domain;

import enums.State;

import java.util.Objects;

public class Car {
    String regNumber;
    String color;
    String modelName;
    State state;
    boolean inUse;
    boolean ifRemoved;

    public Car() {}

    public Car(String regNumber, String color, String modelName) {
        this.regNumber = regNumber;
        this.color = color;
        this.modelName = modelName;
        this.state = State.EXCELLENT;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public String getColor() {
        return color;
    }

    public String getModelName() {
        return modelName;
    }

    public State getState() {
        return state;
    }

    public boolean isInUse() {
        return inUse;
    }

    public boolean isIfRemoved() {
        return ifRemoved;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public void setIfRemoved(boolean ifRemoved) {
        this.ifRemoved = ifRemoved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(regNumber, car.regNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNumber);
    }

    @Override
    public String toString() {
        return "Car{" +
                "regNumber='" + regNumber + '\'' +
                ", color='" + color + '\'' +
                ", modelName='" + modelName + '\'' +
                ", state=" + state +
                ", inUse=" + inUse +
                ", ifRemoved=" + ifRemoved +
                '}';
    }
}
