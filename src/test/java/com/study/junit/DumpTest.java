package com.study.junit;

import com.study.junit.entity.Member;
import com.study.junit.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DumpTest {
    @Autowired
    MemberRepository memberRepository;

    @Test
    void members() {
        List<Member> members = memberRepository.findAll();
        System.out.println("Member size : " + members.size());
        for (Member member : members) {
            System.out.println(member);
        }

        assertThat(members.size())
                .isEqualTo(3);
    }
}
