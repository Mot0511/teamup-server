package ru.matveysuvorov.teamup.search;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matveysuvorov.teamup.search.dto.SearchRequest;
import ru.matveysuvorov.teamup.search.dto.SearchResponse;

import java.util.UUID;

@RestController
@RequestMapping("/searches")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/")
    public SearchResponse search(
            Authentication auth,
            SearchRequest request
    ) {
        return searchService.getSearch(auth.getName(), request);
    }

    @PostMapping("/{searchId}/leave")
    public void leave(
            @PathVariable UUID searchId,
            Authentication auth
    ) {
        searchService.leaveSearch(auth.getName(), searchId);
    }

}
