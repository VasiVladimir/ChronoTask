package com.example.ChronoTask.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.ChronoTask.security.CustomUserDetails;

@Route("login")
@CssImport("./styles/styles.css")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final AuthenticationManager authenticationManager;

    public LoginView(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;

        addClassName("fadeIn");
        H1 title = new H1("Авторизуйтесь в систему");


        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);
        usernameField.setErrorMessage("Поле обязательно для заполнения");

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        passwordField.setErrorMessage("Поле обязательно для заполнения");


        Button loginButton = new Button("Войти", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Поля обязательны к заполнению", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {

                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password)
                );

                SecurityContextHolder.getContext().setAuthentication(auth);


                if (auth.getPrincipal() instanceof CustomUserDetails) {
                    Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

                    VaadinSession.getCurrent().setAttribute("userId", userId);
                }


                UI.getCurrent().navigate("calendar");

            } catch (AuthenticationException ex) {

                Notification.show("Неверный логин или пароль", 3000, Notification.Position.MIDDLE);
            }
        });


        Button registerButton = new Button("Регистрация", event -> {
            UI.getCurrent().navigate("register");
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(loginButton, registerButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        add(title, usernameField, passwordField, buttonLayout);
    }
}
