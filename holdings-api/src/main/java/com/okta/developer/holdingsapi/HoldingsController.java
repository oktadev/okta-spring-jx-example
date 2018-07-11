package com.okta.developer.holdingsapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/holdings")
public class HoldingsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();
    private final Client client;
    private static final String HOLDINGS_ATTRIBUTE_NAME = "holdings";

    public HoldingsController(Client client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    private User getUser(Principal principal) {
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            Map<String, Object> details = (Map<String, Object>) authentication.getUserAuthentication().getDetails();
            return client.getUser(details.get("sub").toString());
        } else {
            return client.getUser(principal.getName());
        }
    }

    @GetMapping
    public List<Holding> getHoldings(Principal principal) {
        User user = getUser(principal);

        String holdingsFromOkta = (String) user.getProfile().get(HOLDINGS_ATTRIBUTE_NAME);
        List<Holding> holdings = new LinkedList<>();

        if (holdingsFromOkta != null) {
            try {
                holdings = mapper.readValue(holdingsFromOkta, new TypeReference<List<Holding>>() {});
            } catch (IOException io) {
                logger.error("Error marshalling Okta custom data: " + io.getMessage());
                io.printStackTrace();
            }
        }

        return holdings;
    }

    @PostMapping
    public Holding[] saveHoldings(@RequestBody Holding[] holdings, Principal principal) {
        User user = getUser(principal);

        try {
            String json = mapper.writeValueAsString(holdings);
            user.getProfile().put(HOLDINGS_ATTRIBUTE_NAME, json);
            user.update();
        } catch (JsonProcessingException e) {
            logger.error("Error saving Okta custom data: " + e.getMessage());
            e.printStackTrace();
        }
        return holdings;
    }
}
