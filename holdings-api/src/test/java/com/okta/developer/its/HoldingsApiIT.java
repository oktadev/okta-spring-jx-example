package com.okta.developer.its;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.okta.developer.holdingsapi.Holding;
import com.okta.developer.holdingsapi.HoldingsApiApplication;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = HoldingsApiIT.RandomPortInitializer.class)
@SpringBootTest(classes = {HoldingsApiApplication.class},
                webEnvironment = RANDOM_PORT,
                properties = {
                    "okta.client.token=FAKE_TEST_TOKEN",
                    "okta.oauth2.localTokenValidation=false",
                    "okta.oauth2.discoveryDisabled=true",
                    "okta.client.orgUrl=http://localhost:${wiremock.server.port}",
                    "okta.oauth2.issuer=http://localhost:${wiremock.server.port}/oauth/issuer",
                    "security.oauth2.client.clientId=" + HoldingsApiIT.TEST_CLIENT_ID,
                    "security.oauth2.client.clientSecret=" + HoldingsApiIT.TEST_CLIENT_SECRET,
                    "security.oauth2.resource.userInfoUri=http://localhost:${wiremock.server.port}/oauth/userInfoUri",
                    "security.oauth2.client.accessTokenUri=http://localhost:${wiremock.server.port}/oauth/token",
                    "security.oauth2.client.userAuthorizationUri=http://localhost:${wiremock.server.port}/oauth/authorize"
                })
public class HoldingsApiIT {

    private final static String TEST_ACCESS_TOKEN = "fake-access-token";
    private final static String TEST_USER_ID = "user-id-123";
    final static String TEST_CLIENT_ID = "FAKE_CLIENT_ID";
    final static String TEST_CLIENT_SECRET = "FAKE_CLIENT_SECRET";

    private WireMockServer wireMockServer;

    @Value("${wiremock.server.port}")
    private int mockServerPort;

    @LocalServerPort
    int applicationPort;

    private ExtractableResponse performLogin() {
        String expectedRedirect = Pattern.quote(
                "http://localhost:" + mockServerPort + "/oauth/authorize" +
                "?client_id=" + TEST_CLIENT_ID +
                "&redirect_uri=http://localhost:" + applicationPort +"/login" +
                "&response_type=code" +
                "&scope=profile%20email%20openid" +
                "&state=")+".{6}";

        ExtractableResponse response1 = given()
            .redirects()
                .follow(false)
            .accept(ContentType.JSON)
        .when()
            .get("http://localhost:" + applicationPort + "/login")
        .then()
            .statusCode(302)
            .header("Location", matchesPattern(expectedRedirect))
        .extract();

        String redirectUrl = response1.header("Location");
        String state = redirectUrl.substring(redirectUrl.lastIndexOf('=')+1);
        String code = "TEST_CODE";
        String requestUrl = "http://localhost:" + applicationPort + "/login?code=" + code + "&state=" + state;

        return given()
            .accept(ContentType.JSON)
            .cookies(response1.cookies())
            .redirects()
                .follow(false)
        .when()
            .get(requestUrl)
        .then()
            .statusCode(302)
        .extract();
    }

    @Test
    public void testGetHoldings() {

        ExtractableResponse response = given()
            .accept(ContentType.JSON)
            .cookies(performLogin().cookies())
            .redirects()
                .follow(false)
        .when()
            .get("http://localhost:" + applicationPort+"/api/holdings")
        .then()
            .statusCode(200)
            .extract();

        List<Holding> holdings = Arrays.asList(response.body().as(Holding[].class));

        // use Spotify's hamcrest-pojo to validate the objects
        assertThat(holdings, contains(
                pojo(Holding.class)
                    .withProperty("crypto",   is("crypto-1"))
                    .withProperty("currency", is("currency-1"))
                    .withProperty("amount",   is("amount-1")),
                pojo(Holding.class)
                    .withProperty("crypto",   is("crypto-2"))
                    .withProperty("currency", is("currency-2"))
                    .withProperty("amount",   is("amount-2"))
        ));
    }

    @Test
    public void testPostHoldings() {

        Holding[] inputHoldings = {
                new Holding()
                    .setCrypto("crypto-1")
                    .setCurrency("currency-1")
                    .setAmount("amount-1"),
                new Holding()
                    .setCrypto("crypto-2")
                    .setCurrency("currency-2")
                    .setAmount("amount-2")
        };

        // get a vaild csrf token
        ExtractableResponse loginResponse = performLogin();

        ExtractableResponse response2 = given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .cookies(loginResponse.cookies())
            .header("X-XSRF-TOKEN", loginResponse.cookies().get("XSRF-TOKEN"))
            .body(inputHoldings)
            .redirects()
                .follow(false)
        .when()
            .post("http://localhost:" + applicationPort + "/api/holdings")
        .then()
            .statusCode(200)
            .extract();

        List<Holding> outputHoldings = Arrays.asList(response2.body().as(Holding[].class));

        // output is the same as the input
        assertThat(outputHoldings, contains(
                pojo(Holding.class)
                    .withProperty("crypto",   is("crypto-1"))
                    .withProperty("currency", is("currency-1"))
                    .withProperty("amount",   is("amount-1")),
                pojo(Holding.class)
                    .withProperty("crypto",   is("crypto-2"))
                    .withProperty("currency", is("currency-2"))
                    .withProperty("amount",   is("amount-2"))
        ));

        // match a few of the encoded json values (the
        wireMockServer.verify(
                WireMock.putRequestedFor(WireMock.urlEqualTo("/api/v1/users/" + TEST_USER_ID))
                     .withRequestBody(WireMock.matchingJsonPath("$.profile.holdings", WireMock.containing("\"amount\":\"amount-1\"")))
                    .withRequestBody(WireMock.matchingJsonPath("$.profile.holdings", WireMock.containing("\"crypto\":\"crypto-2\""))));
    }

    @Before
    public void startMockServer() throws IOException {
        wireMockServer = new WireMockServer(wireMockConfig().port(mockServerPort));
        configureWireMock();
        wireMockServer.start();
    }

    @After
    public void stopMockServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    private void configureWireMock() throws IOException {
         // load a JSON file from the classpath
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/its/user.json"), StandardCharsets.UTF_8);

        // respond to GET for user
        wireMockServer.stubFor(WireMock.get("/api/v1/users/" + TEST_USER_ID)
                .willReturn(aResponse().withBody(body)));

        // respond to PUT for user
        wireMockServer.stubFor(WireMock.put("/api/v1/users/" + TEST_USER_ID)
                .willReturn(aResponse().withBody(body)));

        // OAuth token
        wireMockServer.stubFor(
                WireMock.post(urlPathEqualTo("/oauth/token"))
                            .withRequestBody(containing("grant_type=authorization_code"))
                            .withRequestBody(containing("code=TEST_CODE&"))
                            .withRequestBody(matching(".*" + Pattern.quote("redirect_uri=http%3A%2F%2Flocalhost%3A") + "\\d+" + Pattern.quote("%2Flogin") + ".*"))
                            .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json;charset=UTF-8")
                            .withBody(
                                "{" +
                                  "\"access_token\":\"" + TEST_ACCESS_TOKEN + "\",\n" +
                                  "\"token_type\":\"Bearer\",\n" +
                                  "\"expires_in\":3600,\n" +
                                  "\"scope\":\"profile openid email\",\n" +
                                  "\"id_token\":\"idTokenjwt\"\n" +
                                "}")));

        // OAuth userInfoUri
        String userInfoBody = StreamUtils.copyToString(getClass().getResourceAsStream("/its/userInfo.json"), StandardCharsets.UTF_8);
        wireMockServer.stubFor(
                WireMock.get("/oauth/userInfoUri")
                    .withHeader("Authorization", WireMock.equalTo("Bearer "+ TEST_ACCESS_TOKEN))
                .willReturn(aResponse()
                        .withBody(userInfoBody)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                ));
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
}