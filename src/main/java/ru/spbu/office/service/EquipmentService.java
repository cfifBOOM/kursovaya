package ru.spbu.office.service;

import org.springframework.stereotype.Service;
import ru.spbu.office.model.Equipment;
import ru.spbu.office.model.EquipmentType;
import ru.spbu.office.model.User;
import ru.spbu.office.repository.EquipmentRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EquipmentService {

    private final EquipmentRepository repo;

    public EquipmentService(EquipmentRepository repo) {
        this.repo = repo;
    }

    // Список оборудования пользователя, при необходимости с фильтром по типу.
    public List<Equipment> list(User user, EquipmentType type) {
        return type == null
                ? repo.findByUserOrderByIdDesc(user)
                : repo.findByUserAndEquipmentTypeOrderByIdDesc(user, type);
    }

    // Ищем только среди записей текущего пользователя, чужие недоступны.
    public Equipment get(Long id, User user) {
        return repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoSuchElementException("Запись не найдена"));
    }

    public void create(Equipment e, User user) {
        e.setId(null);
        e.setUser(user);
        repo.save(e);
    }

    public void update(Long id, Equipment form, User user) {
        Equipment e = get(id, user);
        e.setName(form.getName());
        e.setInventoryNumber(form.getInventoryNumber());
        e.setRoom(form.getRoom());
        e.setEquipmentType(form.getEquipmentType());
        repo.save(e);
    }

    public void delete(Long id, User user) {
        repo.delete(get(id, user));
    }
}
