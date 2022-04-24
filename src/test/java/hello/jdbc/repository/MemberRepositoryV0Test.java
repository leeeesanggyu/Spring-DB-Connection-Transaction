package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class MemberRepositoryV0Test {

    final MemberRepositoryV0 repo = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        final Member member = new Member("살구4", 1000);
        repo.save(member);

        // findById
        final Member findMember = repo.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        assertThat(findMember).isEqualTo(member);
    }
}