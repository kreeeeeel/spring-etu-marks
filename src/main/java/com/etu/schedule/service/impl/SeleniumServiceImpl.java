package com.etu.schedule.service.impl;

import com.etu.schedule.service.SeleniumService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("deprecation")
public class SeleniumServiceImpl implements SeleniumService {

    @Override
    public WebDriver getWebDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--headless");

        WebDriver webDriver = new FirefoxDriver(firefoxOptions);
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        return webDriver;
    }

}
