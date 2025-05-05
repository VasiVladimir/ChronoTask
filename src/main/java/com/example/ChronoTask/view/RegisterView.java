package com.example.ChronoTask.view;

import com.example.ChronoTask.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.dependency.CssImport;

@Route("register")
@CssImport("./styles/styles.css") // Подключение CSS файла
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;


        H1 title = new H1("Регистрация в системе");


        TextField usernameField = new TextField("Логин");
        usernameField.setRequired(true);
        usernameField.setErrorMessage("Поле обязательно для заполнения");


        EmailField emailField = new EmailField("Email");
        emailField.setRequired(true);
        emailField.setErrorMessage("Введите корректный email");


        PasswordField passwordField = new PasswordField("Пароль");
        passwordField.setRequired(true);
        passwordField.setMinLength(6);
        passwordField.setErrorMessage("Пароль должен содержать минимум 6 символов");


        Button registerButton = new Button("Зарегистрироваться", event -> {
            String username = usernameField.getValue();
            String email = emailField.getValue();
            String password = passwordField.getValue();


            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Notification.show("Поля обязательны к заполнению", 3000, Notification.Position.MIDDLE);
                return;
            }


            if (password.length() < 6) {
                Notification.show("Пароль должен содержать минимум 6 символов", 3000, Notification.Position.MIDDLE);
                return;
            }


            boolean success = userService.registerUser(username, email, password);
            if (success) {
                Notification.show("Регистрация успешна!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                Notification.show("Ошибка: пользователь уже существует", 3000, Notification.Position.MIDDLE);
            }
        });


        Button backToLoginButton = new Button("Назад", event -> {

            this.getElement().getChildren().forEach(child -> {
                child.getClassList().add("fadeOut");
            });


            getUI().ifPresent(ui -> ui.access(() -> {

                ui.getPage().executeJs("setTimeout(function() { window.location = '/login'; }, 1000);");
            }));
        });


        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();


        addClassName("fadeIn");

        add(title, usernameField, emailField, passwordField, registerButton, backToLoginButton);
    }
}
