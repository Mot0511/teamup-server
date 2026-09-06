package ru.matveysuvorov.teamup.search.dto;

import ru.matveysuvorov.teamup.search.enums.SearchResult;
import ru.matveysuvorov.teamup.search.models.Search;

public class SearchCreatedResponse extends SearchResponse {

    private final Search search;

    public SearchCreatedResponse(Search search) {
        this.search = search;
        super.result = SearchResult.CREATED;
    }

}
