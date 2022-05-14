package hello.jdbc.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class MyTransactionalAspect {

    private final PlatformTransactionManager transactionManager;

    @Around("@annotation(MyTransactional)")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("[Transaction Start] {}", joinPoint.getSignature());
            final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                // 비지니스 로직
                final Object result = joinPoint.proceed();
                // 트랜잭션 종료
                transactionManager.commit(status);
            } catch (Exception e) {
                // 실패시 롤백
                transactionManager.rollback(status);
                throw new IllegalStateException(e);
            }
            log.info("[Transaction End] {}", joinPoint.getSignature());
            return null;
        } catch (IllegalStateException e) {
            log.info("[Transaction Rollback] {}", joinPoint.getSignature());
            throw e;
        } finally {
            log.info("[Resource Release] {}", joinPoint.getSignature());
        }
    }
}
