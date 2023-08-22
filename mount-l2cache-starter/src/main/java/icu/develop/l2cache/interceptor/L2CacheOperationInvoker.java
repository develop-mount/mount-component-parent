package icu.develop.l2cache.interceptor;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 20:59
 */
@FunctionalInterface
public interface L2CacheOperationInvoker {

    /**
     * Invoke the cache operation defined by this instance. Wraps any exception that is thrown during the invocation in a CacheOperationInvoker.ExceptionWrapper.
     *
     * @return the result of the operation
     * @throws WrapperException if an error occurred while invoking the operation
     */
    Object invoke() throws WrapperException;


    /**
     * Wrap any exception thrown while invoking {@link #invoke()}.
     */
    @SuppressWarnings("serial")
    class WrapperException extends RuntimeException {

        private final Throwable original;

        public WrapperException(Throwable original) {
            super(original.getMessage(), original);
            this.original = original;
        }

        public Throwable getOriginal() {
            return this.original;
        }
    }
}
