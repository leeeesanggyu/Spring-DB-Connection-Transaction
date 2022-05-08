package hello.jdbc.service;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

/**
 * 기본동작, 트랜잭션이 없어서 문제발생
 */
class MemberServiceV1Test {

    private MemberServiceV1 memberServiceV1;
    private MemberRepositoryV1 memberRepositoryV1;

    private static final String toMemberId = "이상규";
    private static final String fromMemberId = "박승진";
    private static final String errorMemberId = "ex";
    private static final int money = 5000;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepositoryV1 = new MemberRepositoryV1(dataSource);
        memberServiceV1 = new MemberServiceV1(memberRepositoryV1);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepositoryV1.delete(toMemberId);
        memberRepositoryV1.delete(fromMemberId);
        memberRepositoryV1.delete(errorMemberId);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        final Member toMember = new Member(toMemberId, money);
        final Member fromMember = new Member(fromMemberId, money);
        memberRepositoryV1.save(toMember);
        memberRepositoryV1.save(fromMember);

        memberServiceV1.accountTransfer(toMember.getMemberId(), fromMember.getMemberId(), money);

        final Member toMemberResult = memberRepositoryV1.findById(toMember.getMemberId());
        final Member fromMemberResult = memberRepositoryV1.findById(fromMember.getMemberId());
        assertThat(toMemberResult.getMoney()).isEqualTo(money-money);
        assertThat(fromMemberResult.getMoney()).isEqualTo(money+money);
    }

    @Test
    @DisplayName("이체 중 예외발생")
    void accountTransferEx() throws SQLException {
        final Member toMember = new Member(toMemberId, money);
        final Member fromMember = new Member(errorMemberId, money);
        memberRepositoryV1.save(toMember);
        memberRepositoryV1.save(fromMember);

        assertThatThrownBy(() -> memberServiceV1.accountTransfer(toMember.getMemberId(), fromMember.getMemberId(), money))
                .isInstanceOf(IllegalStateException.class);
        final Member toMemberResult = memberRepositoryV1.findById(toMember.getMemberId());
        final Member fromMemberResult = memberRepositoryV1.findById(fromMember.getMemberId());
        assertThat(toMemberResult.getMoney()).isEqualTo(money-money);
        assertThat(fromMemberResult.getMoney()).isEqualTo(money);
    }
}