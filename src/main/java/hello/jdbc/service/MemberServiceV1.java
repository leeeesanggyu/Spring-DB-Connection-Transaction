package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepositoryV1;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        final Member fromMember = memberRepositoryV1.findById(fromId);
        final Member toMember = memberRepositoryV1.findById(toId);

        memberRepositoryV1.update(fromMember.getMemberId(), fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV1.update(toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("이체 중 예외발생");
    }
}
