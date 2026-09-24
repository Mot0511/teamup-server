package ru.matveysuvorov.teamup.search.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.matveysuvorov.teamup.search.enums.SearchEvent;
import ru.matveysuvorov.teamup.search.enums.SearchResult;
import ru.matveysuvorov.teamup.search.models.Search;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "result"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = SearchCreatedResponse.class,
                name = "CREATED"
        ),
        @JsonSubTypes.Type(
                value = SearchFormedResponse.class,
                name = "FORMED"
        ),
        @JsonSubTypes.Type(
                value = SearchFoundResponse.class,
                name = "FOUND"
        )
})
abstract public class SearchResponse {}