package icu.develop.l2cache.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/11 14:13
 */
@SuppressWarnings("serial")
public class L2CacheInterceptor extends L2CacheAspectSupport implements MethodInterceptor {

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        L2CacheOperationInvoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                throw new L2CacheOperationInvoker.WrapperException(ex);
            }
        };

        try {
            return execute(aopAllianceInvoker, invocation.getThis(), method, invocation.getArguments());
        } catch (L2CacheOperationInvoker.WrapperException ew) {
            throw ew.getOriginal();
        }
    }
}
