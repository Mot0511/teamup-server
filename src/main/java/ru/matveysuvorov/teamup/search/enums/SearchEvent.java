package ru.matveysuvorov.teamup.search.enums;

public enum SearchEvent {
    NEW_USER("new_user"), LEAVE_USER("leave_user"),
    FORMED("formed");

    private final String value;

    SearchEvent(String value) { this.value = value; }

    public String value() { return value; }
}