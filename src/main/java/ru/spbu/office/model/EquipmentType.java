package ru.spbu.office.model;

// Предустановленные типы оборудования.
public enum EquipmentType {
    COMPUTER("Компьютер"),
    MONITOR("Монитор"),
    PRINTER("Принтер"),
    NETWORK("Сетевое оборудование");

    private final String title;

    EquipmentType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
