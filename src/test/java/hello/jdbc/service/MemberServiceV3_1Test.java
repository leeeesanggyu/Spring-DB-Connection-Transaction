package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 커넥션 파라미터 방식 동기화
 */
class MemberServiceV3_1Test {

    private MemberServiceV3_1 memberService;
    private MemberRepositoryV3 memberRepository;

    private static final String toMemberId = "이상규";
    private static final String fromMemberId = "박승진";
    private static final String errorMemberId = "ex";
    private static final int money = 5000;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_1(new JdbcTransactionManager(dataSource), memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(toMemberId);
        memberRepository.delete(fromMemberId);
        memberRepository.delete(errorMemberId);
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