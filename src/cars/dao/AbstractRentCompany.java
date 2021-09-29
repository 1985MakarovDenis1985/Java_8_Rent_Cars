package cars.dao;

public abstract class AbstractRentCompany implements IRentCompany{
    protected int finePercent;
    protected int gasPrice;

    public AbstractRentCompany() {
        this.finePercent = 15;
        this.gasPrice = 10;
    }

    public void setFinePercent(int finePercent) {
        this.finePercent = finePercent;
    }

    public void setGasPrice(int gasPrice) {
        this.gasPrice = gasPrice;
    }

    public int getFinePercent() {
        return finePercent;
    }

    public int getGasPrice() {
        return gasPrice;
    }

    @Override
    public String toString() {
        return "AbstractRentCompany{" +
                "finePercent=" + finePercent +
                ", gasPrice=" + gasPrice +
                '}';
    }

}
