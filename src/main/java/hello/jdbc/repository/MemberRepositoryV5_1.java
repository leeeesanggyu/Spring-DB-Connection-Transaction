package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.exception.MyDBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * CheckedException -> RuntimeException
 * MemberRepository Interface 사용
 * throws SQLException 제거
 */
@Slf4j
public class MemberRepositoryV5_1 implements MemberRepository{

    private final DataSource dataSource;

    public MemberRepositoryV5_1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();  // return int; : 영향받은 DB row 수 반환

            return member;
        } catch (SQLException e) {
            throw new MyDBException(e);
        } finally {
            close(con, pstmt, null);
        }

    }

    @Override
    public int update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            final int resultSize = pstmt.executeUpdate(); // return int; : 영향받은 DB row 수 반환
            log.info("update result size = {}", resultSize);
            return resultSize;
        } catch (SQLException e) {
            throw new MyDBException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public int delete(String memberId) {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            final int resultSize = pstmt.executeUpdate(); // return int; : 영향받은 DB row 수 반환
            log.info("delete result size = {}", resultSize);
            return resultSize;
        } catch (SQLException e) {
            throw new MyDBException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();// return int; : 영향받은 DB row 수 반환

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            throw new MyDBException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
//        JdbcUtils.closeConnection(con);
        // 트랜잭션 동기화를 사용하려면 DataSourceUtils 을 사용해야 합니다.
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 트랜잭션 동기화를 사용하려면 DataSourceUtils 을 사용해야 합니다.
        final Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("getConnection={} class={}", con, con.getClass());
        return con;
    }
}
