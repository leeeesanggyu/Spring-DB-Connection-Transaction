package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV0Test {

    final MemberRepositoryV0 repo = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
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
    }
}