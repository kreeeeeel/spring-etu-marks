package com.etu.schedule.service.impl;

import com.etu.schedule.entity.UserEntity;
import com.etu.schedule.entry.PairEntry;
import com.etu.schedule.exception.NotAuthException;
import com.etu.schedule.exception.ParseScheduleException;
import com.etu.schedule.repository.UserRepository;
import com.etu.schedule.service.NoteService;
import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.service.SeleniumService;
import com.etu.schedule.telegram.TelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final SeleniumService seleniumService;
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    @PostConstruct
    @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(cron = "0 50 9 * * ?")
    @Scheduled(cron = "0 40 11 * * ?")
    @Scheduled(cron = "0 40 13 * * ?")
    @Scheduled(cron = "0 30 15 * * ?")
    @Scheduled(cron = "0 20 17 * * ?")
    public void noteUsers() {
        log.info("Start note for users.");
        List<PairEntry> pairEntries = scheduleService.getLessonNow();
        if (pairEntries == null){
            return;
        }

        List<UserEntity> userForNote = userRepository.findUserForNote(
                pairEntries.stream()
                        .map(PairEntry::getGroup)
                        .toList()
        );
        userForNote.forEach(it -> executor.execute(() -> noteUser(it)));
        log.info("Noted " + userForNote.size() + " users.");
    }

    private void noteUser(UserEntity user) {

        StringBuilder stringBuilder = new StringBuilder();
        String authError = "❌ Не удалось авторизоваться в личном кабинете.." + System.lineSeparator() + "Авторизуйтесь заново через команду /auth";
        String parseError = "❌ Произошла ошибка при парсинге данных" + System.lineSeparator() + "Возможно не загружается личный кабинет";
        String webDriverError = "❌ Невозможно загрузить страницу" + System.lineSeparator() + "Неполадки в личном кабинете";

        WebDriver webDriver = seleniumService.getWebDriver();
        try {
            stringBuilder.append(noteEtu(webDriver, user.getEmail(), user.getPassword()));
        } catch (NotAuthException exception){
            stringBuilder.append(authError);
            user.setNote(false);
            userRepository.save(user);
        } catch (ParseScheduleException exception) {
            stringBuilder.append(parseError);
        } catch (WebDriverException exception){
            stringBuilder.append(webDriverError);
        }

        telegramBot.send(user.getTelegramId(), stringBuilder.toString());
        webDriver.quit();

    }

    private String noteEtu(WebDriver webDriver, String email, String password) {

        log.info("Start note user " + email);

        String LK_URL = "https://lk.etu.ru/login";
        webDriver.get(LK_URL);

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .findFirst()
                .ifPresent(element -> element.sendKeys(email));

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .reduce((first, second) -> second)
                .ifPresent(element -> element.sendKeys(password));

        webDriver.findElement(By.xpath("//button[@class='btn btn-lg btn-primary btn-login']")).click();
        if(webDriver.getCurrentUrl().equals(LK_URL)) {
            throw new NotAuthException();
        }

        webDriver.findElements(By.xpath("//div[@class='card-body d-flex align-items-start flex-row']")).stream()
                .filter(it -> it.getText().equals("Посещаемость"))
                .findFirst()
                .orElseThrow(NotAuthException::new)
                .click();

        webDriver.switchTo().window(
                webDriver.getWindowHandles().stream()
                        .reduce((a, b) -> b)
                        .stream()
                        .findFirst()
                        .orElseThrow(ParseScheduleException::new));

        webDriver.findElement(By.xpath("//button[@class='btn auth-card__button mb-2 btn-white']")).click();
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

        return String.format("""
                ✅ Вы были отмечены:
                %s""", stringBuilder);
    }
}
