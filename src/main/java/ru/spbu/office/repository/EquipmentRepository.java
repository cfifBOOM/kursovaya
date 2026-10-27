package ru.spbu.office.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.spbu.office.model.Equipment;
import ru.spbu.office.model.EquipmentType;
import ru.spbu.office.model.User;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByUserOrderByIdDesc(User user);
    List<Equipment> findByUserAndEquipmentTypeOrderByIdDesc(
            User user, EquipmentType type);
    Optional<Equipment> findByIdAndUser(Long id, User user);
}
