package com.example.ChronoTask.view;

import com.example.ChronoTask.model.Task;
import com.example.ChronoTask.model.NotificationEntity;
import com.example.ChronoTask.model.TaskReschedules;
import com.example.ChronoTask.service.TaskService;
import com.example.ChronoTask.service.NotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;

import com.vaadin.flow.component.html.Span;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;


import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Route("archive")
@AnonymousAllowed
@CssImport("./styles/archive-view.css")
public class ArchiveView extends VerticalLayout {

    private final TaskService taskService;
    private final NotificationService notificationService;

    public ArchiveView(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H2 header = new H2("Архив задач");
        add(header);

        Long userId = getCurrentUserId();
        List<Task> archivedTasks = taskService.findArchivedTasksByUser(userId);

        if (archivedTasks.isEmpty()) {
            add(new Span("Нет архивированных задач."));
        } else {
            for (Task task : archivedTasks) {
                VerticalLayout taskLayout = new VerticalLayout();
                taskLayout.addClassName("archive-task-item");

                taskLayout.add(new Span("Название: " + task.getTitle()));
                taskLayout.add(new Span("Описание: " + task.getDescription()));
                taskLayout.add(new Span("Дата: " + (task.getTaskDate() != null ? task.getTaskDate().toString() : "")));
                taskLayout.add(new Span("Время: " + (task.getTaskTime() != null ?
                        task.getTaskTime().toLocalDateTime().toLocalTime().toString() : "")));

                if ("LOW".equalsIgnoreCase(task.getPriority())) {
                    Button historyButton = new Button("История переноса", e -> openRescheduleHistoryDialog(task));
                    taskLayout.add(historyButton);
                }

                add(taskLayout);
            }
        }

        Button backButton = new Button("Назад в календарь", e -> UI.getCurrent().navigate("calendar"));
        add(backButton);
    }


    private void openRescheduleHistoryDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("История переноса: " + task.getTitle());
        VerticalLayout layout = new VerticalLayout();

        List<?> history = taskService.getRescheduleHistory(task.getId());

        if (history.isEmpty()) {
            layout.add(new Span("Нет истории переноса для этой задачи."));
        } else {
            for (Object record : history) {
                TaskReschedules reschedule = (TaskReschedules) record;
               layout.add(new Span("Новая дата: " + reschedule.getNewDate() +
               ", новое время: " + (reschedule.getNewTime() != null ?
               reschedule.getNewTime().toLocalDateTime().toLocalTime().toString() : "") +
               ", причина: " + reschedule.getRescheduleReason()));
            }
        }
        dialog.add(layout);
        Button close = new Button("Закрыть", e -> dialog.close());
        dialog.getFooter().add(close);
        dialog.open();
    }

    private Long getCurrentUserId() {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            UI.getCurrent().navigate("login");
            throw new IllegalStateException("Пользователь не аутентифицирован!");
        }
        return userId;
    }
}
