package ru.matveysuvorov.teamup.search.dto;

import ru.matveysuvorov.teamup.search.enums.SearchResult;
import ru.matveysuvorov.teamup.search.models.Search;

public class SearchFoundResponse extends SearchResponse {

    private final Search search;
public SearchFoundResponse(Search search) {
        this.search = search;
    }

    public Search getSearch() {
        return search;
    }

}
