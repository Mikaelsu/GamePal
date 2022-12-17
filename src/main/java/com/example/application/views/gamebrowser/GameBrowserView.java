package com.example.application.views.gamebrowser;

import com.example.application.components.card.Card;
import com.example.application.data.entity.MapGame;
import com.example.application.data.service.MapGameService;
import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Game Browser")
@Route(value = "gamebrowser", layout = MainLayout.class)
@PermitAll
public class GameBrowserView extends VerticalLayout {

    private final SecurityService securityService;
    private final MapGameService mapGameService;

    private List<MapGame> allGames;

    private final String user;

    public GameBrowserView(SecurityService securityService, MapGameService mapGameService) {
        this.securityService = securityService;
        this.mapGameService = mapGameService;
        this.user = securityService.getAuthenticatedUser().getUsername();
        allGames = new ArrayList<>();

        refresh();
    }

    private void refresh() {
        removeAll();
        allGames = mapGameService.getAll();
        for (MapGame mapGame : allGames) {
            Card card = new Card(mapGameService, mapGame, mapGame.getPlayers().size());
            var settings = new MenuBar();
            settings.addThemeVariants(MenuBarVariant.LUMO_SMALL, MenuBarVariant.LUMO_TERTIARY);
            var cogWheel = createIconItem(settings, VaadinIcon.COG, null,
                    null, false, null);

            var subMenu = cogWheel.getSubMenu();

            createIconItem(subMenu, VaadinIcon.EDIT, "Edit", "", true, menuItemClickEvent -> {
                EditSession editSession = new EditSession(mapGameService, mapGame);
                editSession.open();
                editSession.addDetachListener(detachEvent -> refresh());
            });
            createIconItem(subMenu, VaadinIcon.TRASH, "Delete", "", true, menuItemClickEvent -> {
                ConfirmDialog confirmDialog = new ConfirmDialog("Delete Session?",
                        "Are you sure you want to delete this session?", "Delete", confirmEvent -> {
                    mapGameService.delete(mapGame);
                    refresh();
                });
                confirmDialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());
                confirmDialog.setCancelable(true);
                confirmDialog.open();

            });

            card.getJoinLayout().addComponentAsFirst(settings);

            add(card);
        }
        setPadding(true);

        addComponentAsFirst(new Button("New Game", VaadinIcon.PLUS_CIRCLE_O.create(), buttonClickEvent -> {
            EditSession editSession = new EditSession(mapGameService);
            editSession.open();
            editSession.addDetachListener(detachEvent -> refresh());
        }));
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
                                    String label, String ariaLabel, boolean isChild,
                                    ComponentEventListener<ClickEvent<MenuItem>> clickEventComponent) {
        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, clickEventComponent);

        if (ariaLabel != null) {
            item.getElement().setAttribute("aria-label", ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }
}
