package com.etu.schedule.telegram.handler;

import com.etu.schedule.service.AuthorizationService;
import com.etu.schedule.service.TelegramService;
import com.etu.schedule.telegram.TelegramHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthHandler implements TelegramHandler{

    private final AuthorizationService authorizationService;
    private final TelegramService telegramService;

    @Override
    public String getCommand() {
        return "auth";
    }

    @Override
    public List<String> getListCallback() {
        return null;
    }

    @Override
    public EditMessageText editMessageFromCallback(CallbackQuery callbackQuery) {
        return null;
    }

    @Override
    public String getDescription() {
        return "Авторизация в кабинете";
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard(Long userId) {
        return null;
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {

        if (!update.getMessage().getFrom().getId().equals(update.getMessage().getChatId())){
            return Pair.of("""
                    😑 Авторизация
                    
                    Данная команда не работает в группе
                    Так как будет видно конфиденциальная информация
                    """, false);
        }

        String INVALID_MESSAGE = """
            😑 Авторизация
            Для авторизация, вы должны отправить свои данные для личного кабинета ЛЭТИ
            Чтобы в дальнейшем, бот смог отмечаться за вас
            
            Пример сообщения:
            /auth example@example.com password
            
            После авторизации, мы сохраняем только куки, для выполнения запросов
            """;

        String message = telegramService.getMessageReplaced(
                update.getMessage().getText().substring(("/" + getCommand()).length())
        ).trim();
        if (message.isEmpty()){
            return Pair.of(INVALID_MESSAGE, false);
        }

        String[] split = message.split(" ");
        if (split.length != 2){
            return Pair.of(INVALID_MESSAGE, false);
        }

        if (!split[0].matches("^[-\\w.]+@([A-z0-9][-A-z0-9]+\\.)+[A-z]{2,4}$")){
            return Pair.of("😑 Ваша почта не валидна! Попробуйте ещё раз.", false);
        }
        return Pair.of("⌛ Выполняется, пожалуйста подождите..", true);
    }

    @Override
    public String postCommand(Update update) {
        String[] emailWithPassword = telegramService.getMessageReplaced(
                    update.getMessage().getText()
                )
                .substring(("/" + getCommand()).length())
                .trim()
                .split(" ");

        String email = emailWithPassword[0];
        String password = emailWithPassword[1];
        return authorizationService.authEtu(update.getMessage().getFrom().getId(), email, password);
    }

}
