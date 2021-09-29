package cars.domain;

import java.util.Objects;

public class Driver {
    long licenceId;
    String name;
    int birthDay;
    String phone;

    public Driver() {}

    public Driver(long licenceId, String name, int birthDay, String phone) {
        this.licenceId = licenceId;
        this.name = name;
        this.birthDay = birthDay;
        this.phone = phone;
    }

    public long getLicenceId() {
        return licenceId;
    }

    public String getName() {
        return name;
    }

    public int getBirthDay() {
        return birthDay;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        return licenceId == driver.licenceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenceId);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "licenceId=" + licenceId +
                ", name='" + name + '\'' +
                ", birthDay=" + birthDay +
                ", phone='" + phone + '\'' +
                '}';
    }
}
