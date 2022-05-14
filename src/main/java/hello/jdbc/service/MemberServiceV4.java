package hello.jdbc.service;

import hello.jdbc.aop.MyTransactional;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@Component
public class MemberServiceV4 {

    private final MemberRepositoryV4 memberRepository;

    public MemberServiceV4(MemberRepositoryV4 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @MyTransactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        accountTransferLogic(fromId, toId, money);
    }

    private void accountTransferLogic(String fromId, String toId, int money) throws SQLException {
        final Member fromMember = memberRepository.findById(fromId);
        final Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromMember.getMemberId(), fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("이체 중 예외발생");
    }

}
