package ru.matveysuvorov.teamup.search.dto;

import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.search.enums.SearchResult;
import ru.matveysuvorov.teamup.search.models.Search;

public class SearchFormedResponse extends SearchResponse {

    private final ChatEntity team;

    public SearchFormedResponse(ChatEntity team) {
        this.team = team;
        super.result = SearchResult.FOUND;
    }

}
