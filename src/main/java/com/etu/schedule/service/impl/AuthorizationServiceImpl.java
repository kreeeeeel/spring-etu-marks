package com.etu.schedule.service.impl;

import com.etu.schedule.entity.UserEntity;
import com.etu.schedule.exception.ParseScheduleException;
import com.etu.schedule.repository.UserRepository;
import com.etu.schedule.service.AuthorizationService;
import com.etu.schedule.service.SeleniumService;
import com.etu.schedule.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    @Value("${secret.key}")
    private String secret;

    private final SeleniumService seleniumService;
    private final UserRepository userRepository;

    @Override
    @SneakyThrows
    public String authEtu(Long userId, String email, String password) {

        log.info("Start autorization for user " + userId);

        String URL = "https://lk.etu.ru/login";
        String NOT_AUTH_MESSAGE = """
            🤔 Не удалось авторизоваться:(
            
            Проверьте пожалуйста ваши личные данные
            И попробуйте снова!
            """;

        WebDriver webDriver = seleniumService.getWebDriver();
        webDriver.get(URL);

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .findFirst()
                .ifPresent(element -> element.sendKeys(email));

        webDriver.findElements(By.xpath("//input[@class='form-control form-control-lg form-control-login mb-1']"))
                .stream()
                .reduce((first, second) -> second)
                .ifPresent(element -> element.sendKeys(password));

        webDriver.findElement(By.xpath("//button[@class='btn btn-lg btn-primary btn-login']")).click();
        if(webDriver.getCurrentUrl().equals(URL)){
            webDriver.close();
            return NOT_AUTH_MESSAGE;
        }

        webDriver.findElements(By.xpath("//div[@class='button-menu-mobile open-left flex-center']")).stream()
                .reduce((a, b) -> b)
                .stream()
                .findFirst()
                .ifPresent(WebElement::click);

        webDriver.findElements(By.xpath("//li[@role='presentation']")).stream()
                .filter(it -> it.getText().trim().equals("Аккаунт ETU ID"))
                .findFirst()
                .ifPresent(WebElement::click);

        webDriver.switchTo().window(
                webDriver.getWindowHandles().stream()
                        .reduce((a, b) -> b)
                        .stream()
                        .findFirst()
                        .orElseThrow(ParseScheduleException::new));

        webDriver.findElement(By.xpath("//button[@class='btn btn-login btn-block btn-primary btn-approve']"))
                .click();

        String name = webDriver.findElement(By.xpath("//div[@class='row align-items-center no-gutters align-content-center']"))
                .findElement(By.xpath("//h4[@class='mb-1']")).getText().trim();

        String group = webDriver.findElements(By.xpath("//div[@class='col-sm-12 col-md-6']")).stream()
                .filter(it -> it.getText().trim().startsWith("Номер группы"))
                .map(it -> it.getText().trim().replace("Номер группы", "").trim())
                .findFirst()
                .orElse(null);

        webDriver.quit();
        if (group != null) {

            UserEntity userEntity = userRepository.findByTelegramId(userId)
                    .orElse(UserEntity.builder().telegramId(userId).build());

            userEntity.setEmail(EncryptUtil.encrypt(email, secret));
            userEntity.setName(name);
            userEntity.setGroupEtu(group);
            userEntity.setGroupSchedule(group);
            userEntity.setPassword(EncryptUtil.encrypt(password, secret));
            userEntity.setNote(true);

            userRepository.save(userEntity);
            return String.format("""
                    😀 Авторизация прошла успешно!
                    
                    %s
                    Ваша группа: %s
                    
                    Теперь бот будет заходить и отмечаться за вас!
                    """, name, group);
        }
        return NOT_AUTH_MESSAGE;
    }

}
