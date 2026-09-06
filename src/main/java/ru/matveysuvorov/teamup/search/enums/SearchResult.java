package ru.matveysuvorov.teamup.search.enums;

public enum SearchResult {
    CREATED("created"), FOUND("found"),
    FORMED("formed");

    private final String value;

    SearchResult(String value) { this.value = value; }

    public String value() { return value; }
}