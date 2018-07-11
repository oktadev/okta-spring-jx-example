# CI/CD for Spring Boot with Jenkins X and Kubernetes
 
This example app shows how to CI/CD a [Spring Boot](https://spring.io/projects/spring-boot) and [Angular](https://angular.io) app using [Jenkins X](https://jenkins-x.io) and [Kubernetes](https://kubernetes.io/).

Please read [Add CI/CD to Your Spring Boot App with Jenkins X and Kubernetes]() to learn how to continuously test and deploy this application using Jenkins X.

**Prerequisites:** [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Node.js](https://nodejs.org/).

> [Okta](https://developer.okta.com/) has Authentication and User Management APIs that reduce development time with instant-on, scalable user infrastructure. Okta's intuitive API and expert support make it easy for developers to authenticate, manage, and secure users and roles in any application.

* [Getting Started](#getting-started)
* [Links](#links)
* [Help](#help)
* [License](#license)

## Getting Started

To install this example application, run the following commands:

```bash
git clone https://github.com/oktadeveloper/okta-spring-jx-example.git okta-jenkinsx
cd okta-jenkinsx
```

This will get a copy of the project installed locally. To install all of its dependencies and start each app, follow the instructions below.

To run the server, cd into the `holdings-api` directory and run:
 
```bash
./mvnw spring-boot:run
```

To run the client, cd into the `crypto-pwa` directory and run:
 
```bash
npm install -g ionic
npm i && ionic serve
```

To package everything into a single JAR for deployment, run the following command in the `holdings-api` directory.

```bash
./mvnw package -Pprod
```

### Setup Okta

The first thing you’ll need to do is add a `holdings` attribute to your organization’s user profiles. Log in to the Okta Developer Console, then navigate to **Users** > **Profile Editor**. Click on **Profile** for the first profile in the table. You can identify it by its Okta logo. Click **Add Attribute** and use the following values:

* Display name: `Holdings`
* Variable name: `holdings`
* Description: `Cryptocurrency Holdings`

You will need to [create an API Token and OIDC App](https://developer.okta.com/blog/2018/01/23/replace-local-storage-with-okta-profile-attributes#create-an-api-token) to get your values to perform authentication. 

Log in to your Okta Developer account (or [sign up](https://developer.okta.com/signup/) if you don’t have an account) and navigate to **Applications** > **Add Application**. Click **Web**, click **Next**, and give the app a name you’ll remember. Click **Done**. You'll need the client ID and client secret on the resulting screen below.

For the Okta Java SDK to talk to Okta’s API, you’ll need to create an API token. The abbreviated steps are as follows:

1. Log in to your Developer Console
2. Navigate to **API** > **Tokens** and click **Create Token**
3. Give your token a name, then copy its value

#### Okta App Configuration

Open `holdings-api/src/main/resources/application.yml` and add your API token as a property. While you're there, set the `issuer` and `clientId` to match your OIDC application.

**NOTE:** The value of `{yourOktaDomain}` should be something like `dev-123456.oktapreview.com`. Make sure you don't include `-admin` in the value!

```properties
okta:
  client:
    orgUrl: https://{yourOktaDomain}.com
    token: XXX
security:
    oauth2:
        client:
            access-token-uri: https://{yourOktaDomain}.com/oauth2/default/v1/token
            user-authorization-uri: https://{yourOktaDomain}.com/oauth2/default/v1/authorize
            client-id: {yourClientId}
            client-secret: {yourClientSecret}
            scope: openid profile email
        resource:
            user-info-uri: https://{yourOktaDomain}.com/oauth2/default/v1/userinfo
```

<!-- okta.oauth2.orgUrl=https://{yourOktaDomain}.com
okta.oauth2.issuer=https://{yourOktaDomain}.com/oauth2/default
okta.oauth2.clientId={yourClientId}
okta.client.token=XXX -->
## Links

This example uses the following libraries provided by Okta:

* [Okta Spring Boot Starter](https://github.com/okta/okta-spring-boot)
* [Okta Auth SDK](https://github.com/okta/okta-auth-js)

## Help

Please post any questions as comments on the [blog post](), or visit our [Okta Developer Forums](https://devforum.okta.com/). You can also email developers@okta.com if would like to create a support ticket.

## License

Apache 2.0, see [LICENSE](LICENSE).
