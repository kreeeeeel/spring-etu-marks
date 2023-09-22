package com.etu.schedule.service.impl;

import com.etu.schedule.entity.UserEntity;
import com.etu.schedule.exception.NotAuthException;
import com.etu.schedule.exception.ParseScheduleException;
import com.etu.schedule.repository.UserRepository;
import com.etu.schedule.service.NoteService;
import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.service.SeleniumService;
import com.etu.schedule.telegram.TelegramBot;
import com.etu.schedule.util.EncryptUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    @Value("${secret.key}")
    private String secret;

    private final SeleniumService seleniumService;
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    @PostConstruct
    @Scheduled(cron = "0 0 8 ? * MON-SAT")
    @Scheduled(cron = "0 50 9 ? * MON-SAT")
    @Scheduled(cron = "0 40 11 ? * MON-SAT")
    @Scheduled(cron = "0 40 13 ? * MON-SAT")
    @Scheduled(cron = "0 30 15 ? * MON-SAT")
    @Scheduled(cron = "0 20 17 ? * MON-SAT")
    public void noteUsers() {
        executor.execute(() -> {
            log.info("Start note for users.");
            List<UserEntity> userForNote = userRepository.findUserForNote(
                    scheduleService.getCurrentWeek(),
                    scheduleService.getCurrentDay(),
                    scheduleService.getCurrentPair()
            );

            if (userForNote == null || userForNote.isEmpty()) {
                log.info("Not found users for note");
                return;
            }

            userForNote.forEach(it -> executor.execute(() -> noteUser(it)));
            log.info("Noted " + userForNote.size() + " users.");
        });
    }

    private void noteUser(UserEntity user) {

        StringBuilder stringBuilder = new StringBuilder();
        String authError = "❌ Не удалось авторизоваться в личном кабинете.." + System.lineSeparator() + "Авторизуйтесь заново через команду /auth";
        String parseError = "❌ Произошла ошибка при парсинге данных" + System.lineSeparator() + "Возможно не загружается личный кабинет";
        String webDriverError = "❌ Невозможно загрузить страницу" + System.lineSeparator() + "Неполадки в личном кабинете";

        WebDriver webDriver = seleniumService.getWebDriver();
        try {
            String value = noteEtu(
                    webDriver,
                    EncryptUtil.decrypt(user.getEmail(), secret),
                    EncryptUtil.decrypt(user.getPassword(), secret)
            );
            if (value != null) stringBuilder.append(value);
        } catch (NotAuthException exception){
            stringBuilder.append(authError);
            user.setNote(false);
            userRepository.save(user);
        } catch (ParseScheduleException exception) {
            stringBuilder.append(parseError);
        } catch (WebDriverException exception){
            stringBuilder.append(webDriverError);
        }

        if (!stringBuilder.isEmpty()) telegramBot.send(user.getTelegramId(), stringBuilder.toString());
        webDriver.quit();

    }

    private String noteEtu(WebDriver webDriver, String email, String password) {

        webDriver.get("https://digital.etu.ru/attendance/auth");
        webDriver.findElement(By.xpath("//button[@class='btn auth-card__button mb-2 btn-white']")).click();

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .findFirst()
                .ifPresent(element -> element.sendKeys(email));

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .reduce((first, second) -> second)
                .ifPresent(element -> element.sendKeys(password));

        webDriver.findElement(By.xpath("//button[@class='btn btn-lg btn-primary btn-login']")).click();
        if(webDriver.getCurrentUrl().equals("https://lk.etu.ru/login")) {
            throw new NotAuthException();
        }

        webDriver.findElement(By.xpath("//button[@class='btn btn-login btn-block btn-primary btn-approve']")).click();
        StringBuilder stringBuilder = new StringBuilder();
        webDriver.findElements(By.xpath("//div[@class='card class-card mt-2']")).forEach(it -> {
            try {
                it.findElement(By.xpath("//button[@class='btn custom-button small mt-3 btn-primary']")).click();

                List<WebElement> elements1 = it.findElements(By.xpath("//div[@class='title-3']"));
                String[] strings = elements1.get(1).getText().split("\n");
                String name = strings[1];
                String time = elements1.get(0).getText();
                String type = strings[0];

                stringBuilder.append(String.format("%s - %s (%s)", time, name, type)).append(System.lineSeparator());
            } catch (NoSuchElementException ignored) {}
        });

        return !stringBuilder.isEmpty() ? String.format("""
                ✅ Вы были отмечены:
                %s""", stringBuilder) : null;
    }
}
