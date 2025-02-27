package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "yar1kk_tg_tinder_helper_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7586816756:AAHZfsxw0cPUhzqIKjhhN8GD3GEnR59HAHE";
    public static final String OPEN_AI_TOKEN = "gpt:T0iTZNHd48ZlWS64y3EfJFkblB3TMr4vkS5kVmutmQNPEEps";
    public DialogMode mode = DialogMode.MAIN;
    public ChatGPTService gptService = new ChatGPTService(OPEN_AI_TOKEN);
    private List<String> chat;
    private UserInfo myInfo;
    private UserInfo personInfo;
    private int questionNumber;
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        switch (message) {
            case "/start" -> {
                mode = DialogMode.MAIN;

                showMainMenu(
                        "головне меню бота", "/start",
                        "генерація Tinder-профілю", "/profile",
                        "повідомлення для знайомства", "/opener",
                        "листування від вашого імені", "/message",
                        "листування із зірками", "/date",
                        "поставити запитання чату GPT", "/gpt");

                sendTextMessage(loadMessage("main"));
                sendPhotoMessage("main");
                return;
            }

            case "/gpt" -> {
                mode = DialogMode.GPT;

                sendPhotoMessage("gpt");
                String gptMessage = loadMessage("gpt");
                sendTextMessage(gptMessage);
                return;
            }

            case "/date" -> {
                mode = DialogMode.DATE;

                sendPhotoMessage("date");
                String dateMessage = loadMessage("date");
                sendTextButtonsMessage(dateMessage,
                        "Аріана Гранде 🔥", "date_grande",
                        "Марго Роббі 🔥🔥", "date_robbie",
                        "Зендея 🔥🔥🔥", "date_zendaya",
                        "Райан Гослінг 😎", "date_gosling",
                        "Том Харді 😎😎", "date_hardy");
                return;
            }

            case "/message" -> {
                mode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                String gptMessageHelper = loadMessage("message");
                sendTextButtonsMessage(gptMessageHelper,
                        "Наступне повідомлення", "message_next",
                        "Запросити на побачення", "message_date");

                chat = new ArrayList<>();
                return;
            }

            case "/profile" -> {
                mode = DialogMode.PROFILE;
                sendPhotoMessage("profile");
                String profileMessage = loadMessage("profile");
                sendTextMessage(profileMessage);

                myInfo = new UserInfo();
                questionNumber = 1;
                sendTextMessage("Введіть ім'я: ");

                return;
            }

            case "/opener" -> {
                mode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                String openerMessage = loadMessage("opener");
                sendTextMessage(openerMessage);

                personInfo = new UserInfo();
                questionNumber = 1;
                sendTextMessage("Введіть ім'я: ");

                return;
            }
        }

        switch (mode) {
            case GPT -> {
                String prompt = loadPrompt("gpt");
                Message msg = sendTextMessage("Почекай...");

                String answer = gptService.sendMessage(prompt, message);
                updateTextMessage(msg, answer);
            }

            case DATE -> {
                String query = getCallbackQueryButtonKey();

                if (query.startsWith("date_")) {
                    sendPhotoMessage(query);
                    String prompt1 = loadPrompt(query);
                    gptService.setPrompt(prompt1);
                    return;
                }

                Message msgDate = sendTextMessage("Почекай...");

                String answer1 = gptService.addMessage(message);
                updateTextMessage(msgDate, answer1);
            }

            case MESSAGE -> {
                String queryMessage = getCallbackQueryButtonKey();

                if (queryMessage.startsWith("message_")) {
                    String promptMsg = loadPrompt(queryMessage);
                    String history = String.join("/n/n", chat);

                    Message msgChat = sendTextMessage("Почекай...");

                    updateTextMessage(msgChat, gptService.sendMessage(promptMsg, history));
                }
                chat.add(message);
            }

            case PROFILE -> {
                if (questionNumber <= 6) {
                    askQuestion(message, myInfo, "profile");
                }
            }
            case OPENER -> {
                if (questionNumber <= 6) {
                    askQuestion(message, personInfo, "opener");
                }
            }

        }

    }

    private void askQuestion(String message, UserInfo user, String profileName) {
        switch (questionNumber) {
            case 1 -> {
                user.name = message;
                questionNumber = 2;
                sendTextMessage("Введіть вік: ");

                return;
            }
            case 2 -> {
                user.age = message;
                questionNumber = 3;
                sendTextMessage("Введіть місто? ");

                return;
            }
            case 3 -> {
                user.city = message;
                questionNumber = 4;
                sendTextMessage("Введіть професію: ");

                return;
            }
            case 4 -> {
                user.occupation = message;
                questionNumber = 5;
                sendTextMessage("Введіть хоббі?");

                return;
            }
            case 5 -> {
                user.hobby = message;
                questionNumber = 6;
                sendTextMessage("Введіть цілі знайомства?");

                return;
            }
            case 6 -> {
                user.goals = message;

                String prompt = loadPrompt(profileName);
                Message msgDate = sendTextMessage("Почекай...");

                String answer = gptService.sendMessage(prompt, user.toString());
                updateTextMessage(msgDate, answer);

                return;
            }
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
