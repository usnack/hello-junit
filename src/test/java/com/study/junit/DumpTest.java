package com.study.junit;

import com.study.junit.entity.Member;
import com.study.junit.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
    static class Constants {
        static final String VERSION = "230821-1";
        static final List<Member> MEMBERS = List.of(
                new Member(1L, "first"),
                new Member(2L, "second"),
                new Member(3L, "third"),
                new Member(4L, "fourth"),
                new Member(5L, "fifth")
        );
        static final int MEMBER_SIZE = MEMBERS.size();
    }

    @Container
    static OracleContainer oracle =
            new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:18.4.0-slim-faststart"))
                    .withExposedPorts(1521, 5500)
                    .withEnv("ORACLE_PASSWORD", "1234")
                    .withFileSystemBind(Path.of("src", "test", "resources", "oracle", "startup").toAbsolutePath().toString().concat(File.separator), "/container-entrypoint-initdb.d/")
                    .withFileSystemBind(Path.of("src", "test", "resources", "oracle", "dump").toAbsolutePath().toString().concat(File.separator), "/opt/oracle/dump/");
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
        while(!oracle.isRunning()) {
            System.out.println("oracle container is not healthy...");
            Thread.sleep(1000*5);
        }
    }
    //
    @Test
    void exportTest() throws InterruptedException, IOException {
        memberRepository.saveAll(Constants.MEMBERS);

        org.testcontainers.containers.Container.ExecResult execResult = oracle.execInContainer(
                "sh",
                "-c",
                String.format("expdp C##DUMPER/1234@XE tables=member directory=dump_vol dumpfile=%s.dmp logfile=%s.log",
                        Constants.VERSION, Constants.VERSION
                )
        );
        System.out.println(execResult);
        assertThat(execResult.getExitCode())
                .isZero();
    }

    @Test
    void importTest() throws IOException, InterruptedException {
        org.testcontainers.containers.Container.ExecResult execResult = oracle.execInContainer(
                "sh",
                "-c",
                String.format("impdp C##DUMPER/1234@XE tables=member directory=dump_vol content=data_only dumpfile=%s.dmp logfile=%s.log",
                        Constants.VERSION, Constants.VERSION
                )
        );
        System.out.println(execResult);
        assertThat(execResult.getExitCode())
                .isZero();
        //
        List<Member> members = memberRepository.findAll();
        System.out.println("Member size : " + members.size());
        for (Member member : members) {
            System.out.println(member);
        }

        assertThat(members)
                .hasSize(Constants.MEMBER_SIZE);
    }
}
