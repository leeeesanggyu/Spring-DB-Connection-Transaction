package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        final Connection con = dataSource.getConnection();
        try {
            // 트래잭션 시작
            con.setAutoCommit(false);

            // 비지니스 로직
            accountTransferLogic(con, fromId, toId, money);

            // 트랜잭션 종료
            con.commit();
        } catch (Exception e) {
            // 실패시 롤백
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(con);

        }
    }

    private void accountTransferLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        final Member fromMember = memberRepository.findById(con, fromId);
        final Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromMember.getMemberId(), fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("이체 중 예외발생");
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                // connection pool 에 오토커밋이 false 인 채로 반환되면 안되기 때문에 설정해줍니다.
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
                log.info("error :", e);
            }
        }
    }
}
