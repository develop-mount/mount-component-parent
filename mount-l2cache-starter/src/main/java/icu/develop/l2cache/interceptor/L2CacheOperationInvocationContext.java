package icu.develop.l2cache.interceptor;

import java.lang.reflect.Method;

public interface L2CacheOperationInvocationContext<O extends L2CacheOperation> {

    /**
     * Return the cache operation.
     * @return 操作对象
     */
    O getOperation();

    /**
     * Return the target instance on which the method was invoked.
     * @return 目标实例
     */
    Object getTarget();

    /**
     * Return the method which was invoked.
     * @return 方法
     */
    Method getMethod();

    /**
     * Return the argument list used to invoke the method.
     * @return 参数
     */
    Object[] getArgs();

}
