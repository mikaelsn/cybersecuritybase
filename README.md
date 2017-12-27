The application is based on the starter code and it can be found from https://github.com/mikaelsn/cybersecuritybase. The purpose of the application is to share who is hosting a party and where. The application should be exclusive and only people with the username and password should be able to use it (predefined account is party:party123). Most likely there should be an admin account as well, but the developer was too lazy to do that, so the application uses a very secret URL instead to enter the admin-panel.

One can run the application like any normal Maven/Spring-based application by using the Maven-command "run" or running the main class of the application. To generate a few test inputs, visit any URL which is not routed, i.e. http://localhost:8080/.

The application has at least five different OWASP Top 10 vulnerabilities:
- SQL Injection.
- Cross-Site Scripting (XSS).
- Insecure Direct Object References.
- Security Misconfiguration.
- Using Components with Known Vulnerabilities.

One can start identifying the vulnerabilities by using OWASP ZAP and running the Quick Start on URL http://localhost:8080. ZAP should have identified five different alerts and they should contain: Cross Site Scripting (2 different types), SQL Injection, Application Error Disclosure and Buffer Overflow. One can examine the inputs and outputs which ZAP gave and received and where. In this case we are interested on the SQL Injection and Cross Site Scripting vulnerabilities.

All of the steps below assume that you once visited some address (i.e http://localhost:8080/) which generates two signups and one of them should be a secret party. Alternatively one can generate enter own their parties by using the form.

Issue: SQL Injection
Steps to reproduce:
1. Open http://localhost:8080/participants
2. Enter the following to the ID-field: '' or '1'='1'
4. Press Submit.
5. You can now see all addresses, including the secret party location. Most likely you can also execute some other SQL which can cause harm.

Issue: Stored XSS Attack
Steps to reproduce:
1. Open http://localhost:8080/form
2. Enter <script>alert("Hello!");</script> as the name or address or both.
3. Press Submit.
4. You are redirected to to the participant-page.
5. The script is now executed. There is no escaping of HTML when printing the user information.

Issue: Insecure Direct Object Reference
Steps to reproduce:
1. Open http://localhost:8080/form
2. Copy the link address to the admin panel: http://localhost:8080/admin?admin=false
3. Change it to http://localhost:8080/admin?admin=true
4. You are redirected to to the admin-page. Here one can see all of the information, including the secret party.

Issue: Security Misconfiguration
Steps to reproduce:
1. Open any address on http://localhost:8080/
2. The application should only be accessed when entered the proper username and password. There is one account for everybody at the moment, which is probably not the best option. Also the admin panel does not have any security configuration at all as discussed one the previous example.

Issue: Using Components with Known Vulnerabilities
Steps to reproduce:
1. Open the project on your IDE or navigate to the root of the project on your command line.
2. Execute Maven-command "dependency-check-maven:check" with your IDE or command line.
3. Examine the report on target/dependency-check-report.html.
4. There should be quite a few dependencies with vulnerabilities. Some of the vulnerabilities include the ones mentioned in OWASP Top Ten, for example SQL injection and Cross Site Scripting.

To fix the SQL injection, go to src/main/java/sec/project/controller/SignupController.java and on the getAddress-method and instead of using plain SQL without any escaping of parameters, use for example signupRepository.findOne(Long.getLong(id)). Also using JPA-parameters works: change the query to: SELECT address FROM Signup WHERE id = :id. Then add query.setParameter("id", id) and one can also set the result to only obtain singleResult instead of a list of results.

To fix the Stored XSS Attack, go to src/main/resources/templates/participants.html and replace the th:utext to th:text in the file to properly escape HTML.

To fix the Insecure Direct Object Reference, go to src/main/java/sec/project/config/SecurityConfiguration.java and add a user with the role "ADMIN". Then go to src/main/java/sec/project/controller/SignupController.java and on the getAdminPanel-method remove the old boolean checking of admin-parameter and add annotation: @PreAuthorize("hasRole('ADMIN')").

To fix the Security Misconfiguration, go to src/main/java/sec/project/config/SecurityConfiguration.java and remove the existing configuration completely and uncomment the old settings, which were meant for testing purposes only as described in the comment before. This should enable HTTP basic-authentication for all requests, as this was a application for exclusive people only. Also CSRF should be now harder as Spring automatically adds CSRF-tokens to the requests from now on.

To fix the Using Components with Known Vulnerabilities, go to pom.xml and update the spring-boot-starter-parent to the latest release. At the time of writing, 1.5.9.RELEASE is the latest release.

After doing all of the fixes you now have an application which does not do anything useful and most likely there is still some ways to do unintended actions to the system. Overall one really needs to try hard to fail this bad.
