package cars.dao;

import cars.domain.Car;
import cars.domain.Driver;
import cars.domain.Model;
import cars.domain.RentRecord;
import enums.CarsReturnCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public interface IRentCompany {
    CarsReturnCode addModel(Model model); // OK, MODEL_EXIST
    CarsReturnCode addCar(Car car); // OK, CAR_EXIST, NO_MODEL
    CarsReturnCode addDriver(Driver driver); // OK, DRIVER_EXIST
    Model getModel(String modelName);
    Car getCar(String regNumber);
    Driver getDriver(long licence);
    CarsReturnCode rentCar(String regNumber, long licence, LocalDate rentDate, int rentDays); // OK, CAR_IN_USE, CAR_NOT_EXIST, NO_DRIVER
    CarsReturnCode returnCar(String regNumber, long licenceId, LocalDate returnDate, int gasTankPercent, int damages); // OK, NO_DRIVER, CAR_NOT_RENTED,RETURN_DATE_WRONG || in case of damages up to 10% state is GOOD, up 30% - BAD, more - remove car
    CarsReturnCode removeCar(String regNumber); // OK, CAR_IN_USE, CAR_NOT_EXIST || removing car is setting flRemoved in true
    List<Car> clear(LocalDate currentDate, int days); // all cars for which the returnDate before currentDate - days with ifRemoved are deleted from an information model along with all related records it returns list of the deleted cars
    List<Driver> getCarDrivers(String regNumber);  // returns all drivers that have been renting the car
    List<Car> getDriverCars(long licence); // returns list of all cars that have been rented by the driver
    Stream<Car> getAllCars();
    Stream<Driver> getAllDrivers();
    Stream<RentRecord> getAllRecords();
    List<String> getMostPopularModelNames(); // list of model have been rented most time
    double getModelProfit(String modelName); // returns value of money
    List<String> getMostProfitModelNames(); // returns list of most profitable model names
}
