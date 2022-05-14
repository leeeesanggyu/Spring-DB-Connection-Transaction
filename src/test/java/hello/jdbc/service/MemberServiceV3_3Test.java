package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

    @Autowired
    MemberServiceV3_3 memberService;

    @Autowired
    MemberRepositoryV3 memberRepository;

    private static final String toMemberId = "이상규";
    private static final String fromMemberId = "박승진";
    private static final String errorMemberId = "ex";
    private static final int money = 5000;

    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @AfterEach
    void after() throws SQLException {
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
    void accountTransfer() throws SQLException {
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
    void accountTransferEx() throws SQLException {
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