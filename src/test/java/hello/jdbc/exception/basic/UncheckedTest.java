package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(service::callThrow)
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException 을 상속받은 Exception 은 Unchecked Exception 이 됩니다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked Exception은 예외를 catch, throw 하지 않아도 됩니다.
     * 예외를 catch 하지 않으면 자동으로 throw 합니다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 catch 하면 됩니다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                log.info("예외처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 catch 하지 않아도 됩니다.
         * Checked Exception 과 다르게 throw 선언을 하지 않아도 상위로 넘어갑니다.
         */
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
