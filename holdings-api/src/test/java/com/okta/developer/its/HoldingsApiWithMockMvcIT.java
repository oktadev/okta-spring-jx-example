package com.okta.developer.its;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.okta.developer.holdingsapi.Holding;
import com.okta.developer.holdingsapi.HoldingsApiApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = HoldingsApiWithMockMvcIT.RandomPortInitializer.class)
@SpringBootTest(classes = {
                    HoldingsApiWithMockMvcIT.TestResourceServerConfiguration.class,
                    HoldingsApiApplication.class
                },
                webEnvironment = RANDOM_PORT,
                properties = {
                    "okta.client.token=FAKE_TEST_TOKEN",
                    "okta.client.orgUrl=http://localhost:${wiremock.server.port}",
                })
public class HoldingsApiWithMockMvcIT {

    private final static String TEST_USER_ID = "user-id-123";

    private WireMockServer wireMockServer;

    @Value("${wiremock.server.port}")
    private int mockServerPort;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    @WithMockUser(username=TEST_USER_ID)
    public void testGetHoldings() throws Exception {

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/holdings")
                .accept(MediaType.APPLICATION_JSON)
                .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().is(200))

                .andExpect(jsonPath("$[0].amount").value("amount-1"))
                .andExpect(jsonPath("$[0].crypto").value("crypto-1"))
                .andExpect(jsonPath("$[0].currency").value("currency-1"))

                .andExpect(jsonPath("$[1].amount").value("amount-2"))
                .andExpect(jsonPath("$[1].crypto").value("crypto-2"))
                .andExpect(jsonPath("$[1].currency").value("currency-2"));
    }

    @Test
    @WithMockUser(username=TEST_USER_ID)
    public void testPostHoldings() throws Exception {

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        Holding[] holdings = {
                new Holding()
                    .setCrypto("crypto-1")
                    .setCurrency("currency-1")
                    .setAmount("amount-1"),
                new Holding()
                    .setCrypto("crypto-2")
                    .setCurrency("currency-2")
                    .setAmount("amount-2")
        };

        mockMvc.perform(MockMvcRequestBuilders.post("/api/holdings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .with(securityContext(SecurityContextHolder.getContext()))
                .with(csrf())
                .content(new ObjectMapper().writeValueAsString(holdings)))

                .andExpect(status().is(200))

                .andExpect(jsonPath("$[0].amount").value("amount-1"))
                .andExpect(jsonPath("$[0].crypto").value("crypto-1"))
                .andExpect(jsonPath("$[0].currency").value("currency-1"))

                .andExpect(jsonPath("$[1].amount").value("amount-2"))
                .andExpect(jsonPath("$[1].crypto").value("crypto-2"))
                .andExpect(jsonPath("$[1].currency").value("currency-2"));
    }

    @Before
    public void startMockServer() throws IOException {
        wireMockServer = new WireMockServer(wireMockConfig().port(mockServerPort));

        // load a JSON file from the classpath
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/its/user.json"), StandardCharsets.UTF_8);

        wireMockServer.stubFor(WireMock.get("/api/v1/users/" + TEST_USER_ID)
                .willReturn(aResponse().withBody(body)));

        wireMockServer.stubFor(WireMock.put("/api/v1/users/" + TEST_USER_ID)
                .willReturn(aResponse().withBody(body)));

        wireMockServer.start();
    }

    @After
    public void stopMockServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            int randomPort = SocketUtils.findAvailableTcpPort();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "wiremock.server.port=" + randomPort
            );
        }
    }

    public static class TestResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer security) {
            security.stateless(false);
        }
    }
}