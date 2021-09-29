package cars.dao;

import cars.domain.Car;
import cars.domain.Driver;
import cars.domain.Model;
import cars.domain.RentRecord;
import enums.CarsReturnCode;
import enums.State;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RentCompany extends AbstractRentCompany {
    HashMap<String, Car> cars = new HashMap<>(); // number of model, car
    HashMap<Long, Driver> drivers = new HashMap<>();
    HashMap<String, List<RentRecord>> carRecords = new HashMap<>(); // number of car, list all records
    HashMap<Long, List<RentRecord>> driverRecords = new HashMap<>();
    TreeMap<LocalDate, List<RentRecord>> returnedRecords = new TreeMap<>(); // all completed rents
    HashMap<String, Model> models = new HashMap<>();

    private final int DAMAGE_BAD_STATE = 10;
    private final int DAMAGE_FOR_REMOVE = 30;

    @Override
    public CarsReturnCode addModel(Model model) {
        String newKey = model.getModelName();
        if (!models.containsKey(model.getModelName())) {
            models.put(newKey, model);
            return CarsReturnCode.OK;
        }
        return CarsReturnCode.MODEL_EXIST;
    }

    @Override
    public CarsReturnCode addCar(Car car) {
        if (!models.containsKey(car.getModelName())) return CarsReturnCode.NO_MODEL;
        if (cars.containsKey(car.getRegNumber())) return CarsReturnCode.CAR_EXIST;
        cars.put(car.getRegNumber(), car);
        return CarsReturnCode.OK;
    }

    @Override
    public CarsReturnCode addDriver(Driver driver) {
        if (drivers.containsKey(driver.getLicenceId())) return CarsReturnCode.DRIVER_EXIST;
        drivers.put(driver.getLicenceId(), driver);
        return CarsReturnCode.OK;
    }

    @Override
    public Model getModel(String modelName) {
        return models.get(modelName);
    }

    @Override
    public Car getCar(String regNumber) {
        return cars.get(regNumber);
    }

    @Override
    public Driver getDriver(long licence) {
        return drivers.get(licence);
    }

    @Override
    public CarsReturnCode rentCar(String regNumber, long licenceId, LocalDate rentDate, int rentDays) {
        if (!cars.containsKey(regNumber) || cars.get(regNumber).isIfRemoved() || rentDate.isAfter(LocalDate.now()))
            return CarsReturnCode.CAR_NOT_EXIST;
        if (!drivers.containsKey(licenceId)) return CarsReturnCode.NO_DRIVER;
        if (cars.get(regNumber).isInUse()) return CarsReturnCode.CAR_IN_USE;

        RentRecord newRent = new RentRecord(licenceId, regNumber, rentDate, rentDays);
        float contractCoast = rentDays * models.get(cars.get(regNumber).getModelName()).getPriceDay();
        newRent.setCoast(contractCoast);
        if (!carRecords.containsKey(regNumber)) {
            carRecords.put(regNumber, new ArrayList<>());
        }
        if (!driverRecords.containsKey(licenceId)) {
            driverRecords.put(licenceId, new ArrayList<>());
        }
        carRecords.get(regNumber).add(newRent);
        driverRecords.get(licenceId).add(newRent);
        cars.get(regNumber).setInUse(true);

        return CarsReturnCode.OK;
    }

    @Override
    public CarsReturnCode returnCar(String regNumber, long licenceId, LocalDate returnDate, int gasTankPercent, int damages) {
        if (!cars.containsKey(regNumber) || !cars.get(regNumber).isInUse()) return CarsReturnCode.CAR_NOT_RENTED;

        /*
        ПОДСКАЖИТЕ ПОЖАЛУЙСТА, КАКОЕ РЕШЕНИЕ ПОИСКА НЕ ЗАКРЫТОГО RentRecord ДАННОГО АВТО ЛУЧШЕ??? СПАСИБО!
        */

        // 1) мы ищем как вы говорили по null...
//        RentRecord lastRecordOfThisCar = carRecords.get(regNumber).stream().filter(e -> e.getReturnDate()==null).findFirst().orElse(null);

        // 2) можно ли так:  берем последнюю запись в листе данного авто по ключу (она в любом случае всегда не закрытая, иначе авто NOT_RENT) и пробегаться по листу не надо
        RentRecord lastRecordOfThisCar = carRecords.get(regNumber).get(carRecords.get(regNumber).size() - 1);

        if (!drivers.containsKey(licenceId) || lastRecordOfThisCar.getLicenceId() != licenceId)
            return CarsReturnCode.NO_DRIVER;
        if (returnDate.isBefore(lastRecordOfThisCar.getRentDate()) || returnDate.isAfter(LocalDate.now()))
            return CarsReturnCode.RETURN_DATE_WRONG;

        int thisModelPrice = getModel(getCar(regNumber).getModelName()).getPriceDay();
        int thisModelGasTank = getModel(getCar(regNumber).getModelName()).getGasTank();

        lastRecordOfThisCar.setDamages(damages);
        lastRecordOfThisCar.setReturnDate(returnDate);
        lastRecordOfThisCar.setGasTankPercent(gasTankPercent);

        long factDaysRent = ChronoUnit.DAYS.between(lastRecordOfThisCar.getRentDate(), returnDate);

        // sum coast over rent
        if (returnDate.isAfter(lastRecordOfThisCar.getRentDate().plusDays(lastRecordOfThisCar.getRentDays()))) {
            float sumOverRent = (factDaysRent - lastRecordOfThisCar.getRentDays()) * (thisModelPrice + (thisModelPrice / 100 * finePercent));
            lastRecordOfThisCar.setCoast(lastRecordOfThisCar.getCoast() + sumOverRent);
        }

        // sum coast if car returned before contract
        if (returnDate.isBefore(lastRecordOfThisCar.getRentDate().plusDays(lastRecordOfThisCar.getRentDays()))) {
            float factCoast = factDaysRent * thisModelPrice;
            lastRecordOfThisCar.setCoast(factCoast);
        }

        // price of petrol
        if (lastRecordOfThisCar.getGasTankPercent() < 100) {
            float petrolCoast = (thisModelGasTank - Math.round((float) thisModelGasTank / 100 * gasTankPercent)) * gasPrice;
            lastRecordOfThisCar.setCoast(lastRecordOfThisCar.getCoast() + petrolCoast);
        }

        if (lastRecordOfThisCar.getDamages() <= DAMAGE_BAD_STATE) {
            cars.get(regNumber).setState(State.GOOD);
        } else if (lastRecordOfThisCar.getDamages() > DAMAGE_BAD_STATE && lastRecordOfThisCar.getDamages() < DAMAGE_FOR_REMOVE) {
            cars.get(regNumber).setState(State.BAD);
        } else if (lastRecordOfThisCar.getDamages() >= DAMAGE_FOR_REMOVE) {
            cars.get(regNumber).setIfRemoved(true);
        }
        cars.get(regNumber).setInUse(false);

        if (!returnedRecords.containsKey(returnDate)) returnedRecords.put(returnDate, new ArrayList<>());
        returnedRecords.get(returnDate).add(lastRecordOfThisCar);

        return CarsReturnCode.OK;
    }

    @Override
    public CarsReturnCode removeCar(String regNumber) {
        if (!cars.containsKey(regNumber)) return CarsReturnCode.CAR_NOT_EXIST;
        if (getCar(regNumber).isInUse()) return CarsReturnCode.CAR_IN_USE;
        getCar(regNumber).setIfRemoved(true);
        return CarsReturnCode.OK;
    }

    @Override
    public List<Car> clear(LocalDate currentDate, int days) {
        List<Car> removedCarList = returnedRecords.headMap(currentDate.minusDays(days)).entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .filter(e -> e.getDamages() >= DAMAGE_FOR_REMOVE)
                .distinct()
                .map(e -> cars.get(e.getRegNumber()))
                .collect(Collectors.toList());


        List<String> regNumOfRemovedCar = removedCarList.stream()
                .map(Car::getRegNumber)
                .collect(Collectors.toList());

        for (Map.Entry<Long, List<RentRecord>> longListEntry : driverRecords.entrySet()) {
            longListEntry.getValue().removeIf(r -> regNumOfRemovedCar.contains(r.getRegNumber()));
        }
        driverRecords.entrySet().removeIf(e -> e.getValue().size() == 0);

        for (Map.Entry<LocalDate, List<RentRecord>> localDateListEntry : returnedRecords.entrySet()) {
            localDateListEntry.getValue().removeIf(r -> regNumOfRemovedCar.contains(r.getRegNumber()));
        }
        returnedRecords.entrySet().removeIf(e -> e.getValue().size() == 0);

        carRecords.entrySet().removeIf(r -> regNumOfRemovedCar.contains(r.getKey()));
        cars.entrySet().removeIf(r -> regNumOfRemovedCar.contains(r.getKey()));

        return removedCarList;
    }


    @Override
    public List<Driver> getCarDrivers(String regNumber) {
        return carRecords.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .map(o -> drivers.get(o.getLicenceId()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getDriverCars(long licence) {
        return driverRecords.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .map(o -> cars.get(o.getRegNumber()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Car> getAllCars() {
        return cars.values().stream();
    }

    @Override
    public Stream<Driver> getAllDrivers() {
        return drivers.values().stream();
    }

    @Override
    public Stream<RentRecord> getAllRecords() {
        return carRecords.entrySet().stream()
                .flatMap(e -> e.getValue().stream());
    }

    @Override
    public List<String> getMostPopularModelNames() {
        Map<String, Long> mostPopularModel = getAllRecords()
                .map(e -> cars.get(e.getRegNumber()).getModelName())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        Long max = mostPopularModel.values().stream()
                .max(Long::compare).orElse(null);

//        if (max == 0) return null;
        return mostPopularModel.entrySet().stream()
                .filter(e -> e.getValue().equals(max))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public double getModelProfit(String modelName) {
        return returnedRecords.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .filter(e -> cars.get(e.getRegNumber()).getModelName().equals(modelName))
                .mapToDouble(RentRecord::getCoast).sum();
    }

    @Override
    public List<String> getMostProfitModelNames() {
        Map<String, Double> modelNameList = returnedRecords.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.groupingBy(t -> cars.get(t.getRegNumber()).getModelName(), Collectors.summingDouble(RentRecord::getCoast)));

        Double max = modelNameList.values().stream()
                .max(Double::compare).orElse(null);

        return modelNameList.entrySet().stream()
                .filter(e -> e.getValue().equals(max))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
