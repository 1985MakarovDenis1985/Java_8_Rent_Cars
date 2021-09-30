package test;

import cars.dao.AbstractRentCompany;
import cars.dao.IRentCompany;
import cars.dao.RentCompany;
import cars.domain.Car;
import cars.domain.Driver;
import cars.domain.Model;
import cars.domain.RentRecord;
import enums.CarsReturnCode;
import enums.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentCompanyTest {

    IRentCompany rentCompany;
    private static final String REG_NUMBER1 = "123";
    private static final String REG_NUMBER2 = "124";
    private static final String REG_NUMBER3 = "125";
    private static final String MODEL1 = "BMW12";
    private static final String MODEL2 = "B4";
    private static final long LICENSE1 = 123;
    private static final long LICENSE2 = 124;
    private static final LocalDate RENT_DATE1 = LocalDate.of(2021, 2, 15);
    private static final int RENT_DAYS1 = 5;
    private static final LocalDate RETURN_DATE = RENT_DATE1.plusDays(RENT_DAYS1);
    private static final LocalDate RETURN_DATE_WRONG = RENT_DATE1.minusDays(1);
    private static final long DELAY_DAYS = 2;
    private static final LocalDate RETURN_DATE_DELAY = RETURN_DATE.plusDays(DELAY_DAYS);
    private static final int DAMAGES = 50;
    private static final int GAS_PERCENT = 50;
    private static final LocalDate CURRENT_DATE = LocalDate.of(2021, 3, 15);
    private static final int CLEAR_DAYS = 15;
    Car car1 = new Car(REG_NUMBER1, "red", MODEL1);
    Car car2 = new Car(REG_NUMBER2, "green", MODEL2);
    Car car3 = new Car(REG_NUMBER3, "silver", MODEL1);
    Model model1 = new Model(MODEL1, 55, "Germany", "BMW", 200);
    Model model2 = new Model(MODEL2, 50, "Japan", "Subaru", 190);
    Driver driver1 = new Driver(LICENSE1, "Moshe", 1980, "050-1234567");
    Driver driver2 = new Driver(LICENSE2, "David", 1960, "050-7654321");
    private RentRecord record = new RentRecord(LICENSE1, REG_NUMBER1, RENT_DATE1, RENT_DAYS1);

    @BeforeEach
    void setUp() throws Exception {
        rentCompany = new RentCompany();
        rentCompany.addModel(model1);
        rentCompany.addDriver(driver1);
        rentCompany.addCar(car1);
        rentCompany.rentCar(REG_NUMBER1, LICENSE1, RENT_DATE1, RENT_DAYS1);
    }

    @Test
    void testAddCar() {
        assertEquals(CarsReturnCode.CAR_EXIST, rentCompany.addCar(car1));
        assertEquals(CarsReturnCode.NO_MODEL, rentCompany.addCar(car2));
        assertEquals(CarsReturnCode.OK, rentCompany.addCar(car3));
    }

    @Test
    void testAddDriver() {
        assertEquals(CarsReturnCode.DRIVER_EXIST, rentCompany.addDriver(driver1));
        assertEquals(CarsReturnCode.OK, rentCompany.addDriver(driver2));
    }

    @Test
    void testAddModel() {
        assertEquals(CarsReturnCode.MODEL_EXIST, rentCompany.addModel(model1));
        assertEquals(CarsReturnCode.OK, rentCompany.addModel(model2));
    }

    @Test
    void testGetCar() {
        assertNull(rentCompany.getCar(REG_NUMBER2));
        assertEquals(car1, rentCompany.getCar(REG_NUMBER1));
    }

    @Test
    void testGetDriver() {
        assertNull(rentCompany.getDriver(LICENSE2));
        assertEquals(driver1, rentCompany.getDriver(LICENSE1));
    }

    @Test
    void testGetModel() {
        assertNull(rentCompany.getModel(MODEL2));
        assertEquals(model1, rentCompany.getModel(MODEL1));
    }

    @Test
    void testRentCar() {
        assertEquals(CarsReturnCode.CAR_IN_USE,
                rentCompany.rentCar(REG_NUMBER1, LICENSE1, RENT_DATE1, RENT_DAYS1));
        assertEquals(CarsReturnCode.CAR_NOT_EXIST,
                rentCompany.rentCar(REG_NUMBER2, LICENSE1, RENT_DATE1, RENT_DAYS1));
        rentCompany.addModel(model2);
        rentCompany.addCar(car2);
        assertEquals(CarsReturnCode.NO_DRIVER,
                rentCompany.rentCar(REG_NUMBER2, LICENSE2, RENT_DATE1, RENT_DAYS1));
        assertEquals(CarsReturnCode.OK,
                rentCompany.rentCar(REG_NUMBER2, LICENSE1, RENT_DATE1, RENT_DAYS1));
        assertTrue(car1.isInUse());
        RentRecord record1 = getRecord(REG_NUMBER1);
        assertEquals(record , record1);
        assertEquals(RENT_DAYS1, record1.getRentDays());

    }

    private RentRecord getRecord(String regNumber1) {
        return rentCompany.getAllRecords()
                .filter(r -> r.getRegNumber() == regNumber1)
                .findFirst()
                .orElse(null);
    }

    @Test
    void testGetAllRecords() {
        RentRecord record1 = rentCompany.getAllRecords().findFirst().orElse(null);
        assertEquals(record, record1);
    }

    @Test
    void testGetCarDrivers() {
        rentCompany.getCarDrivers(REG_NUMBER1).forEach(System.out::println);
        rentCompany.getCarDrivers(REG_NUMBER1)
                .forEach(d -> assertEquals(driver1, d));
        assertNull(rentCompany.getCarDrivers(REG_NUMBER2));
    }

    @Test
    void testGetDriverCars() {
        rentCompany.getDriverCars(LICENSE1)
                .forEach(c -> assertEquals(car1, c));
        assertNull(rentCompany.getDriverCars(LICENSE2));
    }

    @Test
    void testReturnCarCodes() {
        rentCompany.addModel(model2);
        rentCompany.addCar(car2);
        assertEquals(CarsReturnCode.CAR_NOT_RENTED,
                rentCompany.returnCar(REG_NUMBER2, LICENSE1, RETURN_DATE, 100, 0));
        assertEquals(CarsReturnCode.NO_DRIVER,
                rentCompany.returnCar(REG_NUMBER1, LICENSE2, RETURN_DATE, 100, 0));
        rentCompany.addDriver(driver2);
        assertEquals(CarsReturnCode.CAR_NOT_RENTED,
                rentCompany.returnCar(REG_NUMBER1, LICENSE2, RETURN_DATE, 100, 0));
        assertEquals(CarsReturnCode.RETURN_DATE_WRONG,
                rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE_WRONG, 100, 0));
        assertEquals(CarsReturnCode.OK,
                rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE, 100, 0));
    }

    @Test
    void testReturnCarNoDamagesNoAdditionalCost() {
        rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE, 100, 0);
        assertFalse(car1.isInUse());
        assertFalse(car1.isIfRemoved());
        assertEquals(State.EXCELLENT, car1.getState());
        record.setReturnDate(RETURN_DATE);
        record.setDamages(0);
        record.setGasTankPercent(100);
        record.setCoast(model1.getPriceDay() * RENT_DAYS1);
        RentRecord actual = getRecord(REG_NUMBER1);
        assertEquals(record, actual);
        assertEquals(record.getReturnDate(), actual.getReturnDate());
        assertEquals(record.getDamages(), actual.getDamages());
        assertEquals(record.getGasTankPercent(), actual.getGasTankPercent());
        assertEquals(record.getCoast(), actual.getCoast(), 0.01);
    }

    @Test
    void testReturnCarWithDamagesAdditionalCosts() {
        rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE_DELAY, DAMAGES, GAS_PERCENT);
        assertFalse(car1.isInUse());
        assertTrue(car1.isIfRemoved());
        record.setDamages(DAMAGES);
        record.setGasTankPercent(GAS_PERCENT);
        record.setReturnDate(RETURN_DATE_DELAY);
        record.setCoast((float) (model1.getPriceDay() * RENT_DAYS1 + getAdditionalCost()));
        RentRecord actual = getRecord(REG_NUMBER1);
        assertEquals(record, actual);
        assertEquals(record.getReturnDate(), actual.getReturnDate());
        assertEquals(record.getDamages(), actual.getDamages());
        assertEquals(record.getGasTankPercent(), actual.getGasTankPercent());
        assertEquals(record.getCoast(), actual.getCoast(), 0.01);
    }

    private double getAdditionalCost() {
        int gasPrice = ((AbstractRentCompany)rentCompany).getGasPrice();
        int finePercent = ((AbstractRentCompany)rentCompany).getFinePercent();
        int gasTank = model1.getGasTank();
        int priceDay = model1.getPriceDay();
        return (gasTank - gasTank * GAS_PERCENT / 100.) * gasPrice
                + (priceDay + priceDay * finePercent / 100.) * DELAY_DAYS;
    }

    @Test
    void testGetAllCars() {
        rentCompany.addCar(car3);
        Car[] expected = {car1, car3};
        Object[] actual = rentCompany.getAllCars()
                .sorted((c1, c2) -> c1.getRegNumber().compareTo(c2.getRegNumber()))
                .toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetAllDrivers() {
        rentCompany.addDriver(driver2);
        Driver[] expected = {driver1, driver2};
        Driver[] actual = rentCompany.getAllDrivers()
                .sorted((d1,d2) -> Long.compare(d1.getLicenceId(), d2.getLicenceId()))
                .toArray(Driver[]::new);
        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetMostPopularModelNames() {
        setupStatistics();
        String[] expected = {MODEL2, MODEL1};
        String[] actual = rentCompany.getMostPopularModelNames()
                .stream()
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetModelProfit() {
        setupStatistics();
        assertEquals(model1.getPriceDay() * RENT_DAYS1 * 3, rentCompany.getModelProfit(MODEL1));
        assertEquals(model2.getPriceDay() * RENT_DAYS1 * 3, rentCompany.getModelProfit(MODEL2));
    }

    @Test
    void testGetMostProfitModelNames() {
        setupStatistics();
        String[] expected = {MODEL1};
        String[] actual = rentCompany.getMostProfitModelNames().stream().toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }

    private void setupStatistics() {
        rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE, 100, 0);
        rentReturn(REG_NUMBER1, 2);
        rentCompany.addModel(model2);
        rentCompany.addCar(car2);
        rentReturn(REG_NUMBER2, 3);
    }

    private void rentReturn(String regNumber, int n) {
        for(int i = 0; i < n; i++) {
            rentCompany.rentCar(regNumber, LICENSE1, RENT_DATE1, RENT_DAYS1);
            rentCompany.returnCar(regNumber, LICENSE1, RETURN_DATE, 100, 0);
        }

    }

    @Test
    void testRemoveCar() {
        assertEquals(CarsReturnCode.CAR_IN_USE, rentCompany.removeCar(REG_NUMBER1));
        assertEquals(CarsReturnCode.CAR_NOT_EXIST, rentCompany.removeCar(REG_NUMBER2));
        rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE, 100, 0);
        assertEquals(CarsReturnCode.OK, rentCompany.removeCar(REG_NUMBER1));
        assertTrue(car1.isIfRemoved());
    }

    @Test
    void testClear() {
        setUpClear();
        //assumed car1 and car2 are deleted
        //car3 is not deleted
        Car[] expected = {car1, car2};
        Car[] actual = rentCompany.clear(CURRENT_DATE, CLEAR_DAYS)
                .stream()
                .sorted((c1, c2) -> c1.getRegNumber().compareTo(c2.getRegNumber()))
                .toArray(Car[]::new);
        assertArrayEquals(expected, actual);
        assertNull(rentCompany.getCar(REG_NUMBER1));
        assertNull(rentCompany.getCar(REG_NUMBER2));
        assertNull(getRecord(REG_NUMBER1));
        assertNull(getRecord(REG_NUMBER2));
        assertNotNull(rentCompany.getCar(REG_NUMBER3));
        assertNotNull(getRecord(REG_NUMBER3));
    }

    private void setUpClear() {
        rentCompany.returnCar(REG_NUMBER1, LICENSE1, RETURN_DATE, 0, 90);
        rentCompany.addModel(model2);
        rentCompany.addCar(car2); //124
        rentCompany.addCar(car3); //125
        rentCompany.rentCar(REG_NUMBER2, LICENSE1, RENT_DATE1, RENT_DAYS1);
        rentCompany.rentCar(REG_NUMBER3, LICENSE1, RENT_DATE1, RENT_DAYS1);
        rentCompany.returnCar(REG_NUMBER2, LICENSE1, RETURN_DATE, 100, 0);
        rentCompany.removeCar(REG_NUMBER2);
        rentCompany.returnCar(REG_NUMBER3, LICENSE1, RETURN_DATE, 100, 0);

    }

}