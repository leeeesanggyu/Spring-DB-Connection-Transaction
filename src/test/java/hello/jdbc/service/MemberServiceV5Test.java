package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV5_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * CheckedException -> RuntimeException
 * MemberRepository Interface 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV5Test {

    @Autowired
    MemberServiceV5 memberService;

    @Autowired
    MemberRepository memberRepository;

    private static final String toMemberId = "이상규";
    private static final String fromMemberId = "박승진";
    private static final String errorMemberId = "ex";
    private static final int money = 5000;

    @TestConfiguration
    static class TestConfig {
        // spring container 에 dataSource 가 자동으로 등록됩니다.
        private final DataSource dataSource;

        TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            return new MemberRepositoryV5_1(dataSource);
        }

        @Bean
        MemberServiceV5 memberService() {
            return new MemberServiceV5(memberRepository());
        }
    }

    @AfterEach
    void after() {
        memberRepository.delete(toMemberId);
        memberRepository.delete(fromMemberId);
        memberRepository.delete(errorMemberId);
    }

    @Test
    void AopProxyCheck() {
        log.info("memberService class = {}", memberService.getClass());
        log.info("memberRepository class = {}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {
        final Member toMember = new Member(toMemberId, money);
        final Member fromMember = new Member(fromMemberId, money);
        memberRepository.save(toMember);
        memberRepository.save(fromMember);

        memberService.accountTransfer(toMember.getMemberId(), fromMember.getMemberId(), money);

        final Member toMemberResult = memberRepository.findById(toMember.getMemberId());
        final Member fromMemberResult = memberRepository.findById(fromMember.getMemberId());
        assertThat(toMemberResult.getMoney()).isEqualTo(money-money);
        assertThat(fromMemberResult.getMoney()).isEqualTo(money+money);
    }

    @Test
    @DisplayName("이체 중 예외발생")
    void accountTransferEx() {
        final Member toMember = new Member(toMemberId, money);
        final Member fromMember = new Member(errorMemberId, money);
        memberRepository.save(toMember);
        memberRepository.save(fromMember);
        assertThatThrownBy(() -> memberService.accountTransfer(toMember.getMemberId(), fromMember.getMemberId(), money))
                .isInstanceOf(IllegalStateException.class);
        final Member toMemberResult = memberRepository.findById(toMember.getMemberId());
        final Member fromMemberResult = memberRepository.findById(fromMember.getMemberId());
        assertThat(toMemberResult.getMoney()).isEqualTo(money);
        assertThat(fromMemberResult.getMoney()).isEqualTo(money);
    }
}