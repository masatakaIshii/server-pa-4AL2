package fr.esgi.pa.server.e2e;

import fr.esgi.pa.server.code.core.Code;
import fr.esgi.pa.server.code.core.CodeState;
import fr.esgi.pa.server.code.infrastructure.entrypoint.CodeRequest;
import fr.esgi.pa.server.common.core.exception.NotFoundException;
import fr.esgi.pa.server.common.core.utils.process.ProcessHelper;
import fr.esgi.pa.server.helper.AuthHelper;
import fr.esgi.pa.server.language.core.LanguageName;
import fr.esgi.pa.server.role.core.RoleDao;
import fr.esgi.pa.server.role.core.RoleName;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CodeApiTest {
    @Autowired
    private RoleDao roleDao;

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private ProcessHelper processHelper;

    @LocalServerPort
    private int localPort;

    private String jwtUser;

    @BeforeEach
    void setup() {
        port = localPort;
    }

    @BeforeAll
    void initAll() {
        var userRole = roleDao.findByRoleName(RoleName.ROLE_USER);
        userRole.ifPresent(presentUserRole -> {
            try {
                jwtUser = authHelper.createUserAndGetJwt(
                        "user",
                        "user@name.fr",
                        "password",
                        Set.of(presentUserRole));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    @AfterAll
    void afterAll() throws IOException, InterruptedException {
        var deleteImagesProcess = processHelper.createCommandProcess(new String[]{
                "docker", "image", "prune", "-a", "-f"});
        if (deleteImagesProcess.waitFor() != 0) {
            System.err.println("Problem delete all images");
        }
    }

    @DisplayName("METHOD POST /api/code")
    @Nested
    class PostCompileCode {
        @Test
        void should_send_result_compiled_code() {
            var content = "#include <stdio.h>\n" +
                    "int main() {\n" +
                    "   // printf() displays the string inside quotation\n" +
                    "   printf(\"Hello World\");\n" +
                    "   return 0;\n" +
                    "}";
            var codeRequest = new CodeRequest()
                    .setLanguage("C")
                    .setContent(content);
            var code = given()
                    .header("Authorization", "Bearer " + jwtUser)
                    .contentType(ContentType.JSON)
                    .body(codeRequest)
                    .when()
                    .post("/api/code")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Code.class);

            assertThat(code).isNotNull();
            assertThat(code.getLanguage()).isNotNull();
            assertThat(code.getCodeState()).isEqualTo(CodeState.SUCCESS);
            assertThat(code.getOutput()).isEqualTo("Hello World");
            assertThat(code.getLanguage().getLanguageName()).isEqualTo(LanguageName.C);
            assertThat(code.getCodeState()).isEqualTo(CodeState.SUCCESS);
        }
    }
}
