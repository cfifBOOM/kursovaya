package ru.spbu.office.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.spbu.office.model.Equipment;
import ru.spbu.office.model.EquipmentType;
import ru.spbu.office.model.User;
import ru.spbu.office.service.EquipmentService;
import ru.spbu.office.service.UserService;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipment;
    private final UserService users;

    public EquipmentController(EquipmentService equipment, UserService users) {
        this.equipment = equipment;
        this.users = users;
    }

    @ModelAttribute("types")
    public EquipmentType[] types() {
        return EquipmentType.values();
    }

    // Страница 3: список оборудования + фильтр по типу.
    @GetMapping
    public String list(@RequestParam(required = false) EquipmentType type,
                       Authentication auth, Model model) {
        User user = users.getByLogin(auth.getName());
        model.addAttribute("items", equipment.list(user, type));
        model.addAttribute("selectedType", type);
        return "equipment-list";
    }

    // Страница 4: форма добавления.
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("equipment", new Equipment());
        model.addAttribute("action", "/equipment/new");
        model.addAttribute("title", "Добавление оборудования");
        return "equipment-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("equipment") Equipment form,
                         BindingResult result, Authentication auth, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/equipment/new");
            model.addAttribute("title", "Добавление оборудования");
            return "equipment-form";
        }
        equipment.create(form, users.getByLogin(auth.getName()));
        return "redirect:/equipment";
    }

    // Страница 5: форма редактирования.
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        User user = users.getByLogin(auth.getName());
        model.addAttribute("equipment", equipment.get(id, user));
        model.addAttribute("action", "/equipment/" + id + "/edit");
        model.addAttribute("title", "Редактирование оборудования");
        return "equipment-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("equipment") Equipment form,
                         BindingResult result, Authentication auth, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/equipment/" + id + "/edit");
            model.addAttribute("title", "Редактирование оборудования");
            return "equipment-form";
        }
        equipment.update(id, form, users.getByLogin(auth.getName()));
        return "redirect:/equipment";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        equipment.delete(id, users.getByLogin(auth.getName()));
        return "redirect:/equipment";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String notFound() {
        return "redirect:/equipment";
    }
}
