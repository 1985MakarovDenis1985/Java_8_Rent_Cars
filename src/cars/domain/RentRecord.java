package cars.domain;

import java.time.LocalDate;
import java.util.Objects;

public class RentRecord {

    long licenceId;
    String regNumber;
    LocalDate rentDate;
    LocalDate returnDate;
    int gasTankPercent; // percent of full tank at return
    int rentDays; // rent period  with prepayment
    float coast; // overall coast
    int damages; // percent of damages

    public RentRecord() {}

    public RentRecord(long licenceId, String regNumber, LocalDate rentDate, int rentDays) {
        this.licenceId = licenceId;
        this.regNumber = regNumber;
        this.rentDate = rentDate;
        this.rentDays = rentDays;
    }

    public long getLicenceId() {
        return licenceId;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public LocalDate getRentDate() {
        return rentDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public int getGasTankPercent() {
        return gasTankPercent;
    }

    public int getRentDays() {
        return rentDays;
    }

    public float getCoast() {
        return coast;
    }

    public int getDamages() {
        return damages;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setGasTankPercent(int gasTankPercent) {
        this.gasTankPercent = gasTankPercent;
    }

    public void setCoast(float coast) {
        this.coast = coast;
    }

    public void setDamages(int damages) {
        this.damages = damages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentRecord that = (RentRecord) o;
        return Objects.equals(regNumber, that.regNumber) && Objects.equals(rentDate, that.rentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNumber, rentDate);
    }

    @Override
    public String toString() {
        return "RentRecord{" +
                "licence=" + licenceId +
                ", regNumber='" + regNumber + '\'' +
                ", rentDate=" + rentDate +
                ", returnDate=" + returnDate +
                ", gasTankPercent=" + gasTankPercent +
                ", rentDays=" + rentDays +
                ", coast=" + coast +
                ", damages=" + damages +
                '}';
    }
}
