package ru.spbu.office.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Укажите название")
    @Size(max = 100, message = "Не более 100 символов")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Укажите инвентарный номер")
    @Size(max = 50, message = "Не более 50 символов")
    @Column(name = "inventory_number", nullable = false, length = 50)
    private String inventoryNumber;

    @NotBlank(message = "Укажите кабинет")
    @Size(max = 20, message = "Не более 20 символов")
    @Column(nullable = false, length = 20)
    private String room;

    @NotNull(message = "Выберите тип")
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false, length = 20)
    private EquipmentType equipmentType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }
}
