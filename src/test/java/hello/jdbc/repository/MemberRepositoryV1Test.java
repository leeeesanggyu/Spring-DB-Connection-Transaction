package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repo;

    @BeforeEach
    void beforeEach() {
        // 기본 DriverManager - 항상 새로운 커넥션을 얻습니다.
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // connection fooling
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        repo = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException, InterruptedException {
        String memberId = "살구14";
        int money = 1000;
        int updateMoney = 3000;
        final Member member = new Member(memberId, money);

        // save
        repo.save(member);

        // findById
        final Member findMember = repo.findById(member.getMemberId());
        log.info("findMember = {}", findMember);

        assertThat(findMember).isEqualTo(member);

        // update
        final int updateSize = repo.update(member.getMemberId(), updateMoney);
        final Member updateMember = repo.findById(member.getMemberId());
        log.info("updateMember = {}", updateMember);

        assertThat(updateMember.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(updateMoney);
        assertThat(updateSize).isEqualTo(1);

        // delete
        final int deleteSize = repo.delete(member.getMemberId());

        assertThatThrownBy(() -> {
            final Member deleteMember = repo.findById(member.getMemberId());
        }).isInstanceOf(NoSuchElementException.class);

        Thread.sleep(1000);
    }
}