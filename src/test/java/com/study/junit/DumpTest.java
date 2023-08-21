package com.study.junit;

import com.study.junit.entity.Member;
import com.study.junit.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class DumpTest {
    @Container
    static OracleContainer oracle = new OracleContainer(
//            DockerImageName.parse("container-registry.oracle.com/database/express:18.4.0-xe")
            DockerImageName.parse("oracle/database:18.4.0-xe-snapshot")
                    .asCompatibleSubstituteFor("gvenzl/oracle-xe")
//            DockerImageName.parse("gvenzl/oracle-xe:18.4.0-slim-faststart")
    )
            .withExposedPorts(1521, 5500)
            .withEnv("ORACLE_PASSWORD", "1234")
            .withEnv("ORACLE_PWD", "1234")
            .withEnv("ORACLE_CHARACTERSET", "AL32UTF8")
            .withFileSystemBind(Path.of("src", "test", "resources", "oracle", "startup", "01_config.sql").toAbsolutePath().toString(), "/opt/oracle/scripts/startup/01_config.sql")
            .withFileSystemBind(Path.of("src", "test", "resources", "oracle", "dump").toAbsolutePath().toString().concat(File.separator), "/opt/oracle/dump/")
            ;
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format("jdbc:oracle:thin:@localhost:%s/XE", oracle.getMappedPort(1521)));
        registry.add("spring.datasource.username", () -> "C##DUMPER");
        registry.add("spring.datasource.password", () -> "1234");
    }
    //
    @Autowired
    MemberRepository memberRepository;
    //
    @BeforeAll
    static void waitForHealthy() throws InterruptedException {
        while(!oracle.isHealthy()) {
            System.out.println("oracle container is not healthy...");
            Thread.sleep(1000*5);
        }
    }
    //

    @Transactional
    @Rollback(value = false)
    @Test
    void exportTest() throws InterruptedException, IOException {
        memberRepository.saveAll(
                List.of(
                        new Member(1L, "first"),
                        new Member(2L, "second"),
                        new Member(3L, "third"),
                        new Member(4L, "fourth"),
                        new Member(5L, "fifth")
                )
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();

        org.testcontainers.containers.Container.ExecResult execResult = oracle.execInContainer(
                "sh",
                "-c",
                "expdp C##DUMPER/1234@XE tables=member directory=dump_vol dumpfile=230821_1.dmp logfile=230821_1.log"
        );
        System.out.println(execResult);
    }


    private void importDump() throws IOException, InterruptedException {
        org.testcontainers.containers.Container.ExecResult execResult = oracle.execInContainer(
                "sh",
                "-c",
                "impdp C##DUMPER/1234@XE tables=member directory=dump_vol content=data_only dumpfile=230821_1.dmp logfile=230821_1.log"
        );
        System.out.println(execResult);
    }
    //
    @Test
    void importTest() throws IOException, InterruptedException {
        importDump();
        //
        List<Member> members = memberRepository.findAll();
        System.out.println("Member size : " + members.size());
        for (Member member : members) {
            System.out.println(member);
        }

        assertThat(members.size())
                .isEqualTo(5);
    }
}
