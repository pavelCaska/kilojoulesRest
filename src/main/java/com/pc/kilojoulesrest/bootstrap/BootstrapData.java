package com.pc.kilojoulesrest.bootstrap;

import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.model.FoodCSVRecord;
import com.pc.kilojoulesrest.model.JournalMealFormDTO;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.repository.FoodRepository;
import com.pc.kilojoulesrest.service.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

@Profile("!test")
@Component("bootstrapData")
public class BootstrapData implements CommandLineRunner {

    private final UserService userService;
    private final FoodRepository foodRepository;
    private final FoodCsvService foodCsvService;
    private final FoodService foodService;
    private final PortionService portionService;
    private final MealService mealService;
    private final JournalService journalService;
    private final JournalMealService journalMealService;

    public BootstrapData(UserService userService, FoodRepository foodRepository, FoodCsvService foodCsvService, FoodService foodService, PortionService portionService, MealService mealService, JournalService journalService, JournalMealService journalMealService) {
        this.userService = userService;
        this.foodRepository = foodRepository;
        this.foodCsvService = foodCsvService;
        this.foodService = foodService;
        this.portionService = portionService;
        this.mealService = mealService;
        this.journalService = journalService;
        this.journalMealService = journalMealService;
    }
//    private static final Logger log = LoggerFactory.getLogger(BootstrapData.class);



    @Transactional
    @Override
    public void run(String... args) throws Exception {
        populateUsers();
        loadCsvData();
        populatePortions();
        populateIndividualPortions();
        populateIndividualMeals();
        populateJournalWithFoods();
        populateJournalWithMeals();
    }

    private void populateUsers() {
        User admin = User.builder()
                .username("admin1")
                .password(userService.encodePassword("admin1pwd"))
                .roles("ROLE_ADMIN")
                .build();
        userService.saveUser(admin);
        User user = User.builder()
                .username("activeUser")
                .password(userService.encodePassword("user1pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(user);
        User noDataUser = User.builder()
                .username("noDataUser")
                .password(userService.encodePassword("user2pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(noDataUser);
    }

    private void loadCsvData() throws FileNotFoundException {
        if (foodRepository.count() < 10){
            File file = ResourceUtils.getFile("classpath:csvdata/potraviny.csv");

            List<FoodCSVRecord> recs = foodCsvService.convertCSV(file);

            recs.forEach(foodCSVRecord -> {

                foodRepository.save(Food.builder()
                                .name(foodCSVRecord.getName())
                                .quantity(foodCSVRecord.getQuantity() != null ? foodCSVRecord.getQuantity() : BigDecimal.ZERO)
                                .kiloJoules(foodCSVRecord.getKiloJoules() != null ? foodCSVRecord.getKiloJoules() : BigDecimal.ZERO)
                                .proteins(foodCSVRecord.getProteins() != null ? foodCSVRecord.getProteins() : BigDecimal.ZERO)
                                .carbohydrates(foodCSVRecord.getCarbohydrates() != null ? foodCSVRecord.getCarbohydrates() : BigDecimal.ZERO)
                                .fiber(foodCSVRecord.getFiber() != null ? foodCSVRecord.getFiber() : BigDecimal.ZERO)
                                .sugar(foodCSVRecord.getSugar() != null ? foodCSVRecord.getSugar() : BigDecimal.ZERO)
                                .fat(foodCSVRecord.getFat() != null ? foodCSVRecord.getFat() : BigDecimal.ZERO)
                                .safa(foodCSVRecord.getSafa() != null ? foodCSVRecord.getSafa() : BigDecimal.ZERO)
                                .tfa(foodCSVRecord.getTfa() != null ? foodCSVRecord.getTfa() : BigDecimal.ZERO)
                                .cholesterol(foodCSVRecord.getCholesterol() != null ? foodCSVRecord.getCholesterol() : BigDecimal.ZERO)
                                .sodium(foodCSVRecord.getSodium() != null ? foodCSVRecord.getSodium() : BigDecimal.ZERO)
                                .calcium(foodCSVRecord.getCalcium() != null ? foodCSVRecord.getCalcium() : BigDecimal.ZERO)
                                .phe(foodCSVRecord.getPhe() != null ? foodCSVRecord.getPhe() : BigDecimal.ZERO)
                                .createdAt(foodCSVRecord.getCreatedAt())
                        .build());
            });
        }
    }

    private void populatePortions() {
        List<Food> foodList = foodService.fetchAllFoods();
        foodList.forEach(food -> {
            List<Portion> portions = food.getPortions();
            Portion portion = Portion.builder()
                    .portionName("1 g")
                    .portionSize(BigDecimal.ONE)
                    .food(food)
                    .build();
            portions.add(portion);
            food.setPortions(portions);
            foodService.saveFood(food);
        });
        foodList.forEach((Food food) -> {
            List<Portion> portions = food.getPortions();
            Portion portion = Portion.builder()
                    .portionName("100 g")
                    .portionSize(ONE_HUNDRED)
                    .food(food)
                    .build();
            portions.add(portion);
            food.setPortions(portions);
            foodService.saveFood(food);
        });
    }
    private void populateIndividualPortions() {
        Food food1 = foodService.getFoodById(1L); // omacka Kaiser bolognese
        Portion portion1 = Portion.builder()
                    .portionName("balení 350 g")
                    .portionSize(BigDecimal.valueOf(350))
                    .food(food1)
                    .build();
        portionService.addPortionToList(food1, portion1);

        Food food2 = foodService.getFoodById(2L); // Kitchin tunak
        Portion portion2 = Portion.builder()
                    .portionName("pevný podíl 150 g")
                    .portionSize(BigDecimal.valueOf(150))
                    .food(food2)
                    .build();
        portionService.addPortionToList(food2, portion2);

        Food food7 = foodService.getFoodById(7L); // brambory
        Portion portion7 = Portion.builder()
                .portionName("standard 250 g")
                .portionSize(BigDecimal.valueOf(250))
                .food(food7)
                .build();
        portionService.addPortionToList(food7, portion7);

        Food food9 = foodService.getFoodById(9L); // cibule
        Portion portion9 = Portion.builder()
                .portionName("malý kus 50 g")
                .portionSize(BigDecimal.valueOf(50))
                .food(food9)
                .build();
        portionService.addPortionToList(food9, portion9);

        Portion portion10 = Portion.builder()
                .portionName("střední kus 70 g")
                .portionSize(BigDecimal.valueOf(70))
                .food(food9)
                .build();
        portionService.addPortionToList(food9, portion10);

        Portion portion11 = Portion.builder()
                .portionName("velký kus 100 g")
                .portionSize(BigDecimal.valueOf(100))
                .food(food9)
                .build();
        portionService.addPortionToList(food9, portion11);

        Food food12 = foodService.getFoodById(12L); // michana vejce
        Portion portion12 = Portion.builder()
                .portionName("malý kus 50 g")
                .portionSize(BigDecimal.valueOf(50))
                .food(food12)
                .build();
        portionService.addPortionToList(food12, portion12);
    }
    private void populateIndividualMeals() {

        User user = userService.fetchUserByUsername("activeUser");

//        Creating meal 'Bolognese s brambory' with food 'Omacka Kaiser Bolognese'
        MealFormDTO omackaDTO = new MealFormDTO("Bolognese s brambory", BigDecimal.ONE, new BigDecimal("350"));
        List<Long> omackaList = List.of(1L);
        Meal bologneseSBrambory = mealService.createMeal(user, omackaDTO, omackaList);

//        Adding food 'brambory' to this meal
        MealFormDTO bramboryDTO = new MealFormDTO("Bolognese s brambory", BigDecimal.ONE, new BigDecimal("250"));
        List<Long> bramboryList = List.of(7L);
        mealService.addFoodToMeal(user, bologneseSBrambory.getId(), bramboryDTO, bramboryList);

        MealFormDTO vejceDTO = new MealFormDTO("Vejce s cibulí", new BigDecimal("3"), new BigDecimal("50"));
        List<Long> vejceList = List.of(12L);
        Meal vejceSCibuli = mealService.createMeal(user, vejceDTO, vejceList);

        MealFormDTO cibuleDTO = new MealFormDTO("Vejce s cibulí", BigDecimal.ONE, new BigDecimal("50"));
        List<Long> cibuleList = List.of(9L);
        mealService.addFoodToMeal(user, vejceSCibuli.getId(), cibuleDTO, cibuleList);

        MealFormDTO toastDTO = new MealFormDTO("Toast se šunkou", BigDecimal.ONE, new BigDecimal("76"));
        List<Long> toastList = List.of(5L);
        Meal toastSeSunkou = mealService.createMeal(user, toastDTO, toastList);

        MealFormDTO sunkaDTO = new MealFormDTO("Toast se šunkou", BigDecimal.ONE, new BigDecimal("64"));
        List<Long> sunkaList = List.of(6L);
        mealService.addFoodToMeal(user, toastSeSunkou.getId(), sunkaDTO, sunkaList);

        MealFormDTO kavaDTO = new MealFormDTO("Káva se smetanou", BigDecimal.ONE, new BigDecimal("100"));
        List<Long> kavaList = List.of(3L);
        Meal kavaSeSmetatnou = mealService.createMeal(user, kavaDTO, kavaList);

        MealFormDTO smetanaDTO = new MealFormDTO("Káva se smetanou", BigDecimal.ONE, new BigDecimal("10"));
        List<Long> smetanaList = List.of(4L);
        mealService.addFoodToMeal(user, kavaSeSmetatnou.getId(), smetanaDTO, smetanaList);

        MealFormDTO test1DTO = new MealFormDTO("test meal 1", BigDecimal.ONE, new BigDecimal("100"));
        List<Long> test1List = List.of(32L);
        Meal testMeal = mealService.createMeal(user, test1DTO, test1List);

        MealFormDTO test2DTO = new MealFormDTO("test meal 1", BigDecimal.ONE, new BigDecimal("100"));
        List<Long> test2List = List.of(33L);
        mealService.addFoodToMeal(user, testMeal.getId(), test2DTO, test2List);

    }

    private void populateJournalWithFoods() {
//        add food 'michana vejce' to Journal
        User user = userService.fetchUserByUsername("activeUser");
        LocalDate today = LocalDate.now();

        Food foodTest1 = foodService.getFoodById(32L);
        BigDecimal quantity = new BigDecimal("200");
        String mealType = "BREAKFAST";
        String foodName = "testing food item 1";
        Journal journal = journalService.addFoodToJournal(foodTest1, quantity, today, mealType, foodName, user);

        Food foodTest2 = foodService.getFoodById(33L);
        BigDecimal quantityT2 = new BigDecimal("200");
        String mealTypeT2 = "BREAKFAST";
        String foodNameT2 = "testing food item 2";
        Journal journalT2 = journalService.addFoodToJournal(foodTest2, quantityT2, today, mealTypeT2, foodNameT2, user);

        Food food1 = foodService.getFoodById(12L);
        BigDecimal quantity1 = new BigDecimal("150");
        String mealType1 = "BREAKFAST";
        String foodName1 = "míchaná vejce";
        Journal journal1 = journalService.addFoodToJournal(food1, quantity1, today, mealType1, foodName1, user);

        Food food2 = foodService.getFoodById(9L);
        BigDecimal quantity2 = new BigDecimal("50");
        String mealType2 = "BREAKFAST";
        String foodName2 = "cibule";
        Journal journal2 = journalService.addFoodToJournal(food2, quantity2, today, mealType2, foodName2, user);

        Food food3 = foodService.getFoodById(3L);
        BigDecimal quantity3 = new BigDecimal("100");
        String mealType3 = "AFTERNOON_SNACK";
        String foodName3 = "káva espresso";
        Journal journal3 = journalService.addFoodToJournal(food3, quantity3, today, mealType3, foodName3, user);

        LocalDate yesterday = LocalDate.now().minusDays(1);

        Food food4 = foodService.getFoodById(24L);
        BigDecimal quantity4 = new BigDecimal("200");
        String mealType4 = "AFTERNOON_SNACK";
        String foodName4 = "kofola bez cukru";
        Journal journal4 = journalService.addFoodToJournal(food4, quantity4, yesterday, mealType4, foodName4, user);

        Food food5 = foodService.getFoodById(19L);
        BigDecimal quantity5 = new BigDecimal("160");
        String mealType5 = "AFTERNOON_SNACK";
        String foodName5 = "mandarinka";
        Journal journal5 = journalService.addFoodToJournal(food5, quantity5, yesterday, mealType5, foodName5, user);

        Food food6 = foodService.getFoodById(30L);
        BigDecimal quantity6 = new BigDecimal("140");
        String mealType6 = "DINNER";
        String foodName6 = "Řecký jogurt vanilka Milko";
        Journal journal6 = journalService.addFoodToJournal(food6, quantity6, yesterday, mealType6, foodName6, user);

        LocalDate dayBeforeYesterday = LocalDate.now().minusDays(2);

        Food food7 = foodService.getFoodById(19L);
        BigDecimal quantity7 = new BigDecimal("160");
        String mealType7 = "MID_MORNING_SNACK";
        String foodName7 = "mandarinka";
        Journal journal7 = journalService.addFoodToJournal(food7, quantity7, dayBeforeYesterday, mealType7, foodName7, user);

        Food food8 = foodService.getFoodById(30L);
        BigDecimal quantity8 = new BigDecimal("140");
        String mealType8 = "AFTERNOON_SNACK";
        String foodName8 = "Řecký jogurt vanilka Milko";
        Journal journal8 = journalService.addFoodToJournal(food8, quantity8, dayBeforeYesterday, mealType8, foodName8, user);
    }

    private void populateJournalWithMeals() {
        User user = userService.fetchUserByUsername("activeUser");
        Meal meal = mealService.getMealById(5L);
        JournalMeal jm = journalMealService.convertMealToJournalMeal(meal);
        Journal journal = journalService.createJournalEntryWithMeal(LocalDate.now(), "LUNCH", jm, user);

        Meal meal1 = mealService.getMealById(1L);
        JournalMeal jm1 = journalMealService.convertMealToJournalMeal(meal1);
        Journal journal1 = journalService.createJournalEntryWithMeal(LocalDate.now(), "DINNER", jm1, user);

        Meal meal2 = mealService.getMealById(2L);
        JournalMeal jm2 = journalMealService.convertMealToJournalMeal(meal2);
        Journal journal2 = journalService.createJournalEntryWithMeal(LocalDate.now(), "BREAKFAST", jm2, user);

        Meal meal3 = mealService.getMealById(1L);
        JournalMeal jm3 = journalMealService.convertMealToJournalMeal(meal3);
        Journal journal3 = journalService.createJournalEntryWithMeal(LocalDate.now().minusDays(1), "LUNCH", jm3, user);

        Meal meal4 = mealService.getMealById(2L);
        JournalMeal jm4 = journalMealService.convertMealToJournalMeal(meal4);
        Journal journal4 = journalService.createJournalEntryWithMeal(LocalDate.now().minusDays(1), "BREAKFAST", jm4, user);

        Meal meal5 = mealService.getMealById(3L);
        JournalMeal jm5 = journalMealService.convertMealToJournalMeal(meal5);
        Journal journal5 = journalService.createJournalEntryWithMeal(LocalDate.now().minusDays(2), "BREAKFAST", jm5, user);

        Meal meal6 = mealService.getMealById(4L);
        JournalMeal jm6 = journalMealService.convertMealToJournalMeal(meal6);
        Journal journal6 = journalService.createJournalEntryWithMeal(LocalDate.now().minusDays(2), "MID_MORNING_SNACK", jm6, user);
    }
}
